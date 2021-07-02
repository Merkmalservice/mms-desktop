package at.researchstudio.sat.mmsdesktop.service;

import at.researchstudio.sat.mmsdesktop.model.auth.UserSession;
import at.researchstudio.sat.mmsdesktop.model.task.LogoutResult;
import at.researchstudio.sat.mmsdesktop.util.JavaFXKeycloakInstalled;
import java.lang.invoke.MethodHandles;
import java.util.Locale;
import javafx.application.HostServices;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthService {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Task<UserSession> loginTask;
    private final Task<LogoutResult> logoutTask;
    private final JavaFXKeycloakInstalled keycloak;

    public AuthService(HostServices hostService) {
        this.keycloak = new JavaFXKeycloakInstalled(hostService);
        this.keycloak.setLocale(Locale.getDefault());
        this.loginTask = generateLoginTask();
        this.logoutTask = generateLogoutTask();
        loggedIn = new SimpleBooleanProperty(false);
        userName = new SimpleStringProperty("Anonymous");
    }

    private UserSession userSession;

    private final BooleanProperty loggedIn;
    private final StringProperty userName;

    private Task<UserSession> generateLoginTask() {

        return new Task<>() {
            @Override
            public UserSession call() throws Exception {
                try {
                    keycloak.loginDesktop();
                    return new UserSession(keycloak.getToken());
                } catch (InterruptedException e) {
                    logger.warn("Login process cancelled by User");
                }

                return null;
            }
        };
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

    public void setUserSession(UserSession userSession) {
        if (userSession != null) {
            this.userSession = userSession;
            loggedInProperty().setValue(true);
            userNameProperty().setValue(this.userSession.getUsername());
        } else {
            this.userSession = null;
            loggedInProperty().setValue(false);
            userNameProperty().setValue("Anonymous");
        }
    }

    public Task<UserSession> getLoginTask() {
        return loginTask;
    }

    public Task<LogoutResult> getLogoutTask() {
        return logoutTask;
    }

    public UserSession getUserSession() {
        return userSession;
    }

    public boolean isLoggedIn() {
        return loggedIn.get();
    }

    public BooleanProperty loggedInProperty() {
        return loggedIn;
    }

    public String getUserName() {
        return userName.get();
    }

    public StringProperty userNameProperty() {
        return userName;
    }

    public boolean isSignedIn() {
        return userSession != null;
    }
}