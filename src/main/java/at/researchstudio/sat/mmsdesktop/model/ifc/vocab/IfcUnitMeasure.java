package at.researchstudio.sat.mmsdesktop.model.ifc.vocab;

import org.apache.jena.rdf.model.Resource;

public enum IfcUnitMeasure {
    METRE("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#METRE"),
    SQUARE_METRE("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#SQUARE_METRE"),
    CUBIC_METRE("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#CUBIC_METRE"),
    GRAM("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#GRAM"),
    SECOND("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#SECOND"),
    HERTZ("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#HERTZ"),
    DEGREE_CELSIUS("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#DEGREE_CELSIUS"),
    AMPERE("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#AMPERE"),
    VOLT("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#VOLT"),
    WATT("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#WATT"),
    NEWTON("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#NEWTON"),
    LUX("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#LUX"),
    LUMEN("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#LUMEN"),
    CANDELA("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#CANDELA"),
    PASCAL("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#PASCAL"),
    UNKNOWN();

    // declaring private variable for getting values
    private final String[] measureUris;

    IfcUnitMeasure(String... measureUris) {
        this.measureUris = measureUris;
    }

    public static IfcUnitMeasure fromResource(Resource unitMeasure) throws IllegalArgumentException {
        String measureTypeUri = unitMeasure.getURI();

        for (IfcUnitMeasure type : IfcUnitMeasure.values()) {
            for(String enumUri : type.measureUris) {
                if(enumUri.equals(measureTypeUri)) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + measureTypeUri);
    }
}
