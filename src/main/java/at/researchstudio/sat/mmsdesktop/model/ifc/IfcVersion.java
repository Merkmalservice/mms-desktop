package at.researchstudio.sat.mmsdesktop.model.ifc;

public enum IfcVersion {
    IFC2X3("IFC2x3"), IFC4("IFC4"), IFC4_ADD2("IFC4_ADD2"), IFC4x3_RC1("IFC4x3_RC1"), UNKNOWN("Unknown");

    private final String ifcVersion;

    IfcVersion(String ifcVersion) {
        this.ifcVersion = ifcVersion;
    }
}
