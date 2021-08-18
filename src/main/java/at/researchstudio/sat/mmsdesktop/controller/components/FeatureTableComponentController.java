package at.researchstudio.sat.mmsdesktop.controller.components;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.model.NumericFeature;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import at.researchstudio.sat.mmsdesktop.util.MessageUtils;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
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
    private ResourceBundle resourceBundle;

    @FXML private TableColumn centerResultFeaturesTableTypeColumn;
    @FXML private TableColumn centerResultFeaturesTableQuantityKindColumn;
    @FXML private TableColumn centerResultFeaturesTableUnitColumn;
    @FXML private TableView centerResultFeaturesTable;

    @Autowired
    public FeatureTableComponentController(ReactiveStateService stateService) {
        this.stateService = stateService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        stateService
                .getExtractState()
                .getSortedExtractedFeatures()
                .comparatorProperty()
                .bind(centerResultFeaturesTable.comparatorProperty());

        centerResultFeaturesTableTypeColumn.setCellFactory(
                c -> new IconLabelTableCell<>(resourceBundle));
        centerResultFeaturesTableTypeColumn.setCellValueFactory(
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

        centerResultFeaturesTableUnitColumn.setCellValueFactory(
                (Callback<TableColumn.CellDataFeatures<Feature, String>, ObservableValue<String>>)
                        p -> {
                            if (p.getValue() != null) {
                                Feature f = p.getValue();

                                if (f instanceof NumericFeature) {
                                    return new SimpleStringProperty(
                                            MessageUtils.getKeyForUnit(
                                                    resourceBundle,
                                                    ((NumericFeature) f).getUnit()));
                                }
                            }

                            return new SimpleStringProperty("");
                        });

        centerResultFeaturesTableQuantityKindColumn.setCellValueFactory(
                (Callback<TableColumn.CellDataFeatures<Feature, String>, ObservableValue<String>>)
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

        centerResultFeaturesTable.setRowFactory(
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
