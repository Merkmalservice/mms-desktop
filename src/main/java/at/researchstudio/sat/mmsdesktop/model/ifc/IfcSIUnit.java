package at.researchstudio.sat.mmsdesktop.model.ifc;

import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitMeasure;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitMeasurePrefix;
import at.researchstudio.sat.mmsdesktop.util.Utils;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IfcSIUnit extends IfcUnit {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final IfcUnitMeasure measure;
    private final IfcUnitMeasurePrefix prefix;

    public IfcSIUnit(Resource type, Resource measure, Resource prefix) {
        super(type);
        IfcUnitMeasure tempMeasure =
                Utils.executeOrDefaultOnException(
                        () -> IfcUnitMeasure.fromString(measure.getURI()),
                        IfcUnitMeasure.UNKNOWN,
                        NullPointerException.class,
                        IllegalArgumentException.class);
        IfcUnitMeasurePrefix tempPrefix =
                Utils.executeOrDefaultOnException(
                        () -> IfcUnitMeasurePrefix.fromString(prefix.getURI()),
                        IfcUnitMeasurePrefix.NONE,
                        NullPointerException.class,
                        IllegalArgumentException.class);

        this.measure = tempMeasure;
        this.prefix = tempPrefix;
    }

    public IfcSIUnit(String type, String measure, String prefix) {
        super(type);
        IfcUnitMeasure tempMeasure = IfcUnitMeasure.UNKNOWN;
        IfcUnitMeasurePrefix tempPrefix = IfcUnitMeasurePrefix.NONE;

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

        this.measure = tempMeasure;
        this.prefix = tempPrefix;
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
        IfcSIUnit ifcSIUnit = (IfcSIUnit) o;
        return getType() == ifcSIUnit.getType() && measure == ifcSIUnit.measure;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), measure);
    }

    @Override
    public String toString() {
        return "IfcSIUnit{"
                + "type="
                + getType()
                + ", measure="
                + measure
                + ", prefix="
                + prefix
                + '}';
    }
}