package at.researchstudio.sat.merkmalservice.ifc.convert.support.unit;

import at.researchstudio.sat.merkmalservice.ifc.support.ProjectUnits;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcUnit;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QudtUnitConverter {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ProjectUnits projectUnits;

    public QudtUnitConverter(ProjectUnits projectUnits) {
        this.projectUnits = projectUnits;
    }

    /**
     * Converts the specified value between units.
     *
     * @param value value to transform
     * @param fromUnit IRI of the source unit (QUDT)
     * @param toUnit IRI of the target unit (QUDT)
     * @return the converted value
     */
    public double convert(double value, String fromUnit, String toUnit) {
        if (fromUnit.equals(toUnit)) {
            return value;
        }
        logger.info(
                "TODO: unit conversion skipped - value is not changed (should convert {} {} into {})",
                value,
                fromUnit,
                toUnit);
        return value;
    }

    public double convert(double value, IfcUnit fromUnit, String toUnit) {
        throw new UnsupportedOperationException("TODO: not implemented yet");
    }

    public double convert(double value, String fromUnit, IfcUnit toUnit) {
        throw new UnsupportedOperationException("TODO: not implemented yet");
    }

    public double convert(double value, IfcUnit fromUnit, IfcUnit toUnit) {
        throw new UnsupportedOperationException("TODO: not implemented yet");
    }
}
