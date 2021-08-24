package at.researchstudio.sat.mmsdesktop.model.ifc;

public enum IfcVersion {
    IFC2X3("IFC2x3"),
    IFC4("IFC4"),
    IFC4_ADD2("IFC4_ADD2"),
    IFC4x3_RC1("IFC4x3_RC1"),
    UNKNOWN("Unknown");

    private final String ifcVersion;
    private final String ifcToRdfVersion;
    private final String ifcOntologyUri;
    private final String propertyQueryResourceString;
    private final String projectUnitQueryResourceString;
    private final String derivedUnitQueryResourceString;

    IfcVersion(String ifcVersion) {
        this.ifcVersion = ifcVersion;
        this.ifcToRdfVersion = generateIfcToRdfVersion(ifcVersion);
        this.ifcOntologyUri = generateIfcOntologyUri(ifcToRdfVersion);

        propertyQueryResourceString = "classpath:extract_properties_" + ifcToRdfVersion + ".rq";
        projectUnitQueryResourceString =
                "classpath:extract_projectunits_" + ifcToRdfVersion + ".rq";
        derivedUnitQueryResourceString =
                "classpath:extract_derivedunits_" + ifcToRdfVersion + ".rq";
    }

    private static String generateIfcToRdfVersion(String ifcVersion) {
        String lowerCaseIfcVersion = ifcVersion.toLowerCase();

        if (lowerCaseIfcVersion.contains("ifc2x3")) {
            return "IFC2X3_TC1";
        }

        if (lowerCaseIfcVersion.contains("ifc4x3")) {
            return "IFC4x3_RC1";
        }

        if (lowerCaseIfcVersion.contains("ifc4x1")) {
            return "IFC4x1";
        }

        if (lowerCaseIfcVersion.contains("ifc4")) {
            return "IFC4_ADD2_TC1";
        }

        return "UNKNOWN";
    }

    private static String generateIfcOntologyUri(String ifcToRdfVersion) {
        if (ifcToRdfVersion.equalsIgnoreCase("IFC2X3_TC1")) {
            return "http://standards.buildingsmart.org/IFC/DEV/IFC2x3/TC1/OWL";
        }

        if (ifcToRdfVersion.equalsIgnoreCase("IFC4x3_RC1")) {
            return "http://standards.buildingsmart.org/IFC/DEV/IFC4_3/RC1/OWL";
        }

        if (ifcToRdfVersion.equalsIgnoreCase("IFC4x1")) {
            return "http://standards.buildingsmart.org/IFC/DEV/IFC4_1/OWL";
        }

        if (ifcToRdfVersion.equalsIgnoreCase("IFC4_ADD2_TC1")) {
            return "http://standards.buildingsmart.org/IFC/DEV/IFC4/ADD2_TC1/OWL";
        }

        return "UNKNONW";
    }

    public String getIfcOntologyUri() {
        return ifcOntologyUri;
    }

    public String getPropertyQueryResourceString() {
        return propertyQueryResourceString;
    }

    public String getProjectUnitQueryResourceString() {
        return projectUnitQueryResourceString;
    }

    public String getDerivedUnitQueryResourceString() {
        return derivedUnitQueryResourceString;
    }
}
