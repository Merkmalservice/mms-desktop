package at.researchstudio.sat.mmsdesktop.gui.extract;

import at.researchstudio.sat.merkmalservice.ifc.FileWrapper;
import at.researchstudio.sat.merkmalservice.ifc.support.FileUtils;
import at.researchstudio.sat.merkmalservice.ifc.support.IfcUtils;
import at.researchstudio.sat.merkmalservice.model.JsonModel;
import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.mmsdesktop.logic.extract.PropertyExtractor;
import at.researchstudio.sat.mmsdesktop.model.task.ExtractResult;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import at.researchstudio.sat.mmsdesktop.support.MessageUtils;
import com.jfoenix.controls.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@FxmlView("extract.fxml")
public class ExtractController implements Initializable {
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
    @FXML private JFXTextArea centerResultLog;
    // BorderPane Elements
    @FXML private BorderPane parentPane;
    @FXML private HBox topPickFiles;
    @FXML private TableView<FileWrapper> centerPickFiles;
    @FXML private BorderPane centerProgress;
    @FXML private BorderPane centerResults;
    @FXML private JFXToggleButton centerResultUniqueValuesToggle;
    @FXML private JFXTextField centerResultFeaturesSearch;
    @FXML private HBox bottomResults;
    @FXML private HBox bottomPickFiles;
    private FileChooser saveFileChooser;
    private FileChooser saveLogFileChooser;
    private FileChooser fileChooser;
    private DirectoryChooser directoryChooser;
    private JFXSnackbar snackbar;
    private ResourceBundle resourceBundle;

    @Autowired
    public ExtractController(ReactiveStateService stateService) {
        this.stateService = stateService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        topPickFiles
                .visibleProperty()
                .bind(
                        stateService
                                .getExtractState()
                                .stateProperty()
                                .isEqualTo(ExtractState.INITIAL));
        topPickFiles
                .managedProperty()
                .bind(
                        stateService
                                .getExtractState()
                                .stateProperty()
                                .isEqualTo(ExtractState.INITIAL));
        centerPickFiles
                .visibleProperty()
                .bind(
                        stateService
                                .getExtractState()
                                .stateProperty()
                                .isEqualTo(ExtractState.INITIAL));
        centerPickFiles
                .managedProperty()
                .bind(
                        stateService
                                .getExtractState()
                                .stateProperty()
                                .isEqualTo(ExtractState.INITIAL));
        bottomPickFiles
                .visibleProperty()
                .bind(
                        stateService
                                .getExtractState()
                                .stateProperty()
                                .isEqualTo(ExtractState.INITIAL));
        bottomPickFiles
                .managedProperty()
                .bind(
                        stateService
                                .getExtractState()
                                .stateProperty()
                                .isEqualTo(ExtractState.INITIAL));
        centerProgress
                .visibleProperty()
                .bind(
                        stateService
                                .getExtractState()
                                .stateProperty()
                                .isEqualTo(ExtractState.PROCESSING));
        centerProgress
                .managedProperty()
                .bind(
                        stateService
                                .getExtractState()
                                .stateProperty()
                                .isEqualTo(ExtractState.PROCESSING));
        centerResults
                .visibleProperty()
                .bind(
                        stateService
                                .getExtractState()
                                .stateProperty()
                                .isEqualTo(ExtractState.EXTRACTED));
        centerResults
                .managedProperty()
                .bind(
                        stateService
                                .getExtractState()
                                .stateProperty()
                                .isEqualTo(ExtractState.EXTRACTED));
        bottomResults
                .visibleProperty()
                .bind(
                        stateService
                                .getExtractState()
                                .stateProperty()
                                .isEqualTo(ExtractState.EXTRACTED));
        bottomResults
                .managedProperty()
                .bind(
                        stateService
                                .getExtractState()
                                .stateProperty()
                                .isEqualTo(ExtractState.EXTRACTED));
        topPickFilesClearList
                .disableProperty()
                .bind(stateService.getExtractState().selectedExtractFilesPresentProperty().not());
        bottomPickFilesExtract
                .disableProperty()
                .bind(stateService.getExtractState().selectedExtractFilesPresentProperty().not());
        centerResultLog
                .textProperty()
                .bind(stateService.getExtractState().extractLogOutputProperty());
        centerResultFeaturesJson
                .textProperty()
                .bind(stateService.getExtractState().extractJsonOutput());
        this.resourceBundle = resourceBundle;
        if (stateService.getExtractState().getExtractedFeatures().size() > 0) {
            stateService.getExtractState().showExtractedView();
        } else {
            stateService.getExtractState().showInitialView();
        }
        fileChooser = new FileChooser();
        fileChooser
                .getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("JSON/IFC Files", "*.ifc", "*.json"));
        saveFileChooser = new FileChooser();
        saveFileChooser.setInitialFileName("extracted-features.json");
        saveFileChooser
                .getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        saveLogFileChooser = new FileChooser();
        saveLogFileChooser.setInitialFileName("extraction-log.txt");
        saveLogFileChooser
                .getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
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
                                                    if (StringUtils.isEmpty(searchText))
                                                        return true;
                                                    String name = feature.getName();
                                                    String description = feature.getDescription();
                                                    return (Objects.nonNull(name)
                                                                    && name.toLowerCase()
                                                                            .contains(
                                                                                    searchText
                                                                                            .toLowerCase()))
                                                            || (Objects.nonNull(description)
                                                                    && description
                                                                            .toLowerCase()
                                                                            .contains(
                                                                                    searchText
                                                                                            .toLowerCase()));
                                                }));
        centerResultUniqueValuesToggle
                .selectedProperty()
                .bindBidirectional(
                        stateService.getExtractState().includeInstanceValuesInJsonOutputProperty());
        snackbar = new JFXSnackbar(parentPane);
    }

    @FXML
    public void handlePickDirectoryAction(ActionEvent actionEvent) {
        File selectedDirectory = directoryChooser.showDialog(parentPane.getScene().getWindow());
        try {
            stateService
                    .getExtractState()
                    .setSelectedExtractFiles(
                            FileUtils.getValidExtractionFilesFromDirectory(selectedDirectory));
        } catch (FileNotFoundException | NotDirectoryException e) {
            logger.warn("No Valid Directory selected");
        }
    }

    @FXML
    public void handlePickFileAction(ActionEvent actionEvent) {
        stateService
                .getExtractState()
                .setSelectedExtractFiles(
                        fileChooser.showOpenMultipleDialog(parentPane.getScene().getWindow()));
    }

    @FXML
    public void handleSaveFileAction(ActionEvent actionEvent) {
        File file = saveFileChooser.showSaveDialog(parentPane.getScene().getWindow());
        if (Objects.nonNull(file)) {
            try {
                Utils.writeToJson(
                        file.getAbsolutePath(),
                        new JsonModel(
                                stateService.getExtractState().getFilteredExtractedFeatures(),
                                null,
                                stateService.getExtractState().getExtractedPropertySets()),
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
                logger.error(IfcUtils.stacktraceToString(ioException));
                // TODO: SHOW ERROR
            }
        }
    }

    @FXML
    public void handleSaveLogAction(ActionEvent actionEvent) {
        File file = saveLogFileChooser.showSaveDialog(parentPane.getScene().getWindow());
        if (Objects.nonNull(file)) {
            try {
                Files.writeString(
                        file.toPath(),
                        stateService.getExtractState().extractLogOutputProperty().getValue(),
                        StandardCharsets.UTF_8);
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
    public void handleClearListAction(ActionEvent actionEvent) {
        stateService.getExtractState().resetSelectedExtractFiles();
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
                PropertyExtractor.generateExtractFilesToJsonTask(
                        stateService.getExtractState().getSelectedExtractFiles(), resourceBundle);
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

    public ObservableList<FileWrapper> getSelectedExtractFiles() {
        return stateService.getExtractState().getSelectedExtractFiles();
    }
}
