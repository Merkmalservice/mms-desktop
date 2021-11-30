package at.researchstudio.sat.mmsdesktop.gui.convert.perform;

import static at.researchstudio.sat.mmsdesktop.view.components.ProcessState.*;

import at.researchstudio.sat.merkmalservice.ifc.IfcFileWriter;
import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.ConversionEngine;
import at.researchstudio.sat.merkmalservice.ifc.convert.ConversionRule;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.MappingConversionRuleFactory;
import at.researchstudio.sat.merkmalservice.support.progress.TaskProgressListener;
import at.researchstudio.sat.mmsdesktop.gui.convert.inputfile.InputFileState;
import at.researchstudio.sat.mmsdesktop.model.task.IfcFileVO;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import at.researchstudio.sat.mmsdesktop.view.components.ProcessState;
import com.jfoenix.controls.JFXProgressBar;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@FxmlView("performConversion.fxml")
public class PerformConversionController implements Initializable {
    private FileChooser fileChooser;
    private ReactiveStateService stateService;
    private InputFileState inputFileState;
    private PerformConversionState state;
    @FXML BorderPane pcParentPane;
    @FXML private JFXProgressBar pcCenterProgressProgressBar;
    @FXML private BorderPane pcCenterProgress;
    @FXML private Label pcCenterProgressProgressInfo;

    @Autowired
    public PerformConversionController(ReactiveStateService stateService) {
        this.stateService = stateService;
        this.inputFileState = stateService.getConvertState().getInputFileState();
        this.state = stateService.getConvertState().getPerformConversionState();
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
    }

    @FXML
    public void handleConvertAction(ActionEvent actionEvent) {
        SimpleObjectProperty<ProcessState> processState =
                stateService
                        .getConvertState()
                        .getPerformConversionState()
                        .stepPerformConversionStatusProperty();
        File file = fileChooser.showSaveDialog(pcParentPane.getScene().getWindow());
        if (Objects.nonNull(file)) {
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
                            ParsedIfcFile convertedIfcFile =
                                    engine.convert(
                                            inputFileState.parsedIfcFileProperty().get(),
                                            taskProgressListener);
                            // TODO: Cancel op
                            IfcFileWriter.write(convertedIfcFile, file);
                            return new IfcFileVO(convertedIfcFile);
                        }
                    };
            task.setOnSucceeded(e -> processState.set(STEP_COMPLETE));
            task.setOnFailed(e -> processState.set(STEP_FAILED));
            stateService.getConvertState().getInputFileState().showLoadProgressView();
            pcCenterProgressProgressBar.progressProperty().bind(task.progressProperty());
            pcCenterProgressProgressInfo.textProperty().bind(task.messageProperty());
            processState.set(STEP_PROCESSING);
            new Thread(task).start();
        }
    }
}
