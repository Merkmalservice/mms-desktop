package at.researchstudio.sat.mmsdesktop.controller;

import at.researchstudio.sat.mmsdesktop.model.task.DataResult;
import at.researchstudio.sat.mmsdesktop.model.task.ProjectResult;
import at.researchstudio.sat.mmsdesktop.service.DataService;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import com.google.gson.Gson;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.commons.codec.Charsets;
import org.apache.jena.ext.com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@FxmlView("projects.fxml")
public class ProjectsController implements Initializable {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ReactiveStateService stateService;
    private final ObservableList<ProjectResult> loadedProjects;
    // BorderPane Elements
    @FXML private BorderPane parentPane;

    public ProjectsController(ReactiveStateService stateService) {
        this.stateService = stateService;
        this.loadedProjects = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {}

    @FXML
    public void handleLoadProjectsAction(ActionEvent actionEvent) {
        String idTokenString = stateService.getLoginState().getUserSession().getIdTokenString();
        URL josnUrl = Resources.getResource("graphql/query-projects.json");
        try {
            String queryString = Resources.toString(josnUrl, Charsets.UTF_8);
            String result = DataService.callGraphQlEndpoint(queryString, idTokenString);
            Gson gson = new Gson();
            List<ProjectResult> projects =
                    gson.fromJson(result, DataResult.class).getData().getProjects();
            loadedProjects.setAll(projects);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ObservableList<ProjectResult> getLoadedProjects() {
        return loadedProjects;
    }
}
