package at.researchstudio.sat.merkmalservice.ifc.convert.support.modification;

import at.researchstudio.sat.merkmalservice.ifc.convert.ParsedIfcFileModification;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.ifc.support.IfcLinePredicates;
import at.researchstudio.sat.merkmalservice.model.mapping.MappingExecutionValue;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature;
import java.util.List;
import java.util.stream.Stream;

public abstract class Modification {
    private static final ParsedIfcFileModification NOP = new NoModification();

    public static ParsedIfcFileModification nop() {
        return NOP;
    }

    public static ParsedIfcFileModification multiple(ParsedIfcFileModification... modifications) {
        return new ParsedIfcFileModificationGroup(modifications);
    }

    public static ParsedIfcFileModification multiple(
            List<ParsedIfcFileModification> modifications) {
        return new ParsedIfcFileModificationGroup(modifications);
    }

    public static ParsedIfcFileModification multiple(
            Stream<ParsedIfcFileModification> modifications) {
        return new ParsedIfcFileModificationGroup(modifications);
    }

    public static <T extends IfcLine> ParsedIfcFileModification removePropertyWithName(
            String propertyName, T fromElement) {
        return new RemovePropertyOrQuantityModification<>(
                IfcLinePredicates.isPropertyWithName(propertyName)
                        .or(IfcLinePredicates.isEnumValueWithName(propertyName)),
                fromElement);
    }

    public static <T extends IfcLine> ParsedIfcFileModification removePropertyWithMatchingName(
            String regex, T fromElement) {
        return new RemovePropertyOrQuantityModification<>(
                IfcLinePredicates.isPropertyWithNameMatching(regex)
                        .or(IfcLinePredicates.isEnumValueWithNameMatching(regex)),
                fromElement);
    }

    public static <T extends IfcLine> ParsedIfcFileModification removeQuantityWithName(
            String quantityName, T fromElement) {
        return new RemovePropertyOrQuantityModification<>(
                IfcLinePredicates.isQuantityWithName(quantityName), fromElement);
    }

    public static <T extends IfcLine> ParsedIfcFileModification removeQuantityWithMatchingName(
            String regex, T fromElement) {
        return new RemovePropertyOrQuantityModification<>(
                IfcLinePredicates.isQuantityWithNameMatching(regex), fromElement);
    }

    public static <T extends IfcLine> ParsedIfcFileModification addProperty(
            Feature feature, MappingExecutionValue value, String propertySetName, T toElement) {
        return new AddPropertyModification<T>(toElement, feature, value, propertySetName);
    }
}
