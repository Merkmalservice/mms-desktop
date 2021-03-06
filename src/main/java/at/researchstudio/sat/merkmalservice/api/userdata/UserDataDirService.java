package at.researchstudio.sat.merkmalservice.api.userdata;

import at.researchstudio.sat.mmsdesktop.support.exception.UserDataDirException;
import java.io.File;
import java.lang.invoke.MethodHandles;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserDataDirService implements InitializingBean {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final String userDataDir;

    @Autowired
    public UserDataDirService(@Value("${mms.desktop.userDataDir}") String userDataDir) {
        this.userDataDir = userDataDir;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            ensureUserDataDirExists();
        } catch (Exception e) {
            logger.info("Unable to refresh token: {}", e.getMessage());
        }
    }

    public void ensureUserDataDirExists() {
        String dataDir = getUserDataDir();
        File dataDirFile = new File(dataDir);
        if (!dataDirFile.exists()) {
            boolean success = new File(dataDir).mkdir();
            if (!success) {
                throw new UserDataDirException(
                        String.format("Could not create user data directory under %s", dataDir));
            }
        }
    }

    @NotNull
    public String getUserDataDir() {
        return userDataDir;
    }
}
