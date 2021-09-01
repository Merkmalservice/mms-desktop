package at.researchstudio.sat.mmsdesktop.controller;

import at.researchstudio.sat.mmsdesktop.service.DataService;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import net.rgielen.fxweaver.core.FxmlView;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@FxmlView("projects.fxml")
public class ProjectsController implements Initializable {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ReactiveStateService stateService;
    // BorderPane Elements
    @FXML private BorderPane parentPane;

    public ProjectsController(ReactiveStateService stateService) {
        this.stateService = stateService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {}

    @FXML
    public void handleLoadProjectsAction(ActionEvent actionEvent) {
        System.out.println("Loading Projects");
        AccessToken accessToken = stateService.getLoginState().getUserSession().getAccessToken();
        DataService.callGraphQlEndpoint("test", accessToken);
    }
}
