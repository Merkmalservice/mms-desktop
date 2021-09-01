package at.researchstudio.sat.mmsdesktop.state;

import at.researchstudio.sat.mmsdesktop.model.ifc.IfcLine;
import at.researchstudio.sat.mmsdesktop.model.task.LoadResult;
import java.util.HashMap;
import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.springframework.stereotype.Component;

@Component
public class ConvertState {
    private final BooleanProperty showInitial;
    private final BooleanProperty showLoadProgress;
    private final BooleanProperty showInputFile;

    private final ObservableList<IfcLine> inputFileContent;
    private final HashMap<String, IfcLine> inputFileContentMap;

    public ConvertState() {
        this.showInitial = new SimpleBooleanProperty(true);
        this.showLoadProgress = new SimpleBooleanProperty(false);
        this.showInputFile = new SimpleBooleanProperty(false);
        this.inputFileContent = FXCollections.observableArrayList();
        this.inputFileContentMap = new HashMap<>();
    }

    public void showInitialView() {
        showLoadProgress.setValue(false);
        showInputFile.setValue(false);
        showInitial.setValue(true);
    }

    public void showLoadProgressView() {
        showInputFile.setValue(false);
        showInitial.setValue(false);
        showLoadProgress.setValue(true);
    }

    public void showConvertView() {
        showLoadProgress.setValue(false);
        showInputFile.setValue(true);
        showInitial.setValue(false);
    }

    public void resetSelectedConvertFile() {
        // TODO: ONLY DO THIS WITH A DIALOG
        this.inputFileContent.clear();
        showInitialView();
    }

    public BooleanProperty showLoadProgressProperty() {
        return showLoadProgress;
    }

    public BooleanProperty showInitialProperty() {
        return showInitial;
    }

    public BooleanProperty showInputFileProperty() {
        return showInputFile;
    }

    public ObservableList<IfcLine> getInputFileContent() {
        return inputFileContent;
    }

    public void setLoadResult(Task<LoadResult> task) {
        this.inputFileContent.clear();
        this.inputFileContentMap.clear();

        this.inputFileContent.setAll(task.getValue().getLines());
        this.inputFileContentMap.putAll(task.getValue().getDataLines());

        if (Objects.isNull(task.getException())) {
            // TODO: ERROR HANDLING
        } else {
            //
            // this.extractLogOutput.setValue(Throwables.getStackTraceAsString(task.getException()));
            //            this.extractJsonOutput.setValue("[]");
        }
    }
}
