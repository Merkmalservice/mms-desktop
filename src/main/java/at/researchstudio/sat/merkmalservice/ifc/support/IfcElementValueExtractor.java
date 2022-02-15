package at.researchstudio.sat.merkmalservice.ifc.support;

import static at.researchstudio.sat.merkmalservice.model.mapping.action.convert.ExtractionSource.ELEMENT_DESCRIPTION;
import static at.researchstudio.sat.merkmalservice.model.mapping.action.convert.ExtractionSource.ELEMENT_NAME;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.propertyvalue.StepValueAndType;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.propertyvalue.StepValueAndTypeAndIfcUnit;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.ifc.model.element.IfcElementLine;
import at.researchstudio.sat.merkmalservice.model.mapping.action.convert.ExtractionSource;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcPropertyType;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public interface IfcElementValueExtractor
        extends BiFunction<ParsedIfcFile, IfcLine, Optional<StepValueAndTypeAndIfcUnit>> {
    IfcElementValueExtractor ifcElementNameExtractor =
            IfcElementValueExtractor.forSource(ELEMENT_NAME);
    IfcElementValueExtractor ifcElementDescriptionExtractor =
            IfcElementValueExtractor.forSource(ELEMENT_DESCRIPTION);
    Map<ExtractionSource, IfcElementValueExtractor> ifcElementValueExtractors =
            Map.ofEntries(
                    Map.entry(ELEMENT_NAME, ifcElementNameExtractor),
                    Map.entry(ELEMENT_DESCRIPTION, ifcElementDescriptionExtractor));

    static IfcElementValueExtractor forSource(ExtractionSource source) {
        switch (source) {
            case ELEMENT_NAME:
                return (model, line) -> {
                    if (line instanceof IfcElementLine) {
                        return Optional.ofNullable(((IfcElementLine) line).getName())
                                .map(
                                        n ->
                                                new StepValueAndTypeAndIfcUnit(
                                                        new StepValueAndType(
                                                                n,
                                                                IfcPropertyType.LABEL
                                                                        .getStepTypeName()),
                                                        null));
                    }
                    return Optional.empty();
                };
            case ELEMENT_DESCRIPTION:
                return (model, line) -> {
                    if (line instanceof IfcElementLine) {
                        return Optional.ofNullable(((IfcElementLine) line).getDescription())
                                .map(
                                        n ->
                                                new StepValueAndTypeAndIfcUnit(
                                                        new StepValueAndType(
                                                                n,
                                                                (n.length() > 255
                                                                                ? IfcPropertyType
                                                                                        .TEXT
                                                                                : IfcPropertyType
                                                                                        .LABEL)
                                                                        .getStepTypeName()),
                                                        null));
                    }
                    return Optional.empty();
                };
            default:
                throw new IllegalArgumentException(
                        "Cannot generate IfcElementValueExtractor for source " + source);
        }
    }
}
