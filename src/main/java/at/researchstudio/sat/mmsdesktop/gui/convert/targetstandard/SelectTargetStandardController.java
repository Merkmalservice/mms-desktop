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
import at.researchstudio.sat.mmsdesktop.service.AuthService;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import at.researchstudio.sat.mmsdesktop.view.components.ProcessState;
import com.jfoenix.controls.JFXButton;
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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
    @FXML private JFXComboBox<Project> projectList;
    @FXML private JFXComboBox<Standard> standardList;
    @FXML private JFXButton reloadButton;
    @FXML private VBox mappingsView;
    @FXML private JFXListView<Mapping> mappingsList;
    @FXML private ObservableList<Mapping> mappings;

    @FXML public JFXButton toPerformConversion;

    @Autowired ResourceLoader resourceLoader;
    @Autowired TokenRefreshingDataService dataService;
    @Autowired AuthService authService;
    private final ResourceBundle resourceBundle = ResourceBundle.getBundle("messages");
    private final TargetStandardState state;

    @Autowired
    public SelectTargetStandardController(ReactiveStateService stateService) {
        this.stateService = stateService;
        this.state = stateService.getConvertState().getTargetStandardState();
        this.mappings = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        this.projectList
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, deSelected, selected) -> {
                            if (selected != null) {
                                if (state.projectProperty().get() == null
                                        || !state.projectProperty().get().equals(selected)) {
                                    state.projectProperty().set(selected);
                                    state.targetStandardProperty().set(null);
                                    standardList.getSelectionModel().clearSelection();
                                }
                                if (!state.standardsProperty().equals(selected.getStandards())) {
                                    state.standardsProperty().setAll(selected.getStandards());
                                }
                                state.stepTargetStandardStatusProperty()
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
                            if (state.targetStandardProperty().get() == null
                                    || state.targetStandardProperty().get() != selected) {
                                state.targetStandardProperty().set(selected);
                            }
                            handleLoadMappingsAction(null);
                            state.stepTargetStandardStatusProperty().set(STEP_COMPLETE);
                        });
        this.mappingsView.visibleProperty().bind(state.targetStandardProperty().isNotNull());
        this.mappingsView.managedProperty().bind(state.targetStandardProperty().isNotNull());

        toPerformConversion
                .disableProperty()
                .bind(
                        state.targetStandardProperty()
                                .isNull()
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
        this.mappings.addListener(
                (ListChangeListener<Mapping>)
                        c -> {
                            while (c.next()) {}
                            this.state.mappingsProperty().setAll(this.mappings);
                        });
        this.reloadButton
                .disableProperty()
                .bind(not(stateService.getLoginState().loggedInProperty()));
        if (stateService.getLoginState().isLoggedIn()
                && state.targetStandardProperty().isNull().get()) {
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
                    state.projectsProperty().setAll(dataService.getProjectsWithFeatureSets());
                    restoreProjectStandardSelection();
                });
    }

    public void restoreProjectStandardSelection() {
        Platform.runLater(
                () -> {
                    selectPreviouslySelected(
                            this.state.projectProperty(),
                            this.state.projectsProperty(),
                            this.projectList.getSelectionModel());
                    selectPreviouslySelected(
                            this.state.targetStandardProperty(),
                            this.state.standardsProperty(),
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
        Platform.runLater(
                () -> {
                    if (!(this.state
                                    .projectProperty()
                                    .isNotNull()
                                    .and(this.state.targetStandardProperty().isNotNull()))
                            .get()) {
                        return;
                    }
                    String standardId = state.targetStandardProperty().get().getId();
                    List<String> mappingIds =
                            state.projectProperty().get().getMappings().stream()
                                    .filter(
                                            m ->
                                                    m.getFeatureSets().stream()
                                                            .anyMatch(
                                                                    s ->
                                                                            standardId.equals(
                                                                                    s.getId())))
                                    .map(Mapping::getId)
                                    .collect(Collectors.toList());
                    List<Mapping> loadedMappings = dataService.getMappings(mappingIds);
                    this.mappings.setAll(loadedMappings);
                });
    }

    public ObservableList<Project> getProjects() {
        return state.projectsProperty();
    }

    public ObservableList<Standard> getStandards() {
        return state.standardsProperty();
    }

    public ObservableList<Mapping> getMappings() {
        return mappings;
    }
}
