package at.researchstudio.sat.mmsdesktop.vocab.qudt;

import at.researchstudio.sat.mmsdesktop.model.ifc.IfcProperty;
import at.researchstudio.sat.mmsdesktop.model.ifc.vocab.IfcUnitMeasurePrefix;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class QudtUnit {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String METRE = "http://qudt.org/vocab/unit/M";
    public static final String CENTIMETRE = "http://qudt.org/vocab/unit/CentiM";
    public static final String DECIMETRE = "http://qudt.org/vocab/unit/DeciM";

    public static final String SQUARE_METRE = "http://qudt.org/vocab/unit/M2";
    public static final String CUBIC_METRE = "http://qudt.org/vocab/unit/M3";
    public static final String GRAM = "http://qudt.org/vocab/unit/GM";
    public static final String SECOND = "http://qudt.org/vocab/unit/SEC";
    public static final String HERTZ = "http://qudt.org/vocab/unit/HZ";
    public static final String DEGREE_CELSIUS = "http://qudt.org/vocab/unit/DEG_C";
    public static final String AMPERE = "http://qudt.org/vocab/unit/A";
    public static final String VOLT = "http://qudt.org/vocab/unit/V";
    public static final String WATT = "http://qudt.org/vocab/unit/W";
    public static final String NEWTON = "http://qudt.org/vocab/unit/N";
    public static final String LUX = "http://qudt.org/vocab/unit/LUX";
    public static final String LUMEN = "http://qudt.org/vocab/unit/LM";
    public static final String CANDELA = "http://qudt.org/vocab/unit/CD";
    public static final String PASCAL = "http://qudt.org/vocab/unit/PA";
    public static final String UNITLESS = "http://qudt.org/vocab/unit/UNITLESS";

    public static String extractUnitFromProperty(IfcProperty property) {
        switch (property.getMeasure()) {
            case METRE:
                switch (property.getMeasurePrefix()) {
                    case DECI:
                        return DECIMETRE;
                    case CENTI:
                        return CENTIMETRE;
                    case NONE:
                        return METRE;
                }
            case SQUARE_METRE:
                if (IfcUnitMeasurePrefix.NONE.equals(property.getMeasurePrefix())) {
                    return SQUARE_METRE;
                }
            case CUBIC_METRE:
                if (IfcUnitMeasurePrefix.NONE.equals(property.getMeasurePrefix())) {
                    return CUBIC_METRE;
                }
            case GRAM:
                if (IfcUnitMeasurePrefix.NONE.equals(property.getMeasurePrefix())) {
                    return GRAM;
                }
            case SECOND:
                if (IfcUnitMeasurePrefix.NONE.equals(property.getMeasurePrefix())) {
                    return SECOND;
                }
            case HERTZ:
                if (IfcUnitMeasurePrefix.NONE.equals(property.getMeasurePrefix())) {
                    return HERTZ;
                }
            case DEGREE_CELSIUS:
                if (IfcUnitMeasurePrefix.NONE.equals(property.getMeasurePrefix())) {
                    return DEGREE_CELSIUS;
                }
            case AMPERE:
                if (IfcUnitMeasurePrefix.NONE.equals(property.getMeasurePrefix())) {
                    return AMPERE;
                }
            case VOLT:
                if (IfcUnitMeasurePrefix.NONE.equals(property.getMeasurePrefix())) {
                    return VOLT;
                }
            case WATT:
                if (IfcUnitMeasurePrefix.NONE.equals(property.getMeasurePrefix())) {
                    return WATT;
                }
            case NEWTON:
                if (IfcUnitMeasurePrefix.NONE.equals(property.getMeasurePrefix())) {
                    return NEWTON;
                }
            case LUX:
                if (IfcUnitMeasurePrefix.NONE.equals(property.getMeasurePrefix())) {
                    return LUX;
                }
            case LUMEN:
                if (IfcUnitMeasurePrefix.NONE.equals(property.getMeasurePrefix())) {
                    return LUMEN;
                }
            case CANDELA:
                if (IfcUnitMeasurePrefix.NONE.equals(property.getMeasurePrefix())) {
                    return CANDELA;
                }
            case PASCAL:
                if (IfcUnitMeasurePrefix.NONE.equals(property.getMeasurePrefix())) {
                    return PASCAL;
                }
        }

        logger.error(
                "Could not find QudtUnit for ifcProperty: "
                        + property
                        + ", returning UNITLESS("
                        + UNITLESS
                        + ")");
        return UNITLESS;
    }
}
