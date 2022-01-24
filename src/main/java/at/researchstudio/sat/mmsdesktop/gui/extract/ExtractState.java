package at.researchstudio.sat.mmsdesktop.gui.extract;

import at.researchstudio.sat.merkmalservice.ifc.FileWrapper;
import at.researchstudio.sat.merkmalservice.ifc.IfcFileWrapper;
import at.researchstudio.sat.merkmalservice.ifc.support.FileUtils;
import at.researchstudio.sat.merkmalservice.ifc.support.IfcUtils;
import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.model.PropertySet;
import at.researchstudio.sat.merkmalservice.utils.ExcludeDescriptionStrategy;
import at.researchstudio.sat.mmsdesktop.gui.component.featureset.FeatureSetBox;
import at.researchstudio.sat.mmsdesktop.model.task.ExtractResult;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import org.springframework.stereotype.Component;

@Component
public class ExtractState {
    public static final int INITIAL = 0;
    public static final int PROCESSING = 1;
    public static final int EXTRACTED = 2;
    private final IntegerProperty state;
    private final ObservableList<FileWrapper> selectedExtractFiles;
    private final BooleanProperty selectedExtractFilesPresent;
    private final SimpleStringProperty extractLogOutput;
    private final SimpleStringProperty extractJsonOutput;
    private final ObservableList<Feature> extractedFeatures;
    private final FilteredList<Feature> filteredExtractedFeatures;
    private final SortedList<Feature> sortedExtractedFeatures;
    private final ObservableList<PropertySet> extractedPropertySets;
    private final BooleanProperty extractedFeatureSetsPresent;
    private final ObservableList<FeatureSetBox> extractedFeatureSets;
    private final SortedList<FeatureSetBox> sortedFeatureSets;
    private final BooleanProperty includeDescriptionInJsonOutput;

    public ExtractState() {
        this.state = new SimpleIntegerProperty(INITIAL);
        this.includeDescriptionInJsonOutput = new SimpleBooleanProperty(true);
        this.selectedExtractFiles = FXCollections.observableArrayList();
        this.selectedExtractFilesPresent = new SimpleBooleanProperty(false);
        this.extractLogOutput = new SimpleStringProperty("");
        this.extractJsonOutput = new SimpleStringProperty("[]");
        this.extractedFeatures = FXCollections.observableArrayList();
        this.filteredExtractedFeatures = new FilteredList<>(extractedFeatures);
        this.sortedExtractedFeatures = new SortedList<>(filteredExtractedFeatures);
        this.extractedPropertySets = FXCollections.observableArrayList();
        this.extractedFeatureSets = FXCollections.observableArrayList();
        this.extractedFeatureSetsPresent = new SimpleBooleanProperty(false);
        this.sortedFeatureSets = new SortedList<>(extractedFeatureSets);
        this.sortedFeatureSets.setComparator(
                (o1, o2) -> {
                    String featureSetName1 = o1.getFeatureSet().getName();
                    String featureSetName2 = o2.getFeatureSet().getName();
                    return featureSetName1.compareToIgnoreCase(featureSetName2);
                });
        this.includeDescriptionInJsonOutput.addListener(
                (observable, oldValue, newValue) -> {
                    GsonBuilder gsonBuilder = (new GsonBuilder()).setPrettyPrinting();
                    if (!newValue) {
                        gsonBuilder.setExclusionStrategies(new ExcludeDescriptionStrategy());
                    }
                    this.extractJsonOutput.setValue(
                            gsonBuilder.create().toJson(filteredExtractedFeatures));
                });
        this.filteredExtractedFeatures.addListener(
                (ListChangeListener<? super Feature>)
                        listener -> {
                            GsonBuilder gsonBuilder = (new GsonBuilder()).setPrettyPrinting();
                            if (!includeDescriptionInJsonOutput.getValue()) {
                                gsonBuilder.setExclusionStrategies(
                                        new ExcludeDescriptionStrategy());
                            }
                            this.extractJsonOutput.setValue(
                                    gsonBuilder.create().toJson(listener.getList()));
                        });
    }

    public BooleanProperty selectedExtractFilesPresentProperty() {
        return selectedExtractFilesPresent;
    }

    public BooleanProperty includeDescriptionInJsonOutputProperty() {
        return includeDescriptionInJsonOutput;
    }

    public BooleanProperty extractedFeatureSetsPresentProperty() {
        return extractedFeatureSetsPresent;
    }

    public IntegerProperty stateProperty() {
        return state;
    }

    public ObservableList<FileWrapper> getSelectedExtractFiles() {
        return selectedExtractFiles;
    }

    public void setSelectedExtractFiles(List<File> selectedFiles) {
        if (Objects.nonNull(selectedFiles) && selectedFiles.size() > 0) {
            Set<FileWrapper> selectedExtractFileSet = new HashSet<>(selectedExtractFiles);
            selectedExtractFileSet.addAll(
                    selectedFiles.stream()
                            .map(
                                    f ->
                                            FileUtils.isIfcFile(f)
                                                    ? new IfcFileWrapper(f)
                                                    : new FileWrapper(f))
                            .collect(Collectors.toList()));
            selectedExtractFiles.setAll(selectedExtractFileSet);
            selectedExtractFilesPresent.set(true);
        }
    }

    public void showExtractedView() {
        state.setValue(EXTRACTED);
    }

    public void showInitialView() {
        state.setValue(INITIAL);
    }

    public void showProcessView() {
        state.setValue(PROCESSING);
    }

    public void resetSelectedExtractFiles() {
        selectedExtractFiles.clear();
        selectedExtractFilesPresent.set(false);
    }

    public void setExtractResult(Task<ExtractResult> task) {
        this.extractedFeatures.setAll(task.getValue().getExtractedFeatures());
        this.extractedPropertySets.setAll(task.getValue().getExtractedPropertySets());
        this.extractedFeatureSets.setAll(task.getValue().getExtractedUniqueFeatureSetControls());
        if (Objects.isNull(task.getException())) {
            this.extractLogOutput.setValue(task.getValue().getLogOutput());
            this.extractedFeatureSetsPresent.setValue(!this.extractedFeatureSets.isEmpty());
        } else {
            this.extractLogOutput.setValue(IfcUtils.stacktraceToString(task.getException()));
            this.extractJsonOutput.setValue("[]");
            this.extractedFeatureSetsPresent.setValue(false);
        }
    }

    public SimpleStringProperty extractLogOutputProperty() {
        return extractLogOutput;
    }

    public SimpleStringProperty extractJsonOutput() {
        return extractJsonOutput;
    }

    public void resetExtractResults() {
        this.extractedFeatures.clear();
        this.extractedPropertySets.clear();
        this.filteredExtractedFeatures.clear();
        this.sortedExtractedFeatures.clear();
        this.extractLogOutput.set("");
        this.extractJsonOutput.set("[]");
        this.extractedFeatureSets.clear();
    }

    public ObservableList<Feature> getExtractedFeatures() {
        return extractedFeatures;
    }

    public ObservableList<PropertySet> getExtractedPropertySets() {
        return extractedPropertySets;
    }

    public SortedList<Feature> getSortedExtractedFeatures() {
        return sortedExtractedFeatures;
    }

    public SortedList<FeatureSetBox> getSortedFeatureSets() {
        return sortedFeatureSets;
    }

    public FilteredList<Feature> getFilteredExtractedFeatures() {
        return filteredExtractedFeatures;
    }

    public void includeDescriptionInJsonOutput(Boolean newValue) {
        GsonBuilder gsonBuilder = (new GsonBuilder()).setPrettyPrinting();
        if (!newValue) {
            gsonBuilder.setExclusionStrategies(new ExcludeDescriptionStrategy());
        }
        this.extractJsonOutput.setValue(gsonBuilder.create().toJson(this.extractedFeatures));
    }
}
