package at.researchstudio.sat.mmsdesktop.model.ifc;

import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitType;
import at.researchstudio.sat.mmsdesktop.util.Utils;
import java.lang.invoke.MethodHandles;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IfcUnit {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final IfcUnitType type;

    public IfcUnit(Resource type) {
        this.type =
                Utils.executeOrDefaultOnException(
                        () -> IfcUnitType.fromString(type.getURI()),
                        IfcUnitType.UNKNOWN,
                        NullPointerException.class,
                        IllegalArgumentException.class);
    }

    public IfcUnit(String type) {
        IfcUnitType tempType = IfcUnitType.UNKNOWN;
        try {
            tempType = IfcUnitType.fromString(type);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
        }

        this.type = tempType;
    }

    public IfcUnit(IfcUnitType type) {
        this.type = type;
    }

    public IfcUnitType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "IfcUnit{" + "type=" + type + '}';
    }
}
