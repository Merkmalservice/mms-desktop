package at.researchstudio.sat.mmsdesktop.controller.components;

import at.researchstudio.sat.merkmalservice.model.EnumFeature;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import com.jfoenix.controls.JFXTextArea;
import com.sandec.mdfx.MarkdownView;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxmlView;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;

@Component
@FxmlView("featureComponent.fxml")
public class FeatureComponentController implements Initializable {
    @FXML public GridPane featureNumericType;
    @FXML public TableView featureEnumType;
    @FXML public FontIcon featureTypeIcon;
    @FXML private VBox parentPane;
    @FXML private Label featureName;
    @FXML private MarkdownView featureDescriptionMarkdown;
    @FXML private JFXTextArea featureJson;

    @FXML private Label featureUnit;
    @FXML private Label featureQuantityKind;

    private final ReactiveStateService stateService;

    public FeatureComponentController(ReactiveStateService stateService) {
        this.stateService = stateService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.featureName
                .textProperty()
                .bind(stateService.getSelectedFeatureState().featureNameProperty());
        this.featureDescriptionMarkdown
                .mdStringProperty()
                .bind(stateService.getSelectedFeatureState().featureDescriptionProperty());
        this.featureJson
                .textProperty()
                .bind(stateService.getSelectedFeatureState().featureJsonProperty());

        this.featureQuantityKind
                .textProperty()
                .bind(stateService.getSelectedFeatureState().featureQuantityKindProperty());
        this.featureUnit
                .textProperty()
                .bind(stateService.getSelectedFeatureState().featureUnitProperty());

        this.featureNumericType
                .managedProperty()
                .bind(stateService.getSelectedFeatureState().featureIsNumericProperty());
        this.featureNumericType
                .visibleProperty()
                .bind(stateService.getSelectedFeatureState().featureIsNumericProperty());

        this.featureEnumType
                .managedProperty()
                .bind(stateService.getSelectedFeatureState().featureIsEnumerationProperty());
        this.featureEnumType
                .visibleProperty()
                .bind(stateService.getSelectedFeatureState().featureIsEnumerationProperty());

        this.featureTypeIcon
                .iconCodeProperty()
                .bind(stateService.getSelectedFeatureState().featureTypeIconProperty());
    }

    public ObservableList<EnumFeature.OptionValue> getFeatureOptionValues() {
        return stateService.getSelectedFeatureState().getFeatureOptionValues();
    }
}
