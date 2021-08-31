package at.researchstudio.sat.mmsdesktop.controller;

import at.researchstudio.sat.mmsdesktop.model.task.LoadResult;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import at.researchstudio.sat.mmsdesktop.util.IfcFileWrapper;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXProgressBar;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
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
    @FXML private JFXListView<String> centerInputFileContent;

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

        /*centerInputFileContent
        .textProperty()
        .bind(stateService.getConvertState().inputFileContentProperty()); */

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
                    new Task<LoadResult>() {
                        @Override
                        protected LoadResult call() throws Exception {
                            IfcFileWrapper ifcFile = new IfcFileWrapper(file);
                            System.out.println("TODO: select" + ifcFile);
                            List<String> lines = new ArrayList<String>();
                            try (LineIterator it =
                                    FileUtils.lineIterator(
                                            ifcFile.getFile(), StandardCharsets.UTF_8.toString())) {
                                while (it.hasNext()) {
                                    String line = it.nextLine();
                                    lines.add(line);
                                    updateTitle(line);
                                }
                            }
                            // TODO: Cancel op
                            return new LoadResult(lines);
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
    public void handleResetFileAction(ActionEvent actionEvent) {
        stateService.getConvertState().resetSelectedConvertFile();
    }

    public ObservableList<String> getFileContentList() {
        return stateService.getConvertState().getInputFileContent();
    }
}
