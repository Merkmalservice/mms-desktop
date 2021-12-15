package at.researchstudio.sat.mmsdesktop.gui.convert.targetstandard;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static javafx.beans.binding.Bindings.not;

import at.researchstudio.sat.merkmalservice.api.DataService;
import at.researchstudio.sat.merkmalservice.model.Organization;
import at.researchstudio.sat.merkmalservice.model.Project;
import at.researchstudio.sat.merkmalservice.model.Standard;
import at.researchstudio.sat.merkmalservice.model.mapping.Mapping;
import at.researchstudio.sat.mmsdesktop.gui.component.about.AboutController;
import at.researchstudio.sat.mmsdesktop.model.auth.UserSession;
import at.researchstudio.sat.mmsdesktop.service.AuthService;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import at.researchstudio.sat.mmsdesktop.view.components.ProcessState;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.layout.VBox;
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
    private final ObservableList<Project> projects;
    private final ObservableList<Standard> standards;
    private final ObservableList<Mapping> mappings;
    private final SimpleObjectProperty<Project> selectedProject;
    private final SimpleObjectProperty<Standard> selectedStandard;
    @FXML private JFXComboBox<Project> projectList;
    @FXML private JFXComboBox<Standard> standardList;
    @FXML private Button reloadButton;
    @FXML private VBox mappingsView;
    @FXML private JFXListView<Mapping> mappingsList;
    @Autowired ResourceLoader resourceLoader;
    @Autowired DataService dataService;
    @Autowired AuthService authService;
    private final ResourceBundle resourceBundle = ResourceBundle.getBundle("messages");

    @Autowired
    public SelectTargetStandardController(ReactiveStateService stateService) {
        this.stateService = stateService;
        this.projects = FXCollections.observableArrayList();
        this.standards = FXCollections.observableArrayList();
        this.mappings = FXCollections.observableArrayList();
        this.selectedProject = new SimpleObjectProperty<>();
        this.selectedStandard = new SimpleObjectProperty<>();
        this.stateService
                .getConvertState()
                .getTargetStandardState()
                .projectProperty()
                .bind(selectedProject);
        this.stateService
                .getConvertState()
                .getTargetStandardState()
                .targetStandardProperty()
                .bind(selectedStandard);
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        this.projectList
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, deSelected, selected) -> {
                            if (selected != null) {
                                if (selectedProject.get() == null
                                        || !selectedProject.get().equals(selected)) {
                                    selectedProject.set(selected);
                                    selectedStandard.set(null);
                                    standardList.getSelectionModel().clearSelection();
                                }
                                if (!standards.equals(selected.getStandards())) {
                                    standards.setAll(selected.getStandards());
                                }
                                stateService
                                        .getConvertState()
                                        .getTargetStandardState()
                                        .stepTargetStandardStatusProperty()
                                        .set(ProcessState.STEP_ACTIVE);
                            }
                        });
        this.projectList.setCellFactory(
                param ->
                        new JFXListCell<>() {
                            @Override
                            protected void updateItem(Project item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || item == null) {
                                    setText(null);
                                    setGraphic(
                                            new Label(
                                                    resourceBundle.getString(
                                                            "label.projectpicker.chooseProject")));
                                    return;
                                }
                                setGraphic(new Label(item.getName()));
                                setText(null);
                            }
                        });
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
        this.standardList.setCellFactory(
                param ->
                        new JFXListCell<>() {
                            @Override
                            protected void updateItem(Standard item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || item == null) {
                                    setGraphic(
                                            new Label(
                                                    resourceBundle.getString(
                                                            "label.projectpicker.chooseFeatureSet")));
                                    setText(null);
                                    return;
                                }
                                setText(null);
                                setGraphic(
                                        new Label(
                                                Optional.ofNullable(item.getOrganization())
                                                        .map(Organization::getName)
                                                        .orElse(
                                                                resourceBundle.getString(
                                                                        "label.projects.projectStandard"))));
                            }
                        });
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
                            if (selectedStandard.get() == null
                                    || selectedStandard.get() != selected) {
                                selectedStandard.set(selected);
                            }
                            handleLoadMappingsAction(null);
                            stateService
                                    .getConvertState()
                                    .getTargetStandardState()
                                    .stepTargetStandardStatusProperty()
                                    .set(ProcessState.STEP_COMPLETE);
                        });
        this.mappingsView.visibleProperty().bind(selectedStandard.isNotNull());
        this.mappingsView.managedProperty().bind(selectedStandard.isNotNull());
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
        this.mappings.addListener(
                (ListChangeListener<Mapping>)
                        c -> {
                            while (c.next()) {}
                            this.stateService
                                    .getConvertState()
                                    .getTargetStandardState()
                                    .mappingsProperty()
                                    .setAll(this.mappings);
                        });
        this.reloadButton
                .disableProperty()
                .bind(not(stateService.getLoginState().loggedInProperty()));
        if (stateService.getLoginState().isLoggedIn() && selectedStandard.isNull().get()) {
            handleLoadProjectsAction(null);
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
        Platform.runLater(
                () -> {
                    try {
                        setLoadedProjects();
                    } catch (Exception e) {
                        Task<UserSession> refreshTokenTask =
                                authService.getRefreshTokenTask(
                                        stateService
                                                .getLoginState()
                                                .getUserSession()
                                                .getRefreshTokenString());
                        setRefreshTokenTaskActions(
                                refreshTokenTask,
                                () -> {
                                    setLoadedProjects();
                                });
                    }
                });
    }

    private void setLoadedProjects() {
        String idTokenString = stateService.getLoginState().getUserSession().getIdTokenString();
        List<Project> loadedProjects = dataService.getProjectsWithFeatureSets(idTokenString);
        this.projects.setAll(loadedProjects);
        restoreProjectStandardSelection();
    }

    public void restoreProjectStandardSelection() {
        Platform.runLater(
                () -> {
                    selectPreviouslySelected(
                            this.selectedProject,
                            this.projects,
                            this.projectList.getSelectionModel());
                    selectPreviouslySelected(
                            this.selectedStandard,
                            this.standards,
                            this.standardList.getSelectionModel());
                });
    }

    private <T> void selectPreviouslySelected(
            SimpleObjectProperty<T> selected,
            ObservableList<T> items,
            SingleSelectionModel<T> selectionModel) {
        if (selected.isNotNull().get()) {
            if (selectionModel.isEmpty()) {
                int idx = items.indexOf(selected.get());
                if (idx != -1) {
                    selectionModel.select(idx);
                }
            }
        }
    }

    @FXML
    public void handleLoadMappingsAction(ActionEvent actionEvent) {
        Platform.runLater(
                () -> {
                    if (!(this.selectedProject.isNotNull().and(this.selectedStandard.isNotNull()))
                            .get()) {
                        return;
                    }
                    String standardId = selectedStandard.get().getId();
                    List<String> mappingIds =
                            selectedProject.get().getMappings().stream()
                                    .filter(
                                            m ->
                                                    m.getFeatureSets().stream()
                                                            .anyMatch(
                                                                    s ->
                                                                            standardId.equals(
                                                                                    s.getId())))
                                    .map(Mapping::getId)
                                    .collect(Collectors.toList());
                    try {
                        setLoadedMappings(mappingIds);
                    } catch (Exception e) {
                        Task<UserSession> refreshTokenTask =
                                authService.getRefreshTokenTask(
                                        stateService
                                                .getLoginState()
                                                .getUserSession()
                                                .getRefreshTokenString());
                        setRefreshTokenTaskActions(
                                refreshTokenTask,
                                () -> {
                                    setLoadedMappings(mappingIds);
                                });
                    }
                });
    }

    private void setLoadedMappings(List<String> mappingIds) {
        String idTokenString = stateService.getLoginState().getUserSession().getIdTokenString();
        List<Mapping> loadedMappings = dataService.getMappings(mappingIds, idTokenString);
        this.mappings.setAll(loadedMappings);
    }

    private void setRefreshTokenTaskActions(Task<UserSession> refreshTokenTask, Runnable runnable) {
        refreshTokenTask.setOnSucceeded(
                t -> {
                    stateService.getLoginState().setUserSession(refreshTokenTask.getValue());
                    authService.resetLoginTask();
                    try {
                        runnable.run();
                    } catch (Exception ex) {
                        logger.error("Could not Load Data also not with Refresh Token");
                        stateService.getLoginState().setUserSession(null);
                        stateService.getLoginState().setLoggedIn(false);
                    }
                });
        refreshTokenTask.setOnCancelled(
                t -> {
                    // TODO: Cancelled views
                    stateService.getViewState().switchCenterPane(AboutController.class);
                    stateService.getLoginState().setUserSession(null);
                    authService.resetLoginTask();
                });
        refreshTokenTask.setOnFailed(
                t -> {
                    // TODO: Error Handling
                    stateService.getViewState().switchCenterPane(AboutController.class);
                    stateService.getLoginState().setUserSession(null);
                    authService.resetLoginTask();
                });
        new Thread(refreshTokenTask).start();
    }

    public ObservableList<Project> getProjects() {
        return projects;
    }

    public ObservableList<Standard> getStandards() {
        return standards;
    }

    public ObservableList<Mapping> getMappings() {
        return mappings;
    }
}
