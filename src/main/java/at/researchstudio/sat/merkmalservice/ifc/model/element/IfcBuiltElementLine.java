package at.researchstudio.sat.merkmalservice.ifc.model.element;

import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;

public abstract class IfcBuiltElementLine extends IfcLine {
    public static final String IDENTIFIER = "NOT-AN-IDENTIFIER-IBEL";

    public IfcBuiltElementLine(String line) {
        super(line);
    }
}
