package at.researchstudio.sat.mmsdesktop.state;

import at.researchstudio.sat.mmsdesktop.model.task.LoadResult;
import java.util.List;
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

    private final ObservableList<String> inputFileContent;

    public ConvertState() {
        this.showInitial = new SimpleBooleanProperty(true);
        this.showLoadProgress = new SimpleBooleanProperty(false);
        this.showInputFile = new SimpleBooleanProperty(false);
        this.inputFileContent = FXCollections.observableArrayList();
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

    public void setSelectedConvertFile(List<String> lines) {
        this.inputFileContent.clear();
        this.inputFileContent.addAll(lines);

        /*IfcFileWrapper ifcFile = new IfcFileWrapper(file);
        System.out.println("TODO: select" + ifcFile);

        // TODO: ADD PROGRESS AND MOVE TO OWN TASK
        try (LineIterator it =
                FileUtils.lineIterator(ifcFile.getFile(), StandardCharsets.UTF_8.toString())) {
            StringBuilder sb = new StringBuilder();
            while (it.hasNext()) {
                this.inputFileContent.add(it.nextLine());
            }

            showInitial.setValue(false);
            showInputFile.setValue(true);
        } catch (IOException e) {
            // TODO: ERROR HANDLING
            this.inputFileContent.clear();
            this.inputFileContent.add("ERROR: TODO");
            showInitial.setValue(false);
            showInputFile.setValue(true);
        }*/
    }

    public void resetSelectedConvertFile() {
        // TODO: ONLY DO THIS WITH A DIALOG
        this.inputFileContent.clear();
        showInputFile.setValue(false);
        showInitial.setValue(true);
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

    public ObservableList<String> getInputFileContent() {
        return inputFileContent;
    }

    public void setLoadResult(Task<LoadResult> task) {
        this.inputFileContent.setAll(task.getValue().getLines());
        if (Objects.isNull(task.getException())) {
            // TODO: ERROR HANDLING
            //            Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
            //            this.extractLogOutput.setValue(task.getValue().getLogOutput());
            //            this.extractJsonOutput.setValue(gson.toJson(this.extractedFeatures));
        } else {
            //
            // this.extractLogOutput.setValue(Throwables.getStackTraceAsString(task.getException()));
            //            this.extractJsonOutput.setValue("[]");
        }
    }
}
