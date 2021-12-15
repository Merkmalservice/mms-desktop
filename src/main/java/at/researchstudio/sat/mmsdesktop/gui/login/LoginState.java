package at.researchstudio.sat.mmsdesktop.gui.login;

import at.researchstudio.sat.mmsdesktop.model.auth.UserSession;
import at.researchstudio.sat.mmsdesktop.service.AuthService;
import at.researchstudio.sat.mmsdesktop.support.exception.UserDataDirException;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoginState {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String DATA_DIR_NAME = ".mms";
    private static final String TOKEN_FILENAME = "refreshtoken.txt";
    private final BooleanProperty loggedIn;
    private final StringProperty fullName;
    private final StringProperty userInitials;
    private final File tokenFile;
    private UserSession userSession;
    private final AuthService authService;

    public LoginState(AuthService authService) {
        loggedIn = new SimpleBooleanProperty(false);
        fullName = new SimpleStringProperty("Anonymous");
        userInitials = new SimpleStringProperty("AN");
        tokenFile = new File(getUserDataDirectory() + File.separator + TOKEN_FILENAME);
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
                writeToFile(this.userSession.getRefreshTokenString(), tokenFile);
            } catch (Exception e) {
                logger.error(
                        "Could not store RefreshToken in {}: {}",
                        tokenFile.getAbsolutePath(),
                        e.getMessage());
            }
        } else {
            this.userSession = null;
            loggedInProperty().setValue(false);
            fullNameProperty().setValue("Anonymous");
            userInitialsProperty().setValue("AN");
            try {
                if (tokenFile.delete()) {
                    logger.info(tokenFile.getName() + " deleted");
                } else {
                    logger.error("Failed to delete {}", tokenFile.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Failed to delete {}", tokenFile.getName());
            }
        }
    }

    public void writeToFile(String token, File file) throws Exception {
        try (OutputStream outputStream = new FileOutputStream(file);
                Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            writer.write(token);
        } catch (Exception e) {
            throw e;
        }
    }

    public String loadRefreshToken() {
        try {
            Object o = readTokenFromFile(tokenFile);
            return o.toString();
        } catch (Exception e) {
            logger.info("Could not load refresh token from {}:{}", tokenFile, e.getMessage());
            return null;
        }
    }

    public String readTokenFromFile(File file) throws Exception {
        try (FileInputStream fileInputStream = new FileInputStream(file);
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        fileInputStream, Charset.forName("UTF-8"))); ) {
            return reader.readLine();
        } catch (Exception e) {
            throw e;
        }
    }

    public String getUserDataDirectory() {
        String dataDir = System.getProperty("user.home") + File.separator + DATA_DIR_NAME;
        File dataDirFile = new File(dataDir);
        if (!dataDirFile.exists()) {
            boolean success = new File(dataDir).mkdir();
            if (!success) {
                throw new UserDataDirException(
                        String.format("Could not create user data directory under %s", dataDir));
            }
        }
        return dataDir;
    }
}
