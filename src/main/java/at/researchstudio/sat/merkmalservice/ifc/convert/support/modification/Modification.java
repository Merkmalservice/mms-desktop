package at.researchstudio.sat.merkmalservice.ifc.convert.support.modification;

import at.researchstudio.sat.merkmalservice.ifc.convert.ParsedIfcFileModification;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.ifc.support.IfcLinePredicates;
import at.researchstudio.sat.merkmalservice.model.mapping.MappingExecutionValue;
import at.researchstudio.sat.merkmalservice.model.mapping.action.convert.ExtractionSource;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature;

public abstract class Modification {

    public static <T extends IfcLine> ParsedIfcFileModification removePropertyWithName(
            Object modificationSource, String propertyName, T fromElement) {
        return new RemovePropertyOrQuantityModification<>(
                modificationSource,
                IfcLinePredicates.isPropertyWithName(propertyName)
                        .or(IfcLinePredicates.isQuantityWithName(propertyName))
                        .or(IfcLinePredicates.isEnumValueWithName(propertyName)),
                fromElement);
    }

    public static <T extends IfcLine> ParsedIfcFileModification removePropertyWithMatchingName(
            Object modificationSource, String regex, T fromElement) {
        return new RemovePropertyOrQuantityModification<>(
                modificationSource,
                IfcLinePredicates.isPropertyWithNameMatching(regex)
                        .or((IfcLinePredicates.isQuantityWithNameMatching(regex)))
                        .or(IfcLinePredicates.isEnumValueWithNameMatching(regex)),
                fromElement);
    }

    public static <T extends IfcLine> ParsedIfcFileModification addProperty(
            Object modificationSource,
            Feature feature,
            MappingExecutionValue value,
            String propertySetName,
            T toElement) {
        return new AddPropertyModification<>(
                modificationSource, toElement, feature, value, propertySetName);
    }

    public static <T extends IfcLine> ParsedIfcFileModification convertProperty(
            Object modificationSource,
            Feature inputFeature,
            Feature outputFeature,
            String propertySetName,
            T element) {
        return new ConvertPropertyModification<T>(
                modificationSource, element, inputFeature, outputFeature, propertySetName);
    }

    public static <T extends IfcLine> ParsedIfcFileModification extractValueIntoProperty(
            Object modificationSource,
            ExtractionSource source,
            Feature outputFeature,
            String propertySetName,
            T element) {
        return new ExtractValueIntoPropertyModification<T>(
                modificationSource, element, source, outputFeature, propertySetName);
    }
}
