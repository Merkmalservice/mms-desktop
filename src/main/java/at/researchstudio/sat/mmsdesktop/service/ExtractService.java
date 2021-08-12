package at.researchstudio.sat.mmsdesktop.service;

import at.researchstudio.sat.merkmalservice.model.Feature;
import org.springframework.stereotype.Component;

@Component
public class ExtractService {
    private Feature selectedFeature;

    public Feature getSelectedFeature() {
        return selectedFeature;
    }

    public void setSelectedFeature(Feature selectedFeature) {
        this.selectedFeature = selectedFeature;
    }

    public void resetSelectedFeature(Feature selectedFeature) {
        this.selectedFeature = null;
    }
}
