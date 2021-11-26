package at.researchstudio.sat.mmsdesktop.gui.convert;

import at.researchstudio.sat.mmsdesktop.gui.convert.inputfile.InputFileState;
import at.researchstudio.sat.mmsdesktop.gui.convert.perform.PerformConversionState;
import at.researchstudio.sat.mmsdesktop.gui.convert.targetstandard.TargetStandardState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConvertState {
    private InputFileState inputFileState;
    private TargetStandardState targetStandardState;
    private PerformConversionState performConversionState;

    @Autowired
    public ConvertState(
            InputFileState inputFileState,
            TargetStandardState targetStandardState,
            PerformConversionState performConversionState) {
        this.inputFileState = inputFileState;
        this.targetStandardState = targetStandardState;
        this.performConversionState = performConversionState;
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
}