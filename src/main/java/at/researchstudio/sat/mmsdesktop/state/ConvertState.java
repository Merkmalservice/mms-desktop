package at.researchstudio.sat.mmsdesktop.state;

import at.researchstudio.sat.mmsdesktop.util.IfcFileWrapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.springframework.stereotype.Component;

@Component
public class ConvertState {
    private final BooleanProperty showInitial;
    private final BooleanProperty showInputFile;

    private final SimpleStringProperty inputFileContent;

    public ConvertState() {
        this.showInitial = new SimpleBooleanProperty(true);
        this.showInputFile = new SimpleBooleanProperty(false);
        this.inputFileContent = new SimpleStringProperty("");
    }

    public void setSelectedConvertFile(File file) {
        IfcFileWrapper ifcFile = new IfcFileWrapper(file);
        System.out.println("TODO: select" + ifcFile);

        // TODO: ADD PROGRESS AND MOVE TO OWN TASK
        try (LineIterator it =
                FileUtils.lineIterator(ifcFile.getFile(), StandardCharsets.UTF_8.toString())) {
            StringBuilder sb = new StringBuilder();
            while (it.hasNext()) {
                sb.append(it.nextLine()).append(System.lineSeparator());
            }
            this.inputFileContent.setValue(sb.toString());

            showInitial.setValue(false);
            showInputFile.setValue(true);
        } catch (IOException e) {
            // TODO: ERROR HANDLING
            this.inputFileContent.setValue("COULD NOT READ FILE");
            showInitial.setValue(false);
            showInputFile.setValue(true);
        }
    }

    public void resetSelectedConvertFile() {
        // TODO: ONLY DO THIS WITH A DIALOG
        this.inputFileContent.setValue("");
        showInputFile.setValue(false);
        showInitial.setValue(true);
    }

    public BooleanProperty showInitialProperty() {
        return showInitial;
    }

    public BooleanProperty showInputFileProperty() {
        return showInputFile;
    }

    public SimpleStringProperty inputFileContentProperty() {
        return inputFileContent;
    }

    /*private final BooleanProperty showInitial;
    private final BooleanProperty showExtractProcess;
    private final BooleanProperty showExtracted;

    private final ObservableList<FileWrapper> selectedExtractFiles;
    private final BooleanProperty selectedExtractFilesPresent;

    private final SimpleStringProperty extractLogOutput;
    private final SimpleStringProperty extractJsonOutput;

    private final ObservableList<Feature> extractedFeatures;
    private final FilteredList<Feature> filteredExtractedFeatures;
    private final SortedList<Feature> sortedExtractedFeatures;

    public ConvertState() {
        this.showInitial = new SimpleBooleanProperty(true);
        this.showExtractProcess = new SimpleBooleanProperty(false);
        this.showExtracted = new SimpleBooleanProperty(false);
        this.selectedExtractFiles = FXCollections.observableArrayList();
        this.selectedExtractFilesPresent = new SimpleBooleanProperty(false);
        this.extractLogOutput = new SimpleStringProperty("");
        this.extractJsonOutput = new SimpleStringProperty("[]");
        this.extractedFeatures = FXCollections.observableArrayList();
        this.filteredExtractedFeatures = new FilteredList<>(extractedFeatures);
        this.sortedExtractedFeatures = new SortedList<>(filteredExtractedFeatures);
    }

    public BooleanProperty selectedExtractFilesPresentProperty() {
        return selectedExtractFilesPresent;
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

    public void resetSelectedExtractFiles() {
        selectedExtractFiles.clear();
        selectedExtractFilesPresent.set(false);
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
    }*/

}
