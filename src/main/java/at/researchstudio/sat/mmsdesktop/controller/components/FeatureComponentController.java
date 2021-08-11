package at.researchstudio.sat.mmsdesktop.controller.components;

import at.researchstudio.sat.mmsdesktop.service.ExtractService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
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
    @FXML private Label userLabel;

    private final ExtractService extractService;

    private SimpleStringProperty featureName;

    public FeatureComponentController(ExtractService extractService) {
        this.extractService = extractService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (extractService.getSelectedFeature() != null) {
            featureName = new SimpleStringProperty(extractService.getSelectedFeature().getName());
        }
    }
}
