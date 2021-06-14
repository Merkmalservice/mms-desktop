package at.researchstudio.sat.mmsdesktop.controller;

import at.researchstudio.sat.mmsdesktop.util.FileUtils;
import at.researchstudio.sat.mmsdesktop.util.FileWrapper;
import com.jfoenix.controls.JFXButton;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.NotDirectoryException;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ExtractController implements Initializable {

    private FileChooser fileChooser;
    private DirectoryChooser directoryChooser;

    @FXML private AnchorPane anchorPane;

    @FXML private JFXButton pickFile;

    @FXML private JFXButton pickDirectory;

    @FXML private TableView ifcFileTable;

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
        File selectedDirectory = directoryChooser.showDialog(anchorPane.getScene().getWindow());
        try {
            selectedIfcFiles.addAll(FileUtils.getIfcFilesFromDirectory(selectedDirectory).stream().map(f -> new FileWrapper(f)).collect(Collectors.toList()));
            ifcFileTable.setItems(FXCollections.observableArrayList(selectedIfcFiles));
        } catch (FileNotFoundException e) {
            //TODO: HANDLE ERROR
        } catch (NotDirectoryException e) {
            //TODO: HANDLE ERROR
        }
    }

    @FXML
    public void handlePickFileAction(ActionEvent actionEvent) {
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(anchorPane.getScene().getWindow());

        selectedIfcFiles.addAll(selectedFiles.stream().map(f -> new FileWrapper(f)).collect(Collectors.toList()));
        ifcFileTable.setItems(FXCollections.observableArrayList(selectedIfcFiles));
    }

    @FXML
    public void handleClearListAction(ActionEvent actionEvent) {
        selectedIfcFiles.clear();
        ifcFileTable.setItems(FXCollections.observableArrayList(selectedIfcFiles));
    }
}
