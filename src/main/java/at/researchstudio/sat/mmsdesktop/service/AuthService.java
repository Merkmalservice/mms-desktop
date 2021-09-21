package at.researchstudio.sat.mmsdesktop.service;

import at.researchstudio.sat.mmsdesktop.model.auth.UserSession;
import at.researchstudio.sat.mmsdesktop.model.task.LogoutResult;
import at.researchstudio.sat.mmsdesktop.util.JavaFXKeycloakInstalled;
import java.lang.invoke.MethodHandles;
import java.util.Locale;
import javafx.application.HostServices;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthService {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final JavaFXKeycloakInstalled keycloak;
    private Task<UserSession> loginTask;
    private Task<LogoutResult> logoutTask;

    public AuthService(HostServices hostService) {
        this.keycloak = new JavaFXKeycloakInstalled(hostService);
        this.keycloak.setLocale(Locale.getDefault());
        this.loginTask = generateLoginTask();
        this.logoutTask = generateLogoutTask();
    }

    private Task<UserSession> generateLoginTask() {
        return new Task<>() {
            @Override
            public UserSession call() throws Exception {
                try {
                    keycloak.loginDesktop();
                    return new UserSession(keycloak.getToken(), keycloak.getTokenString());
                } catch (InterruptedException e) {
                    logger.warn("Login process cancelled by User");
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
}
