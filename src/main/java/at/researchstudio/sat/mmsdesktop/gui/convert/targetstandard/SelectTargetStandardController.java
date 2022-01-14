package at.researchstudio.sat.mmsdesktop.gui.convert.targetstandard;

import static at.researchstudio.sat.mmsdesktop.view.components.ProcessState.STEP_COMPLETE;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static javafx.beans.binding.Bindings.not;

import at.researchstudio.sat.merkmalservice.api.graphql.TokenRefreshingDataService;
import at.researchstudio.sat.merkmalservice.model.Organization;
import at.researchstudio.sat.merkmalservice.model.Project;
import at.researchstudio.sat.merkmalservice.model.Standard;
import at.researchstudio.sat.merkmalservice.model.mapping.Mapping;
import at.researchstudio.sat.mmsdesktop.gui.convert.perform.PerformConversionController;
import at.researchstudio.sat.mmsdesktop.gui.project.ProjectListCell;
import at.researchstudio.sat.mmsdesktop.gui.standard.StandardListCell;
import at.researchstudio.sat.mmsdesktop.service.AuthService;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import at.researchstudio.sat.mmsdesktop.view.components.ProcessState;
import com.jfoenix.controls.*;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
@FxmlView("selectTargetStandard.fxml")
public class SelectTargetStandardController implements Initializable {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ReactiveStateService stateService;
    @FXML private BorderPane stsParentPane;

    @FXML private JFXComboBox<Project> projectList;
    @FXML private JFXComboBox<Standard> standardList;
    @FXML private JFXButton reloadButton;
    @FXML private BorderPane mappingsView;
    @FXML private VBox loadingView;
    @FXML private VBox noMappingsView;
    @FXML private JFXListView<Mapping> mappingsList;

    @FXML public JFXButton toPerformConversion;

    @Autowired ResourceLoader resourceLoader;
    @Autowired TokenRefreshingDataService dataService;
    @Autowired AuthService authService;
    private final ResourceBundle resourceBundle = ResourceBundle.getBundle("messages");
    private final TargetStandardState targetStandardState;
    private JFXSnackbar snackbar;

    @Autowired
    public SelectTargetStandardController(ReactiveStateService stateService) {
        this.stateService = stateService;
        this.targetStandardState = stateService.getConvertState().getTargetStandardState();
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        snackbar = new JFXSnackbar(stsParentPane);

        this.projectList.disableProperty().bind(targetStandardState.loadingMappingsProperty());
        this.projectList
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, deSelected, selected) ->
                                targetStandardState.setSelectedProject(
                                        selected, standardList.getSelectionModel()));
        this.projectList.setCellFactory(param -> new ProjectListCell<Project>());
        this.projectList.setConverter(
                new StringConverter<>() {
                    @Override
                    public String toString(Project project) {
                        if (project == null) {
                            return resourceBundle.getString("label.projectpicker.chooseProject");
                        }
                        return project.getName();
                    }

                    @Override
                    public Project fromString(String string) {
                        return projectList.getItems().stream()
                                .filter(p -> p.getName().equals(string))
                                .findFirst()
                                .orElse(null);
                    }
                });

        this.standardList.disableProperty().bind(targetStandardState.loadingMappingsProperty());
        this.standardList.setCellFactory(param -> new StandardListCell<Standard>());
        this.standardList.setConverter(
                new StringConverter<>() {
                    @Override
                    public String toString(Standard standard) {
                        if (standard == null) {
                            return resourceBundle.getString("label.projectpicker.chooseFeatureSet");
                        }
                        return Optional.ofNullable(standard.getOrganization())
                                .map(Organization::getName)
                                .orElse(resourceBundle.getString("label.projects.projectStandard"));
                    }

                    @Override
                    public Standard fromString(String string) {
                        return standardList.getItems().stream()
                                .filter(p -> p.getOrganization().getName().equals(string))
                                .findFirst()
                                .orElse(null);
                    }
                });

        this.standardList
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, deSelected, selected) -> {
                            targetStandardState.setSelectedTargetStandard(selected);
                            handleLoadMappingsAction(null);
                        });
        this.mappingsView
                .visibleProperty()
                .bind(targetStandardState.selectedTargetStandardProperty().isNotNull());
        this.mappingsView
                .managedProperty()
                .bind(targetStandardState.selectedTargetStandardProperty().isNotNull());

        this.mappingsList
                .visibleProperty()
                .bind(
                        targetStandardState
                                .loadingMappingsProperty()
                                .not()
                                .and(
                                        Bindings.isNotEmpty(
                                                targetStandardState.selectedMappingsProperty())));
        this.mappingsList
                .managedProperty()
                .bind(
                        targetStandardState
                                .loadingMappingsProperty()
                                .not()
                                .and(
                                        Bindings.isNotEmpty(
                                                targetStandardState.selectedMappingsProperty())));
        this.noMappingsView
                .visibleProperty()
                .bind(
                        targetStandardState
                                .loadingMappingsProperty()
                                .not()
                                .and(
                                        Bindings.isEmpty(
                                                targetStandardState.selectedMappingsProperty())));
        this.noMappingsView
                .managedProperty()
                .bind(
                        targetStandardState
                                .loadingMappingsProperty()
                                .not()
                                .and(
                                        Bindings.isEmpty(
                                                targetStandardState.selectedMappingsProperty())));
        this.loadingView.visibleProperty().bind(targetStandardState.loadingMappingsProperty());
        this.loadingView.managedProperty().bind(targetStandardState.loadingMappingsProperty());

        toPerformConversion
                .disableProperty()
                .bind(
                        targetStandardState
                                .selectedTargetStandardProperty()
                                .isNull()
                                .or(targetStandardState.loadingMappingsProperty())
                                .or(
                                        stateService
                                                .getConvertState()
                                                .getInputFileState()
                                                .stepFileStatusProperty()
                                                .isNotEqualTo(STEP_COMPLETE)));

        this.mappingsList.setCellFactory(
                param ->
                        new JFXListCell<>() {
                            @Override
                            protected void updateItem(Mapping item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || item == null) {
                                    setText(null);
                                    setGraphic(null);
                                    return;
                                }
                                setGraphic(new Label(makeMappingLabelText(item)));
                                setText(null);
                            }
                        });
        this.reloadButton
                .disableProperty()
                .bind(not(stateService.getLoginState().loggedInProperty()));
        if (stateService.getLoginState().isLoggedIn()
                && targetStandardState.selectedTargetStandardProperty().isNull().get()) {
            targetStandardState.setAvailableProjects(dataService.getProjectsWithFeatureSets());
        }
        restoreProjectStandardSelection();
    }

    private String makeMappingLabelText(Mapping mapping) {
        StringBuilder sb = new StringBuilder();
        sb.append(resourceBundle.getString("label.mapping.name"))
                .append(": ")
                .append(mapping.getName());
        String actionGroupsString =
                mapping.getActionGroups().stream()
                        .collect(Collectors.groupingBy(g -> g.getClass(), toSet()))
                        .entrySet()
                        .stream()
                        .map(e -> Map.entry(e.getKey(), e.getValue().size()))
                        .map(e -> String.format("%d %s", e.getValue(), e.getKey().getSimpleName()))
                        .collect(joining(", "));
        if (StringUtils.isNotEmpty(actionGroupsString)) {
            sb.append(", ")
                    .append(resourceBundle.getString("label.mapping.actionGroups"))
                    .append(": ")
                    .append(actionGroupsString);
        }
        return sb.toString();
    }

    @FXML
    public void handleLoadProjectsAction(ActionEvent actionEvent) {
        // TODO: THIS SHOULD PROBABLY BE A TASK
        targetStandardState.setAvailableProjects(dataService.getProjectsWithFeatureSets());
        restoreProjectStandardSelection();
    }

    public void restoreProjectStandardSelection() {
        if (this.targetStandardState.selectedProjectProperty().isNotNull().get()) {
            if (this.projectList.getSelectionModel().isEmpty()) {
                int idx =
                        this.targetStandardState
                                .availableProjectsProperty()
                                .indexOf(this.targetStandardState.selectedProjectProperty().get());
                if (idx != -1) {
                    this.projectList.getSelectionModel().select(idx);
                }
            }
            if (this.standardList.getSelectionModel().isEmpty()) {
                int idx =
                        this.targetStandardState
                                .availableStandardsProperty()
                                .indexOf(
                                        this.targetStandardState
                                                .selectedTargetStandardProperty()
                                                .get());
                if (idx != -1) {
                    this.standardList.getSelectionModel().select(idx);
                }
            }
        }
    }

    @FXML
    public void handleToPerformConversion(ActionEvent actionEvent) {
        stateService.getViewState().switchCenterPane(PerformConversionController.class);

        if (stateService
                .getConvertState()
                .getPerformConversionState()
                .stepPerformConversionStatusProperty()
                .get()
                .isActive()) {
            stateService
                    .getConvertState()
                    .getPerformConversionState()
                    .stepPerformConversionStatusProperty()
                    .set(ProcessState.STEP_OPEN);
        }
        if (stateService
                .getConvertState()
                .getPerformConversionState()
                .stepPerformConversionStatusProperty()
                .get()
                .isOpen()) {
            stateService
                    .getConvertState()
                    .getPerformConversionState()
                    .stepPerformConversionStatusProperty()
                    .set(ProcessState.STEP_ACTIVE);
        }
    }

    @FXML
    public void handleLoadMappingsAction(ActionEvent actionEvent) {
        // TODO: THIS SHOULD PROBABLY BE A TASK
        if (targetStandardState.isProjectAndStandardSelected()) {
            Task<List<Mapping>> task =
                    new Task<List<Mapping>>() {
                        @Override
                        protected List<Mapping> call() throws Exception {
                            return dataService.getMappings(
                                    targetStandardState.getSelectedMappingIds());
                        }
                    };
            task.setOnSucceeded(
                    e -> {
                        targetStandardState.setSelectedMappings(task.getValue());
                    });
            task.setOnFailed(
                    e -> {
                        Platform.runLater(
                                () ->
                                        snackbar.fireEvent(
                                                new JFXSnackbar.SnackbarEvent(
                                                        new JFXSnackbarLayout(
                                                                resourceBundle.getString(
                                                                        "label.convert.mappings.fetchfailed")),
                                                        Duration.seconds(5),
                                                        null)));
                        targetStandardState.clear();
                    });
            targetStandardState.setLoadingMappings(true);
            new Thread(task).start();
        } else {
            targetStandardState.clearSelectedMappings();
        }
    }

    public ObservableList<Project> getAvailableProjects() {
        return targetStandardState.availableProjectsProperty();
    }

    public ObservableList<Standard> getAvailableStandards() {
        return targetStandardState.availableStandardsProperty();
    }

    public ObservableList<Mapping> getSelectedMappings() {
        return targetStandardState.selectedMappingsProperty();
    }
}
