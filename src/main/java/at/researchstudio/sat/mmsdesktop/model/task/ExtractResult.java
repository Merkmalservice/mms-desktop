package at.researchstudio.sat.mmsdesktop.model.task;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.mmsdesktop.model.helper.FeatureSet;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

// TODO: REFACTOR THIS -> ExtractResult is based upon ALL files, this might be confusing somehow
public class ExtractResult implements Serializable {
    private final Set<FeatureSet> extractedUniqueFeatureSetNames;
    private final List<Feature> extractedFeatures;
    private final String logOutput;

    public ExtractResult(
            List<Feature> extractedFeatures,
            Set<FeatureSet> extractedUniqueFeatureSetNames,
            String logOutput) {
        this.extractedUniqueFeatureSetNames = extractedUniqueFeatureSetNames;
        this.extractedFeatures = extractedFeatures;
        this.logOutput = logOutput;
    }

    public List<Feature> getExtractedFeatures() {
        return extractedFeatures;
    }

    public Set<FeatureSet> getExtractedUniqueFeatureSetNames() {
        return extractedUniqueFeatureSetNames;
    }

    public String getLogOutput() {
        return logOutput;
    }
}
