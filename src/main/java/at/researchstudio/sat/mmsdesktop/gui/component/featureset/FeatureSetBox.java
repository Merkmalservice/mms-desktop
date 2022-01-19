package at.researchstudio.sat.mmsdesktop.gui.component.featureset;

import at.researchstudio.sat.mmsdesktop.constants.ViewConstants;
import at.researchstudio.sat.mmsdesktop.model.helper.FeatureSet;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.apache.commons.lang3.StringUtils;

public class FeatureSetBox extends HBox {
    private final FeatureSet featureSet;

    public FeatureSetBox(FeatureSet featureSet) {
        this.featureSet = featureSet;
        this.setPadding(new Insets(5, 0, 5, 0));
        this.setSpacing(10);
        Label featureSetName = new Label();
        featureSetName.setWrapText(true);
        featureSetName.setFont(new Font("System Bold", 12));
        featureSetName.setText(featureSet.getName());
        if (StringUtils.isNotBlank(featureSet.getDescription())) {
            featureSetName.setTooltip(new Tooltip(featureSet.getDescription()));
        }
        Label featureCount = new Label();
        featureCount.setWrapText(true);
        featureCount.setBackground(
                new Background(
                        new BackgroundFill(
                                Color.valueOf("#0000ff0c"),
                                ViewConstants.DEFAULT_CORNER_RADIUS,
                                new Insets(-5.0))));
        featureCount.setText("" + this.featureSet.getFeatures().size());
        getChildren().add(featureSetName);
        getChildren().add(featureCount);
    }

    public FeatureSet getFeatureSet() {
        return featureSet;
    }
}
