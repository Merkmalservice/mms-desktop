package at.researchstudio.sat.mmsdesktop.model.ifc;

public class IfcLine {
    private String line;
    private String id;

    public IfcLine(String line) {
        this.line = line;
        if (line.startsWith("#")) {
            id = line.split("=")[0];
        }
    }

    public String getLine() {
        return line;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return line;
    }
}
