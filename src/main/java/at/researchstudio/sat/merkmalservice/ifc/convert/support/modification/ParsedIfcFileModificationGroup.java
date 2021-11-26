package at.researchstudio.sat.merkmalservice.ifc.convert.support.modification;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.ParsedIfcFileModification;
import java.util.Arrays;
import java.util.List;

public class ParsedIfcFileModificationGroup implements ParsedIfcFileModification {
    private List<ParsedIfcFileModification> modifications;

    ParsedIfcFileModificationGroup(ParsedIfcFileModification... modifications) {
        this.modifications = Arrays.asList(modifications);
    }

    @Override
    public void accept(ParsedIfcFile parsedIfcFile) {
        for (ParsedIfcFileModification modification : modifications) {
            modification.accept(parsedIfcFile);
        }
    }
}
