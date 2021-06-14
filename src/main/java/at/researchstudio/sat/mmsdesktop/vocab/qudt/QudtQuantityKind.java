package at.researchstudio.sat.mmsdesktop.vocab.qudt;

public abstract class QudtQuantityKind {
    public static final String AREA = "http://qudt.org/vocab/quantitykind/Area";
    public static final String VOLUME = "http://qudt.org/vocab/quantitykind/Volume";
    public static final String LENGTH = "http://qudt.org/vocab/quantitykind/Length";
    public static final String WIDTH = "http://qudt.org/vocab/quantitykind/Width";
    public static final String HEIGHT = "http://qudt.org/vocab/quantitykind/Height";
    public static final String RADIUS = "http://qudt.org/vocab/quantitykind/Radius";
    public static final String DIAMETER = "http://qudt.org/vocab/quantitykind/Diameter";
    public static final String DEPTH = "http://qudt.org/vocab/quantitykind/Depth";
    public static final String THICKNESS = "http://qudt.org/vocab/quantitykind/Thickness";
    public static final String DIMENSIONLESS = "http://qudt.org/vocab/quantitykind/Dimensionless";

    public static String getQuantityKindLengthBasedOnName(String featureName) {
        //TODO: This is really simple and probably not going to be too useful, but it will do the trick for now
        String lowerCaseName = featureName.toLowerCase();

        if(lowerCaseName.contains("höhe")) {
            return HEIGHT;
        }
        if(lowerCaseName.contains("länge")) {
            return LENGTH;
        }
        if(lowerCaseName.contains("breite")) {
            return WIDTH;
        }
        if(lowerCaseName.contains("durchmesser")) {
            return DIAMETER;
        }
        if(lowerCaseName.contains("radius")) {
            return RADIUS;
        }
        if(lowerCaseName.contains("tiefe")) {
            return DEPTH;
        }
        if(lowerCaseName.contains("dicke") || lowerCaseName.contains("stärke")) {
            return THICKNESS;
        }
        return DIMENSIONLESS;
    }
}
