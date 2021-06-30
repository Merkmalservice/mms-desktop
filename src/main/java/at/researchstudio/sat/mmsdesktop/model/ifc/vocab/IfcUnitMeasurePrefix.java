package at.researchstudio.sat.mmsdesktop.model.ifc.vocab;

import org.apache.jena.rdf.model.Resource;

public enum IfcUnitMeasurePrefix {
    MEGA("MEGA"),
    KILO("KILO"),
    DECI("DECI"),
    CENTI("CENTI"),
    NONE();

    // declaring private variable for getting values
    private final String[] prefixUris;

    IfcUnitMeasurePrefix(String... prefixUris) {
        this.prefixUris = prefixUris;
    }

    public static IfcUnitMeasurePrefix fromResource(Resource unitMeasurePrefix)
            throws IllegalArgumentException {
        if (unitMeasurePrefix == null) return NONE;

        return fromString(unitMeasurePrefix.getURI());
    }

    public static IfcUnitMeasurePrefix fromString(String unitMeasurePrefix)
            throws IllegalArgumentException {
        if (unitMeasurePrefix == null) return NONE;

        for (IfcUnitMeasurePrefix type : IfcUnitMeasurePrefix.values()) {
            for (String enumUri : type.prefixUris) {
                if (unitMeasurePrefix.contains("#")) {
                    String[] splitUnitMeasure = unitMeasurePrefix.split("#");
                    if (splitUnitMeasure.length == 2 && enumUri.equals(splitUnitMeasure[1])) {
                        return type;
                    }
                }
                if (enumUri.equals(unitMeasurePrefix)) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException(
                "No enum IfcUnitMeasurePrefix constant for value: " + unitMeasurePrefix);
    }
}
