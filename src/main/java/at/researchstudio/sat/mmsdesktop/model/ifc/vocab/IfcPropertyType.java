package at.researchstudio.sat.mmsdesktop.model.ifc.vocab;

import org.apache.jena.rdf.model.Resource;

public enum IfcPropertyType {
    TEXT("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcText"),
    LABEL("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcLabel"),
    LOGICAL("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcLogical"),
    IDENTIFIER("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcIdentifier"),
    BOOL("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcBoolean"),
    EXPRESS_REAL("https://w3id.org/express#REAL"),
    EXPRESS_BOOL("https://w3id.org/express#BOOLEAN"),
    EXPRESS_INTEGER("https://w3id.org/express#INTEGER"),
    INTEGER("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcInteger"),
    LENGTH_MEASURE("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcLengthMeasure"),
    POSITIVE_LENGTH_MEASURE("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcPositiveLengthMeasure"),
    NORMALISED_RATIO_MEASURE("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcNormalisedRatioMeasure"),
    PLANE_ANGLE_MEASURE("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcPlaneAngleMeasure"),
    AREA_MEASURE("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcAreaMeasure"),
    VOLUME_MEASURE("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcVolumeMeasure"),
    THERMAL_TRANSMITTANCE_MEASURE("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcThermalTransmittanceMeasure"),
    TIMESTAMP("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcTimeStamp"),
    //REF("bla"), //TODO: HOW DO REF PROPS LOOK LIKE
    //ENUM("bla"), //TODO: HOW DO ENUM PROPS LOOK LIKE
    REAL("http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL#IfcReal"),
    UNKNOWN();



    // declaring private variable for getting values
    private final String[] typeUris;

    IfcPropertyType(String... typeUris) {
        this.typeUris = typeUris;
    }

    public static IfcPropertyType fromResource(Resource propertyType) throws IllegalArgumentException {
        String propertyTypeUri = propertyType.getURI();

        for (IfcPropertyType type : IfcPropertyType.values()) {
            for(String enumUri : type.typeUris) {
                if(enumUri.equals(propertyTypeUri)) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + propertyTypeUri);
    }

    public boolean isMeasureType() {
        switch (this) {
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
            case NORMALISED_RATIO_MEASURE:
            case PLANE_ANGLE_MEASURE:
            default:
                return null;
        }
    }
}
