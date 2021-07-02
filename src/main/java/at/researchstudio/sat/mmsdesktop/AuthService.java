package at.researchstudio.sat.mmsdesktop;

import at.researchstudio.sat.mmsdesktop.model.task.LoginResult;
import at.researchstudio.sat.mmsdesktop.model.task.LogoutResult;
import at.researchstudio.sat.mmsdesktop.util.JavaFXKeycloakInstalled;
import java.lang.invoke.MethodHandles;
import java.util.Locale;
import javafx.application.HostServices;
import javafx.concurrent.Task;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthService {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Task<LoginResult> loginTask;
    private final Task<LogoutResult> logoutTask;
    private final JavaFXKeycloakInstalled keycloak;

    public AuthService(HostServices hostService) {
        this.keycloak = new JavaFXKeycloakInstalled(hostService);
        this.loginTask = generateLoginTask();
        this.logoutTask = generateLogoutTask();
    }

    private Task<LoginResult> generateLoginTask() {

        return new Task<>() {
            @Override
            public LoginResult call() throws Exception {
                keycloak.setLocale(Locale.getDefault());

                try {
                    keycloak.loginDesktop();

                    AccessToken token = keycloak.getToken();
                    logger.info("Logged in...");
                    logger.info("Token: " + token.getSubject());
                    logger.info("Username: " + token.getPreferredUsername());
                    logger.info("AccessToken: " + keycloak.getTokenString());
                } catch (InterruptedException e) {
                    logger.warn("Login process cancelled by User");
                }

                return new LoginResult(keycloak.getToken());
            }
        };
    }

    private Task<LogoutResult> generateLogoutTask() {
        return new Task<>() {
            @Override
            public LogoutResult call() throws Exception {
                keycloak.setLocale(Locale.getDefault());

                try {
                    keycloak.logout();
                } catch (InterruptedException e) {
                    logger.warn("Logout process cancelled by User");
                }

                return new LogoutResult(true);
            }
        };
    }

    public Task<LoginResult> getLoginTask() {
        return loginTask;
    }
}
