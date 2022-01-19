package at.researchstudio.sat.mmsdesktop.gui.convert.inputfile;

import static at.researchstudio.sat.mmsdesktop.view.components.ProcessState.*;

import at.researchstudio.sat.merkmalservice.ifc.IfcFileReader;
import at.researchstudio.sat.merkmalservice.ifc.IfcFileWrapper;
import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.ifc.support.FeatureUtils;
import at.researchstudio.sat.merkmalservice.support.progress.TaskProgressListener;
import at.researchstudio.sat.mmsdesktop.gui.component.feature.FeatureLabel;
import at.researchstudio.sat.mmsdesktop.gui.component.ifc.IfcLineClassLabel;
import at.researchstudio.sat.mmsdesktop.gui.convert.targetstandard.SelectTargetStandardController;
import at.researchstudio.sat.mmsdesktop.model.task.IfcFileVO;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import at.researchstudio.sat.mmsdesktop.view.components.ProcessState;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXProgressBar;
import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@FxmlView("selectInputFile.fxml")
public class SelectInputFileController implements Initializable {

    private FileChooser fileChooser;
    private final ReactiveStateService stateService;

    @FXML private BorderPane parentPane;

    @FXML private HBox topInputFile;
    @FXML private BorderPane centerInputFile;
    @FXML private AnchorPane bottomInputFile;
    @FXML private HBox topPickFile;

    @FXML private JFXProgressBar centerProgressProgressBar;
    @FXML private BorderPane centerProgress;
    @FXML private Label centerProgressProgressInfo;
    @FXML private JFXListView<IfcLine> fullFileContentList;
    @FXML private JFXListView<IfcLine> filteredFileContentList;
    @FXML private JFXListView<IfcLine> filteredFileContentList2;
    @FXML private JFXListView<FeatureLabel> extractedFeaturesList;
    @FXML private JFXListView<IfcLineClassLabel> extractedBuiltElementsList;

    @Autowired
    public SelectInputFileController(ReactiveStateService stateService) {
        this.stateService = stateService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        topPickFile
                .visibleProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .getInputFileState()
                                .stepFileStatusProperty()
                                .isEqualTo(STEP_ACTIVE));
        topPickFile
                .managedProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .getInputFileState()
                                .stepFileStatusProperty()
                                .isEqualTo(STEP_ACTIVE));
        topInputFile
                .visibleProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .getInputFileState()
                                .stepFileStatusProperty()
                                .isEqualTo(STEP_COMPLETE)
                                .or(
                                        stateService
                                                .getConvertState()
                                                .getInputFileState()
                                                .stepFileStatusProperty()
                                                .isEqualTo(STEP_FAILED)));
        topInputFile
                .managedProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .getInputFileState()
                                .stepFileStatusProperty()
                                .isEqualTo(STEP_COMPLETE)
                                .or(
                                        stateService
                                                .getConvertState()
                                                .getInputFileState()
                                                .stepFileStatusProperty()
                                                .isEqualTo(STEP_FAILED)));
        centerProgress
                .visibleProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .getInputFileState()
                                .stepFileStatusProperty()
                                .isEqualTo(STEP_PROCESSING));
        centerProgress
                .managedProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .getInputFileState()
                                .stepFileStatusProperty()
                                .isEqualTo(STEP_PROCESSING));

        centerInputFile
                .visibleProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .getInputFileState()
                                .stepFileStatusProperty()
                                .isEqualTo(STEP_COMPLETE)
                                .or(
                                        stateService
                                                .getConvertState()
                                                .getInputFileState()
                                                .stepFileStatusProperty()
                                                .isEqualTo(STEP_FAILED)));
        centerInputFile
                .managedProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .getInputFileState()
                                .stepFileStatusProperty()
                                .isEqualTo(STEP_COMPLETE)
                                .or(
                                        stateService
                                                .getConvertState()
                                                .getInputFileState()
                                                .stepFileStatusProperty()
                                                .isEqualTo(STEP_FAILED)));

        bottomInputFile
                .visibleProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .getInputFileState()
                                .stepFileStatusProperty()
                                .isEqualTo(STEP_COMPLETE));

        bottomInputFile
                .managedProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .getInputFileState()
                                .stepFileStatusProperty()
                                .isEqualTo(STEP_COMPLETE));

        extractedBuiltElementsList
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, deSelectedBuiltElement, selectedBuiltElement) ->
                                stateService
                                        .getConvertState()
                                        .getInputFileState()
                                        .getFilteredInputFileContent()
                                        .setPredicate(
                                                ifcLine -> {
                                                    if (Objects.isNull(selectedBuiltElement)) {
                                                        return true;
                                                    }

                                                    return ifcLine.getClass()
                                                            == selectedBuiltElement
                                                                    .getIfcLineClass();
                                                }));

        extractedFeaturesList
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, deSelectedIfcFeature, selectedIfcFeature) ->
                                stateService
                                        .getConvertState()
                                        .getInputFileState()
                                        .getFilteredInputFileContent()
                                        .setPredicate(
                                                ifcLine -> {
                                                    if (Objects.isNull(selectedIfcFeature)
                                                            || StringUtils.isEmpty(
                                                                    selectedIfcFeature
                                                                            .getFeature()
                                                                            .getName()))
                                                        return true;

                                                    return FeatureUtils.isFeatureWithinLine(
                                                            selectedIfcFeature.getFeature(),
                                                            ifcLine);
                                                }));

        fullFileContentList
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, deSelectedIfcLine, selectedIfcLine) ->
                                stateService
                                        .getConvertState()
                                        .getInputFileState()
                                        .setSelectedIfcLine(selectedIfcLine));

        filteredFileContentList
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, deSelectedIfcLine, selectedIfcLine) ->
                                stateService
                                        .getConvertState()
                                        .getInputFileState()
                                        .setSelectedIfcLine(selectedIfcLine));

        filteredFileContentList2
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, deSelectedIfcLine, selectedIfcLine) ->
                                stateService
                                        .getConvertState()
                                        .getInputFileState()
                                        .setSelectedIfcLine(selectedIfcLine));

        fileChooser = new FileChooser();
        fileChooser
                .getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("IFC File", "*.ifc"));
    }

    @FXML
    public void handleToTargetStandardPicker(ActionEvent actionEvent) {
        stateService.getViewState().switchCenterPane(SelectTargetStandardController.class);
        if (stateService
                .getConvertState()
                .getInputFileState()
                .stepFileStatusProperty()
                .get()
                .isActive()) {
            stateService
                    .getConvertState()
                    .getInputFileState()
                    .stepFileStatusProperty()
                    .set(ProcessState.STEP_OPEN);
        }
        if (stateService
                .getConvertState()
                .getTargetStandardState()
                .stepTargetStandardStatusProperty()
                .get()
                .isOpen()) {
            stateService
                    .getConvertState()
                    .getTargetStandardState()
                    .stepTargetStandardStatusProperty()
                    .set(ProcessState.STEP_ACTIVE);
        }
    }

    @FXML
    public void handlePickFileAction(ActionEvent actionEvent) {
        File file = fileChooser.showOpenDialog(parentPane.getScene().getWindow());

        if (Objects.nonNull(file) && file.exists()) {
            // TODO: Maybe handle if/else within task, but probably not
            Task<IfcFileVO> task =
                    new Task<>() {
                        @Override
                        protected IfcFileVO call() throws Exception {
                            TaskProgressListener taskProgressListener =
                                    new TaskProgressListener() {
                                        @Override
                                        public void notifyProgress(
                                                String task, String message, float level) {
                                            updateProgress(level, 100);
                                        }

                                        @Override
                                        public void notifyFinished(String task) {
                                            updateProgress(100, 100);
                                        }

                                        @Override
                                        public void notifyFailed(String s) {}
                                    };
                            // TODO: ERROR HANDLING FOR BETTER USABILITY
                            ParsedIfcFile parsedIfcFile =
                                    IfcFileReader.readIfcFile(
                                            new IfcFileWrapper(file), taskProgressListener);

                            // TODO: Cancel op
                            return new IfcFileVO(parsedIfcFile);
                        }
                    };

            task.setOnSucceeded(
                    t ->
                            stateService
                                    .getConvertState()
                                    .getInputFileState()
                                    .setFileStepResult(task));

            task.setOnFailed(
                    event -> {
                        // TODO: MAYBE SHOW DIALOG INSTEAD
                        stateService.getConvertState().getInputFileState().setFileStepResult(task);
                        stateService.getConvertState().resetConvertResults();
                    });
            stateService.getConvertState().getInputFileState().showLoadProgressView();
            centerProgressProgressBar.progressProperty().bind(task.progressProperty());
            centerProgressProgressInfo.textProperty().bind(task.titleProperty());
            new Thread(task).start();
        }
    }

    @FXML
    public void handleResetFileAction(ActionEvent actionEvent) {
        stateService.getConvertState().getInputFileState().resetSelectedConvertFile();
        stateService.getConvertState().resetConvertResults();
    }

    public ObservableList<IfcLine> getFileContentList() {
        return stateService.getConvertState().getInputFileState().getInputFileContent();
    }

    public ObservableList<FeatureLabel> getFileContentFeatures() {
        return stateService.getConvertState().getInputFileState().getInputFileExtractedFeatures();
    }

    public ObservableList<IfcLineClassLabel> getFileContentBuiltElements() {
        return stateService
                .getConvertState()
                .getInputFileState()
                .getInputFileExtractedIfcLineClasses();
    }

    public ObservableList<IfcLine> getFileContentFiltered() {
        return stateService.getConvertState().getInputFileState().getFilteredInputFileContent();
    }
}
