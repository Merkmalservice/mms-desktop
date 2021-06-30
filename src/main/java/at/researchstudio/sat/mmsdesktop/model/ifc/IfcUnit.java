package at.researchstudio.sat.mmsdesktop.model.ifc;

import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitMeasure;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitMeasurePrefix;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitType;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IfcUnit {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final IfcUnitType type;
    private final IfcUnitMeasure measure;
    private final IfcUnitMeasurePrefix prefix;

    public IfcUnit(Resource type, Resource measure, Resource prefix) {
        IfcUnitMeasure tempMeasure = IfcUnitMeasure.UNKNOWN;
        IfcUnitType tempType = IfcUnitType.UNKNOWN;
        IfcUnitMeasurePrefix tempPrefix = IfcUnitMeasurePrefix.NONE;

        try {
            tempType = IfcUnitType.fromString(type.getURI());
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
        } catch (NullPointerException e) {
            logger.error(e.getMessage());
        }

        try {
            tempMeasure = IfcUnitMeasure.fromString(measure.getURI());
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
        } catch (NullPointerException e) {
            logger.error(e.getMessage());
        }

        try {
            tempPrefix = IfcUnitMeasurePrefix.fromString(prefix.getURI());
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
        } catch (NullPointerException e) {
            logger.error(e.getMessage());
        }

        this.type = tempType;
        this.measure = tempMeasure;
        this.prefix = tempPrefix;
    }

    public IfcUnit(String type, String measure, String prefix) {
        IfcUnitMeasure tempMeasure = IfcUnitMeasure.UNKNOWN;
        IfcUnitType tempType = IfcUnitType.UNKNOWN;
        IfcUnitMeasurePrefix tempPrefix = IfcUnitMeasurePrefix.NONE;
        try {
            tempType = IfcUnitType.fromString(type);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
        }

        try {
            tempMeasure = IfcUnitMeasure.fromString(measure);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
        }

        try {
            tempPrefix = IfcUnitMeasurePrefix.fromString(prefix);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
        }

        this.type = tempType;
        this.measure = tempMeasure;
        this.prefix = tempPrefix;
    }

    public IfcUnitType getType() {
        return type;
    }

    public IfcUnitMeasure getMeasure() {
        return measure;
    }

    public IfcUnitMeasurePrefix getPrefix() {
        return prefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IfcUnit ifcUnit = (IfcUnit) o;
        return type == ifcUnit.type && measure == ifcUnit.measure;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, measure);
    }

    @Override
    public String toString() {
        return "IfcUnit{" + "type=" + type + ", measure=" + measure + ", prefix=" + prefix + '}';
    }
}
