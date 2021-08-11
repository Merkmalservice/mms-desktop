package at.researchstudio.sat.mmsdesktop.service;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.mmsdesktop.model.task.ExtractResult;
import org.springframework.stereotype.Component;

@Component
public class ExtractService {
    private ExtractResult extractResult;
    private Feature selectedFeature;

    public ExtractResult getExtractResult() {
        return extractResult;
    }

    public void setExtractResult(ExtractResult extractResult) {
        this.extractResult = extractResult;
    }

    public void resetExtractResults() {
        this.extractResult = null;
    }

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
