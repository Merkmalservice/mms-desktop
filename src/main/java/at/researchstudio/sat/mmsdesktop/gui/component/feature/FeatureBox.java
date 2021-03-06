package at.researchstudio.sat.mmsdesktop.gui.component.feature;

import at.researchstudio.sat.merkmalservice.ifc.support.IfcUtils;
import at.researchstudio.sat.merkmalservice.model.EnumFeature;
import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.model.NumericFeature;
import at.researchstudio.sat.merkmalservice.model.StringFeature;
import at.researchstudio.sat.mmsdesktop.constants.ViewConstants;
import at.researchstudio.sat.mmsdesktop.support.MessageUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jfoenix.controls.JFXTextArea;
import com.sandec.mdfx.MarkdownView;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class FeatureBox extends VBox {
    private final ResourceBundle resourceBundle;
    private Feature feature;

    private final FeatureLabel featureName;
    private final GridPane featureNumericType;
    private final TableView<EnumFeature.OptionValue> featureEnumType;
    private final TableView<EnumFeature.OptionValue> instanceValues;
    private final MarkdownView featureDescriptionMarkdown;
    private final JFXTextArea featureJson;

    private final Label featureQuantityKind;
    private final Label featureUnit;

    private boolean showJson = true;

    public FeatureBox() {
        this.resourceBundle = ResourceBundle.getBundle("messages", Locale.getDefault());

        featureName = new FeatureLabel();
        featureName.setFont(new Font(24));
        featureName.setWrapText(true);
        getChildren().add(featureName);

        featureNumericType = new GridPane();
        featureNumericType.setHgap(10);
        featureNumericType.setVgap(10);

        Label quantityKindLabel = new Label(resourceBundle.getString("label.feature.quantitykind"));
        Label unitLabel = new Label(resourceBundle.getString("label.feature.unit"));
        featureUnit = new Label();
        featureQuantityKind = new Label();

        featureUnit.setFont(ViewConstants.FONT_PT16);
        featureQuantityKind.setFont(ViewConstants.FONT_PT16);

        unitLabel.setFont(ViewConstants.FONT_PT16_SYSTEM_BOLD);
        quantityKindLabel.setFont(ViewConstants.FONT_PT16_SYSTEM_BOLD);

        featureNumericType.add(quantityKindLabel, 0, 0);
        featureNumericType.add(featureQuantityKind, 0, 1);
        featureNumericType.add(unitLabel, 1, 0);
        featureNumericType.add(featureUnit, 1, 1);
        getChildren().add(featureNumericType);

        instanceValues = new TableView<>();
        TableColumn<EnumFeature.OptionValue, String> instanceValuesCol =
                new TableColumn<>(resourceBundle.getString("label.feature.instanceValues"));
        instanceValuesCol.setEditable(false);
        instanceValuesCol.setPrefWidth(100);
        instanceValuesCol.setSortType(TableColumn.SortType.DESCENDING);
        instanceValuesCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        instanceValues.getColumns().add(instanceValuesCol);
        getChildren().add(instanceValues);

        featureEnumType = new TableView<>();
        TableColumn<EnumFeature.OptionValue, String> column =
                new TableColumn<>(resourceBundle.getString("label.feature.options"));
        column.setEditable(false);
        column.setPrefWidth(100);
        column.setSortType(TableColumn.SortType.DESCENDING);
        column.setCellValueFactory(new PropertyValueFactory<>("value"));
        featureEnumType.getColumns().add(column);
        getChildren().add(featureEnumType);

        Label description = new Label(resourceBundle.getString("label.feature.description"));
        description.setFont(ViewConstants.FONT_PT16_SYSTEM_BOLD);
        getChildren().add(description);

        featureDescriptionMarkdown = new MarkdownView();
        getChildren().add(featureDescriptionMarkdown);

        featureJson = new JFXTextArea();
        featureJson.setEditable(false);
        featureJson.setStyle(
                "-fx-background-color: white; -fx-font-family: Consolas,monaco,monospace;");
        getChildren().add(featureJson);
    }

    public FeatureBox(Feature feature) {
        this();
        setFeature(feature);
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
        if (Objects.nonNull(feature)) {
            featureName.setFeature(feature);
            featureDescriptionMarkdown.setMdString(feature.getDescription());
            if (showJson) {
                featureJson.setText(
                        IfcUtils.executeOrDefaultOnException(
                                () -> {
                                    Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
                                    return gson.toJson(feature);
                                },
                                ""));
            }
            featureJson.setManaged(showJson);
            featureJson.setVisible(showJson);

            featureNumericType.setVisible(false);
            featureNumericType.setManaged(false);

            featureEnumType.setVisible(false);
            featureEnumType.setManaged(false);
            instanceValues.setVisible(false);
            instanceValues.setManaged(false);

            if (feature instanceof EnumFeature) {
                featureEnumType.setVisible(true);
                featureEnumType.setManaged(true);

                featureEnumType.setItems(
                        FXCollections.observableList(((EnumFeature) feature).getOptions()));
            } else if (feature instanceof NumericFeature) {
                featureNumericType.setVisible(true);
                featureNumericType.setManaged(true);

                this.featureQuantityKind.setText(
                        MessageUtils.getKeyForQuantityKind(
                                resourceBundle, ((NumericFeature) feature).getQuantityKind()));
                this.featureUnit.setText(
                        MessageUtils.getKeyForUnit(
                                resourceBundle, ((NumericFeature) feature).getUnit()));

                instanceValues.setVisible(true);
                instanceValues.setManaged(true);
                instanceValues.setItems(FXCollections.observableList(feature.getInstanceValues()));
            } else if (feature instanceof StringFeature) {
                instanceValues.setVisible(true);
                instanceValues.setManaged(true);
                instanceValues.setItems(FXCollections.observableList(feature.getInstanceValues()));
            }
        }
    }

    public void setShowJson(boolean showJson) {
        this.showJson = showJson;
        featureJson.setManaged(this.showJson);
        featureJson.setVisible(this.showJson);
    }

    public Feature getFeature() {
        return feature;
    }
}
