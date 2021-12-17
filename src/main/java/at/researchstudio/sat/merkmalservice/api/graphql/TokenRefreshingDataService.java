package at.researchstudio.sat.merkmalservice.api.graphql;

import at.researchstudio.sat.merkmalservice.api.DataService;
import at.researchstudio.sat.merkmalservice.api.auth.KeycloakService;
import at.researchstudio.sat.merkmalservice.model.Project;
import at.researchstudio.sat.merkmalservice.model.mapping.Mapping;
import at.researchstudio.sat.merkmalservice.support.exception.NoGraphQlResponseException;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TokenRefreshingDataService implements DataService {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired GraphQLDataService delegate;
    @Autowired KeycloakService keycloakService;

    public TokenRefreshingDataService() {}

    public TokenRefreshingDataService(
            @Autowired GraphQLDataService delegate, @Autowired KeycloakService keycloakService) {
        this.delegate = delegate;
        this.keycloakService = keycloakService;
    }

    public TokenRefreshingDataService(@Autowired GraphQLDataService delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<Project> getProjectsWithFeatureSets() {
        return refreshTokenIfNoResponse(
                token -> delegate.getProjectsWithFeatureSets(token), Collections.emptyList());
    }

    @Override
    public List<Mapping> getMappings(List<String> mappingIds) {
        return refreshTokenIfNoResponse(
                (token) -> delegate.getMappings(mappingIds, token), Collections.emptyList());
    }

    private <T> T refreshTokenIfNoResponse(Function<String, T> fetcher, T defaultResult) {
        String tokenString = keycloakService.getTokenString();
        try {
            return fetcher.apply(tokenString);
        } catch (NoGraphQlResponseException e) {
            refreshToken();
            tokenString = keycloakService.getTokenString();
            try {
                return fetcher.apply(tokenString);
            } catch (Throwable t) {
                logger.info("Unable to fetch result after token refresh: {}", t.getMessage());
                return defaultResult;
            }
        }
    }

    private void refreshToken() {
        try {
            keycloakService.refreshToken();
        } catch (Exception e) {
            logger.info("Error refreshing token: {}", e.getMessage());
        }
    }
}
