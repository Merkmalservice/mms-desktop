package at.researchstudio.sat.mmsdesktop.gui.convert.perform;

import at.researchstudio.sat.mmsdesktop.gui.convert.inputfile.InputFileState;
import at.researchstudio.sat.mmsdesktop.gui.convert.targetstandard.TargetStandardState;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PerformConversionController implements Initializable {
    private ReactiveStateService stateService;
    private InputFileState inputFileState;
    private TargetStandardState targetStandardState;
    private PerformConversionState state;

    @Autowired
    public PerformConversionController(ReactiveStateService stateService) {
        this.stateService = stateService;
        this.inputFileState = stateService.getConvertState().getInputFileState();
        this.targetStandardState = stateService.getConvertState().getTargetStandardState();
        this.state = stateService.getConvertState().getPerformConversionState();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {}
}
