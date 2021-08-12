package at.researchstudio.sat.mmsdesktop.controller.components;

import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import com.jfoenix.controls.JFXTextArea;
import com.sandec.mdfx.MarkdownView;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

@Component
@FxmlView("featureComponent.fxml")
public class FeatureComponentController implements Initializable {
    @FXML private VBox parentPane;
    @FXML private Label featureName;
    @FXML private Label featureType;
    @FXML private MarkdownView featureDescriptionMarkdown;
    @FXML private JFXTextArea featureJson;

    private final ReactiveStateService stateService;

    public FeatureComponentController(ReactiveStateService stateService) {
        this.stateService = stateService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.featureName
                .textProperty()
                .bind(stateService.getSelectedFeatureState().featureNameProperty());
        this.featureType
                .textProperty()
                .bind(stateService.getSelectedFeatureState().featureTypeProperty());
        this.featureDescriptionMarkdown
                .mdStringProperty()
                .bind(stateService.getSelectedFeatureState().featureDescriptionProperty());
        this.featureJson
                .textProperty()
                .bind(stateService.getSelectedFeatureState().featureJsonProperty());
    }
}
