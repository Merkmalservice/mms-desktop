package at.researchstudio.sat.mmsdesktop.model.task;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.model.PropertySet;
import at.researchstudio.sat.mmsdesktop.gui.component.featureset.FeatureSetBox;
import at.researchstudio.sat.mmsdesktop.model.helper.FeatureSet;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// TODO: REFACTOR THIS -> ExtractResult is based upon ALL files, this might be confusing somehow
public class ExtractResult implements Serializable {
    private final Set<FeatureSet> extractedUniqueFeatureSets;
    private final List<Feature> extractedFeatures;
    private final List<PropertySet> extractedPropertySets;
    private final String logOutput;

    public ExtractResult(
            List<Feature> extractedFeatures,
            Set<FeatureSet> extractedUniqueFeatureSets,
            List<PropertySet> extractedPropertySets,
            String logOutput) {
        this.extractedUniqueFeatureSets = extractedUniqueFeatureSets;
        this.extractedFeatures = extractedFeatures;
        this.extractedPropertySets = extractedPropertySets;
        this.logOutput = logOutput;
    }

    public List<Feature> getExtractedFeatures() {
        return extractedFeatures;
    }

    public List<PropertySet> getExtractedPropertySets() {
        return extractedPropertySets;
    }

    public Set<FeatureSetBox> getExtractedUniqueFeatureSetControls() {
        return extractedUniqueFeatureSets.stream()
                .map(FeatureSetBox::new)
                .collect(Collectors.toSet());
    }

    public String getLogOutput() {
        return logOutput;
    }
}
