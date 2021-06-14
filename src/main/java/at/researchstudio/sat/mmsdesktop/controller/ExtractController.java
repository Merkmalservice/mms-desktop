package at.researchstudio.sat.mmsdesktop.controller;

import at.researchstudio.sat.mmsdesktop.logic.PropertyExtractor;
import at.researchstudio.sat.mmsdesktop.util.FileUtils;
import at.researchstudio.sat.mmsdesktop.util.FileWrapper;
import com.jfoenix.controls.JFXButton;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.file.NotDirectoryException;
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
  @FXML private FlowPane fpProgress;
  @FXML private ProgressBar pbExtraction;

  private FileChooser fileChooser;
  private DirectoryChooser directoryChooser;
  private Set<FileWrapper> selectedIfcFiles;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("IFC Files", "*.ifc"));

    directoryChooser = new DirectoryChooser();
    selectedIfcFiles = new HashSet<>();
    ifcFileTable.setItems(FXCollections.observableArrayList(selectedIfcFiles));
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
    hbFileActions.setVisible(false);
    hbFileActions.setManaged(false);
    ifcFileTable.setVisible(false);

    fpProgress.setVisible(true);
    fpProgress.setManaged(true);

    //TODO: Adapt Properties
    PropertyExtractor.parseIfcFilesToJsonFeatures(false, "extracted-features.json", selectedIfcFiles.stream().map(FileWrapper::getFile).collect(Collectors.toList()));
    pbExtraction.setProgress(60.0);
  }
}
