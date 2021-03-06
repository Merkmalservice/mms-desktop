package at.researchstudio.sat.mmsdesktop.model.auth;

import at.researchstudio.sat.merkmalservice.ifc.support.IfcUtils;
import java.lang.invoke.MethodHandles;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserSession {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final AccessToken accessToken;
    private final String tokenString;
    private final String refreshTokenString;

    public UserSession(AccessToken accessToken) {
        logger.info("Logged in as '{}'", accessToken.getPreferredUsername());
        this.accessToken = accessToken;
        this.tokenString = null;
        this.refreshTokenString = null;
    }

    public UserSession(AccessToken accessToken, String tokenString, String refreshTokenString) {
        logger.info("Logged in as '{}'", accessToken.getPreferredUsername());
        this.accessToken = accessToken;
        this.tokenString = tokenString;
        this.refreshTokenString = refreshTokenString;
    }

    public UserSession(String tokenString, String refreshTokenString) {
        logger.info("Logged in without access token");
        this.accessToken = null;
        this.tokenString = tokenString;
        this.refreshTokenString = refreshTokenString;
    }

    public String getUsername() {
        return accessToken.getPreferredUsername();
    }

    public String getInitials() {
        String initials =
                IfcUtils.executeOrDefaultOnException(
                                () -> String.valueOf(getGivenName().charAt(0)), "")
                        + IfcUtils.executeOrDefaultOnException(
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

    public String getTokenString() {
        return tokenString;
    }

    public String getRefreshTokenString() {
        return refreshTokenString;
    }
}
