package at.researchstudio.sat.mmsdesktop.gui.component.featuretable;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.model.NumericFeature;
import at.researchstudio.sat.mmsdesktop.gui.component.feature.IconLabelTableCell;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import at.researchstudio.sat.mmsdesktop.support.MessageUtils;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@FxmlView("featureTableComponent.fxml")
public class FeatureTableComponentController implements Initializable {
    private final ReactiveStateService stateService;

    @FXML private TableView<Feature> featuresTable;
    @FXML private TableColumn typeColumn;
    @FXML private TableColumn<Feature, String> quantityKindColumn;
    @FXML private TableColumn<Feature, String> unitColumn;

    @Autowired
    public FeatureTableComponentController(ReactiveStateService stateService) {
        this.stateService = stateService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        stateService
                .getExtractState()
                .getSortedExtractedFeatures()
                .comparatorProperty()
                .bind(featuresTable.comparatorProperty());

        typeColumn.setCellFactory(c -> new IconLabelTableCell<>(resourceBundle));
        typeColumn.setCellValueFactory(
                (Callback<
                                TableColumn.CellDataFeatures<Feature, String>,
                                SimpleObjectProperty<Feature>>)
                        p -> {
                            if (p.getValue() != null) {
                                Feature f = p.getValue();
                                return new SimpleObjectProperty<>(f);
                            } else {
                                return new SimpleObjectProperty<>();
                            }
                        });

        unitColumn.setCellValueFactory(
                p -> {
                    if (p.getValue() != null) {
                        Feature f = p.getValue();

                        if (f instanceof NumericFeature) {
                            return new SimpleStringProperty(
                                    MessageUtils.getKeyForUnit(
                                            resourceBundle, ((NumericFeature) f).getUnit()));
                        }
                    }

                    return new SimpleStringProperty("");
                });

        quantityKindColumn.setCellValueFactory(
                p -> {
                    if (p.getValue() != null) {
                        Feature f = p.getValue();

                        if (f instanceof NumericFeature) {
                            return new SimpleStringProperty(
                                    MessageUtils.getKeyForQuantityKind(
                                            resourceBundle,
                                            ((NumericFeature) f).getQuantityKind()));
                        }
                    }

                    return new SimpleStringProperty("");
                });

        featuresTable.setRowFactory(
                tv -> {
                    TableRow<Feature> row = new TableRow<>();
                    row.setOnMouseClicked(
                            event -> {
                                if (!row.isEmpty()) {
                                    stateService
                                            .getSelectedFeatureState()
                                            .setSelectedFeature(row.getItem());
                                }
                            });
                    return row;
                });
    }

    public SortedList<Feature> getSortedExtractedFeatures() {
        return stateService.getExtractState().getSortedExtractedFeatures();
    }
}
