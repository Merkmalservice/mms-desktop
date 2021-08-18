package at.researchstudio.sat.mmsdesktop.controller;

import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.mmsdesktop.logic.PropertyExtractor;
import at.researchstudio.sat.mmsdesktop.model.task.ExtractResult;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import at.researchstudio.sat.mmsdesktop.util.FileUtils;
import at.researchstudio.sat.mmsdesktop.util.FileWrapper;
import at.researchstudio.sat.mmsdesktop.util.MessageUtils;
import com.jfoenix.controls.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.file.NotDirectoryException;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.jena.ext.com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@FxmlView("extractjson.fxml")
public class ExtractFromJsonController implements Initializable {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ReactiveStateService stateService;
    // @FXML private JFXButton bRemoveSelectedEntry;
    @FXML private JFXButton topPickFilesClearList;
    @FXML private JFXButton bottomPickFilesExtract;
    @FXML private JFXProgressBar centerProgressProgressBar;
    @FXML private JFXTextArea centerProgressLog;
    @FXML private Label centerProgressProgressInfo;
    @FXML private JFXTextArea centerResultFeaturesJson;
    // BorderPane Elements
    @FXML private BorderPane parentPane;
    @FXML private HBox topPickFiles;
    @FXML private TableView centerPickFiles;
    @FXML private BorderPane centerProgress;
    @FXML private BorderPane centerResults;
    @FXML private JFXToggleButton centerResultUniqueValuesToggle;

    @FXML private JFXTextField centerResultFeaturesSearch;
    @FXML private HBox bottomResults;
    @FXML private HBox bottomPickFiles;
    @FXML private BorderPane selectedFeaturePreview;
    private FileChooser saveFileChooser;
    private FileChooser fileChooser;
    private DirectoryChooser directoryChooser;
    private JFXSnackbar snackbar;
    private ResourceBundle resourceBundle;

    @Autowired
    public ExtractFromJsonController(ReactiveStateService stateService) {
        this.stateService = stateService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        topPickFiles.visibleProperty().bind(stateService.getExtractState().showInitialProperty());
        topPickFiles.managedProperty().bind(stateService.getExtractState().showInitialProperty());
        centerPickFiles
                .visibleProperty()
                .bind(stateService.getExtractState().showInitialProperty());
        centerPickFiles
                .managedProperty()
                .bind(stateService.getExtractState().showInitialProperty());
        bottomPickFiles
                .visibleProperty()
                .bind(stateService.getExtractState().showInitialProperty());
        bottomPickFiles
                .managedProperty()
                .bind(stateService.getExtractState().showInitialProperty());

        centerProgress
                .visibleProperty()
                .bind(stateService.getExtractState().showExtractProcessProperty());
        centerProgress
                .managedProperty()
                .bind(stateService.getExtractState().showExtractProcessProperty());

        centerResults
                .visibleProperty()
                .bind(stateService.getExtractState().showExtractedProperty());
        centerResults
                .managedProperty()
                .bind(stateService.getExtractState().showExtractedProperty());
        bottomResults
                .visibleProperty()
                .bind(stateService.getExtractState().showExtractedProperty());
        bottomResults
                .managedProperty()
                .bind(stateService.getExtractState().showExtractedProperty());

        topPickFilesClearList
                .disableProperty()
                .bind(stateService.getExtractState().selectedJsonFilesPresentProperty().not());
        bottomPickFilesExtract
                .disableProperty()
                .bind(stateService.getExtractState().selectedJsonFilesPresentProperty().not());

        centerResultFeaturesJson
                .textProperty()
                .bind(stateService.getExtractState().extractJsonOutput());

        selectedFeaturePreview
                .visibleProperty()
                .bind(stateService.getSelectedFeatureState().showSelectedFeatureProperty());
        selectedFeaturePreview
                .managedProperty()
                .bind(stateService.getSelectedFeatureState().showSelectedFeatureProperty());

        this.resourceBundle = resourceBundle;

        if (stateService.getExtractState().getExtractedFeatures().size() > 0) {
            stateService.getExtractState().showExtractedView();
        } else {
            stateService.getExtractState().showInitialView();
        }

        fileChooser = new FileChooser();
        fileChooser
                .getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        saveFileChooser = new FileChooser();
        saveFileChooser.setInitialFileName("extracted-features.json");
        saveFileChooser
                .getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        directoryChooser = new DirectoryChooser();

        centerResultFeaturesSearch
                .textProperty()
                .addListener(
                        (observable, oldValue, searchText) ->
                                stateService
                                        .getExtractState()
                                        .getFilteredExtractedFeatures()
                                        .setPredicate(
                                                feature -> {
                                                    if (searchText == null || searchText.isEmpty())
                                                        return true;
                                                    return (feature.getName()
                                                                    .toLowerCase()
                                                                    .contains(
                                                                            searchText
                                                                                    .toLowerCase()))
                                                            || (feature.getDescription()
                                                                    .toLowerCase()
                                                                    .contains(
                                                                            searchText
                                                                                    .toLowerCase()));
                                                }));

        centerResultUniqueValuesToggle
                .selectedProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                stateService
                                        .getExtractState()
                                        .includeDescriptionInJsonOutput(newValue));

        snackbar = new JFXSnackbar(parentPane);
    }

    @FXML
    public void handlePickDirectoryAction(ActionEvent actionEvent) {
        File selectedDirectory = directoryChooser.showDialog(parentPane.getScene().getWindow());
        try {
            stateService
                    .getExtractState()
                    .setSelectedJsonFiles(FileUtils.getJsonFilesFromDirectory(selectedDirectory));
        } catch (FileNotFoundException | NotDirectoryException e) {
            logger.warn("No Valid Directory selected");
        }
    }

    @FXML
    public void handlePickFileAction(ActionEvent actionEvent) {
        stateService
                .getExtractState()
                .setSelectedJsonFiles(
                        fileChooser.showOpenMultipleDialog(parentPane.getScene().getWindow()));
    }

    @FXML
    public void handleSaveFileAction(ActionEvent actionEvent) {
        File file = saveFileChooser.showSaveDialog(parentPane.getScene().getWindow());

        if (Objects.nonNull(file)) {
            try {
                Utils.writeToJson(
                        file.getAbsolutePath(),
                        stateService.getExtractState().getExtractedFeatures(),
                        centerResultUniqueValuesToggle.selectedProperty().getValue());

                final String message =
                        MessageUtils.getKeyWithParameters(
                                resourceBundle,
                                "label.extract.export.success",
                                file.getAbsolutePath());

                Platform.runLater(
                        () ->
                                snackbar.fireEvent(
                                        new JFXSnackbar.SnackbarEvent(
                                                new JFXSnackbarLayout(message),
                                                Duration.seconds(5),
                                                null)));

            } catch (IOException ioException) {
                logger.error(Throwables.getStackTraceAsString(ioException));
                // TODO: SHOW ERROR
            }
        }
    }

    @FXML
    public void handleClearListAction(ActionEvent actionEvent) {
        stateService.getExtractState().resetSelectedJsonFiles();
    }

    @FXML
    public void handleResetAction(ActionEvent actionEvent) {
        handleClearListAction(actionEvent);
        stateService.getExtractState().resetExtractResults();
        stateService.getExtractState().showInitialView();

        centerProgressProgressBar.progressProperty().unbind();
        centerProgressProgressInfo.textProperty().unbind();
        centerProgressLog.textProperty().unbind();
    }

    /*
     * @FXML public void handleRemoveSelectedEntryAction(ActionEvent actionEvent) { }
     */

    @FXML
    public void handleConvertAction(ActionEvent actionEvent) {
        stateService.getExtractState().showProcessView();

        Task<ExtractResult> task =
                PropertyExtractor.generateJsonFilesToJsonTask(
                        stateService.getExtractState().getSelectedJsonFiles(), resourceBundle);

        task.setOnSucceeded(
                t -> {
                    stateService.getExtractState().setExtractResult(task);
                    stateService.getExtractState().showExtractedView();
                });

        task.setOnFailed(
                event -> {
                    // TODO: MAYBE SHOW DIALOG INSTEAD
                    stateService.getExtractState().setExtractResult(task);
                    stateService.getExtractState().showExtractedView();
                });

        centerProgressProgressBar.progressProperty().bind(task.progressProperty());
        centerProgressProgressInfo.textProperty().bind(task.titleProperty());
        centerProgressLog.textProperty().bind(task.messageProperty());

        new Thread(task).start();
    }

    public ObservableList<FileWrapper> getSelectedJsonFiles() {
        return stateService.getExtractState().getSelectedJsonFiles();
    }
}
