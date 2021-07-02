package at.researchstudio.sat.mmsdesktop.model.auth;

import java.lang.invoke.MethodHandles;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserSession {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final AccessToken accessToken;

    public UserSession(AccessToken accessToken) {
        logger.info("Logged in...");
        logger.info("Token: " + accessToken.getSubject());
        logger.info("Username: " + accessToken.getPreferredUsername());

        this.accessToken = accessToken;
    }

    public String getUsername() {
        return accessToken.getPreferredUsername();
    }
}
