package at.researchstudio.sat.mmsdesktop.service;

import at.researchstudio.sat.merkmalservice.api.auth.event.TokenRefreshedEvent;
import at.researchstudio.sat.merkmalservice.api.auth.event.UserLoggedInEvent;
import at.researchstudio.sat.merkmalservice.api.auth.event.UserLoggedOutEvent;
import at.researchstudio.sat.mmsdesktop.gui.login.LoginState;
import at.researchstudio.sat.mmsdesktop.model.auth.UserSession;
import at.researchstudio.sat.mmsdesktop.model.task.LogoutResult;
import at.researchstudio.sat.mmsdesktop.support.JavaFXKeycloakInstalled;
import java.lang.invoke.MethodHandles;
import java.util.Locale;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class AuthService implements ApplicationListener {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final JavaFXKeycloakInstalled keycloak;
    private Task<UserSession> loginTask;
    private Task<LogoutResult> logoutTask;
    private Task<UserSession> refreshTokenTask;
    private LoginState loginState;

    public AuthService(
            @Autowired JavaFXKeycloakInstalled javaFXKeycloakInstalled,
            @Autowired LoginState loginState) {
        this.keycloak = javaFXKeycloakInstalled;
        this.keycloak.setLocale(Locale.getDefault());
        this.loginTask = generateLoginTask();
        this.logoutTask = generateLogoutTask();
        this.refreshTokenTask = generateRefreshTokenTask(null);
        this.loginState = loginState;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof TokenRefreshedEvent) {
            this.loginState.setUserSession(newUserSession());
            this.loginState.setLoggedIn(true);
        } else if (applicationEvent instanceof UserLoggedInEvent) {
            this.loginState.setUserSession(newUserSession());
            this.loginState.setLoggedIn(true);
        } else if (applicationEvent instanceof UserLoggedOutEvent) {
            this.loginState.setUserSession(null);
            this.loginState.setLoggedIn(false);
        }
    }

    private Task<UserSession> generateLoginTask() {
        return new Task<>() {
            @Override
            public UserSession call() throws Exception {
                try {
                    keycloak.loginDesktop();
                    return newUserSession();
                } catch (InterruptedException e) {
                    logger.warn("Login process cancelled by User");
                }
                return null;
            }
        };
    }

    private UserSession newUserSession() {
        return new UserSession(
                keycloak.getToken(), keycloak.getTokenString(), keycloak.getRefreshToken());
    }

    private Task<UserSession> generateRefreshTokenTask(String refreshToken) {
        return new Task<>() {
            @Override
            public UserSession call() throws Exception {
                try {
                    keycloak.refreshToken(refreshToken);
                    return newUserSession();
                } catch (Exception e) {
                    logger.info("Unable to refresh token - user is not logged in");
                }
                return null;
            }
        };
    }

    public void resetLoginTask() {
        this.loginTask = generateLoginTask();
    }

    public void resetLogoutTask() {
        this.logoutTask = generateLogoutTask();
    }

    private Task<LogoutResult> generateLogoutTask() {
        return new Task<>() {
            @Override
            public LogoutResult call() throws Exception {
                try {
                    keycloak.logout();
                } catch (InterruptedException e) {
                    logger.warn("Logout process cancelled by User");
                }
                return new LogoutResult(true);
            }
        };
    }

    public Task<UserSession> getLoginTask() {
        return loginTask;
    }

    public Task<LogoutResult> getLogoutTask() {
        return logoutTask;
    }

    public Task<UserSession> getRefreshTokenTask(String refreshToken) {
        return generateRefreshTokenTask(refreshToken);
    }
}
