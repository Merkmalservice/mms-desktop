package at.researchstudio.sat.mmsdesktop.gui.convert;

import at.researchstudio.sat.merkmalservice.ifc.support.IfcUtils;
import at.researchstudio.sat.mmsdesktop.gui.convert.inputfile.InputFileState;
import at.researchstudio.sat.mmsdesktop.gui.convert.outputfile.OutputFileState;
import at.researchstudio.sat.mmsdesktop.gui.convert.perform.PerformConversionState;
import at.researchstudio.sat.mmsdesktop.gui.convert.targetstandard.TargetStandardState;
import at.researchstudio.sat.mmsdesktop.model.task.IfcFileVO;
import java.util.Objects;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConvertState {
    private InputFileState inputFileState;
    private OutputFileState outputFileState;
    private TargetStandardState targetStandardState;
    private PerformConversionState performConversionState;

    private final SimpleStringProperty convertLogOutput;

    @Autowired
    public ConvertState(
            InputFileState inputFileState,
            OutputFileState outputFileState,
            TargetStandardState targetStandardState,
            PerformConversionState performConversionState) {
        this.inputFileState = inputFileState;
        this.outputFileState = outputFileState;
        this.targetStandardState = targetStandardState;
        this.performConversionState = performConversionState;

        this.convertLogOutput = new SimpleStringProperty("");
    }

    public void resetConvertResults() {
        // TODO: RESET NECESSARY VARS
        this.convertLogOutput.set("");
        this.outputFileState.resetConvertedFile();
    }

    public void setConvertResults(Task<IfcFileVO> task) {
        if (Objects.isNull(task.getException())) {
            this.convertLogOutput.setValue(task.getValue().getLogOutput());
            this.outputFileState.setFileStepResult(task);
        } else {
            this.convertLogOutput.setValue(IfcUtils.stacktraceToString(task.getException()));
            this.outputFileState.setFileStepResult(task);
        }
    }

    public InputFileState getInputFileState() {
        return inputFileState;
    }

    public TargetStandardState getTargetStandardState() {
        return targetStandardState;
    }

    public PerformConversionState getPerformConversionState() {
        return performConversionState;
    }

    public OutputFileState getOutputFileState() {
        return outputFileState;
    }

    public SimpleStringProperty convertLogOutputProperty() {
        return convertLogOutput;
    }
}
