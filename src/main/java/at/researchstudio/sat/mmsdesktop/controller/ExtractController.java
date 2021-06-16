package at.researchstudio.sat.mmsdesktop.controller;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.mmsdesktop.logic.PropertyExtractor;
import at.researchstudio.sat.mmsdesktop.model.task.ExtractResult;
import at.researchstudio.sat.mmsdesktop.util.FileUtils;
import at.researchstudio.sat.mmsdesktop.util.FileWrapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.beans.value.ObservableBooleanValue;
import javafx.concurrent.Task;
import javafx.collections.FXCollections;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
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
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ExtractController implements Initializable {
  private static final Logger logger =
          LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


  @FXML private JFXButton bClearList;
  @FXML private JFXButton bExtract;
  //@FXML private JFXButton bRemoveSelectedEntry;
  @FXML private BorderPane borderPane;
  @FXML private JFXButton bPickFile;
  @FXML private JFXButton bPickDirectory;
  @FXML private TableView ifcFileTable;
  @FXML private HBox hbFileActions;
  @FXML private BorderPane bpProgress;
  @FXML private JFXProgressBar pbExtraction;
  @FXML private JFXTextArea taProgressLog;
  @FXML private Label lProgressInfo;
  @FXML private JFXTabPane tpResult;
  @FXML private JFXTextArea taExtractedFeatures;
  @FXML private JFXTextArea taExtractLogOutput;
  @FXML private JFXButton bSaveFile;
  @FXML private JFXButton bSaveLog;

  private FileChooser saveFileChooser;
  private FileChooser saveLogFileChooser;
  private FileChooser fileChooser;
  private DirectoryChooser directoryChooser;
  private Set<FileWrapper> selectedIfcFiles;

  private JFXSnackbar snackbar;

  private ExtractResult extractResult;

  private ResourceBundle resourceBundle;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.resourceBundle = resourceBundle;
    fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("IFC Files", "*.ifc"));

    saveFileChooser = new FileChooser();
    saveFileChooser.setInitialFileName("extracted-features.json");
    saveFileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

    saveLogFileChooser = new FileChooser();
    saveFileChooser.setInitialFileName("extraction-log.txt");
    saveFileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

    directoryChooser = new DirectoryChooser();
    selectedIfcFiles = new HashSet<>();
    ifcFileTable.setItems(FXCollections.observableArrayList(selectedIfcFiles));

    snackbar = new JFXSnackbar(borderPane);
    snackbar.setStyle("-fx-text-fill: white; -fx-background-color: black"); //TODO: Update SnackBar Style
  }

  @FXML
  public void handlePickDirectoryAction(ActionEvent actionEvent) {
    File selectedDirectory = directoryChooser.showDialog(borderPane.getScene().getWindow());
    try {
      List<File> selectedFiles = FileUtils.getIfcFilesFromDirectory(selectedDirectory);
      if (selectedFiles.size() > 0) {
        selectedIfcFiles.addAll(
            selectedFiles.stream().map(FileWrapper::new).collect(Collectors.toList()));
        ifcFileTable.setItems(FXCollections.observableArrayList(selectedIfcFiles));
        bClearList.setDisable(false);
        bExtract.setDisable(false);
      }
    } catch (FileNotFoundException | NotDirectoryException e) {
      logger.warn("No Valid Directory selected");
    }
  }

  @FXML
  public void handlePickFileAction(ActionEvent actionEvent) {
    List<File> selectedFiles =
        fileChooser.showOpenMultipleDialog(borderPane.getScene().getWindow());
    if (Objects.nonNull(selectedFiles) && selectedFiles.size() > 0) {
      selectedIfcFiles.addAll(
          selectedFiles.stream().map(FileWrapper::new).collect(Collectors.toList()));
      ifcFileTable.setItems(FXCollections.observableArrayList(selectedIfcFiles));
      bClearList.setDisable(false);
      bExtract.setDisable(false);
    }
  }

  @FXML
  public void handleSaveFileAction(ActionEvent actionEvent) {
    File file =
            saveFileChooser.showSaveDialog(borderPane.getScene().getWindow());

    if (Objects.nonNull(file)) {
      try {
        Utils.writeToJson(file.getAbsolutePath(), extractResult.getExtractedFeatures());
        final String message = MessageFormat.format(resourceBundle.getString("label.extract.export.success"), file.getAbsolutePath());

        Platform.runLater(() -> {
          snackbar.fireEvent(new JFXSnackbar.SnackbarEvent(new JFXSnackbarLayout(message), Duration.seconds(5), null));
        });

      } catch (IOException ioException) {
        ioException.printStackTrace();
      }
      //TODO: DISABLE/ENABLE BUTTONS ACCORDINGLY (Add Save Success Message incl. file path)
    }
  }

  @FXML
  public void handleSaveLogAction(ActionEvent actionEvent) {

    File file =
            saveLogFileChooser.showSaveDialog(borderPane.getScene().getWindow());

    if (Objects.nonNull(file)) {
      try {
        Files.writeString(file.toPath(), extractResult.getLogOutput(), StandardCharsets.UTF_8);
        final String message = MessageFormat.format(resourceBundle.getString("label.extract.exportLog.success"), file.getAbsolutePath());

        Platform.runLater(() -> {
          snackbar.fireEvent(new JFXSnackbar.SnackbarEvent(new JFXSnackbarLayout(message), Duration.seconds(5), null));
        });

      } catch (IOException ioException) {
        ioException.printStackTrace();
      }
      //TODO: DISABLE/ENABLE BUTTONS ACCORDINGLY (Add Save Success Message incl. file path)
    }
  }

  @FXML
  public void handleClearListAction(ActionEvent actionEvent) {
    selectedIfcFiles.clear();
    ifcFileTable.setItems(FXCollections.observableArrayList(selectedIfcFiles));
    bClearList.setDisable(true);
    bExtract.setDisable(true);
  }

  /*@FXML
  public void handleRemoveSelectedEntryAction(ActionEvent actionEvent) {
  }*/

  @FXML
  public void handleConvertAction(ActionEvent actionEvent) {
    bExtract.setVisible(false);
    bExtract.setManaged(false);
    hbFileActions.setVisible(false);
    hbFileActions.setManaged(false);
    ifcFileTable.setVisible(false);

    bpProgress.setVisible(true);
    bpProgress.setManaged(true);

    Task task = PropertyExtractor.generateIfcFileToJsonTask(false, "extracted-features.json", selectedIfcFiles.stream().map(FileWrapper::getFile).collect(Collectors.toList()));

    task.addEventHandler(
        WorkerStateEvent.WORKER_STATE_SUCCEEDED,
        new EventHandler<WorkerStateEvent>() {
          @Override
          public void handle(WorkerStateEvent t) {
              extractResult = (ExtractResult) task.getValue();

              bpProgress.setVisible(false);
              bpProgress.setManaged(false);
              Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
              taExtractedFeatures.setText(gson.toJson(extractResult.getExtractedFeatures()));
              taExtractLogOutput.setText(extractResult.getLogOutput());
              tpResult.setVisible(true);
              tpResult.setManaged(true);

              bSaveLog.setVisible(true);
              bSaveLog.setManaged(true);
              bSaveFile.setVisible(true);
              bSaveFile.setManaged(true);
          }
        });

    pbExtraction.progressProperty().bind(task.progressProperty());
    lProgressInfo.textProperty().bind(task.titleProperty());
    taProgressLog.textProperty().bind(task.messageProperty()); //TODO: Overwrites Message to append use a changelistener
    new Thread(task).start();
  }
}
