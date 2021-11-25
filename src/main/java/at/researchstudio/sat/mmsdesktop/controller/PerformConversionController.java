package at.researchstudio.sat.mmsdesktop.controller;

import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import at.researchstudio.sat.mmsdesktop.state.InputFileState;
import at.researchstudio.sat.mmsdesktop.state.PerformConversionState;
import at.researchstudio.sat.mmsdesktop.state.TargetStandardState;
import at.researchstudio.sat.mmsdesktop.view.components.ProcessState;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

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

    @Override public void initialize(URL location, ResourceBundle resources) {
    }
}
