package at.researchstudio.sat.mmsdesktop.controller;

import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.mmsdesktop.logic.PropertyExtractor;
import at.researchstudio.sat.mmsdesktop.model.task.ExtractResult;
import at.researchstudio.sat.mmsdesktop.util.FileUtils;
import at.researchstudio.sat.mmsdesktop.util.IfcFileWrapper;
import at.researchstudio.sat.mmsdesktop.util.MessageUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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

@Component public class ExtractController implements Initializable {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  // @FXML private JFXButton bRemoveSelectedEntry;
  @FXML private JFXButton topPickFilesClearList;
  @FXML private JFXButton bottomPickFilesExtract;

  @FXML private JFXProgressBar centerProgressProgressBar;
  @FXML private JFXTextArea centerProgressLog;
  @FXML private Label centerProgressProgressInfo;
  @FXML private JFXTextArea centerResultFeatures;
  @FXML private JFXTextArea centerResultLog;

  // BorderPane Elements
  @FXML private BorderPane parentPane;

  @FXML private HBox topPickFiles;

  @FXML private TableView centerPickFiles;
  @FXML private BorderPane centerProgress;
  @FXML private JFXTabPane centerResults;

  @FXML private HBox bottomResults;
  @FXML private HBox bottomPickFiles;

  private FileChooser saveFileChooser;
  private FileChooser saveLogFileChooser;
  private FileChooser fileChooser;
  private DirectoryChooser directoryChooser;
  private ObservableList<IfcFileWrapper> selectedIfcFiles;

  private JFXSnackbar snackbar;

  private ExtractResult extractResult;

  private ResourceBundle resourceBundle;

  @Override public void initialize(URL url, ResourceBundle resourceBundle) {
    this.resourceBundle = resourceBundle;
    fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("IFC Files", "*.ifc"));

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

    snackbar = new JFXSnackbar(parentPane);
  }

  @FXML
  public void handlePickDirectoryAction(ActionEvent actionEvent) {
    File selectedDirectory = directoryChooser.showDialog(parentPane.getScene().getWindow());
    try {
      List<File> selectedFiles = FileUtils.getIfcFilesFromDirectory(selectedDirectory);
      if (selectedFiles.size() > 0) {
        Set<IfcFileWrapper> selectedIfcFileSet = new HashSet<IfcFileWrapper>(selectedIfcFiles);
        selectedIfcFileSet
            .addAll(selectedFiles.stream().map(IfcFileWrapper::new).collect(Collectors.toList()));
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
      Set<IfcFileWrapper> selectedIfcFileSet = new HashSet<IfcFileWrapper>(selectedIfcFiles);
      selectedIfcFileSet
          .addAll(selectedFiles.stream().map(IfcFileWrapper::new).collect(Collectors.toList()));
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
        Utils.writeToJson(file.getAbsolutePath(), extractResult.getExtractedFeatures());
        final String message =
            MessageUtils.getKeyWithParameters(
                resourceBundle, "label.extract.export.success", file.getAbsolutePath());

        Platform.runLater(
            () ->
                snackbar.fireEvent(
                    new JFXSnackbar.SnackbarEvent(
                        new JFXSnackbarLayout(message), Duration.seconds(5), null)));

      } catch (IOException ioException) {
        ioException.printStackTrace();
        // TODO: SHOW ERROR
      }
    }
  }

  @FXML
  public void handleSaveLogAction(ActionEvent actionEvent) {
    File file = saveLogFileChooser.showSaveDialog(parentPane.getScene().getWindow());

    if (Objects.nonNull(file)) {
      try {
        Files.writeString(file.toPath(), extractResult.getLogOutput(), StandardCharsets.UTF_8);
        final String message =
            MessageUtils.getKeyWithParameters(
                resourceBundle, "label.extract.exportLog.success", file.getAbsolutePath());

        Platform.runLater(
            () ->
                snackbar.fireEvent(new JFXSnackbar.SnackbarEvent(new JFXSnackbarLayout(message),
                    Duration.seconds(5), null)));

      } catch (IOException ioException) {
        ioException.printStackTrace();
        // TODO: SHOW ERROR
      }
    }
  }

  @FXML public void handleClearListAction(ActionEvent actionEvent) {
    selectedIfcFiles.clear();
    topPickFilesClearList.setDisable(true);
    bottomPickFilesExtract.setDisable(true);
  }

  @FXML public void handleResetAction(ActionEvent actionEvent) {
    handleClearListAction(actionEvent);
    extractResult = null;

    bottomPickFiles.setVisible(true);
    bottomPickFiles.setManaged(true);

    topPickFiles.setVisible(true);
    topPickFiles.setManaged(true);
    centerPickFiles.setVisible(true);

    centerProgress.setVisible(false);
    centerProgress.setManaged(false);

    centerResultFeatures.setText("");
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

  @FXML public void handleConvertAction(ActionEvent actionEvent) {
    bottomPickFiles.setVisible(false);
    bottomPickFiles.setManaged(false);

    topPickFiles.setVisible(false);
    topPickFiles.setManaged(false);
    centerPickFiles.setVisible(false);

    centerProgress.setVisible(true);
    centerProgress.setManaged(true);

    Task<ExtractResult> task =
        PropertyExtractor.generateIfcFileToJsonTask(selectedIfcFiles, resourceBundle);

    task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, t -> {
      extractResult = task.getValue();

      centerProgress.setVisible(false);
      centerProgress.setManaged(false);
      Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
      centerResultFeatures.setText(gson.toJson(extractResult.getExtractedFeatures()));
          centerResultLog.setText(extractResult.getLogOutput());
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
}
