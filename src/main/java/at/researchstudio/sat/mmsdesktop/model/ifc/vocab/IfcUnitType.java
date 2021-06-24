package at.researchstudio.sat.mmsdesktop.model.ifc.vocab;

import org.apache.jena.rdf.model.Resource;

public enum IfcUnitType {
  LENGTHUNIT("LENGTHUNIT"), AREAUNIT("AREAUNIT"), VOLUMEUNIT("VOLUMEUNIT"), MASSUNIT(
      "MASSUNIT"), TIMEUNIT("TIMEUNIT"), FREQUENCYUNIT("FREQUENCYUNIT"), PLANEANGLEUNIT(
      "PLANEANGLEUNIT"), THERMODYNAMICTEMPERATUREUNIT(
      "THERMODYNAMICTEMPERATUREUNIT"), ELECTRICCURRENTUNIT(
      "ELECTRICCURRENTUNIT"), ELECTRIVOLTAGEUNIT("ELECTRICVOLTAGEUNIT"), POWERUNIT(
      "POWERUNIT"), ENERGYUNIT("ENERGYUNIT"), FORCEUNIT("FORCEUNIT"), ILLUMINANCEUNIT(
      "ILLUMINANCEUNIT"), LUMINOURFLUXUNIT("LUMINOUSFLUXUNIT"), LUMINOUSINTENSITYUNIT(
      "LUMINOUSINTENSITYUNIT"), PRESSUREUNIT("PRESSUREUNIT"), SOLIDANGLEUNIT(
      "SOLIDANGLEUNIT"), UNKNOWN();

  // declaring private variable for getting values
  private final String[] typeUris;

  IfcUnitType(String... typeUris) {
    this.typeUris = typeUris;
  }

  public static IfcUnitType fromResource(Resource unitType) throws IllegalArgumentException {
    return fromString(unitType.getURI());
  }

  public static IfcUnitType fromString(String unitType) throws IllegalArgumentException {
    for (IfcUnitType type : IfcUnitType.values()) {
      for (String enumUri : type.typeUris) {
        if (unitType.contains("#")) {
          String[] splitUnitType = unitType.split("#");
          if (splitUnitType.length == 2 && enumUri.equals(splitUnitType[1])) {
            return type;
          }
        }
        if (enumUri.equals(unitType)) {
          return type;
        }
      }
    }
    throw new IllegalArgumentException("No enum constant for value: " + unitType);
  }
}
