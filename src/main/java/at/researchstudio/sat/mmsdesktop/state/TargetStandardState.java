package at.researchstudio.sat.mmsdesktop.state;

import static at.researchstudio.sat.mmsdesktop.view.components.ProcessState.STEP_ACTIVE;

import at.researchstudio.sat.merkmalservice.model.Project;
import at.researchstudio.sat.merkmalservice.model.Standard;
import at.researchstudio.sat.mmsdesktop.view.components.ProcessState;
import javafx.beans.property.SimpleObjectProperty;
import org.springframework.stereotype.Component;

@Component
public class TargetStandardState {
    private final SimpleObjectProperty<Project> project;
    private final SimpleObjectProperty<Standard> targetStandard;
    private final SimpleObjectProperty<ProcessState> stepTargetStandardStatus;

    public TargetStandardState() {
        this.targetStandard = new SimpleObjectProperty<>();
        this.project = new SimpleObjectProperty<>();
        this.stepTargetStandardStatus = new SimpleObjectProperty<>(STEP_ACTIVE);
    }

    public SimpleObjectProperty<Project> projectProperty() {
        return project;
    }

    public SimpleObjectProperty<Standard> targetStandardProperty() {
        return targetStandard;
    }

    public SimpleObjectProperty<ProcessState> stepTargetStandardStatusProperty() {
        return stepTargetStandardStatus;
    }
}
