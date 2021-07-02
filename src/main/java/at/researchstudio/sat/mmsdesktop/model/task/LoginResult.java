package at.researchstudio.sat.mmsdesktop.model.task;

import org.keycloak.representations.AccessToken;

public class LoginResult {
    private final AccessToken token;

    public LoginResult(AccessToken token) {
        this.token = token;
    }
}
