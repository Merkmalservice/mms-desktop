package at.researchstudio.sat.mmsdesktop.gui.convert.perform;

import static at.researchstudio.sat.mmsdesktop.view.components.ProcessState.*;

import at.researchstudio.sat.merkmalservice.ifc.IfcFileWriter;
import at.researchstudio.sat.merkmalservice.ifc.convert.ConversionEngine;
import at.researchstudio.sat.merkmalservice.ifc.convert.ConversionRule;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.MappingConversionRuleFactory;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.ifc.support.IfcUtils;
import at.researchstudio.sat.merkmalservice.support.progress.TaskProgressListener;
import at.researchstudio.sat.mmsdesktop.gui.convert.inputfile.InputFileState;
import at.researchstudio.sat.mmsdesktop.gui.convert.inputfile.SelectInputFileController;
import at.researchstudio.sat.mmsdesktop.model.task.IfcFileVO;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import at.researchstudio.sat.mmsdesktop.support.MessageUtils;
import at.researchstudio.sat.mmsdesktop.view.components.ProcessState;
import com.jfoenix.controls.*;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Collection;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
    private FileChooser saveLogFileChooser;
    private JFXSnackbar snackbar;
    private final ReactiveStateService stateService;
    private final InputFileState inputFileState;
    @FXML BorderPane pcParentPane;
    @FXML private JFXProgressBar pcCenterProgressProgressBar;
    @FXML private BorderPane pcCenterProgress;

    @FXML private BorderPane pcCenterResults;
    @FXML private JFXListView<IfcLine> pcCenterConvertedFileContentList;
    @FXML private JFXListView<IfcLine> pcCenterChangesLinesList;
    @FXML private JFXTextArea pcCenterResultLog;
    @FXML private HBox pcBottomResults;

    @FXML private GridPane pcCenterCheck;
    @FXML private AnchorPane pcBottomCheck;

    @FXML private Label pcCenterProgressProgressInfo;

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

        saveLogFileChooser = new FileChooser();
        saveLogFileChooser.setInitialFileName("extraction-log.txt");
        saveLogFileChooser
                .getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        SimpleObjectProperty<ProcessState> processState =
                stateService
                        .getConvertState()
                        .getPerformConversionState()
                        .stepPerformConversionStatusProperty();
        pcBottomCheck.visibleProperty().bind(processState.isEqualTo(STEP_ACTIVE));
        pcBottomCheck.managedProperty().bind(processState.isEqualTo(STEP_ACTIVE));
        pcCenterCheck.visibleProperty().bind(processState.isEqualTo(STEP_ACTIVE));
        pcCenterCheck.managedProperty().bind(processState.isEqualTo(STEP_ACTIVE));

        pcCenterProgress.visibleProperty().bind(processState.isEqualTo(STEP_PROCESSING));
        pcCenterProgress.managedProperty().bind(processState.isEqualTo(STEP_PROCESSING));

        pcCenterResults.visibleProperty().bind(processState.isEqualTo(STEP_COMPLETE));
        pcCenterResults.managedProperty().bind(processState.isEqualTo(STEP_COMPLETE));
        pcBottomResults.visibleProperty().bind(processState.isEqualTo(STEP_COMPLETE));
        pcBottomResults.managedProperty().bind(processState.isEqualTo(STEP_COMPLETE));

        pcCenterResultLog
                .textProperty()
                .bind(stateService.getConvertState().convertLogOutputProperty());

        pcCenterChangesLinesList
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, deSelectedIfcLine, selectedIfcLine) -> {
                            stateService
                                    .getConvertState()
                                    .getOutputFileState()
                                    .setSelectedChangedIfcLine(selectedIfcLine);
                        });

        snackbar = new JFXSnackbar(pcParentPane);
    }

    @FXML
    public void handleSaveConvertedFileAction(ActionEvent actionEvent) {
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
    public void handleSaveLogAction(ActionEvent actionEvent) {
        File file = saveLogFileChooser.showSaveDialog(pcParentPane.getScene().getWindow());
        if (Objects.nonNull(file)) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                IfcFileWriter.writeChangeLog(
                        stateService
                                .getConvertState()
                                .getOutputFileState()
                                .convertedIfcFileProperty()
                                .get()
                                .getChanges(),
                        writer);
                final String message =
                        MessageUtils.getKeyWithParameters(
                                resourceBundle,
                                "label.extract.exportLog.success",
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
    public void handleResetAction(ActionEvent actionEvent) {
        stateService.getConvertState().resetConvertResults();
        stateService
                .getConvertState()
                .getPerformConversionState()
                .stepPerformConversionStatusProperty()
                .set(ProcessState.STEP_ACTIVE);
        stateService.getConvertState().getInputFileState().resetSelectedConvertFile();
        stateService.getViewState().switchCenterPane(SelectInputFileController.class);

        pcCenterProgressProgressBar.progressProperty().unbind();
        pcCenterProgressProgressInfo.textProperty().unbind();
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
                    String lastTitle = "";
                    final StringBuilder logOutput = new StringBuilder();

                    @Override
                    protected IfcFileVO call() throws Exception {
                        TaskProgressListener taskProgressListener =
                                new TaskProgressListener() {
                                    @Override
                                    public void notifyProgress(
                                            String title, String message, float level) {
                                        updateProgress(level, 1);
                                        if (Objects.nonNull(title) && !lastTitle.equals(title)) {
                                            updateTitle(title);
                                            lastTitle = title;
                                        }
                                    }

                                    @Override
                                    public void notifyFinished(String title) {
                                        updateProgress(1, 1);
                                        updateTitle(title + ": FINISHED");
                                    }

                                    @Override
                                    public void notifyFailed(String s) {
                                        updateTitle("FAILED");
                                    }
                                };
                        // TODO: ERROR HANDLING FOR BETTER USABILITY
                        Collection<ConversionRule> rules =
                                new MappingConversionRuleFactory(
                                                stateService
                                                        .getConvertState()
                                                        .getTargetStandardState()
                                                        .selectedMappingsProperty(),
                                                stateService
                                                        .getConvertState()
                                                        .getTargetStandardState()
                                                        .availableStandardsWithPropertySetsProperty())
                                        .getRules();
                        ConversionEngine engine = new ConversionEngine(rules);

                        // TODO: Cancel op
                        return new IfcFileVO(
                                engine.convert(
                                        inputFileState.parsedIfcFileProperty().get(),
                                        taskProgressListener),
                                logOutput.toString());
                    }
                };
        task.setOnSucceeded(
                e -> {
                    stateService.getConvertState().setConvertResults(task);
                    // TODO: PROCESS-STATE SHOULD BE IN A DIFFERENT PLACE
                    processState.set(STEP_COMPLETE);
                });
        task.setOnFailed(
                e -> {
                    // TODO: MAYBE SHOW DIALOG INSTEAD
                    stateService.getConvertState().setConvertResults(task);
                    // TODO: PROCESS-STATE SHOULD BE IN A DIFFERENT PLACE
                    processState.set(STEP_FAILED);
                });
        pcCenterProgressProgressBar.progressProperty().bind(task.progressProperty());
        pcCenterProgressProgressInfo.textProperty().bind(task.titleProperty());
        processState.set(STEP_PROCESSING);
        new Thread(task).start();
    }

    public ObservableList<IfcLine> getFileContentList() {
        return stateService.getConvertState().getOutputFileState().getOutputFileContent();
    }

    public ObservableList<IfcLine> getAllChangedLinesList() {
        return stateService.getConvertState().getOutputFileState().getAllChangedLines();
    }

    public String getProjectName() {
        // TODO: Correct PROJECTNAME property
        return stateService
                .getConvertState()
                .getTargetStandardState()
                .selectedProjectProperty()
                .get()
                .getName();
    }

    public String getTargetStandardName() {
        // TODO: Correct STANDARDNAME property
        if (stateService
                        .getConvertState()
                        .getTargetStandardState()
                        .selectedTargetStandardProperty()
                        .get()
                        .getOrganization()
                != null) {
            return stateService
                            .getConvertState()
                            .getTargetStandardState()
                            .selectedProjectProperty()
                            .get()
                            .getName()
                    + "::"
                    + stateService
                            .getConvertState()
                            .getTargetStandardState()
                            .selectedTargetStandardProperty()
                            .get()
                            .getOrganization()
                            .getName();
        } else {
            return "TODO: PROJECT STANDARD";
        }
    }

    public int getMappingRuleCount() {
        return stateService
                .getConvertState()
                .getTargetStandardState()
                .selectedMappingsProperty()
                .size();
    }

    public String getInputFileName() {
        return stateService
                .getConvertState()
                .getInputFileState()
                .parsedIfcFileProperty()
                .get()
                .getIfcFileWrapper()
                .getName();
    }
}
