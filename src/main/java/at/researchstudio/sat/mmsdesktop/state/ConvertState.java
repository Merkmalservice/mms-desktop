package at.researchstudio.sat.mmsdesktop.state;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.mmsdesktop.controller.components.FeatureLabel;
import at.researchstudio.sat.mmsdesktop.model.ifc.*;
import at.researchstudio.sat.mmsdesktop.model.task.LoadResult;
import java.util.*;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import org.springframework.stereotype.Component;

@Component
public class ConvertState {
    private final BooleanProperty showInitial;
    private final BooleanProperty showLoadProgress;
    private final BooleanProperty showInputFile;

    private final ObjectProperty<IfcLine> selectedIfcLine;
    private final ObjectProperty<Feature> selectedFeature;
    private final ObjectProperty<ParsedIfcFile> parsedIfcFile;

    private final ObservableList<IfcLine> inputFileContent;
    private final FilteredList<IfcLine> filteredInputFileContent;

    private final ObservableList<FeatureLabel> extractedFeatures;

    public ConvertState() {
        this.showInitial = new SimpleBooleanProperty(true);
        this.showLoadProgress = new SimpleBooleanProperty(false);
        this.showInputFile = new SimpleBooleanProperty(false);
        this.inputFileContent = FXCollections.observableArrayList();
        this.filteredInputFileContent = new FilteredList<>(inputFileContent);
        this.extractedFeatures = FXCollections.observableArrayList();
        this.selectedIfcLine = new SimpleObjectProperty<>();
        this.selectedFeature = new SimpleObjectProperty<>();
        this.parsedIfcFile = new SimpleObjectProperty<>();
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
        // TODO: ONLY DO THIS WITH A DIALOG, CLEAR EVERYTHING IN THIS VIEW TOO
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
        this.extractedFeatures.clear();

        if (Objects.isNull(task.getException())) {
            this.parsedIfcFile.setValue(task.getValue().getParsedIfcFile());
            this.inputFileContent.setAll(task.getValue().getLines());
            this.extractedFeatures.addAll(
                    task.getValue().getExtractedFeatures().stream()
                            .sorted(Comparator.comparing(Feature::getName))
                            .map(FeatureLabel::new)
                            .collect(Collectors.toList()));
        } else {
            // TODO: BETTER ERROR HANDLING
            // String errorMessage = task.getException().getMessage();
            this.parsedIfcFile.setValue(null);
            this.inputFileContent.setAll(Collections.emptyList());
            this.extractedFeatures.addAll(Collections.emptyList());
            //
            // this.extractLogOutput.setValue(Throwables.getStackTraceAsString(task.getException()));
            //            this.extractJsonOutput.setValue("[]");
        }
    }

    public void setSelectedIfcLine(IfcLine ifcLine) {
        selectedIfcLine.setValue(ifcLine);
    }

    public void setSelectedFeature(Feature feature) {
        //        System.out.println("BLAARGH: " + feature);
        //        List<IfcLine> filteredList = inputFileContent
        //                .stream().filter(ifcLine -> ifcLine
        //                        .getLine()
        //
        // .contains(Utils.convertUtf8ToIFCString(feature.getName()))).collect(
        //                        Collectors.toList());
        //        System.out.println("FILTERED LISTSIZE: " + filteredList.size());
        selectedFeature.setValue(feature);
    }

    public void closeSelectedIfcLine() {
        setSelectedIfcLine(null);
    }

    public ObjectProperty<IfcLine> selectedIfcLineProperty() {
        return selectedIfcLine;
    }

    public ObjectProperty<Feature> selectedFeatureProperty() {
        return selectedFeature;
    }

    public ObjectProperty<ParsedIfcFile> parsedIfcFileProperty() {
        return parsedIfcFile;
    }

    public ObservableList<FeatureLabel> getInputFileExtractedFeatures() {
        return extractedFeatures;
    }

    public FilteredList<IfcLine> getFilteredInputFileContent() {
        return filteredInputFileContent;
    }
}
