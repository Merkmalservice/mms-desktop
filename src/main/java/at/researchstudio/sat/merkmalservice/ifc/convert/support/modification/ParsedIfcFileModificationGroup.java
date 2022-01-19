package at.researchstudio.sat.merkmalservice.ifc.convert.support.modification;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.ParsedIfcFileModification;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParsedIfcFileModificationGroup implements ParsedIfcFileModification {
    private List<ParsedIfcFileModification> modifications;

    ParsedIfcFileModificationGroup(ParsedIfcFileModification... modifications) {
        this.modifications = Arrays.asList(modifications);
    }

    ParsedIfcFileModificationGroup(Collection<ParsedIfcFileModification> modifications) {
        this.modifications = new ArrayList<>(modifications);
    }

    ParsedIfcFileModificationGroup(Stream<ParsedIfcFileModification> modificationsStream) {
        this.modifications = modificationsStream.collect(Collectors.toList());
    }

    @Override
    public void accept(ParsedIfcFile parsedIfcFile) {
        for (ParsedIfcFileModification modification : modifications) {
            modification.accept(parsedIfcFile);
        }
    }
}
