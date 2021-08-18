package at.researchstudio.sat.mmsdesktop.state;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.utils.ExcludeDescriptionStrategy;
import at.researchstudio.sat.mmsdesktop.model.task.ExtractResult;
import at.researchstudio.sat.mmsdesktop.util.FileWrapper;
import at.researchstudio.sat.mmsdesktop.util.IfcFileWrapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import org.apache.jena.ext.com.google.common.base.Throwables;
import org.springframework.stereotype.Component;

@Component
public class ExtractState {
    private final BooleanProperty showInitial;
    private final BooleanProperty showExtractProcess;
    private final BooleanProperty showExtracted;

    private final ObservableList<IfcFileWrapper> selectedIfcFiles;
    private final BooleanProperty selectedIfcFilesPresent;

    private final ObservableList<FileWrapper> selectedJsonFiles;
    private final BooleanProperty selectedJsonFilesPresent;

    private final SimpleStringProperty extractLogOutput;
    private final SimpleStringProperty extractJsonOutput;

    private final ObservableList<Feature> extractedFeatures;
    private final FilteredList<Feature> filteredExtractedFeatures;
    private final SortedList<Feature> sortedExtractedFeatures;

    public ExtractState() {
        this.showInitial = new SimpleBooleanProperty(true);
        this.showExtractProcess = new SimpleBooleanProperty(false);
        this.showExtracted = new SimpleBooleanProperty(false);
        this.selectedIfcFiles = FXCollections.observableArrayList();
        this.selectedJsonFiles = FXCollections.observableArrayList();
        this.selectedIfcFilesPresent = new SimpleBooleanProperty(false);
        this.selectedJsonFilesPresent = new SimpleBooleanProperty(false);
        this.extractLogOutput = new SimpleStringProperty("");
        this.extractJsonOutput = new SimpleStringProperty("[]");
        this.extractedFeatures = FXCollections.observableArrayList();
        this.filteredExtractedFeatures = new FilteredList<>(extractedFeatures);
        this.sortedExtractedFeatures = new SortedList<>(filteredExtractedFeatures);
    }

    public BooleanProperty selectedIfcFilesPresentProperty() {
        return selectedIfcFilesPresent;
    }

    public BooleanProperty selectedJsonFilesPresentProperty() {
        return selectedJsonFilesPresent;
    }

    public BooleanProperty showInitialProperty() {
        return showInitial;
    }

    public BooleanProperty showExtractProcessProperty() {
        return showExtractProcess;
    }

    public BooleanProperty showExtractedProperty() {
        return showExtracted;
    }

    public ObservableList<IfcFileWrapper> getSelectedIfcFiles() {
        return selectedIfcFiles;
    }

    public ObservableList<FileWrapper> getSelectedJsonFiles() {
        return selectedJsonFiles;
    }

    public void setSelectedIfcFiles(List<File> selectedFiles) {
        if (Objects.nonNull(selectedFiles) && selectedFiles.size() > 0) {
            Set<IfcFileWrapper> selectedIfcFileSet = new HashSet<>(selectedIfcFiles);
            selectedIfcFileSet.addAll(
                    selectedFiles.stream().map(IfcFileWrapper::new).collect(Collectors.toList()));
            selectedIfcFiles.setAll(selectedIfcFileSet);
            selectedIfcFilesPresent.set(true);
        }
    }

    public void setSelectedJsonFiles(List<File> selectedFiles) {
        if (Objects.nonNull(selectedFiles) && selectedFiles.size() > 0) {
            Set<FileWrapper> selectedJsonFileSet = new HashSet<>(selectedJsonFiles);
            selectedJsonFileSet.addAll(
                    selectedFiles.stream().map(FileWrapper::new).collect(Collectors.toList()));
            selectedJsonFiles.setAll(selectedJsonFileSet);
            selectedJsonFilesPresent.set(true);
        }
    }

    public void showExtractedView() {
        showInitial.setValue(false);
        showExtractProcess.setValue(false);
        showExtracted.setValue(true);
    }

    public void showInitialView() {
        showExtractProcess.setValue(false);
        showExtracted.setValue(false);
        showInitial.setValue(true);
    }

    public void showProcessView() {
        showExtracted.setValue(false);
        showInitial.setValue(false);
        showExtractProcess.setValue(true);
    }

    public void resetSelectedIfcFiles() {
        selectedIfcFiles.clear();
        selectedIfcFilesPresent.set(false);
    }

    public void resetSelectedJsonFiles() {
        selectedJsonFiles.clear();
        selectedJsonFilesPresent.set(false);
    }

    public void setExtractResult(Task<ExtractResult> task) {
        this.extractedFeatures.setAll(task.getValue().getExtractedFeatures());
        if (Objects.isNull(task.getException())) {
            Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
            this.extractLogOutput.setValue(task.getValue().getLogOutput());
            this.extractJsonOutput.setValue(gson.toJson(this.extractedFeatures));
        } else {
            this.extractLogOutput.setValue(Throwables.getStackTraceAsString(task.getException()));
            this.extractJsonOutput.setValue("[]");
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
    }

    public ObservableList<Feature> getExtractedFeatures() {
        return extractedFeatures;
    }

    public SortedList<Feature> getSortedExtractedFeatures() {
        return sortedExtractedFeatures;
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
