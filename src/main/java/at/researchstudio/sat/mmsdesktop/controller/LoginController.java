package at.researchstudio.sat.mmsdesktop.controller;

import at.researchstudio.sat.mmsdesktop.AuthService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@FxmlView("login.fxml")
public class LoginController implements Initializable {
    @FXML private VBox loginProcessing;
    @FXML private BorderPane parentPane;

    @Autowired public AuthService authService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {}

    @FXML
    private void handleAbortLoginAction(final ActionEvent event) {
        if (authService.getLoginTask().isRunning()) {
            authService.getLoginTask().cancel();
        }
    }
}
