package at.researchstudio.sat.mmsdesktop.model.helper;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.utils.Utils;

import java.util.ArrayList;
import java.util.Objects;

// TODO: MAYBE IMPLEMENT PropertySet/ElementQuantity, but not sure yet, also move this to the
// mms-utils project
public class FeatureSet {
    private final String name;
    private String description;
    private ArrayList<Feature> features;

    public FeatureSet(String name) {
        this.name = Utils.convertIFCStringToUtf8(name);
    }

    public FeatureSet(String name, String description) {
        this(name);
        this.description = Utils.convertIFCStringToUtf8(description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFeatures(ArrayList<Feature> features) {
        this.features = features;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<Feature> getFeatures() {
        return features;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeatureSet that = (FeatureSet) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "FeatureSet{"
                + "name='"
                + name
                + '\''
                + ", description='"
                + description
                + '\''
                + '}';
    }
}
