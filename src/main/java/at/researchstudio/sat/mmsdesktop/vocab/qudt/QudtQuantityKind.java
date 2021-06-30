package at.researchstudio.sat.mmsdesktop.vocab.qudt;

import at.researchstudio.sat.mmsdesktop.model.ifc.IfcProperty;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class QudtQuantityKind {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String AREA = "http://qudt.org/vocab/quantitykind/Area";
    public static final String ANGLE = "http://qudt.org/vocab/quantitykind/Angle";
    public static final String VOLUME = "http://qudt.org/vocab/quantitykind/Volume";
    public static final String LENGTH = "http://qudt.org/vocab/quantitykind/Length";
    public static final String WIDTH = "http://qudt.org/vocab/quantitykind/Width";
    public static final String HEIGHT = "http://qudt.org/vocab/quantitykind/Height";
    public static final String RADIUS = "http://qudt.org/vocab/quantitykind/Radius";
    public static final String DIAMETER = "http://qudt.org/vocab/quantitykind/Diameter";
    public static final String DEPTH = "http://qudt.org/vocab/quantitykind/Depth";
    public static final String THICKNESS = "http://qudt.org/vocab/quantitykind/Thickness";
    public static final String DIMENSIONLESS = "http://qudt.org/vocab/quantitykind/Dimensionless";

    public static String extractQuantityKindFromPropertyName(IfcProperty property) {
        // TODO: This is really simple and probably not going to be too useful,
        // but it will do the trick
        // for now
        String lowerCaseName = property.getName().toLowerCase();

        if (lowerCaseName.contains("höhe") || lowerCaseName.contains("height")) {
            return HEIGHT;
        }
        if (lowerCaseName.contains("länge") || lowerCaseName.contains("length")) {
            return LENGTH;
        }
        if (lowerCaseName.contains("breite") || lowerCaseName.contains("width")) {
            return WIDTH;
        }
        if (lowerCaseName.contains("durchmesser")) {
            return DIAMETER;
        }
        if (lowerCaseName.contains("radius")) {
            return RADIUS;
        }
        if (lowerCaseName.contains("fläche") || lowerCaseName.contains("area")) {
            return AREA;
        }
        if (lowerCaseName.contains("tiefe") || lowerCaseName.contains("depth")) {
            return DEPTH;
        }
        if (lowerCaseName.contains("dicke") || lowerCaseName.contains("stärke")) {
            return THICKNESS;
        }

        logger.error(
                "Could not find QudtQuantityKind for ifcProperty: "
                        + property
                        + ", returning DIMENSIONLESS("
                        + DIMENSIONLESS
                        + ")");
        return DIMENSIONLESS;
    }
}
