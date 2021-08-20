package at.researchstudio.sat.mmsdesktop.model.ifc;

import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;

public class IfcDerivedUnit extends IfcUnit {
    private Map<IfcSIUnit, Integer> derivedUnitElements;

    private String userDefinedLabel;

    public IfcDerivedUnit(Resource type) {
        super(type);
    }

    public IfcDerivedUnit(Resource type, Literal userDefinedLabel) {
        this(type);
        if (Objects.nonNull(userDefinedLabel)) {
            this.userDefinedLabel = userDefinedLabel.getString();
        }
    }

    public IfcDerivedUnit(IfcUnitType type) {
        super(type);
    }

    public IfcDerivedUnit(IfcUnitType type, Map<IfcSIUnit, Integer> derivedUnitElements) {
        this(type);
        this.derivedUnitElements = derivedUnitElements;
    }

    public String getUserDefinedLabel() {
        return userDefinedLabel;
    }

    public void setUserDefinedLabel(String userDefinedLabel) {
        this.userDefinedLabel = userDefinedLabel;
    }

    public Map<IfcSIUnit, Integer> getDerivedUnitElements() {
        return derivedUnitElements;
    }

    public void addDerivedUnitElement(IfcSIUnit unitElement, int exponent) {
        if (derivedUnitElements == null) {
            derivedUnitElements = new HashMap<>();
        }
        derivedUnitElements.put(unitElement, exponent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IfcDerivedUnit that = (IfcDerivedUnit) o;
        return getType() == that.getType()
                && Objects.equals(userDefinedLabel, that.userDefinedLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), userDefinedLabel);
    }

    @Override
    public String toString() {
        return "IfcDerivedUnit{"
                + "derivedUnitElements="
                + derivedUnitElements
                + ", userDefinedLabel='"
                + userDefinedLabel
                + '\''
                + '}';
    }
}
