package at.researchstudio.sat.mmsdesktop.vocab.qudt;

public abstract class QudtQuantityKind {
    public static final String AREA = "http://qudt.org/vocab/quantitykind/Area";
    public static final String ANGLE = "http://qudt.org/vocab/quantitykind/Angle";
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
        // TODO: This is really simple and probably not going to be too useful,
        // but it will do the trick
        // for now
        String lowerCaseName = featureName.toLowerCase();

        if (lowerCaseName.contains("höhe") || lowerCaseName.contains("height")) {
            return HEIGHT;
        }
        if (lowerCaseName.contains("länge") || lowerCaseName.contains("length")) {
            return LENGTH;
        }
        if (lowerCaseName.contains("breite") || lowerCaseName.contains("width")) {
            return WIDTH;
        }
        if (lowerCaseName.contains("durchmesser")) {
            return DIAMETER;
        }
        if (lowerCaseName.contains("radius")) {
            return RADIUS;
        }
        if (lowerCaseName.contains("fläche") || lowerCaseName.contains("area")) {
            return AREA;
        }
        if (lowerCaseName.contains("tiefe") || lowerCaseName.contains("depth")) {
            return DEPTH;
        }
        if (lowerCaseName.contains("dicke") || lowerCaseName.contains("stärke")) {
            return THICKNESS;
        }
        return DIMENSIONLESS;
    }
}
