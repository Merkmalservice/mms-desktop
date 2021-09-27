package at.researchstudio.sat.mmsdesktop.model.ifc;

public class IfcLine {
    private int id;
    private final String line;
    private String stringId;

    public IfcLine(String line) {
        this.line = line;
        if (line.startsWith("#")) {
            stringId = line.split("=")[0];
            id = Integer.parseInt(stringId.substring(1));
        }
    }

    public String getLine() {
        return line;
    }

    public int getId() {
        return id;
    }

    public String getStringId() {
        return stringId;
    }

    @Override
    public String toString() {
        return line;
    }
}
