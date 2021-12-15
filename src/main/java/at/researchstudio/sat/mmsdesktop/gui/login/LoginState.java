package at.researchstudio.sat.mmsdesktop.gui.login;

import at.researchstudio.sat.mmsdesktop.model.auth.UserSession;
import at.researchstudio.sat.mmsdesktop.service.AuthService;
import com.thoughtworks.xstream.XStream;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;

import static org.apache.commons.io.IOUtils.close;

@Component
public class LoginState {
    private static final Logger logger =
                    LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final BooleanProperty loggedIn;
    private final StringProperty fullName;
    private final StringProperty userInitials;
    private final File settingsFile;
    private UserSession userSession;
    private final AuthService authService;

    public LoginState(AuthService authService) {
        loggedIn = new SimpleBooleanProperty(false);
        fullName = new SimpleStringProperty("Anonymous");
        userInitials = new SimpleStringProperty("AN");
        settingsFile = new File(getUserDataDirectory() + ".msSettings");
        this.authService = authService;
        loadRefreshTokenAndLogin();
    }

    private void loadRefreshTokenAndLogin() {
        String refreshToken = loadRefreshToken();
        if (refreshToken != null) {
            Task<UserSession> refreshTokenTask = authService.getRefreshTokenTask(refreshToken);
            refreshTokenTask.setOnSucceeded(
                            t -> {
                                setUserSession(refreshTokenTask.getValue());
                            });
            new Thread(refreshTokenTask).start();
        }
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

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn.set(loggedIn);
    }

    public void setUserSession(UserSession userSession) {
        if (userSession != null) {
            this.userSession = userSession;
            loggedInProperty().setValue(true);
            fullNameProperty().setValue(StringUtils.abbreviate(this.userSession.getFullName(), 18));
            userInitialsProperty().setValue(this.userSession.getInitials());
            try {
                File settingsFile = new File(getUserDataDirectory() + ".msSettings");
                writeToXML(this.userSession.getRefreshTokenString(), settingsFile);
            } catch (Exception e) {
                logger.error("Could not store RefreshToken due to {}", e);
            }
        } else {
            this.userSession = null;
            loggedInProperty().setValue(false);
            fullNameProperty().setValue("Anonymous");
            userInitialsProperty().setValue("AN");
            try {
                if (settingsFile.delete()) {
                    logger.info(settingsFile.getName() + " deleted");
                } else {
                    logger.error("Failed to delete {}", settingsFile.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Failed to delete {}", settingsFile.getName());
            }
        }
    }

    public void writeToXML(Object object, File file) throws Exception {
        XStream xStream = new XStream();
        OutputStream outputStream = null;
        Writer writer = null;
        try {
            outputStream = new FileOutputStream(file);
            writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
            xStream.toXML(object, writer);
        } catch (Exception e) {
            throw e;
        } finally {
            close(writer);
            close(outputStream);
        }
    }

    public String loadRefreshToken() {
        try {
            Object o = readFromXML(settingsFile);
            return o.toString();
        } catch (Exception e) {
            logger.error("Could not store RefreshToken due to {}", e);
            return null;
        }
    }

    public Object readFromXML(File file) throws Exception {
        XStream xStream = new XStream();
        FileInputStream fileInputStream = null;
        Reader reder = null;
        try {
            fileInputStream = new FileInputStream(file);
            reder = new InputStreamReader(fileInputStream, Charset.forName("UTF-8"));
            return xStream.fromXML(reder);
        } catch (Exception e) {
            throw e;
        } finally {
            close(reder);
            close(fileInputStream);
        }
    }

    public String getUserDataDirectory() {
        return System.getProperty("user.home") + File.separator;
    }
}
