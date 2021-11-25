package at.researchstudio.sat.mmsdesktop.controller.components;

import at.researchstudio.sat.merkmalservice.model.*;
import javafx.scene.control.TableCell;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ResourceBundle;

public class IconLabelTableCell<T> extends TableCell<T, Feature> {
    private final FontIcon fontIcon;
    private final ResourceBundle resourceBundle;

    public IconLabelTableCell(ResourceBundle resourceBundle) {
        this(resourceBundle, null);
    }

    public IconLabelTableCell(ResourceBundle resourceBundle, Feature feature) {
        this.resourceBundle = resourceBundle;
        this.fontIcon = new FontIcon();
        fontIcon.setIconCode(getIcon(feature));
        fontIcon.setIconSize(24);
        setGraphic(fontIcon);
        setText(getLabel(feature));
    }

    @Override
    protected void updateItem(Feature feature, boolean empty) {
        super.updateItem(feature, empty);

        if (empty || feature == null) {
            fontIcon.setIconCode(BootstrapIcons.QUESTION_CIRCLE);
            setGraphic(null);
            setText(null);
        } else {
            fontIcon.setIconCode(getIcon(feature));
            setGraphic(fontIcon);
            setText(getLabel(feature));
        }
    }

    private Ikon getIcon(Feature f) {
        if (f instanceof StringFeature) {
            return BootstrapIcons.FILE_TEXT;
        } else if (f instanceof EnumFeature) {
            return BootstrapIcons.UI_RADIOS;
        } else if (f instanceof ReferenceFeature) {
            return BootstrapIcons.LINK_45DEG;
        } else if (f instanceof BooleanFeature) {
            return BootstrapIcons.TOGGLES;
        } else if (f instanceof NumericFeature) {
            return BootstrapIcons.CALCULATOR;
        }
        return BootstrapIcons.QUESTION_CIRCLE;
    }

    private String getLabel(Feature f) {
        if (f instanceof StringFeature) {
            return resourceBundle.getString("label.feature.type.text");
        } else if (f instanceof EnumFeature) {
            return resourceBundle.getString("label.feature.type.enum");
        } else if (f instanceof ReferenceFeature) {
            return resourceBundle.getString("label.feature.type.reference");
        } else if (f instanceof BooleanFeature) {
            return resourceBundle.getString("label.feature.type.boolean");
        } else if (f instanceof NumericFeature) {
            return resourceBundle.getString("label.feature.type.numeric");
        }
        return "<Unknown>";
    }
}
