package at.researchstudio.sat.mmsdesktop.model.ifc.vocab;

import org.apache.jena.rdf.model.Resource;

public enum IfcUnitType {
  LENGTHUNIT(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#LENGTHUNIT",
                  "http://standards.buildingsmart.org/IFC/DEV/IFC4/ADD1/OWL#LENGTHUNIT",
                  "LENGTHUNIT"),
  AREAUNIT(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#AREAUNIT",
                  "http://standards.buildingsmart.org/IFC/DEV/IFC4/ADD1/OWL#AREAUNIT",
                  "AREAUNIT"),
  VOLUMEUNIT(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#VOLUMEUNIT",
                  "http://standards.buildingsmart.org/IFC/DEV/IFC4/ADD1/OWL#VOLUMEUNIT",
                  "VOLUMEUNIT"),
  MASSUNIT(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#MASSUNIT",
                  "http://standards.buildingsmart.org/IFC/DEV/IFC4/ADD1/OWL#MASSUNIT",
                  "MASSUNIT"),
  TIMEUNIT("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#TIMEUNIT", "TIMEUNIT"),
  FREQUENCYUNIT(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#FREQUENCYUNIT", "FREQUENCYUNIT"),
  PLANEANGLEUNIT(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#PLANEANGLEUNIT", "PLANEANGLEUNIT"),
  THERMODYNAMICTEMPERATUREUNIT(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#THERMODYNAMICTEMPERATUREUNIT",
                  "http://standards.buildingsmart.org/IFC/DEV/IFC4/ADD1/OWL#THERMODYNAMICTEMPERATUREUNIT",
                  "THERMODYNAMICTEMPERATUREUNIT"),
  ELECTRICCURRENTUNIT(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#ELECTRICCURRENTUNIT",
                  "ELECTRICCURRENTUNIT"),
  ELECTRIVOLTAGEUNIT(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#ELECTRICVOLTAGEUNIT",
                  "ELECTRICVOLTAGEUNIT"),
  POWERUNIT("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#POWERUNIT", "POWERUNIT"),
  ENERGYUNIT("http://standards.buildingsmart.org/IFC/DEV/IFC4/ADD1/OWL#ENERGYUNIT", "ENERGYUNIT"),
  FORCEUNIT("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#FORCEUNIT", "FORCEUNIT"),
  ILLUMINANCEUNIT(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#ILLUMINANCEUNIT",
                  "ILLUMINANCEUNIT"),
  LUMINOURFLUXUNIT(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#LUMINOUSFLUXUNIT",
                  "LUMINOUSFLUXUNIT"),
  LUMINOUSINTENSITYUNIT(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#LUMINOUSINTENSITYUNIT",
                  "http://standards.buildingsmart.org/IFC/DEV/IFC4/ADD1/OWL#LUMINOUSINTENSITYUNIT",
                  "LUMINOUSINTENSITYUNIT"),
  PRESSUREUNIT(
                  "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#PRESSUREUNIT", "PRESSUREUNIT"),
  SOLIDANGLEUNIT("http://standards.buildingsmart.org/IFC/DEV/IFC4/ADD1/OWL#SOLIDANGLEUNIT"),
  UNKNOWN();

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
        if (enumUri.equals(unitType)) {
          return type;
        }
      }
    }
    throw new IllegalArgumentException("No enum constant for value: " + unitType);
  }
}
