package at.researchstudio.sat.mmsdesktop.gui.convert.targetstandard;

import static javafx.beans.binding.Bindings.not;

import at.researchstudio.sat.merkmalservice.model.Project;
import at.researchstudio.sat.merkmalservice.model.Standard;
import at.researchstudio.sat.mmsdesktop.model.task.DataResult;
import at.researchstudio.sat.mmsdesktop.service.DataService;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import at.researchstudio.sat.mmsdesktop.view.components.ProcessState;
import com.google.gson.Gson;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListCell;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.StringConverter;
import net.rgielen.fxweaver.core.FxmlView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
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
    private final SimpleObjectProperty<Project> selectedProject;
    private final SimpleObjectProperty<Standard> selectedStandard;
    @FXML private JFXComboBox<Project> projectList;
    @FXML private JFXComboBox<Standard> standardList;
    @FXML private Button reloadButton;

    @Autowired ResourceLoader resourceLoader;

    @Autowired
    public SelectTargetStandardController(ReactiveStateService stateService) {
        this.stateService = stateService;
        this.projects = FXCollections.observableArrayList();
        this.standards = FXCollections.observableArrayList();
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
                                standardList.getItems().clear();
                                standardList.getItems().addAll(selected.getStandards());
                                selectedProject.set(selected);
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
                                    setGraphic(null);
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
                            return null;
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
                                    setGraphic(null);
                                    setText(null);
                                    return;
                                }
                                setText(null);
                                setGraphic(new Label(item.getOrganization().getName()));
                            }
                        });
        this.standardList.setConverter(
                new StringConverter<>() {
                    @Override
                    public String toString(Standard standard) {
                        if (standard == null) {
                            return null;
                        }
                        return standard.getOrganization().getName();
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
                            selectedStandard.set(selected);
                            stateService
                                    .getConvertState()
                                    .getTargetStandardState()
                                    .stepTargetStandardStatusProperty()
                                    .set(ProcessState.STEP_COMPLETE);
                        });
        this.reloadButton
                .disableProperty()
                .bind(not(stateService.getLoginState().loggedInProperty()));
        if (stateService.getLoginState().isLoggedIn()) {
            handleLoadProjectsAction(null);
        }
    }

    @FXML
    public void handleLoadProjectsAction(ActionEvent actionEvent) {
        String idTokenString = stateService.getLoginState().getUserSession().getIdTokenString();
        Resource jsonFile = resourceLoader.getResource("graphql/query-projects.json");
        try {
            String queryString =
                    Files.readString(Path.of(jsonFile.getURI()), StandardCharsets.UTF_8);
            String result = DataService.callGraphQlEndpoint(queryString, idTokenString);
            Gson gson = new Gson();
            List<Project> projects =
                    gson.fromJson(result, DataResult.class).getData().getProjects();
            this.projects.setAll(projects);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ObservableList<Project> getProjects() {
        return projects;
    }

    public ObservableList<Standard> getStandards() {
        return standards;
    }
}
