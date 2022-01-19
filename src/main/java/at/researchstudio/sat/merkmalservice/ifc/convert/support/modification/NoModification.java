package at.researchstudio.sat.merkmalservice.ifc.convert.support.modification;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.ParsedIfcFileModification;

public class NoModification implements ParsedIfcFileModification {

    @Override
    public void accept(ParsedIfcFile parsedIfcFile) {
        // do nothing
    }
}
