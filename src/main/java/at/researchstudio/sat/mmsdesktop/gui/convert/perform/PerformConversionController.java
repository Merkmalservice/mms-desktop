package at.researchstudio.sat.mmsdesktop.gui.convert.perform;

import static at.researchstudio.sat.mmsdesktop.view.components.ProcessState.*;

import at.researchstudio.sat.merkmalservice.ifc.IfcFileWriter;
import at.researchstudio.sat.merkmalservice.ifc.convert.ConversionEngine;
import at.researchstudio.sat.merkmalservice.ifc.convert.ConversionRule;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.MappingConversionRuleFactory;
import at.researchstudio.sat.merkmalservice.ifc.support.IfcUtils;
import at.researchstudio.sat.merkmalservice.support.progress.TaskProgressListener;
import at.researchstudio.sat.mmsdesktop.gui.convert.inputfile.InputFileState;
import at.researchstudio.sat.mmsdesktop.model.task.IfcFileVO;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import at.researchstudio.sat.mmsdesktop.support.MessageUtils;
import at.researchstudio.sat.mmsdesktop.view.components.ProcessState;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSnackbarLayout;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Collection;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import net.rgielen.fxweaver.core.FxmlView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@FxmlView("performConversion.fxml")
public class PerformConversionController implements Initializable {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private FileChooser fileChooser;
    private JFXSnackbar snackbar;
    private ReactiveStateService stateService;
    private InputFileState inputFileState;
    @FXML BorderPane pcParentPane;
    @FXML private JFXProgressBar pcCenterProgressProgressBar;
    @FXML private BorderPane pcCenterProgress;
    @FXML private Label pcCenterProgressProgressInfo;
    @FXML private Button saveConvertedFileButton;
    @FXML private Button performConversionButton;

    private final ResourceBundle resourceBundle = ResourceBundle.getBundle("messages");

    @Autowired
    public PerformConversionController(ReactiveStateService stateService) {
        this.stateService = stateService;
        this.inputFileState = stateService.getConvertState().getInputFileState();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileChooser = new FileChooser();
        fileChooser
                .getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("IFC File", "*.ifc"));
        SimpleObjectProperty<ProcessState> processState =
                stateService
                        .getConvertState()
                        .getPerformConversionState()
                        .stepPerformConversionStatusProperty();
        pcCenterProgress.visibleProperty().bind(processState.isEqualTo(STEP_PROCESSING));
        pcCenterProgress.managedProperty().bind(processState.isEqualTo(STEP_PROCESSING));
        pcParentPane.visibleProperty().bind(processState.isNotEqualTo(STEP_DISABLED));
        pcParentPane.managedProperty().bind(processState.isNotEqualTo(STEP_DISABLED));

        saveConvertedFileButton.disableProperty().bind(processState.isNotEqualTo(STEP_COMPLETE));
        performConversionButton.disableProperty().bind(processState.isEqualTo(STEP_COMPLETE));

        snackbar = new JFXSnackbar(pcParentPane);
    }

    @FXML
    public void handleSaveConvertedFileAction(ActionEvent actionEvent) {
        // TODO: FILE SAVE
        File file = fileChooser.showSaveDialog(pcParentPane.getScene().getWindow());
        if (Objects.nonNull(file)) {
            try {
                // TODO: MOVE TO TASK AND ALSO CHECK FOR CONV FILE PRESENCE (IF TASK ALSO CANCEL OP
                // TODO)
                IfcFileWriter.write(
                        stateService
                                .getConvertState()
                                .getOutputFileState()
                                .convertedIfcFileProperty()
                                .get(),
                        file);
                final String message =
                        MessageUtils.getKeyWithParameters(
                                resourceBundle,
                                "label.convert.perform.success",
                                file.getAbsolutePath());

                Platform.runLater(
                        () ->
                                snackbar.fireEvent(
                                        new JFXSnackbar.SnackbarEvent(
                                                new JFXSnackbarLayout(message),
                                                Duration.seconds(5),
                                                null)));
            } catch (IOException ioException) {
                logger.error(IfcUtils.stacktraceToString(ioException));
                // TODO: SHOW ERROR
            }
        }
    }

    @FXML
    public void handleConvertAction(ActionEvent actionEvent) {
        SimpleObjectProperty<ProcessState> processState =
                stateService
                        .getConvertState()
                        .getPerformConversionState()
                        .stepPerformConversionStatusProperty();
        Task<IfcFileVO> task =
                new Task<>() {
                    @Override
                    protected IfcFileVO call() throws Exception {
                        TaskProgressListener taskProgressListener =
                                new TaskProgressListener() {
                                    @Override
                                    public void notifyProgress(
                                            String task, String message, float level) {
                                        updateProgress(level, 1);
                                        updateMessage(task + ": " + message);
                                    }

                                    @Override
                                    public void notifyFinished(String task) {
                                        updateProgress(1, 1);
                                    }

                                    @Override
                                    public void notifyFailed(String s) {}
                                };
                        // TODO: ERROR HANDLING FOR BETTER USABILITY
                        Collection<ConversionRule> rules =
                                new MappingConversionRuleFactory(
                                                stateService
                                                        .getConvertState()
                                                        .getTargetStandardState()
                                                        .mappingsProperty())
                                        .getRules();
                        ConversionEngine engine = new ConversionEngine(rules);

                        // TODO: Cancel op
                        return new IfcFileVO(
                                engine.convert(
                                        inputFileState.parsedIfcFileProperty().get(),
                                        taskProgressListener));
                    }
                };
        task.setOnSucceeded(
                e -> {
                    stateService.getConvertState().getOutputFileState().setFileStepResult(task);
                    // TODO: PROCESS-STATE SHOULD BE IN A DIFFERENT PLACE
                    processState.set(STEP_COMPLETE);
                });
        task.setOnFailed(
                e -> {
                    // TODO: MAYBE SHOW DIALOG INSTEAD
                    stateService.getConvertState().getOutputFileState().setFileStepResult(task);
                    // TODO: PROCESS-STATE SHOULD BE IN A DIFFERENT PLACE
                    processState.set(STEP_FAILED);
                });
        pcCenterProgressProgressBar.progressProperty().bind(task.progressProperty());
        pcCenterProgressProgressInfo.textProperty().bind(task.messageProperty());
        processState.set(STEP_PROCESSING);
        new Thread(task).start();
    }
}
