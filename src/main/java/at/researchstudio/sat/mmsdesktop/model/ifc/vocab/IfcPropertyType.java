package at.researchstudio.sat.mmsdesktop.model.ifc.vocab;

import org.apache.jena.rdf.model.Resource;

public enum IfcPropertyType {
  TEXT("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcText", "IFCTEXT"),
  LABEL("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcLabel", "IFCLABEL"),
  LOGICAL("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcLogical", "IFCLOGICAL"),
  IDENTIFIER(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcIdentifier", "IFCIDENTIFIER"),
  BOOL("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcBoolean", "IFCBOOLEAN"),
  EXPRESS_REAL("https://w3id.org/express#REAL"),
  EXPRESS_BOOL("https://w3id.org/express#BOOLEAN"),
  EXPRESS_INTEGER("https://w3id.org/express#INTEGER"),
  INTEGER("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcInteger", "IFCINTEGER"),
  LENGTH_MEASURE(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcLengthMeasure",
                  "http://standards.buildingsmart.org/IFC/DEV/IFC4/ADD1/OWL#IfcLengthMeasure",
                  "IFCLENGTHMEASURE"),
  COUNT_MEASURE(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcCountMeasure",
                  "IFCCOUNTMEASURE"),
  POSITIVE_LENGTH_MEASURE(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcPositiveLengthMeasure",
                  "IFCPOSITIVELENGTHMEASURE"),
  NORMALISED_RATIO_MEASURE(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcNormalisedRatioMeasure",
                  "IFCNORMALISEDRATIOMEASURE"),
  PLANE_ANGLE_MEASURE(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcPlaneAngleMeasure",
                  "IFCPLANEANGLEMEASURE"),
  AREA_MEASURE(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcAreaMeasure",
                  "http://standards.buildingsmart.org/IFC/DEV/IFC4/ADD1/OWL#IfcAreaMeasure",
                  "IFCAREAMEASURE"),
  VOLUME_MEASURE(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcVolumeMeasure",
                  "http://standards.buildingsmart.org/IFC/DEV/IFC4/ADD1/OWL#IfcVolumeMeasure",
                  "IFCVOLUMEMEASURE"),
  THERMAL_TRANSMITTANCE_MEASURE(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcThermalTransmittanceMeasure",
                  "IFCTHERMALTRANSMITTANCEMEASURE"),
  TIMESTAMP(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcTimeStamp", "IFCTIMESTAMP"),
  // REF("bla"), //TODO: HOW DO REF PROPS LOOK LIKE
  // ENUM("bla"), //TODO: HOW DO ENUM PROPS LOOK LIKE
  REAL("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcReal", "IFCREAL"),
  UNKNOWN();

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
