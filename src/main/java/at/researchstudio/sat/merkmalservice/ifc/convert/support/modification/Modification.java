package at.researchstudio.sat.merkmalservice.ifc.convert.support.modification;

import at.researchstudio.sat.merkmalservice.ifc.convert.ParsedIfcFileModification;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.ifc.support.IfcLinePredicates;
import at.researchstudio.sat.merkmalservice.model.mapping.MappingExecutionValue;
import at.researchstudio.sat.merkmalservice.model.mapping.action.convert.ExtractionSource;
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
                        .or(IfcLinePredicates.isQuantityWithName(propertyName))
                        .or(IfcLinePredicates.isEnumValueWithName(propertyName)),
                fromElement);
    }

    public static <T extends IfcLine> ParsedIfcFileModification removePropertyWithMatchingName(
            String regex, T fromElement) {
        return new RemovePropertyOrQuantityModification<>(
                IfcLinePredicates.isPropertyWithNameMatching(regex)
                        .or((IfcLinePredicates.isQuantityWithNameMatching(regex)))
                        .or(IfcLinePredicates.isEnumValueWithNameMatching(regex)),
                fromElement);
    }

    public static <T extends IfcLine> ParsedIfcFileModification addProperty(
            Feature feature, MappingExecutionValue value, String propertySetName, T toElement) {
        return new AddPropertyModification<>(toElement, feature, value, propertySetName);
    }

    public static <T extends IfcLine> ParsedIfcFileModification convertProperty(
            Feature inputFeature, Feature outputFeature, String propertySetName, T element) {
        return new ConvertPropertyModification<T>(
                element, inputFeature, outputFeature, propertySetName);
    }

    public static <T extends IfcLine> ParsedIfcFileModification extractValueIntoProperty(
            ExtractionSource source, Feature outputFeature, String propertySetName, T element) {
        return new ExtractValueIntoPropertyModification<T>(
                element, source, outputFeature, propertySetName);
    }
}
