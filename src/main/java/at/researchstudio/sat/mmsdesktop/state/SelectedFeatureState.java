package at.researchstudio.sat.mmsdesktop.state;

import at.researchstudio.sat.merkmalservice.model.*;
import javafx.beans.property.*;
import org.springframework.stereotype.Component;

@Component
public class SelectedFeatureState {
    private final ObjectProperty<Feature> feature;

    public SelectedFeatureState() {
        this.feature = new SimpleObjectProperty<>(null);
    }

    public void clearSelectedFeature() {
        this.setSelectedFeature(null);
    }

    public void setSelectedFeature(Feature feature) {
        this.feature.setValue(feature);
    }

    public ObjectProperty<Feature> featureProperty() {
        return feature;
    }
}
