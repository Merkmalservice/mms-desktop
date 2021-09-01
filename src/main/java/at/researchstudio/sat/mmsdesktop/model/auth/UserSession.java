package at.researchstudio.sat.mmsdesktop.model.auth;

import at.researchstudio.sat.mmsdesktop.util.Utils;
import java.lang.invoke.MethodHandles;
import org.apache.commons.lang3.StringUtils;
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

    public String getInitials() {
        String initials =
                Utils.executeOrDefaultOnException(
                                () -> String.valueOf(getGivenName().charAt(0)), "")
                        + Utils.executeOrDefaultOnException(
                                () -> String.valueOf(getFamilyName().charAt(0)), "");
        return StringUtils.isEmpty(initials) ? String.valueOf(getUsername().charAt(0)) : initials;
    }

    public String getFullName() {
        String name = accessToken.getName();
        return StringUtils.isEmpty(name) ? getUsername() : name;
    }

    public String getFamilyName() {
        return accessToken.getFamilyName();
    }

    public String getGivenName() {
        return accessToken.getGivenName();
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }
}
