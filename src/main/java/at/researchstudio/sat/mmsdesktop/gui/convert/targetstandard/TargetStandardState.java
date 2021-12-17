package at.researchstudio.sat.mmsdesktop.gui.convert.targetstandard;

import static at.researchstudio.sat.mmsdesktop.view.components.ProcessState.STEP_ACTIVE;

import at.researchstudio.sat.merkmalservice.model.Project;
import at.researchstudio.sat.merkmalservice.model.Standard;
import at.researchstudio.sat.merkmalservice.model.mapping.Mapping;
import at.researchstudio.sat.mmsdesktop.view.components.ProcessState;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Component;

@Component
public class TargetStandardState {
    private final SimpleObjectProperty<Project> project;
    private final SimpleObjectProperty<Standard> targetStandard;
    private final SimpleObjectProperty<ProcessState> stepTargetStandardStatus;
    private final ObservableList<Mapping> mappings;
    private final ObservableList<Project> projects;
    private final ObservableList<Standard> standards;

    public TargetStandardState() {
        this.targetStandard = new SimpleObjectProperty<>();
        this.project = new SimpleObjectProperty<>();
        this.mappings = FXCollections.observableArrayList();
        this.projects = FXCollections.observableArrayList();
        this.standards = FXCollections.observableArrayList();
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

    public ObservableList<Mapping> mappingsProperty() {
        return mappings;
    }

    public ObservableList<Project> projectsProperty() {
        return projects;
    }

    public ObservableList<Standard> standardsProperty() {
        return standards;
    }
}
