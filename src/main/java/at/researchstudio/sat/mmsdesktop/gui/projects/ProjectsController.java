package at.researchstudio.sat.mmsdesktop.gui.projects;

import at.researchstudio.sat.merkmalservice.api.DataService;
import at.researchstudio.sat.merkmalservice.api.support.model.DataResult;
import at.researchstudio.sat.merkmalservice.model.Project;
import at.researchstudio.sat.mmsdesktop.model.auth.UserSession;
import at.researchstudio.sat.mmsdesktop.service.AuthService;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import com.google.gson.Gson;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import net.rgielen.fxweaver.core.FxmlView;
import org.keycloak.exceptions.TokenVerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
@FxmlView("projects.fxml")
public class ProjectsController implements Initializable {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ReactiveStateService stateService;
    private final ObservableList<Project> loadedProjects;
    // BorderPane Elements
    @FXML private BorderPane parentPane;
    @Autowired ResourceLoader resourceLoader;
    @Autowired DataService dataService;
    @Autowired AuthService authService;

    public ProjectsController(ReactiveStateService stateService) {
        this.stateService = stateService;
        this.loadedProjects = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {}

    @FXML
    public void handleLoadProjectsAction(ActionEvent actionEvent) {
        String idTokenString = stateService.getLoginState().getUserSession().getIdTokenString();
        Resource jsonFile = resourceLoader.getResource("graphql/query-projects.gql");
        try {
            String queryString =
                    Files.readString(Path.of(jsonFile.getURI()), StandardCharsets.UTF_8);
            String result = dataService.callGraphQlEndpoint(queryString, idTokenString);
            Gson gson = new Gson();
            List<Project> projects =
                    gson.fromJson(result, DataResult.class).getData().getProjects();
            loadedProjects.setAll(projects);
        } catch (TokenVerificationException e) {
            Task<UserSession> refreshTokenTask =
                    authService.getRefreshTokenTask(
                            stateService.getLoginState().getUserSession().getRefreshTokenString());
            stateService.getLoginState().setUserSession(refreshTokenTask.getValue());
            try {
                String queryString =
                        Files.readString(Path.of(jsonFile.getURI()), StandardCharsets.UTF_8);
                String result = dataService.callGraphQlEndpoint(queryString, idTokenString);
                Gson gson = new Gson();
                List<Project> projects =
                        gson.fromJson(result, DataResult.class).getData().getProjects();
                loadedProjects.setAll(projects);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ObservableList<Project> getLoadedProjects() {
        return loadedProjects;
    }
}
