package at.researchstudio.sat.mmsdesktop.controller;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.mmsdesktop.controller.components.IfcLineView;
import at.researchstudio.sat.mmsdesktop.logic.IfcFileReader;
import at.researchstudio.sat.mmsdesktop.model.ifc.*;
import at.researchstudio.sat.mmsdesktop.model.task.LoadResult;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import at.researchstudio.sat.mmsdesktop.util.IfcFileWrapper;
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
    @FXML private JFXListView<Feature> extractedFeaturesList;
    @FXML private BorderPane selectedIfcLineView;
    @FXML private IfcLineView ifcLineView;

    @Autowired
    public ConvertController(ReactiveStateService stateService) {
        this.stateService = stateService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        topPickFile.visibleProperty().bind(stateService.getConvertState().showInitialProperty());
        topPickFile.managedProperty().bind(stateService.getConvertState().showInitialProperty());
        topInputFile.visibleProperty().bind(stateService.getConvertState().showInputFileProperty());
        topInputFile.managedProperty().bind(stateService.getConvertState().showInputFileProperty());
        centerProgress
                .visibleProperty()
                .bind(stateService.getConvertState().showLoadProgressProperty());
        centerProgress
                .managedProperty()
                .bind(stateService.getConvertState().showLoadProgressProperty());

        centerInputFile
                .visibleProperty()
                .bind(stateService.getConvertState().showInputFileProperty());
        centerInputFile
                .managedProperty()
                .bind(stateService.getConvertState().showInputFileProperty());

        stateService
                .getConvertState()
                .selectedIfcLineProperty()
                .addListener(
                        ((observableValue, oldValue, selectedIfcLine) -> {
                            selectedIfcLineView.setVisible(Objects.nonNull(selectedIfcLine));
                            selectedIfcLineView.setManaged(Objects.nonNull(selectedIfcLine));
                            ifcLineView.setExtractedFeatures(
                                    stateService.getConvertState().getInputFileExtractedFeatures());
                            ifcLineView.setIfcDataLines(
                                    stateService.getConvertState().getInputFileDataLines());
                            ifcLineView.setIfcDataLinesByClass(
                                    stateService.getConvertState().getInputFileDataLinesByClass());
                            ifcLineView.setIfcLine(selectedIfcLine);
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
                                                                    selectedIfcFeature.getName()))
                                                        return true;

                                                    String translatedName =
                                                            at.researchstudio.sat.merkmalservice
                                                                    .utils.Utils
                                                                    .convertUtf8ToIFCString(
                                                                            selectedIfcFeature
                                                                                    .getName());
                                                    return ifcLine.getLine()
                                                            .contains("'" + translatedName + "'");
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
                        stateService.getConvertState().setLoadResult(task);
                        stateService.getConvertState().showConvertView();
                    });

            task.setOnFailed(
                    event -> {
                        // TODO: MAYBE SHOW DIALOG INSTEAD
                        stateService.getConvertState().setLoadResult(task);
                        stateService.getConvertState().showConvertView();
                    });
            stateService.getConvertState().showLoadProgressView();
            centerProgressProgressBar.progressProperty().bind(task.progressProperty());
            centerProgressProgressInfo.textProperty().bind(task.titleProperty());

            new Thread(task).start();
        }
    }

    @FXML
    public void handleCloseLineAction(ActionEvent actionEvent) {
        stateService.getConvertState().closeSelectedIfcLine();
    }

    @FXML
    public void handleResetFileAction(ActionEvent actionEvent) {
        stateService.getConvertState().resetSelectedConvertFile();
    }

    public ObservableList<IfcLine> getFileContentList() {
        return stateService.getConvertState().getInputFileContent();
    }

    public ObservableList<Feature> getFileContentFeatures() {
        return stateService.getConvertState().getInputFileExtractedFeatures();
    }

    public ObservableList<IfcLine> getFileContentFiltered() {
        return stateService.getConvertState().getFilteredInputFileContent();
    }
}
