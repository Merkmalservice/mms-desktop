package at.researchstudio.sat.mmsdesktop.model.ifc.vocab;

import org.apache.jena.rdf.model.Resource;

public enum IfcUnitType {
  LENGTHUNIT("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#LENGTHUNIT"),
  AREAUNIT("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#AREAUNIT"),
  VOLUMEUNIT("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#VOLUMEUNIT"),
  MASSUNIT("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#MASSUNIT"),
  TIMEUNIT("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#TIMEUNIT"),
  FREQUENCYUNIT("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#FREQUENCYUNIT"),
  THERMODYNAMICTEMPERATUREUNIT(
      "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#THERMODYNAMICTEMPERATUREUNIT"),
  ELECTRICCURRENTUNIT(
      "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#ELECTRICCURRENTUNIT"),
  ELECTRIVOLTAGEUNIT(
      "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#ELECTRICVOLTAGEUNIT"),
  POWERUNIT("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#POWERUNIT"),
  FORCEUNIT("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#FORCEUNIT"),
  ILLUMINANCEUNIT("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#ILLUMINANCEUNIT"),
  LUMINOURFLUXUNIT("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#LUMINOUSFLUXUNIT"),
  LUMINOUSINTENSITYUNIT(
      "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#LUMINOUSINTENSITYUNIT"),
  PRESSUREUNIT("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#PRESSUREUNIT"),
  UNKNOWN();

  // declaring private variable for getting values
  private final String[] typeUris;

  IfcUnitType(String... typeUris) {
    this.typeUris = typeUris;
  }

  public static IfcUnitType fromResource(Resource unitType) throws IllegalArgumentException {
    String propertyTypeUri = unitType.getURI();

    for (IfcUnitType type : IfcUnitType.values()) {
      for (String enumUri : type.typeUris) {
        if (enumUri.equals(propertyTypeUri)) {
          return type;
        }
      }
    }
    throw new IllegalArgumentException("No enum constant for value: " + propertyTypeUri);
  }
}
