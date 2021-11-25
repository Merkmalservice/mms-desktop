package at.researchstudio.sat.mmsdesktop;

import at.researchstudio.sat.mmsdesktop.UIApplication.StageReadyEvent;
import at.researchstudio.sat.mmsdesktop.controller.MainController;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {
    @Value("classpath:/at/researchstudio/sat/mmsdesktop/controller/main.fxml")
    private Resource mainResource;

    private final String applicationTitle;
    private final ApplicationContext applicationContext;

    public StageInitializer(
            @Value("${spring.application.ui.title}") String applicationTitle,
            ApplicationContext applicationContext) {
        this.applicationTitle = applicationTitle;
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent stageReadyEvent) {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);

        Parent parent =
                fxWeaver.loadView(
                        MainController.class,
                        ResourceBundle.getBundle("messages", Locale.getDefault()));
        Stage stage = stageReadyEvent.getStage();
        stage.setScene(new Scene(parent, 800, 600));
        stage.setTitle(applicationTitle);
        stage.show();
    }
}
