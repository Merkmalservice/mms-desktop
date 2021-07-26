package at.researchstudio.sat.mmsdesktop.controller;

import at.researchstudio.sat.merkmalservice.model.*;
import at.researchstudio.sat.merkmalservice.utils.ExcludeDescriptionStrategy;
import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.mmsdesktop.logic.PropertyExtractor;
import at.researchstudio.sat.mmsdesktop.model.task.ExtractResult;
import at.researchstudio.sat.mmsdesktop.util.FileUtils;
import at.researchstudio.sat.mmsdesktop.util.IfcFileWrapper;
import at.researchstudio.sat.mmsdesktop.util.MessageUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
import org.springframework.stereotype.Component;

@Component
@FxmlView("extract.fxml")
public class ExtractController implements Initializable {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
    @FXML private JFXTabPane centerResults;
    @FXML private JFXToggleButton centerResultUniqueValuesToggle;
    @FXML private TableColumn centerResultFeaturesTableTypeColumn;
    @FXML private JFXTextField centerResultFeaturesSearch;
    @FXML private TableView centerResultFeaturesTable;

    @FXML private HBox bottomResults;
    @FXML private HBox bottomPickFiles;

    private FileChooser saveFileChooser;
    private FileChooser saveLogFileChooser;
    private FileChooser fileChooser;
    private DirectoryChooser directoryChooser;
    private ObservableList<IfcFileWrapper> selectedIfcFiles;
    private ObservableList<Feature> extractedFeatures;
    private FilteredList<Feature> filteredExtractedFeatures;
    private SortedList<Feature> sortedExtractedFeatures;

    private JFXSnackbar snackbar;

    private ExtractResult extractResult;

    private ResourceBundle resourceBundle;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
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
        selectedIfcFiles = FXCollections.observableArrayList();
        extractedFeatures = FXCollections.observableArrayList();
        filteredExtractedFeatures = new FilteredList<>(extractedFeatures);
        sortedExtractedFeatures = new SortedList<>(filteredExtractedFeatures);

        sortedExtractedFeatures
                .comparatorProperty()
                .bind(centerResultFeaturesTable.comparatorProperty());

        centerResultFeaturesSearch
                .textProperty()
                .addListener(
                        (observable, oldValue, searchText) ->
                                filteredExtractedFeatures.setPredicate(
                                        feature -> {
                                            if (searchText == null || searchText.isEmpty())
                                                return true;
                                            return (feature.getName()
                                                            .toLowerCase()
                                                            .contains(searchText.toLowerCase()))
                                                    || (feature.getDescription()
                                                            .toLowerCase()
                                                            .contains(searchText.toLowerCase()));
                                        }));

        centerResultFeaturesTableTypeColumn.setCellValueFactory(
                new Callback<
                        TableColumn.CellDataFeatures<Feature, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(
                            TableColumn.CellDataFeatures<Feature, String> p) {
                        if (p.getValue() != null) {
                            Feature f = p.getValue();

                            if (f instanceof StringFeature) {
                                return new SimpleStringProperty("TEXT");
                            } else if (f instanceof EnumFeature) {
                                return new SimpleStringProperty("ENUM");
                            } else if (f instanceof ReferenceFeature) {
                                return new SimpleStringProperty("REFERENCE");
                            } else if (f instanceof BooleanFeature) {
                                return new SimpleStringProperty("BOOLE");
                            } else if (f instanceof NumericFeature) {
                                return new SimpleStringProperty("NUMERIC");
                            }
                        }

                        return new SimpleStringProperty("<no valid type>");
                    }
                });

        centerResultUniqueValuesToggle
                .selectedProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            if (newValue) {
                                Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
                                centerResultFeaturesJson.setText(
                                        gson.toJson(extractResult.getExtractedFeatures()));
                            } else {
                                Gson gson =
                                        (new GsonBuilder())
                                                .setExclusionStrategies(
                                                        new ExcludeDescriptionStrategy())
                                                .setPrettyPrinting()
                                                .create();
                                centerResultFeaturesJson.setText(
                                        gson.toJson(extractResult.getExtractedFeatures()));
                            }
                        });

        snackbar = new JFXSnackbar(parentPane);
    }

    @FXML
    public void handlePickDirectoryAction(ActionEvent actionEvent) {
        File selectedDirectory = directoryChooser.showDialog(parentPane.getScene().getWindow());
        try {
            List<File> selectedFiles = FileUtils.getIfcFilesFromDirectory(selectedDirectory);
            if (selectedFiles.size() > 0) {
                Set<IfcFileWrapper> selectedIfcFileSet = new HashSet<>(selectedIfcFiles);
                selectedIfcFileSet.addAll(
                        selectedFiles.stream()
                                .map(IfcFileWrapper::new)
                                .collect(Collectors.toList()));
                selectedIfcFiles.setAll(selectedIfcFileSet);
                topPickFilesClearList.setDisable(false);
                bottomPickFilesExtract.setDisable(false);
            }
        } catch (FileNotFoundException | NotDirectoryException e) {
            logger.warn("No Valid Directory selected");
        }
    }

    @FXML
    public void handlePickFileAction(ActionEvent actionEvent) {
        List<File> selectedFiles =
                fileChooser.showOpenMultipleDialog(parentPane.getScene().getWindow());
        if (Objects.nonNull(selectedFiles) && selectedFiles.size() > 0) {
            Set<IfcFileWrapper> selectedIfcFileSet = new HashSet<>(selectedIfcFiles);
            selectedIfcFileSet.addAll(
                    selectedFiles.stream().map(IfcFileWrapper::new).collect(Collectors.toList()));
            selectedIfcFiles.setAll(selectedIfcFileSet);
            topPickFilesClearList.setDisable(false);
            bottomPickFilesExtract.setDisable(false);
        }
    }

    @FXML
    public void handleSaveFileAction(ActionEvent actionEvent) {
        File file = saveFileChooser.showSaveDialog(parentPane.getScene().getWindow());

        if (Objects.nonNull(file)) {
            try {
                Utils.writeToJson(
                        file.getAbsolutePath(),
                        extractResult.getExtractedFeatures(),
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
                        file.toPath(), extractResult.getLogOutput(), StandardCharsets.UTF_8);
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
        selectedIfcFiles.clear();
        topPickFilesClearList.setDisable(true);
        bottomPickFilesExtract.setDisable(true);
    }

    @FXML
    public void handleResetAction(ActionEvent actionEvent) {
        handleClearListAction(actionEvent);
        extractResult = null;

        bottomPickFiles.setVisible(true);
        bottomPickFiles.setManaged(true);

        topPickFiles.setVisible(true);
        topPickFiles.setManaged(true);
        centerPickFiles.setVisible(true);

        centerProgress.setVisible(false);
        centerProgress.setManaged(false);

        centerResultFeaturesJson.setText("");
        centerResultLog.setText("");
        centerResults.setVisible(false);
        centerResults.setManaged(false);

        centerProgressProgressBar.progressProperty().unbind();
        centerProgressProgressInfo.textProperty().unbind();
        centerProgressLog.textProperty().unbind();

        bottomResults.setVisible(false);
        bottomResults.setManaged(false);
    }

    /*
     * @FXML public void handleRemoveSelectedEntryAction(ActionEvent actionEvent) { }
     */

    @FXML
    public void handleConvertAction(ActionEvent actionEvent) {
        bottomPickFiles.setVisible(false);
        bottomPickFiles.setManaged(false);

        topPickFiles.setVisible(false);
        topPickFiles.setManaged(false);
        centerPickFiles.setVisible(false);

        centerProgress.setVisible(true);
        centerProgress.setManaged(true);

        Task<ExtractResult> task =
                PropertyExtractor.generateIfcFileToJsonTask(selectedIfcFiles, resourceBundle);

        task.setOnSucceeded(
                t -> {
                    extractResult = task.getValue();

                    centerProgress.setVisible(false);
                    centerProgress.setManaged(false);

                    extractedFeatures.setAll(extractResult.getExtractedFeatures());

                    Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
                    centerResultFeaturesJson.setText(gson.toJson(extractedFeatures));

                    centerResultLog.setText(extractResult.getLogOutput());
                    centerResults.setVisible(true);
                    centerResults.setManaged(true);

                    bottomResults.setVisible(true);
                    bottomResults.setManaged(true);
                });

        task.setOnFailed(
                event -> {
                    // TODO: MAYBE SHOW DIALOG INSTEAD

                    extractResult = task.getValue();

                    centerProgress.setVisible(false);
                    centerProgress.setManaged(false);
                    centerResultFeaturesJson.setText("[]");
                    centerResultLog.setText(Throwables.getStackTraceAsString(task.getException()));
                    centerResults.setVisible(true);
                    centerResults.setManaged(true);

                    bottomResults.setVisible(true);
                    bottomResults.setManaged(true);
                });

        centerProgressProgressBar.progressProperty().bind(task.progressProperty());
        centerProgressProgressInfo.textProperty().bind(task.titleProperty());
        centerProgressLog.textProperty().bind(task.messageProperty());

        new Thread(task).start();
    }

    public ObservableList<IfcFileWrapper> getSelectedIfcFiles() {
        return selectedIfcFiles;
    }

    public void setSelectedIfcFiles(ObservableList<IfcFileWrapper> selectedIfcFiles) {
        this.selectedIfcFiles = selectedIfcFiles;
    }

    public SortedList<Feature> getSortedExtractedFeatures() {
        return sortedExtractedFeatures;
    }

    public void setSortedExtractedFeatures(SortedList<Feature> sortedExtractedFeatures) {
        this.sortedExtractedFeatures = sortedExtractedFeatures;
    }
}
