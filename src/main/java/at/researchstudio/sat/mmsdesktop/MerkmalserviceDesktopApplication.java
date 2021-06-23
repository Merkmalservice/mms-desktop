package at.researchstudio.sat.mmsdesktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.FutureTask;

@SpringBootApplication public class MerkmalserviceDesktopApplication {
    public static final Thread.UncaughtExceptionHandler ALERT_EXCEPTION_HANDLER =
        (thread, cause) -> {
            try {
                cause.printStackTrace();
                final Runnable showDialog = () -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("An unknown error occurred");
                    alert.showAndWait();
                };
                if (Platform.isFxApplicationThread()) {
                    showDialog.run();
                } else {
                    FutureTask<Void> showDialogTask = new FutureTask<Void>(showDialog, null);
                    Platform.runLater(showDialogTask);
                    showDialogTask.get();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                System.exit(-1);
            }
        };

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(ALERT_EXCEPTION_HANDLER);
        Application.launch(UIApplication.class, args);
    }
}
