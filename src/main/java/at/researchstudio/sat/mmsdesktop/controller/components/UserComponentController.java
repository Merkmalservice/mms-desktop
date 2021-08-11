package at.researchstudio.sat.mmsdesktop.controller.components;

import at.researchstudio.sat.mmsdesktop.service.AuthService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

@Component
@FxmlView("userComponent.fxml")
public class UserComponentController implements Initializable {
    @FXML private HBox parentPane;
    @FXML private Label userLabel;
    @FXML public Text userIconText;

    private final AuthService authService;

    public UserComponentController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        parentPane.visibleProperty().bind(authService.loggedInProperty());
        parentPane.managedProperty().bind(authService.loggedInProperty());
        userLabel.textProperty().bind(authService.fullNameProperty());
        userIconText.textProperty().bind(authService.userInitialsProperty());
    }
}
