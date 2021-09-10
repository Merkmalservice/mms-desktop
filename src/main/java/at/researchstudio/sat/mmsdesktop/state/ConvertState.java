package at.researchstudio.sat.mmsdesktop.state;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.mmsdesktop.model.ifc.*;
import at.researchstudio.sat.mmsdesktop.model.task.LoadResult;
import java.util.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import org.springframework.stereotype.Component;

@Component
public class ConvertState {
    private final BooleanProperty showInitial;
    private final BooleanProperty showLoadProgress;
    private final BooleanProperty showInputFile;

    private final ObjectProperty<IfcLine> selectedIfcLine;

    private final ObservableList<IfcLine> inputFileContent;
    private final HashMap<String, IfcLine> inputFileContentMap;
    private final ObservableList<Feature> extractedFeatures;

    public ConvertState() {
        this.showInitial = new SimpleBooleanProperty(true);
        this.showLoadProgress = new SimpleBooleanProperty(false);
        this.showInputFile = new SimpleBooleanProperty(false);
        this.inputFileContent = FXCollections.observableArrayList();
        this.extractedFeatures = FXCollections.observableArrayList();
        this.inputFileContentMap = new HashMap<>();
        this.selectedIfcLine = new SimpleObjectProperty<>();
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
        this.extractedFeatures.clear();

        if (Objects.isNull(task.getException())) {
            this.inputFileContent.setAll(task.getValue().getLines());
            this.inputFileContentMap.putAll(task.getValue().getDataLines());
            this.extractedFeatures.addAll(task.getValue().getExtractedFeatures());
        } else {
            // TODO: BETTER ERROR HANDLING
            this.inputFileContent.setAll(
                    Collections.singletonList(new IfcLine(task.getException().getMessage())));
            this.inputFileContentMap.putAll(Collections.emptyMap());
            this.extractedFeatures.addAll(Collections.emptyList());
            //
            // this.extractLogOutput.setValue(Throwables.getStackTraceAsString(task.getException()));
            //            this.extractJsonOutput.setValue("[]");
        }
    }

    public void setSelectedIfcLine(IfcLine ifcLine) {
        selectedIfcLine.setValue(ifcLine);
    }

    public void closeSelectedIfcLine() {
        setSelectedIfcLine(null);
    }

    public ObjectProperty<IfcLine> selectedIfcLineProperty() {
        return selectedIfcLine;
    }

    public ObservableList<Feature> getInputFileExtractedFeatures() {
        return extractedFeatures;
    }

    public ObservableMap<String, IfcLine> getInputFileDataLines() {
        return FXCollections.observableMap(inputFileContentMap);
    }
}
