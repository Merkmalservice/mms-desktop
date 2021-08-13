package at.researchstudio.sat.mmsdesktop.controller;

import at.researchstudio.sat.merkmalservice.model.*;
import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.mmsdesktop.controller.components.IconLabelTableCell;
import at.researchstudio.sat.mmsdesktop.logic.PropertyExtractor;
import at.researchstudio.sat.mmsdesktop.model.task.ExtractResult;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import at.researchstudio.sat.mmsdesktop.util.FileUtils;
import at.researchstudio.sat.mmsdesktop.util.IfcFileWrapper;
import at.researchstudio.sat.mmsdesktop.util.MessageUtils;
import com.jfoenix.controls.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.util.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.jena.ext.com.google.common.base.Throwables;
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
    @FXML private TableView centerPickFiles;
    @FXML private BorderPane centerProgress;
    @FXML private BorderPane centerResults;
    @FXML private JFXToggleButton centerResultUniqueValuesToggle;
    @FXML private TableColumn centerResultFeaturesTableTypeColumn;
    @FXML private TableColumn centerResultFeaturesTableQuantityKindColumn;
    @FXML private TableColumn centerResultFeaturesTableUnitColumn;
    @FXML private JFXTextField centerResultFeaturesSearch;
    @FXML private TableView centerResultFeaturesTable;
    @FXML private HBox bottomResults;
    @FXML private HBox bottomPickFiles;
    @FXML private BorderPane selectedFeaturePreview;
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
                .bind(stateService.getExtractState().selectedIfcFilesPresentProperty().not());
        bottomPickFilesExtract
                .disableProperty()
                .bind(stateService.getExtractState().selectedIfcFilesPresentProperty().not());

        centerResultLog
                .textProperty()
                .bind(stateService.getExtractState().extractLogOutputProperty());
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
                .addAll(new FileChooser.ExtensionFilter("IFC Files", "*.ifc"));

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

        stateService
                .getExtractState()
                .getSortedExtractedFeatures()
                .comparatorProperty()
                .bind(centerResultFeaturesTable.comparatorProperty());

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

        centerResultFeaturesTableTypeColumn.setCellFactory(
                c -> new IconLabelTableCell<>(resourceBundle));
        centerResultFeaturesTableTypeColumn.setCellValueFactory(
                (Callback<
                                TableColumn.CellDataFeatures<Feature, String>,
                                SimpleObjectProperty<Feature>>)
                        p -> {
                            if (p.getValue() != null) {
                                Feature f = p.getValue();
                                return new SimpleObjectProperty<>(f);
                            } else {
                                return new SimpleObjectProperty<>();
                            }
                        });

        centerResultFeaturesTableUnitColumn.setCellValueFactory(
                (Callback<TableColumn.CellDataFeatures<Feature, String>, ObservableValue<String>>)
                        p -> {
                            if (p.getValue() != null) {
                                Feature f = p.getValue();

                                if (f instanceof NumericFeature) {
                                    return new SimpleStringProperty(
                                            MessageUtils.getKeyForUnit(
                                                    resourceBundle,
                                                    ((NumericFeature) f).getUnit()));
                                }
                            }

                            return new SimpleStringProperty("");
                        });

        centerResultFeaturesTableQuantityKindColumn.setCellValueFactory(
                (Callback<TableColumn.CellDataFeatures<Feature, String>, ObservableValue<String>>)
                        p -> {
                            if (p.getValue() != null) {
                                Feature f = p.getValue();

                                if (f instanceof NumericFeature) {
                                    return new SimpleStringProperty(
                                            MessageUtils.getKeyForQuantityKind(
                                                    resourceBundle,
                                                    ((NumericFeature) f).getQuantityKind()));
                                }
                            }

                            return new SimpleStringProperty("");
                        });

        centerResultUniqueValuesToggle
                .selectedProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                stateService
                                        .getExtractState()
                                        .includeDescriptionInJsonOutput(newValue));

        centerResultFeaturesTable.setRowFactory(
                tv -> {
                    TableRow<Feature> row = new TableRow<>();
                    row.setOnMouseClicked(
                            event -> {
                                if (!row.isEmpty()) {
                                    Feature rowData = row.getItem();
                                    stateService
                                            .getSelectedFeatureState()
                                            .setSelectedFeature(row.getItem());
                                }
                            });
                    return row;
                });

        snackbar = new JFXSnackbar(parentPane);
    }

    @FXML
    public void handlePickDirectoryAction(ActionEvent actionEvent) {
        File selectedDirectory = directoryChooser.showDialog(parentPane.getScene().getWindow());
        try {
            stateService
                    .getExtractState()
                    .setSelectedIfcFiles(FileUtils.getIfcFilesFromDirectory(selectedDirectory));
        } catch (FileNotFoundException | NotDirectoryException e) {
            logger.warn("No Valid Directory selected");
        }
    }

    @FXML
    public void handlePickFileAction(ActionEvent actionEvent) {
        stateService
                .getExtractState()
                .setSelectedIfcFiles(
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
                logger.error(Throwables.getStackTraceAsString(ioException));
                // TODO: SHOW ERROR
            }
        }
    }

    @FXML
    public void handleClearListAction(ActionEvent actionEvent) {
        stateService.getExtractState().resetSelectedIfcFiles();
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
                PropertyExtractor.generateIfcFileToJsonTask(
                        stateService.getExtractState().getSelectedIfcFiles(), resourceBundle);

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

    public ObservableList<IfcFileWrapper> getSelectedIfcFiles() {
        return stateService.getExtractState().getSelectedIfcFiles();
    }

    public SortedList<Feature> getSortedExtractedFeatures() {
        return stateService.getExtractState().getSortedExtractedFeatures();
    }
}
