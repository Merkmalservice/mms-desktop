package at.researchstudio.sat.merkmalservice.api.auth;

import at.researchstudio.sat.merkmalservice.api.auth.event.TokenRefreshedEvent;
import at.researchstudio.sat.merkmalservice.api.auth.event.UserLoggedInEvent;
import at.researchstudio.sat.merkmalservice.api.auth.event.UserLoggedOutEvent;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.AllowedMethodsHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.ServerRequest;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.common.util.RandomString;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.IDToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class KeycloakService {
    private static final String KEYCLOAK_JSON = "META-INF/keycloak.json";

    private KeycloakDeployment deployment;

    private AccessTokenResponse tokenResponse;
    private String tokenString;
    private String idTokenString;
    private IDToken idToken;
    private AccessToken token;
    private String refreshToken;
    private Locale locale;

    @Autowired private ApplicationEventPublisher eventPublisher;

    public KeycloakService() {
        this(Thread.currentThread().getContextClassLoader().getResourceAsStream(KEYCLOAK_JSON));
    }

    public KeycloakService(InputStream config) {
        this(KeycloakDeploymentBuilder.build(config));
    }

    public KeycloakService(KeycloakDeployment deployment) {
        this.deployment = deployment;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void logout() {
        tokenString = null;
        token = null;
        idTokenString = null;
        idToken = null;
        refreshToken = null;
        eventPublisher.publishEvent(new UserLoggedOutEvent(this));
    }

    public KeycloakUriBuilder getLogoutUrl() {
        return deployment.getLogoutUrl();
    }

    public String createAuthUrl(String redirectUri, String state, KeycloakService.Pkce pkce) {

        KeycloakUriBuilder builder =
                deployment
                        .getAuthUrl()
                        .clone()
                        .queryParam(OAuth2Constants.RESPONSE_TYPE, OAuth2Constants.CODE)
                        .queryParam(OAuth2Constants.CLIENT_ID, deployment.getResourceName())
                        .queryParam(OAuth2Constants.REDIRECT_URI, redirectUri)
                        .queryParam(OAuth2Constants.SCOPE, OAuth2Constants.SCOPE_OPENID);

        if (state != null) {
            builder.queryParam(OAuth2Constants.STATE, state);
        }

        if (locale != null) {
            builder.queryParam(OAuth2Constants.UI_LOCALES_PARAM, locale.getLanguage());
        }

        if (pkce != null) {
            builder.queryParam(OAuth2Constants.CODE_CHALLENGE, pkce.getCodeChallenge());
            builder.queryParam(OAuth2Constants.CODE_CHALLENGE_METHOD, "S256");
        }

        return builder.build().toString();
    }

    public KeycloakService.Pkce generatePkce() {
        return KeycloakService.Pkce.generatePkce();
    }

    public String getTokenString() {
        return tokenString;
    }

    public String getTokenString(long minValidity, TimeUnit unit)
            throws VerificationException, IOException, ServerRequest.HttpFailure {
        long expires = ((long) token.getExpiration()) * 1000 - unit.toMillis(minValidity);
        if (expires < System.currentTimeMillis()) {
            refreshToken();
        }

        return tokenString;
    }

    public void refreshToken()
            throws IOException, ServerRequest.HttpFailure, VerificationException {
        AccessTokenResponse tokenResponse = ServerRequest.invokeRefresh(deployment, refreshToken);
        parseAccessToken(tokenResponse);
        eventPublisher.publishEvent(new TokenRefreshedEvent(this));
    }

    public void refreshToken(String refreshToken)
            throws IOException, ServerRequest.HttpFailure, VerificationException {
        AccessTokenResponse tokenResponse = ServerRequest.invokeRefresh(deployment, refreshToken);
        parseAccessToken(tokenResponse);
        eventPublisher.publishEvent(new TokenRefreshedEvent(this));
    }

    private void parseAccessToken(AccessTokenResponse tokenResponse) throws VerificationException {
        this.tokenResponse = tokenResponse;
        tokenString = tokenResponse.getToken();
        refreshToken = tokenResponse.getRefreshToken();
        idTokenString = tokenResponse.getIdToken();

        AdapterTokenVerifier.VerifiedTokens tokens =
                AdapterTokenVerifier.verifyTokens(tokenString, idTokenString, deployment);
        token = tokens.getAccessToken();
        idToken = tokens.getIdToken();
    }

    public AccessToken getToken() {
        return token;
    }

    public IDToken getIdToken() {
        return idToken;
    }

    public String getIdTokenString() {
        return idTokenString;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public AccessTokenResponse getTokenResponse() {
        return tokenResponse;
    }

    public KeycloakDeployment getDeployment() {
        return deployment;
    }

    public void obtainAccessToken(String code, String redirectUri, KeycloakService.Pkce pkce)
            throws IOException, ServerRequest.HttpFailure, VerificationException {

        AccessTokenResponse tokenResponse =
                ServerRequest.invokeAccessCodeToToken(
                        deployment,
                        code,
                        redirectUri,
                        null,
                        pkce == null ? null : pkce.getCodeVerifier());
        parseAccessToken(tokenResponse);
        eventPublisher.publishEvent(new UserLoggedInEvent(this));
    }

    public String readCode(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();

        char cb[] = new char[1];
        while (reader.read(cb) != -1) {
            char c = cb[0];
            if ((c == ' ') || (c == '\n') || (c == '\r')) {
                break;
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    public class CallbackListener implements HttpHandler {
        private final CountDownLatch shutdownSignal = new CountDownLatch(1);

        private String code;
        private String error;
        private String errorDescription;
        private String state;
        private Undertow server;

        private GracefulShutdownHandler gracefulShutdownHandler;

        public void start() {
            PathHandler pathHandler = Handlers.path().addExactPath("/", this);
            AllowedMethodsHandler allowedMethodsHandler =
                    new AllowedMethodsHandler(pathHandler, Methods.GET);
            gracefulShutdownHandler = Handlers.gracefulShutdown(allowedMethodsHandler);

            server =
                    Undertow.builder()
                            .setIoThreads(1)
                            .setWorkerThreads(1)
                            .addHttpListener(0, "localhost")
                            .setHandler(gracefulShutdownHandler)
                            .build();

            server.start();
        }

        public void stop() {
            try {
                server.stop();
            } catch (Exception ignore) {
                // it is OK to happen if thread is modified while stopping the server, specially
                // when a security manager is enabled
            }
        }

        public int getLocalPort() {
            return ((InetSocketAddress) server.getListenerInfo().get(0).getAddress()).getPort();
        }

        public void await() throws InterruptedException {
            shutdownSignal.await();
        }

        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            gracefulShutdownHandler.shutdown();

            if (!exchange.getQueryParameters().isEmpty()) {
                readQueryParameters(exchange);
            }

            exchange.setStatusCode(StatusCodes.FOUND);
            exchange.getResponseHeaders().add(Headers.LOCATION, getRedirectUrl());
            exchange.endExchange();

            shutdownSignal.countDown();

            ForkJoinPool.commonPool().execute(this::stop);
        }

        private void readQueryParameters(HttpServerExchange exchange) {
            code = getQueryParameterIfPresent(exchange, OAuth2Constants.CODE);
            error = getQueryParameterIfPresent(exchange, OAuth2Constants.ERROR);
            errorDescription =
                    getQueryParameterIfPresent(exchange, OAuth2Constants.ERROR_DESCRIPTION);
            state = getQueryParameterIfPresent(exchange, OAuth2Constants.STATE);
        }

        private String getQueryParameterIfPresent(HttpServerExchange exchange, String name) {
            Map<String, Deque<String>> queryParameters = exchange.getQueryParameters();
            return queryParameters.containsKey(name) ? queryParameters.get(name).getFirst() : null;
        }

        private String getRedirectUrl() {
            String redirectUrl = deployment.getTokenUrl().replace("/token", "/delegated");

            if (error != null) {
                redirectUrl += "?error=true";
            }

            return redirectUrl;
        }
    }

    public static class Pkce {
        // https://tools.ietf.org/html/rfc7636#section-4.1
        public static final int PKCE_CODE_VERIFIER_MAX_LENGTH = 128;

        private final String codeChallenge;
        private final String codeVerifier;

        public Pkce(String codeVerifier, String codeChallenge) {
            this.codeChallenge = codeChallenge;
            this.codeVerifier = codeVerifier;
        }

        public String getCodeChallenge() {
            return codeChallenge;
        }

        public String getCodeVerifier() {
            return codeVerifier;
        }

        public static KeycloakService.Pkce generatePkce() {
            try {
                String codeVerifier =
                        new RandomString(PKCE_CODE_VERIFIER_MAX_LENGTH, new SecureRandom())
                                .nextString();
                String codeChallenge = generateS256CodeChallenge(codeVerifier);
                return new KeycloakService.Pkce(codeVerifier, codeChallenge);
            } catch (Exception ex) {
                throw new RuntimeException("Could not generate PKCE", ex);
            }
        }

        // https://tools.ietf.org/html/rfc7636#section-4.6
        private static String generateS256CodeChallenge(String codeVerifier) throws Exception {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(codeVerifier.getBytes(StandardCharsets.ISO_8859_1));
            return Base64Url.encode(md.digest());
        }
    }
}
