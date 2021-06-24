package at.researchstudio.sat.mmsdesktop.model.ifc.vocab;

import org.apache.jena.rdf.model.Resource;

public enum IfcPropertyType {
  TEXT("IfcText", "IFCTEXT"), LABEL("IfcLabel", "IFCLABEL"), LOGICAL("IfcLogical",
      "IFCLOGICAL"), IDENTIFIER("IfcIdentifier", "IFCIDENTIFIER"), BOOL("IfcBoolean",
      "IFCBOOLEAN"), EXPRESS_REAL("REAL"), EXPRESS_BOOL("BOOLEAN"), EXPRESS_INTEGER(
      "INTEGER"), INTEGER("IfcInteger", "IFCINTEGER"), POSITIVE_INTEGER("IfcPositiveInteger",
      "IFCPOSITIVEINTEGER"), DIMENSION_COUNT("IfcDimensionCount",
      "IFCDIMENSIONCOUNT"), LENGTH_MEASURE("IfcLengthMeasure", "IFCLENGTHMEASURE"), COUNT_MEASURE(
      "IfcCountMeasure", "IFCCOUNTMEASURE"), POSITIVE_LENGTH_MEASURE("IfcPositiveLengthMeasure",
      "IFCPOSITIVELENGTHMEASURE"), NORMALISED_RATIO_MEASURE("IfcNormalisedRatioMeasure",
      "IFCNORMALISEDRATIOMEASURE"), PLANE_ANGLE_MEASURE("IfcPlaneAngleMeasure",
      "IFCPLANEANGLEMEASURE"), AREA_MEASURE("IfcAreaMeasure", "IFCAREAMEASURE"), VOLUME_MEASURE(
      "IfcVolumeMeasure", "IFCVOLUMEMEASURE"), THERMAL_TRANSMITTANCE_MEASURE(
      "IfcThermalTransmittanceMeasure", "IFCTHERMALTRANSMITTANCEMEASURE"), TIMESTAMP("IfcTimeStamp",
      "IFCTIMESTAMP"), // REF("bla"), //TODO: HOW DO REF PROPS LOOK LIKE
  // ENUM("bla"), //TODO: HOW DO ENUM PROPS LOOK LIKE
  REAL("IfcReal", "IFCREAL"), UNKNOWN();

  // declaring private variable for getting values
  private final String[] typeUris;

  IfcPropertyType(String... typeUris) {
    this.typeUris = typeUris;
  }

  public static IfcPropertyType fromResource(Resource propertyType)
      throws IllegalArgumentException {
    return fromString(propertyType.getURI());
  }

  public static IfcPropertyType fromString(String propertyType) throws IllegalArgumentException {
    for (IfcPropertyType type : IfcPropertyType.values()) {
      for (String enumUri : type.typeUris) {
        if (propertyType.contains("#")) {
          String[] splitPropertyType = propertyType.split("#");
          if (splitPropertyType.length == 2 && enumUri.equals(splitPropertyType[1])) {
            return type;
          }
        }
        if (enumUri.equals(propertyType)) {
          return type;
        }
      }
    }
    throw new IllegalArgumentException("No enum constant for value: " + propertyType);
  }

  public boolean isMeasureType() {
    switch (this) {
      case COUNT_MEASURE:
      case LENGTH_MEASURE:
      case POSITIVE_LENGTH_MEASURE:
      case NORMALISED_RATIO_MEASURE:
      case PLANE_ANGLE_MEASURE:
      case AREA_MEASURE:
      case VOLUME_MEASURE:
      case THERMAL_TRANSMITTANCE_MEASURE:
        return true;
      default:
        return false;
    }
  }

  public IfcUnitType getUnitType() {
    switch (this) {
      case LENGTH_MEASURE:
      case POSITIVE_LENGTH_MEASURE:
        return IfcUnitType.LENGTHUNIT;
      case AREA_MEASURE:
        return IfcUnitType.AREAUNIT;
      case VOLUME_MEASURE:
        return IfcUnitType.VOLUMEUNIT;
      case THERMAL_TRANSMITTANCE_MEASURE:
        return IfcUnitType.THERMODYNAMICTEMPERATUREUNIT;
      case PLANE_ANGLE_MEASURE:
        return IfcUnitType.PLANEANGLEUNIT;
      case NORMALISED_RATIO_MEASURE:
      default:
        return null;
    }
  }
}
