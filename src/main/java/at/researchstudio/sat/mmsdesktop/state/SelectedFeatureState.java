package at.researchstudio.sat.mmsdesktop.state;

import at.researchstudio.sat.merkmalservice.model.*;
import at.researchstudio.sat.mmsdesktop.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.stereotype.Component;

@Component
public class SelectedFeatureState {
    private final BooleanProperty showSelectedFeature;
    private final SimpleStringProperty featureName;
    private final SimpleStringProperty featureType;
    private final SimpleStringProperty featureDescription;

    private final SimpleStringProperty featureJson;

    public SelectedFeatureState() {
        this.showSelectedFeature = new SimpleBooleanProperty(false);
        this.featureName = new SimpleStringProperty("");
        this.featureType = new SimpleStringProperty("");
        this.featureDescription = new SimpleStringProperty("");
        this.featureJson = new SimpleStringProperty("");
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
        this.featureType.setValue(
                Utils.executeOrDefaultOnException(
                        () -> {
                            if (feature instanceof StringFeature) {
                                return "TEXT";
                            } else if (feature instanceof EnumFeature) {
                                return "ENUM";
                            } else if (feature instanceof ReferenceFeature) {
                                return "REFERENCE";
                            } else if (feature instanceof BooleanFeature) {
                                return "BOOLE";
                            } else if (feature instanceof NumericFeature) {
                                return "NUMERIC";
                            } else {
                                return "<no valid type>";
                            }
                        },
                        ""));
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

    public SimpleStringProperty featureTypeProperty() {
        return featureType;
    }

    public SimpleStringProperty featureDescriptionProperty() {
        return featureDescription;
    }

    public SimpleStringProperty featureJsonProperty() {
        return featureJson;
    }
}
