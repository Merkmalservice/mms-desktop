package at.researchstudio.sat.merkmalservice.ifc.convert.support.modification;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.ParsedIfcFileModification;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParsedIfcFileModificationGroup implements ParsedIfcFileModification {
    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
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
            try {
                modification.accept(parsedIfcFile);
            } catch (Exception e) {
                logger.warn(
                        "Error applying modification " + modification + ": " + e.getMessage(), e);
            }
        }
    }
}
