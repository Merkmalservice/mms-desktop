package at.researchstudio.sat.mmsdesktop;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.FutureTask;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(
        basePackages = {"at.researchstudio.sat.mmsdesktop", "at.researchstudio.sat.merkmalservice"})
public class MerkmalserviceDesktopApplication {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final Thread.UncaughtExceptionHandler ALERT_EXCEPTION_HANDLER =
            (thread, cause) -> {
                try {
                    logger.error("Error:", cause);
                    final Runnable showDialog =
                            () -> {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setContentText("An unknown error occurred");
                                alert.showAndWait();
                            };
                    if (Platform.isFxApplicationThread()) {
                        showDialog.run();
                    } else {
                        FutureTask<Void> showDialogTask = new FutureTask<>(showDialog, null);
                        Platform.runLater(showDialogTask);
                        showDialogTask.get();
                    }
                } catch (Throwable t) {
                    logger.error("Error", t);
                }
                System.exit(-1);
            };

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(ALERT_EXCEPTION_HANDLER);
        Application.launch(UIApplication.class, args);
    }
}
