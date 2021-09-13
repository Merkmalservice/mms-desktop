package at.researchstudio.sat.mmsdesktop.controller.components;

import at.researchstudio.sat.merkmalservice.model.*;
import javafx.scene.control.Label;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.javafx.FontIcon;

/** Simply Extension of Label that adds the corresponding Icon in front of the Feature name */
public class FeatureLabel extends Label {
    private Feature feature;
    private final FontIcon featureTypeIcon;

    public FeatureLabel() {
        featureTypeIcon = new FontIcon(BootstrapIcons.QUESTION_CIRCLE);
        this.setGraphic(featureTypeIcon);
    }

    public FeatureLabel(Feature feature) {
        this();
        setFeature(feature);
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
        this.setText(feature.getName());

        if (feature instanceof StringFeature) {
            featureTypeIcon.setIconCode(BootstrapIcons.FILE_TEXT);
        } else if (feature instanceof EnumFeature) {
            featureTypeIcon.setIconCode(BootstrapIcons.UI_RADIOS);
        } else if (feature instanceof ReferenceFeature) {
            featureTypeIcon.setIconCode(BootstrapIcons.LINK_45DEG);
        } else if (feature instanceof BooleanFeature) {
            featureTypeIcon.setIconCode(BootstrapIcons.TOGGLES);
        } else if (feature instanceof NumericFeature) {
            featureTypeIcon.setIconCode(BootstrapIcons.CALCULATOR);
        } else {
            featureTypeIcon.setIconCode(BootstrapIcons.QUESTION_CIRCLE);
        }
    }

    public Feature getFeature() {
        return feature;
    }
}
