package at.researchstudio.sat.mmsdesktop.model.ifc.vocab;

import org.apache.jena.rdf.model.Resource;

public enum IfcUnitMeasure {
  METRE("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#METRE","http://standards.buildingsmart.org/IFC/DEV/IFC4/ADD1/OWL#METRE", "METRE"),
  SQUARE_METRE(
      "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#SQUARE_METRE", "http://standards.buildingsmart.org/IFC/DEV/IFC4/ADD1/OWL#SQUARE_METRE", "SQUARE_METRE"),
  CUBIC_METRE(
      "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#CUBIC_METRE", "http://standards.buildingsmart.org/IFC/DEV/IFC4/ADD1/OWL#CUBIC_METRE", "CUBIC_METRE"),
  GRAM("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#GRAM", "http://standards.buildingsmart.org/IFC/DEV/IFC4/ADD1/OWL#GRAM", "GRAM"),
  SECOND("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#SECOND", "SECOND"),
  HERTZ("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#HERTZ", "HERTZ"),
  DEGREE_CELSIUS(
      "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#DEGREE_CELSIUS", "http://standards.buildingsmart.org/IFC/DEV/IFC4/ADD1/OWL#DEGREE_CELSIUS", "DEGREE_CELSIUS"),
  AMPERE("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#AMPERE", "AMPERE"),
  VOLT("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#VOLT", "VOLT"),
  WATT("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#WATT", "WATT"),
  NEWTON("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#NEWTON", "NEWTON"),
  LUX("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#LUX", "LUX"),
  LUMEN("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#LUMEN", "http://standards.buildingsmart.org/IFC/DEV/IFC4/ADD1/OWL#LUMEN", "LUMEN"),
  KELVIN("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#KELVIN", "KELVIN"),
  RADIAN("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#RADIAN", "RADIAN"),
  CANDELA("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#CANDELA", "CANDELA"),
  PASCAL("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#PASCAL", "PASCAL"),
  JOULE("http://standards.buildingsmart.org/IFC/DEV/IFC4/ADD1/OWL#JOULE", "JOULE"),
  STERIDIAN("http://standards.buildingsmart.org/IFC/DEV/IFC4/ADD1/OWL#STERADIAN", "STERIDIAN"),
  UNKNOWN();

  // declaring private variable for getting values
  private final String[] measureUris;

  IfcUnitMeasure(String... measureUris) {
    this.measureUris = measureUris;
  }

  public static IfcUnitMeasure fromResource(Resource unitMeasure) throws IllegalArgumentException {
    return fromString(unitMeasure.getURI());
  }

  public static IfcUnitMeasure fromString(String unitMeasure) throws IllegalArgumentException {
    for (IfcUnitMeasure type : IfcUnitMeasure.values()) {
      for (String enumUri : type.measureUris) {
        if (enumUri.equals(unitMeasure)) {
          return type;
        }
      }
    }
    throw new IllegalArgumentException("No enum constant for value: " + unitMeasure);
  }
}
