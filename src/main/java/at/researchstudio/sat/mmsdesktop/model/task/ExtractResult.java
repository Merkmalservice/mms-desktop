package at.researchstudio.sat.mmsdesktop.model.task;

import at.researchstudio.sat.merkmalservice.model.Feature;

import java.io.Serializable;
import java.util.List;

public class ExtractResult implements Serializable {
    private final List<Feature> extractedFeatures;
    private final String logOutput;

    public ExtractResult(List<Feature> extractedFeatures, String logOutput) {
        this.extractedFeatures = extractedFeatures;
        this.logOutput = logOutput;
    }

    public List<Feature> getExtractedFeatures() {
        return extractedFeatures;
    }

    public String getLogOutput() {
        return logOutput;
    }
}
