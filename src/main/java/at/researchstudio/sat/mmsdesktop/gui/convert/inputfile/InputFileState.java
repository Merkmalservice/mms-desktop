package at.researchstudio.sat.mmsdesktop.gui.convert.inputfile;

import static at.researchstudio.sat.mmsdesktop.view.components.ProcessState.*;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.mmsdesktop.gui.component.feature.FeatureLabel;
import at.researchstudio.sat.mmsdesktop.gui.component.ifc.IfcLineClassLabel;
import at.researchstudio.sat.mmsdesktop.model.task.LoadResult;
import at.researchstudio.sat.mmsdesktop.view.components.ProcessState;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import org.springframework.stereotype.Component;

@Component
public class InputFileState {

    private final ObjectProperty<IfcLine> selectedIfcLine;
    private final ObjectProperty<ParsedIfcFile> parsedIfcFile;

    private final ObservableList<IfcLine> inputFileContent;
    private final FilteredList<IfcLine> filteredInputFileContent;

    private final ObservableList<FeatureLabel> extractedFeatures;
    private final ObservableList<IfcLineClassLabel> extractedIfcLineClasses;

    private final SimpleObjectProperty<ProcessState> stepFileStatus;

    public InputFileState() {
        this.stepFileStatus = new SimpleObjectProperty<>(STEP_ACTIVE);
        this.inputFileContent = FXCollections.observableArrayList();
        this.filteredInputFileContent = new FilteredList<>(inputFileContent);
        this.extractedFeatures = FXCollections.observableArrayList();
        this.extractedIfcLineClasses = FXCollections.observableArrayList();
        this.selectedIfcLine = new SimpleObjectProperty<>();
        this.parsedIfcFile = new SimpleObjectProperty<>();
    }

    public void showInitialView() {
        stepFileStatus.setValue(STEP_ACTIVE);
    }

    public void showLoadProgressView() {
        stepFileStatus.setValue(STEP_PROCESSING);
    }

    public void showConvertView(boolean success) {
        stepFileStatus.setValue(success ? STEP_COMPLETE : STEP_FAILED);
    }

    public void resetSelectedConvertFile() {
        // TODO: ONLY DO THIS WITH A DIALOG, CLEAR EVERYTHING IN THIS VIEW TOO
        this.inputFileContent.clear();
        showInitialView();
    }

    public ObservableList<IfcLine> getInputFileContent() {
        return inputFileContent;
    }

    public void setFileStepResult(Task<LoadResult> task) {
        this.inputFileContent.clear();
        this.extractedFeatures.clear();
        this.extractedIfcLineClasses.clear();

        if (Objects.isNull(task.getException())) {
            this.parsedIfcFile.setValue(task.getValue().getParsedIfcFile());
            this.inputFileContent.setAll(task.getValue().getLines());
            this.extractedFeatures.addAll(
                    task.getValue().getExtractedFeatures().stream()
                            .sorted(Comparator.comparing(Feature::getName))
                            .map(FeatureLabel::new)
                            .collect(Collectors.toList()));
            this.extractedIfcLineClasses.addAll(
                    task.getValue().getParsedIfcFile().getDataLinesByClass().entrySet().stream()
                            .map(IfcLineClassLabel::new)
                            .sorted(Comparator.comparing(IfcLineClassLabel::getCount))
                            .collect(Collectors.toList()));
            this.stepFileStatusProperty().setValue(STEP_COMPLETE);
        } else {
            // TODO: BETTER ERROR HANDLING
            // String errorMessage = task.getException().getMessage();
            this.parsedIfcFile.setValue(null);
            this.inputFileContent.setAll(Collections.emptyList());
            this.extractedFeatures.addAll(Collections.emptyList());
            this.extractedIfcLineClasses.addAll(Collections.emptyList());
            this.stepFileStatusProperty().setValue(STEP_FAILED);
            //
            // this.extractLogOutput.setValue(Utils.stacktraceToString(task.getException()));
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

    public ObjectProperty<ParsedIfcFile> parsedIfcFileProperty() {
        return parsedIfcFile;
    }

    public ObservableList<FeatureLabel> getInputFileExtractedFeatures() {
        return extractedFeatures;
    }

    public ObservableList<IfcLineClassLabel> getInputFileExtractedIfcLineClasses() {
        return extractedIfcLineClasses;
    }

    public FilteredList<IfcLine> getFilteredInputFileContent() {
        return filteredInputFileContent;
    }

    public SimpleObjectProperty<ProcessState> stepFileStatusProperty() {
        return stepFileStatus;
    }
}
