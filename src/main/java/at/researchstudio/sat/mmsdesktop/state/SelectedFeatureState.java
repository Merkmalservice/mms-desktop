package at.researchstudio.sat.mmsdesktop.state;

import at.researchstudio.sat.merkmalservice.model.*;
import at.researchstudio.sat.mmsdesktop.util.MessageUtils;
import at.researchstudio.sat.mmsdesktop.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.springframework.stereotype.Component;

@Component
public class SelectedFeatureState {
    private final ResourceBundle resourceBundle;

    private final BooleanProperty showSelectedFeature;
    private final BooleanProperty featureIsNumeric;
    private final BooleanProperty featureIsEnumeration;
    private final SimpleStringProperty featureName;
    private final SimpleStringProperty featureDescription;
    private final SimpleStringProperty featureUnit;
    private final SimpleStringProperty featureQuantityKind;
    private final SimpleStringProperty featureJson;

    private final ObservableList<EnumFeature.OptionValue> featureOptionValues;

    private final ObjectProperty<Ikon> featureTypeIcon;

    public SelectedFeatureState() {
        this.resourceBundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        this.showSelectedFeature = new SimpleBooleanProperty(false);
        this.featureIsNumeric = new SimpleBooleanProperty(false);
        this.featureIsEnumeration = new SimpleBooleanProperty(false);
        this.featureName = new SimpleStringProperty("");
        this.featureDescription = new SimpleStringProperty("");
        this.featureJson = new SimpleStringProperty("");
        this.featureUnit = new SimpleStringProperty("");
        this.featureQuantityKind = new SimpleStringProperty("");
        this.featureOptionValues = FXCollections.observableArrayList();
        this.featureTypeIcon = new SimpleObjectProperty<Ikon>(BootstrapIcons.FILE_TEXT);
    }

    public BooleanProperty showSelectedFeatureProperty() {
        return showSelectedFeature;
    }

    public void clearSelectedFeature() {
        this.setSelectedFeature(null);
    }

    public void setSelectedFeature(Feature feature) {
        this.showSelectedFeature.setValue(Objects.nonNull(feature));
        this.featureName.setValue(Utils.executeOrDefaultOnException(feature::getName, ""));
        this.featureDescription.setValue(
                Utils.executeOrDefaultOnException(feature::getDescription, ""));
        this.featureTypeIcon.setValue(
                Utils.executeOrDefaultOnException(
                        () -> {
                            this.featureIsNumeric.setValue(false);
                            this.featureIsEnumeration.setValue(false);
                            this.featureOptionValues.clear();
                            if (feature instanceof StringFeature) {
                                return BootstrapIcons.FILE_TEXT;
                            } else if (feature instanceof EnumFeature) {
                                this.featureIsEnumeration.setValue(true);
                                this.featureOptionValues.setAll(
                                        ((EnumFeature) feature).getOptions());
                                return BootstrapIcons.UI_RADIOS;
                            } else if (feature instanceof ReferenceFeature) {
                                return BootstrapIcons.LINK_45DEG;
                            } else if (feature instanceof BooleanFeature) {
                                return BootstrapIcons.TOGGLES;
                            } else if (feature instanceof NumericFeature) {
                                this.featureIsNumeric.setValue(true);
                                this.featureQuantityKind.setValue(
                                        MessageUtils.getKeyForQuantityKind(
                                                resourceBundle,
                                                ((NumericFeature) feature).getQuantityKind()));
                                this.featureUnit.setValue(
                                        MessageUtils.getKeyForUnit(
                                                resourceBundle,
                                                ((NumericFeature) feature).getUnit()));
                                return BootstrapIcons.CALCULATOR;
                            } else {
                                return BootstrapIcons.QUESTION_CIRCLE;
                            }
                        },
                        BootstrapIcons.QUESTION_CIRCLE));
        this.featureJson.setValue(
                Utils.executeOrDefaultOnException(
                        () -> {
                            Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
                            return gson.toJson(feature);
                        },
                        ""));
    }

    public SimpleStringProperty featureNameProperty() {
        return featureName;
    }

    public SimpleStringProperty featureDescriptionProperty() {
        return featureDescription;
    }

    public SimpleStringProperty featureUnitProperty() {
        return featureUnit;
    }

    public SimpleStringProperty featureQuantityKindProperty() {
        return featureQuantityKind;
    }

    public BooleanProperty featureIsNumericProperty() {
        return featureIsNumeric;
    }

    public BooleanProperty featureIsEnumerationProperty() {
        return featureIsEnumeration;
    }

    public ObservableList<EnumFeature.OptionValue> getFeatureOptionValues() {
        return featureOptionValues;
    }

    public SimpleStringProperty featureJsonProperty() {
        return featureJson;
    }

    public ObjectProperty<Ikon> featureTypeIconProperty() {
        return featureTypeIcon;
    }
}
