package at.researchstudio.sat.mmsdesktop.controller.components;

import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
@FxmlView("userComponent.fxml")
public class UserComponentController implements Initializable {
    @FXML private HBox parentPane;
    @FXML private Label userLabel;
    @FXML public Text userIconText;

    private final ReactiveStateService stateService;

    public UserComponentController(ReactiveStateService stateService) {
        this.stateService = stateService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        parentPane.visibleProperty().bind(stateService.getLoginState().loggedInProperty());
        parentPane.managedProperty().bind(stateService.getLoginState().loggedInProperty());
        userLabel.textProperty().bind(stateService.getLoginState().fullNameProperty());
        userIconText.textProperty().bind(stateService.getLoginState().userInitialsProperty());
    }
}
