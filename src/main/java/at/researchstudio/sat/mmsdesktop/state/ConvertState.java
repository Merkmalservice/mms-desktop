package at.researchstudio.sat.mmsdesktop.state;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.mmsdesktop.controller.components.FeatureLabel;
import at.researchstudio.sat.mmsdesktop.controller.components.IfcLineClassLabel;
import at.researchstudio.sat.mmsdesktop.model.ifc.*;
import at.researchstudio.sat.mmsdesktop.model.task.LoadResult;
import java.util.*;
import java.util.stream.Collectors;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import org.springframework.stereotype.Component;

@Component
public class ConvertState {
    public static final int STEP_OPEN = 0;
    public static final int STEP_ACTIVE = 1;
    public static final int STEP_COMPLETE = 2;
    public static final int STEP_DISABLED = 3;
    public static final int STEP_FAILED = 4;
    public static final int STEP_PROCESSING = 5;

    private final ObjectProperty<IfcLine> selectedIfcLine;
    private final ObjectProperty<ParsedIfcFile> parsedIfcFile;

    private final ObservableList<IfcLine> inputFileContent;
    private final FilteredList<IfcLine> filteredInputFileContent;

    private final ObservableList<FeatureLabel> extractedFeatures;
    private final ObservableList<IfcLineClassLabel> extractedIfcLineClasses;

    private final IntegerProperty stepFileStatus;
    private final IntegerProperty stepProjectStatus;
    private final IntegerProperty stepConvertStatus;

    public ConvertState() {
        this.stepFileStatus = new SimpleIntegerProperty(ConvertState.STEP_ACTIVE);
        this.stepProjectStatus = new SimpleIntegerProperty(ConvertState.STEP_DISABLED);
        this.stepConvertStatus = new SimpleIntegerProperty(ConvertState.STEP_DISABLED);

        this.inputFileContent = FXCollections.observableArrayList();
        this.filteredInputFileContent = new FilteredList<>(inputFileContent);
        this.extractedFeatures = FXCollections.observableArrayList();
        this.extractedIfcLineClasses = FXCollections.observableArrayList();
        this.selectedIfcLine = new SimpleObjectProperty<>();
        this.parsedIfcFile = new SimpleObjectProperty<>();
    }

    public void showInitialView() {
        stepFileStatus.setValue(ConvertState.STEP_ACTIVE);
        stepProjectStatus.setValue(ConvertState.STEP_DISABLED);
        stepConvertStatus.setValue(ConvertState.STEP_DISABLED);
    }

    public void showLoadProgressView() {
        stepFileStatus.setValue(ConvertState.STEP_PROCESSING);
        stepProjectStatus.setValue(ConvertState.STEP_DISABLED);
        stepConvertStatus.setValue(ConvertState.STEP_DISABLED);
    }

    public void showConvertView(boolean success) {
        stepFileStatus.setValue(success ? ConvertState.STEP_COMPLETE : ConvertState.STEP_FAILED);

        if (success) {
            stepProjectStatus.setValue(ConvertState.STEP_OPEN);
        } else {
            stepProjectStatus.setValue(ConvertState.STEP_DISABLED);
        }
        stepConvertStatus.setValue(ConvertState.STEP_DISABLED);
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
            showConvertView(true);
        } else {
            // TODO: BETTER ERROR HANDLING
            // String errorMessage = task.getException().getMessage();
            this.parsedIfcFile.setValue(null);
            this.inputFileContent.setAll(Collections.emptyList());
            this.extractedFeatures.addAll(Collections.emptyList());
            this.extractedIfcLineClasses.addAll(Collections.emptyList());
            showConvertView(false);
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

    public IntegerProperty stepFileStatusProperty() {
        return stepFileStatus;
    }

    public IntegerProperty stepProjectStatusProperty() {
        return stepProjectStatus;
    }

    public IntegerProperty stepConvertStatusProperty() {
        return stepConvertStatus;
    }
}
