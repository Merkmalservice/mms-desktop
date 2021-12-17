package at.researchstudio.sat.merkmalservice.api.auth;

import at.researchstudio.sat.merkmalservice.api.auth.event.UserLoggedInEvent;
import at.researchstudio.sat.merkmalservice.api.auth.event.UserLoggedOutEvent;
import at.researchstudio.sat.mmsdesktop.support.exception.UserDataDirException;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class LocalRefreshTokenService implements InitializingBean, ApplicationListener {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String DATA_DIR_NAME = ".mms";
    private static final String TOKEN_FILENAME = "refreshtoken.txt";
    KeycloakService keycloakService;
    private final File tokenFile;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    @Autowired private ApplicationEventPublisher eventPublisher;

    public LocalRefreshTokenService(@Autowired KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
        tokenFile = new File(getUserDataDir() + File.separator + TOKEN_FILENAME);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        executorService.submit(
                () -> {
                    try {
                        ensureUserDataDirExists();
                        String refreshToken = loadRefreshToken();
                        if (refreshToken != null) {
                            keycloakService.refreshToken(refreshToken);
                        }
                    } catch (Exception e) {
                        logger.info("Unable to refresh token: {}", e.getMessage());
                    }
                });
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof UserLoggedOutEvent) {
            deleteTokenFile();
        } else if (applicationEvent instanceof UserLoggedInEvent) {
            storeRefreshTokenLocally();
        }
    }

    private void deleteTokenFile() {
        executorService.submit(
                () -> {
                    try {
                        tokenFile.delete();
                    } catch (Exception e) {
                        logger.info(
                                "Unable to delete token file {}: {} ",
                                tokenFile.getAbsolutePath(),
                                e.getMessage());
                    }
                });
    }

    public void storeRefreshTokenLocally() {
        executorService.submit(
                () -> {
                    try {
                        String refreshToken = keycloakService.getRefreshToken();
                        if (refreshToken == null) {
                            throw new IllegalStateException("No refresh token available");
                        }
                        writeToFile(refreshToken, tokenFile);
                    } catch (Exception e) {
                        logger.info("Unable to store refresh token locally: {}", e.getMessage());
                    }
                });
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

    public String ensureUserDataDirExists() {
        String dataDir = getUserDataDir();
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

    @NotNull
    private String getUserDataDir() {
        String dataDir = System.getProperty("user.home") + File.separator + DATA_DIR_NAME;
        return dataDir;
    }
}
