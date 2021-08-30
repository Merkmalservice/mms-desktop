package at.researchstudio.sat.mmsdesktop.controller;

import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import com.jfoenix.controls.JFXTextArea;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import net.rgielen.fxweaver.core.FxmlView;
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
    @FXML private JFXTextArea centerInputFileContent;

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
        centerInputFile
                .visibleProperty()
                .bind(stateService.getConvertState().showInputFileProperty());
        centerInputFile
                .managedProperty()
                .bind(stateService.getConvertState().showInputFileProperty());

        centerInputFileContent
                .textProperty()
                .bind(stateService.getConvertState().inputFileContentProperty());

        fileChooser = new FileChooser();
        fileChooser
                .getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("IFC File", "*.ifc"));
    }

    @FXML
    public void handlePickFileAction(ActionEvent actionEvent) {
        stateService
                .getConvertState()
                .setSelectedConvertFile(
                        fileChooser.showOpenDialog(parentPane.getScene().getWindow()));
    }

    @FXML
    public void handleResetFileAction(ActionEvent actionEvent) {
        stateService.getConvertState().resetSelectedConvertFile();
    }
}
