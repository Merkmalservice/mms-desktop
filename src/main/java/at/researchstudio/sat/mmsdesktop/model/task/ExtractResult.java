package at.researchstudio.sat.mmsdesktop.model.task;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.mmsdesktop.controller.components.FeatureSetControl;
import at.researchstudio.sat.mmsdesktop.model.helper.FeatureSet;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// TODO: REFACTOR THIS -> ExtractResult is based upon ALL files, this might be confusing somehow
public class ExtractResult implements Serializable {
    private final Set<FeatureSet> extractedUniqueFeatureSets;
    private final List<Feature> extractedFeatures;
    private final String logOutput;

    public ExtractResult(
            List<Feature> extractedFeatures,
            Set<FeatureSet> extractedUniqueFeatureSets,
            String logOutput) {
        this.extractedUniqueFeatureSets = extractedUniqueFeatureSets;
        this.extractedFeatures = extractedFeatures;
        this.logOutput = logOutput;
    }

    public List<Feature> getExtractedFeatures() {
        return extractedFeatures;
    }

    public Set<FeatureSetControl> getExtractedUniqueFeatureSetControls() {
        return extractedUniqueFeatureSets.stream()
                .map(FeatureSetControl::new)
                .collect(Collectors.toSet());
    }

    public String getLogOutput() {
        return logOutput;
    }
}
