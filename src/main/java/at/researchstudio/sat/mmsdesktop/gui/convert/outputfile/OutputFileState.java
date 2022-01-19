package at.researchstudio.sat.mmsdesktop.gui.convert.outputfile;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.mmsdesktop.model.task.IfcFileVO;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.springframework.stereotype.Component;

@Component
public class OutputFileState {
    private final ObjectProperty<IfcLine> selectedChangedIfcLine;
    private final ObjectProperty<ParsedIfcFile> convertedIfcFile;

    private final ObservableList<IfcLine> outputFileContent;
    private final ObservableList<IfcLine> allChangedLines;

    public OutputFileState() {
        this.convertedIfcFile = new SimpleObjectProperty<ParsedIfcFile>();
        this.outputFileContent = FXCollections.observableArrayList();
        this.allChangedLines = FXCollections.observableArrayList();
        this.selectedChangedIfcLine = new SimpleObjectProperty<>();
    }

    public ObjectProperty<ParsedIfcFile> convertedIfcFileProperty() {
        return convertedIfcFile;
    }

    public ObservableList<IfcLine> getOutputFileContent() {
        return outputFileContent;
    }

    public void resetConvertedFile() {
        this.outputFileContent.clear();
        this.allChangedLines.clear();
        this.convertedIfcFile.set(null);
    }

    public void setFileStepResult(Task<IfcFileVO> task) {
        // TODO: CLEAR ALL VARS
        this.outputFileContent.clear();
        //        this.extractedFeatures.clear();
        //        this.extractedIfcLineClasses.clear();

        if (Objects.isNull(task.getException())) {
            // TODO: SET SUCCESS VARS
            this.convertedIfcFile.setValue(task.getValue().getParsedIfcFile());
            this.outputFileContent.setAll(task.getValue().getLines());

            this.allChangedLines.setAll(
                    task.getValue().getParsedIfcFile().getChanges().values().stream()
                            .flatMap(Set::stream)
                            .distinct()
                            .map(id -> task.getValue().getParsedIfcFile().getDataLines().get(id))
                            .sorted(Comparator.comparing(IfcLine::toString))
                            .collect(Collectors.toList()));
            //            this.extractedFeatures.addAll(
            //                task.getValue().getExtractedFeatures().stream()
            //                    .sorted(Comparator.comparing(Feature::getName))
            //                    .map(FeatureLabel::new)
            //                    .collect(Collectors.toList()));
            //            this.extractedIfcLineClasses.addAll(
            //
            // task.getValue().getParsedIfcFile().getDataLinesByClass().entrySet().stream()
            //                    .map(IfcLineClassLabel::new)
            //                    .sorted(Comparator.comparing(IfcLineClassLabel::getCount))
            //                    .collect(Collectors.toList()));
            //            this.stepFileStatusProperty().setValue(STEP_COMPLETE);
        } else {
            // TODO: SET FAILED VARS
            // TODO: BETTER ERROR HANDLING
            // String errorMessage = task.getException().getMessage();
            this.convertedIfcFile.setValue(null);
            this.outputFileContent.setAll(Collections.emptyList());
            this.allChangedLines.setAll(Collections.emptyList());
            //            this.extractedFeatures.addAll(Collections.emptyList());
            //            this.extractedIfcLineClasses.addAll(Collections.emptyList());
            //            this.stepFileStatusProperty().setValue(STEP_FAILED);
            //
            // this.extractLogOutput.setValue(Utils.stacktraceToString(task.getException()));
            //            this.extractJsonOutput.setValue("[]");
        }
    }

    public ObservableList<IfcLine> getAllChangedLines() {
        return allChangedLines;
    }

    public void setSelectedChangedIfcLine(IfcLine selectedIfcLine) {
        this.selectedChangedIfcLine.setValue(selectedIfcLine);
    }

    public void closeSelectedChangedIfcLine() {
        setSelectedChangedIfcLine(null);
    }

    public IfcLine getSelectedChangedIfcLine() {
        return selectedChangedIfcLine.get();
    }

    public ObjectProperty<IfcLine> selectedChangedIfcLineProperty() {
        return selectedChangedIfcLine;
    }
}
