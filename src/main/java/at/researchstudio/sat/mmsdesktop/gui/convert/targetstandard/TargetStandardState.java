package at.researchstudio.sat.mmsdesktop.gui.convert.targetstandard;

import static at.researchstudio.sat.mmsdesktop.view.components.ProcessState.STEP_ACTIVE;
import static at.researchstudio.sat.mmsdesktop.view.components.ProcessState.STEP_COMPLETE;

import at.researchstudio.sat.merkmalservice.model.Project;
import at.researchstudio.sat.merkmalservice.model.Standard;
import at.researchstudio.sat.merkmalservice.model.mapping.Mapping;
import at.researchstudio.sat.mmsdesktop.view.components.ProcessState;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SingleSelectionModel;
import org.springframework.stereotype.Component;

@Component
public class TargetStandardState {
    private final SimpleObjectProperty<Project> selectedProject;
    private final SimpleObjectProperty<Standard> selectedTargetStandard;
    private final SimpleObjectProperty<ProcessState> stepTargetStandardStatus;
    private final ObservableList<Mapping> selectedMappings;

    private final ObservableList<Project> availableProjects;
    private final ObservableList<Standard> availableStandards;
    private final ObservableList<Standard> availableStandardsWithPropertySets;

    private final SimpleBooleanProperty loadingMappings;

    public TargetStandardState() {
        this.selectedTargetStandard = new SimpleObjectProperty<>();
        this.selectedProject = new SimpleObjectProperty<>();

        this.availableProjects = FXCollections.observableArrayList();
        this.availableStandards = FXCollections.observableArrayList();
        this.availableStandardsWithPropertySets = FXCollections.observableArrayList();

        this.selectedMappings = FXCollections.observableArrayList();
        this.stepTargetStandardStatus = new SimpleObjectProperty<>(STEP_ACTIVE);

        this.loadingMappings = new SimpleBooleanProperty(false);
    }

    public SimpleBooleanProperty loadingMappingsProperty() {
        return loadingMappings;
    }

    public void setAvailableProjects(List<Project> availableProjects) {
        this.availableProjects.setAll(availableProjects);
    }

    public void clear() {
        // CLEAR DATA
        availableProjects.clear();
        availableStandards.clear();
        availableStandardsWithPropertySets.clear();

        // CLEAR POSSIBLE SELECTION
        selectedProject.set(null);
        selectedTargetStandard.set(null);
        clearSelectedMappings();
    }

    public ObservableList<Project> availableProjectsProperty() {
        return availableProjects;
    }

    public ObservableList<Standard> availableStandardsProperty() {
        return availableStandards;
    }

    public ObservableList<Standard> availableStandardsWithPropertySetsProperty() {
        return availableStandardsWithPropertySets;
    }

    public SimpleObjectProperty<Project> selectedProjectProperty() {
        return selectedProject;
    }

    public SimpleObjectProperty<Standard> selectedTargetStandardProperty() {
        return selectedTargetStandard;
    }

    public SimpleObjectProperty<ProcessState> stepTargetStandardStatusProperty() {
        return stepTargetStandardStatus;
    }

    public ObservableList<Mapping> selectedMappingsProperty() {
        return selectedMappings;
    }

    public void setSelectedProject(
            Project selectedProject, SingleSelectionModel<Standard> standardListSelectionModel) {
        if (selectedProject != null) {
            if (this.selectedProject.isNotEqualTo(selectedProject).get()) {
                this.selectedProject.set(selectedProject);
                selectedTargetStandard.set(null);
                standardListSelectionModel.clearSelection();
                availableStandards.setAll(selectedProject.getStandards());
            }
        }
        stepTargetStandardStatus.set(ProcessState.STEP_ACTIVE);
    }

    public void setSelectedTargetStandard(Standard selectedTargetStandard) {
        if (selectedTargetStandard != null) {
            if (this.selectedTargetStandard.isNotEqualTo(selectedTargetStandard).get()) {
                this.selectedTargetStandard.set(selectedTargetStandard);
            }
            stepTargetStandardStatus.set(STEP_COMPLETE);
        } else {
            stepTargetStandardStatus.set(STEP_ACTIVE);
        }
    }

    public boolean isProjectAndStandardSelected() {
        return selectedProject.isNotNull().and(selectedTargetStandard.isNotNull()).get();
    }

    public void setSelectedMappings(List<Mapping> loadedMappings) {
        selectedMappings.setAll(loadedMappings);
        loadingMappings.set(false);
    }

    public void clearSelectedMappings() {
        selectedMappings.clear();
    }

    public void setLoadingMappings(boolean loading) {
        loadingMappings.set(loading);
    }

    public List<String> getSelectedMappingIds() {
        String standardId = selectedTargetStandard.get().getId();
        return selectedProject.get().getMappings().stream()
                .filter(
                        m ->
                                m.getFeatureSets().stream()
                                        .anyMatch(s -> standardId.equals(s.getId())))
                .map(Mapping::getId)
                .collect(Collectors.toList());
    }
}
