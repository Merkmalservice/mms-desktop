package at.researchstudio.sat.mmsdesktop.model.ifc;

public enum IfcVersion {
    IFC2X3("IFC2x3"),
    IFC4("IFC4"),
    UNKNOWN("Unknown");

    private final String ifcVersion;

    IfcVersion(String ifcVersion) {
        this.ifcVersion = ifcVersion;
    }
}
