package at.researchstudio.sat.mmsdesktop.controller.components;

import at.researchstudio.sat.merkmalservice.model.*;
import at.researchstudio.sat.mmsdesktop.util.MessageUtils;
import at.researchstudio.sat.mmsdesktop.util.Utils;
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

public class FeatureView extends VBox {
    private final ResourceBundle resourceBundle;
    private Feature feature;

    private final FeatureLabel featureName;
    private final GridPane featureNumericType;
    private final TableView<EnumFeature.OptionValue> featureEnumType;
    private final MarkdownView featureDescriptionMarkdown;
    private final JFXTextArea featureJson;

    private final Label featureQuantityKind;
    private final Label featureUnit;

    private static final Font pt16Font = new Font(16);
    private static final Font pt16SystemBoldFont = new Font("System Bold", 16);
    private boolean showJson = true;

    public FeatureView() {
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

        featureUnit.setFont(pt16Font);
        featureQuantityKind.setFont(pt16Font);

        unitLabel.setFont(pt16SystemBoldFont);
        quantityKindLabel.setFont(pt16SystemBoldFont);

        featureNumericType.add(quantityKindLabel, 0, 0);
        featureNumericType.add(featureQuantityKind, 0, 1);
        featureNumericType.add(unitLabel, 1, 0);
        featureNumericType.add(featureUnit, 1, 1);
        getChildren().add(featureNumericType);

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
        description.setFont(pt16SystemBoldFont);
        getChildren().add(description);

        featureDescriptionMarkdown = new MarkdownView();
        getChildren().add(featureDescriptionMarkdown);

        featureJson = new JFXTextArea();
        featureJson.setEditable(false);
        featureJson.setStyle(
                "-fx-background-color: white; -fx-font-family: Consolas,monaco,monospace;");
        getChildren().add(featureJson);
    }

    public FeatureView(Feature feature) {
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
                        Utils.executeOrDefaultOnException(
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
