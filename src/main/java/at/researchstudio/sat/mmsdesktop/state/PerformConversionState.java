package at.researchstudio.sat.mmsdesktop.state;

import static at.researchstudio.sat.mmsdesktop.view.components.ProcessState.STEP_DISABLED;

import at.researchstudio.sat.mmsdesktop.view.components.ProcessState;
import javafx.beans.property.SimpleObjectProperty;
import org.springframework.stereotype.Component;

@Component
public class PerformConversionState {
    private final SimpleObjectProperty<ProcessState> stepPerformConversionStatus;

    public PerformConversionState() {
        this.stepPerformConversionStatus = new SimpleObjectProperty<>(STEP_DISABLED);
    }

    public SimpleObjectProperty<ProcessState> stepPerformConversionStatusProperty() {
        return stepPerformConversionStatus;
    }
}
