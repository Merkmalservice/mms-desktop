package at.researchstudio.sat.mmsdesktop.controller;

import at.researchstudio.sat.mmsdesktop.controller.components.FeatureLabel;
import at.researchstudio.sat.mmsdesktop.controller.components.IfcLineClassLabel;
import at.researchstudio.sat.mmsdesktop.logic.IfcFileReader;
import at.researchstudio.sat.mmsdesktop.model.ifc.*;
import at.researchstudio.sat.mmsdesktop.model.task.LoadResult;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import at.researchstudio.sat.mmsdesktop.util.FeatureUtils;
import at.researchstudio.sat.mmsdesktop.util.IfcFileWrapper;
import at.researchstudio.sat.mmsdesktop.view.components.JFXStepButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXProgressBar;
import java.io.File;
import java.net.URL;
import java.util.*;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@FxmlView("convert.fxml")
public class ConvertController implements Initializable {
    private FileChooser fileChooser;
    private final ReactiveStateService stateService;

    @FXML private BorderPane centerInputFile;
    @FXML private BorderPane parentPane;
    @FXML private HBox topPickFile;
    @FXML private HBox topInputFile;
    @FXML private JFXProgressBar centerProgressProgressBar;
    @FXML private BorderPane centerProgress;
    @FXML private Label centerProgressProgressInfo;
    @FXML private JFXListView<IfcLine> fullFileContentList;
    @FXML private JFXListView<IfcLine> filteredFileContentList;
    @FXML private JFXListView<IfcLine> filteredFileContentList2;
    @FXML private JFXListView<FeatureLabel> extractedFeaturesList;
    @FXML private JFXListView<IfcLineClassLabel> extractedBuiltElementsList;

    @Autowired
    public ConvertController(ReactiveStateService stateService) {
        this.stateService = stateService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        topPickFile
                .visibleProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .stepFileStatusProperty()
                                .isEqualTo(JFXStepButton.ACTIVE));
        topPickFile
                .managedProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .stepFileStatusProperty()
                                .isEqualTo(JFXStepButton.ACTIVE));
        topInputFile
                .visibleProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .stepFileStatusProperty()
                                .isEqualTo(JFXStepButton.COMPLETE));
        topInputFile
                .managedProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .stepFileStatusProperty()
                                .isEqualTo(JFXStepButton.COMPLETE));
        centerProgress
                .visibleProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .stepFileStatusProperty()
                                .isEqualTo(JFXStepButton.PROCESSING));
        centerProgress
                .managedProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .stepFileStatusProperty()
                                .isEqualTo(JFXStepButton.PROCESSING));

        centerInputFile
                .visibleProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .stepFileStatusProperty()
                                .isEqualTo(JFXStepButton.COMPLETE));
        centerInputFile
                .managedProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .stepFileStatusProperty()
                                .isEqualTo(JFXStepButton.COMPLETE));

        extractedBuiltElementsList
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, deSelectedBuiltElement, selectedBuiltElement) ->
                                stateService
                                        .getConvertState()
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
                                stateService.getConvertState().setSelectedIfcLine(selectedIfcLine));

        filteredFileContentList
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, deSelectedIfcLine, selectedIfcLine) ->
                                stateService.getConvertState().setSelectedIfcLine(selectedIfcLine));

        filteredFileContentList2
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, deSelectedIfcLine, selectedIfcLine) ->
                                stateService.getConvertState().setSelectedIfcLine(selectedIfcLine));

        fileChooser = new FileChooser();
        fileChooser
                .getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("IFC File", "*.ifc"));
    }

    @FXML
    public void handlePickFileAction(ActionEvent actionEvent) {
        File file = fileChooser.showOpenDialog(parentPane.getScene().getWindow());

        if (Objects.nonNull(file) && file.exists()) {
            // TODO: Maybe handle if/else within task, but probably not
            Task<LoadResult> task =
                    new Task<>() {
                        @Override
                        protected LoadResult call() throws Exception {
                            // TODO: ERROR HANDLING FOR BETTER USABILITY
                            ParsedIfcFile parsedIfcFile =
                                    IfcFileReader.readIfcFile(new IfcFileWrapper(file));

                            // TODO: Cancel op
                            return new LoadResult(parsedIfcFile);
                        }
                    };

            task.setOnSucceeded(
                    t -> {
                        stateService.getConvertState().setFileStepResult(task);
                    });

            task.setOnFailed(
                    event -> {
                        // TODO: MAYBE SHOW DIALOG INSTEAD
                        stateService.getConvertState().setFileStepResult(task);
                        stateService
                                .getConvertState()
                                .stepFileStatusProperty()
                                .setValue(JFXStepButton.FAILED);
                    });
            stateService.getConvertState().showLoadProgressView();
            centerProgressProgressBar.progressProperty().bind(task.progressProperty());
            centerProgressProgressInfo.textProperty().bind(task.titleProperty());

            new Thread(task).start();
        }
    }

    @FXML
    public void handleResetFileAction(ActionEvent actionEvent) {
        stateService.getConvertState().resetSelectedConvertFile();
    }

    public ObservableList<IfcLine> getFileContentList() {
        return stateService.getConvertState().getInputFileContent();
    }

    public ObservableList<FeatureLabel> getFileContentFeatures() {
        return stateService.getConvertState().getInputFileExtractedFeatures();
    }

    public ObservableList<IfcLineClassLabel> getFileContentBuiltElements() {
        return stateService.getConvertState().getInputFileExtractedIfcLineClasses();
    }

    public ObservableList<IfcLine> getFileContentFiltered() {
        return stateService.getConvertState().getFilteredInputFileContent();
    }
}
