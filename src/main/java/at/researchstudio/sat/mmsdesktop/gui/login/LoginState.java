package at.researchstudio.sat.mmsdesktop.gui.login;

import at.researchstudio.sat.mmsdesktop.model.auth.UserSession;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class LoginState {
    private final BooleanProperty loggedIn;
    private final StringProperty fullName;
    private final StringProperty userInitials;
    private UserSession userSession;

    public LoginState() {
        loggedIn = new SimpleBooleanProperty(false);
        fullName = new SimpleStringProperty("Anonymous");
        userInitials = new SimpleStringProperty("AN");
    }

    public boolean isLoggedIn() {
        return loggedIn.get();
    }

    public BooleanProperty loggedInProperty() {
        return loggedIn;
    }

    public String getFullName() {
        return fullName.get();
    }

    public StringProperty fullNameProperty() {
        return fullName;
    }

    public String getUserInitials() {
        return userInitials.get();
    }

    public StringProperty userInitialsProperty() {
        return userInitials;
    }

    public UserSession getUserSession() {
        return userSession;
    }

    public void setUserSession(UserSession userSession) {
        if (userSession != null) {
            this.userSession = userSession;
            loggedInProperty().setValue(true);
            fullNameProperty().setValue(StringUtils.abbreviate(this.userSession.getFullName(), 18));
            userInitialsProperty().setValue(this.userSession.getInitials());
        } else {
            this.userSession = null;
            loggedInProperty().setValue(false);
            fullNameProperty().setValue("Anonymous");
            userInitialsProperty().setValue("AN");
        }
    }
}
