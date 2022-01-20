package at.researchstudio.sat.mmsdesktop.gui.login;

import at.researchstudio.sat.merkmalservice.api.auth.KeycloakService;
import at.researchstudio.sat.mmsdesktop.service.AuthService;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@FxmlView("login.fxml")
public class LoginController implements Initializable {
    @FXML private VBox loginProcessing;
    @FXML private VBox logoutProcessing;

    private final AuthService authService;
    private final ReactiveStateService stateService;
    private final KeycloakService keycloakService; // TODO: REMOVE THIS AGAIN

    @Autowired
    public LoginController(
            AuthService authService,
            ReactiveStateService stateService,
            KeycloakService keycloakService) {
        this.authService = authService;
        this.stateService = stateService;
        this.keycloakService = keycloakService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loginProcessing
                .managedProperty()
                .bind(Bindings.not(stateService.getLoginState().loggedInProperty()));
        loginProcessing
                .visibleProperty()
                .bind(Bindings.not(stateService.getLoginState().loggedInProperty()));
        logoutProcessing.managedProperty().bind(stateService.getLoginState().loggedInProperty());
        logoutProcessing.visibleProperty().bind(stateService.getLoginState().loggedInProperty());
    }

    @FXML
    private void handleAbortLoginAction(final ActionEvent event) {
        if (authService.getLoginTask().isRunning()) {
            authService.getLoginTask().cancel();
        }
    }

    @FXML
    private void handleForceLogoutAction(final ActionEvent event) {
        if (authService.getLogoutTask().isRunning()) {
            authService.getLogoutTask().cancel();
            keycloakService.logout();
        }
    }
}
