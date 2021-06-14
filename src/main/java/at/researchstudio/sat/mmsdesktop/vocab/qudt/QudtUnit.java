package at.researchstudio.sat.mmsdesktop.vocab.qudt;

import at.researchstudio.sat.mmsdesktop.model.ifc.vocab.IfcUnitMeasure;

public abstract class QudtUnit {
  public static final String METRE = "http://qudt.org/vocab/unit/M";
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

  public static String getUnitBasedOnIfcUnitMeasureLengthBasedOnName(IfcUnitMeasure measure) {
    switch (measure) {
      case METRE:
        return METRE;
      case SQUARE_METRE:
        return SQUARE_METRE;
      case CUBIC_METRE:
        return CUBIC_METRE;
      case GRAM:
        return GRAM;
      case SECOND:
        return SECOND;
      case HERTZ:
        return HERTZ;
      case DEGREE_CELSIUS:
        return DEGREE_CELSIUS;
      case AMPERE:
        return AMPERE;
      case VOLT:
        return VOLT;
      case WATT:
        return WATT;
      case NEWTON:
        return NEWTON;
      case LUX:
        return LUX;
      case LUMEN:
        return LUMEN;
      case CANDELA:
        return CANDELA;
      case PASCAL:
        return PASCAL;
      default:
        logger.error(
            "Could not find QudtUnit for ifcMeasure: "
                + measure
                + ", returning UNITLESS("
                + UNITLESS
                + ")");
        return UNITLESS;
    }
  }
}
