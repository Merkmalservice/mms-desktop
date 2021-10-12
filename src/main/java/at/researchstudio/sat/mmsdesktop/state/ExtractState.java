package at.researchstudio.sat.mmsdesktop.state;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.utils.ExcludeDescriptionStrategy;
import at.researchstudio.sat.mmsdesktop.controller.components.FeatureSetControl;
import at.researchstudio.sat.mmsdesktop.model.task.ExtractResult;
import at.researchstudio.sat.mmsdesktop.util.FileUtils;
import at.researchstudio.sat.mmsdesktop.util.FileWrapper;
import at.researchstudio.sat.mmsdesktop.util.IfcFileWrapper;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import org.apache.jena.ext.com.google.common.base.Throwables;
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

    private final BooleanProperty extractedFeatureSetsPresent;
    private final ObservableList<FeatureSetControl> extractedFeatureSets;
    private final SortedList<FeatureSetControl> sortedFeatureSets;

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
        this.extractedFeatureSets.setAll(task.getValue().getExtractedUniqueFeatureSetControls());
        if (Objects.isNull(task.getException())) {
            this.extractLogOutput.setValue(task.getValue().getLogOutput());
            this.extractedFeatureSetsPresent.setValue(!this.extractedFeatureSets.isEmpty());
        } else {
            this.extractLogOutput.setValue(Throwables.getStackTraceAsString(task.getException()));
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
        this.filteredExtractedFeatures.clear();
        this.sortedExtractedFeatures.clear();
        this.extractLogOutput.set("");
        this.extractJsonOutput.set("[]");
        this.extractedFeatureSets.clear();
    }

    public ObservableList<Feature> getExtractedFeatures() {
        return extractedFeatures;
    }

    public SortedList<Feature> getSortedExtractedFeatures() {
        return sortedExtractedFeatures;
    }

    public SortedList<FeatureSetControl> getSortedFeatureSets() {
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
