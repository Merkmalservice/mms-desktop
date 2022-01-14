package at.researchstudio.sat.merkmalservice.ifc.convert.support.propertyvalue;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.unit.QudtUnitConverter;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcUnit;
import at.researchstudio.sat.merkmalservice.model.mapping.MappingExecutionValue;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.featuretype.FeatureType;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.featuretype.NumericFeatureType;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcPropertyType;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitType;
import at.researchstudio.sat.merkmalservice.vocab.qudt.QudtQuantityKind;
import at.researchstudio.sat.merkmalservice.vocab.qudt.QudtUnit;
import java.util.Optional;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public class StepPropertyValueFactory {
    private final ParsedIfcFile parsedIfcFile;
    private final QudtUnitConverter converter;

    public StepPropertyValueFactory(ParsedIfcFile parsedIfcFile, QudtUnitConverter converter) {
        this.parsedIfcFile = parsedIfcFile;
        this.converter = converter;
    }

    public StepValueAndType toStepPropertyValue(Feature feature, MappingExecutionValue value) {
        String featureType = feature.getType().getType();
        FeatureType.Types type = FeatureType.Types.valueOf(featureType);
        switch (type) {
            case StringValue:
                return getStringValue(feature, value);
            case BooleanValue:
                return getBooleanValue(feature, value);
            case ReferenceValue:
                return getReferenceValue(feature, value);
            case EnumerationValue:
                return getEnumerationValue(feature, value);
            case NumericValue:
                return getNumericValue(feature, value);
        }
        throw new IllegalArgumentException(
                String.format(
                        "Cannot generate STEP value for feature %s with value %s", feature, value));
    }

    private StepValueAndType getNumericValue(Feature feature, MappingExecutionValue value) {
        Double v =
                value.getFloatValue()
                        .orElseThrow(supplyWrongValueTypeException("Numeric", "FloatValue", value));
        NumericFeatureType numericType = (NumericFeatureType) feature.getType();
        if (QudtQuantityKind.DIMENSIONLESS.equals(numericType.getQuantityKind().getId())
                && QudtUnit.UNITLESS.equals(numericType.getUnit().getId())) {
            return new StepValueAndType(v, IfcPropertyType.REAL.getStepTypeName());
        }
        if (QudtQuantityKind.LENGTH.equals(numericType.getQuantityKind().getId())) {
            // TODO: here, we have to handle all qudt QKs that have skos:broader qudt:Length. Same
            // for all other Ifc Unit types
            IfcUnitType unitType = IfcUnitType.LENGTHUNIT;
            IfcUnit unit = parsedIfcFile.getProjectUnits().getDefaultUnitForUnitType(unitType);
            numericType.getUnit();
            return new StepValueAndType(v, IfcPropertyType.LENGTH_MEASURE.getStepTypeName());
        }

        unsupported(feature, value);
        return null;
    }

    private void unsupported(Feature feature, MappingExecutionValue value) {
        throw new UnsupportedOperationException(
                String.format(
                        "STEP value generation not yet implemented for feature %s with value %s",
                        feature, value));
    }

    private StepValueAndType getEnumerationValue(Feature feature, MappingExecutionValue value) {
        Object v = value.getValue();
        if (value.getStringValue().isPresent()) {
            return new StepValueAndType(
                    value.getStringValue().get(), IfcPropertyType.LABEL.getStepTypeName());
        }
        if (value.getFloatValue().isPresent()) {
            return new StepValueAndType(
                    value.getFloatValue().get(), IfcPropertyType.REAL.getStepTypeName());
        }
        if (value.getBooleanValue().isPresent()) {
            return new StepValueAndType(
                    value.getBooleanValue().get(), IfcPropertyType.BOOL.getStepTypeName());
        }
        if (value.getIdValue().isPresent()) {
            return new StepValueAndType(
                    value.getIdValue().get(), IfcPropertyType.IDENTIFIER.getStepTypeName());
        }
        unsupported(feature, value);
        return null;
    }

    private StepValueAndType getReferenceValue(Feature feature, MappingExecutionValue value) {
        Optional<String> v = value.getStringValue();
        String s = v.orElseThrow(supplyWrongValueTypeException("ReferenceValue", "String", value));
        return new StepValueAndType(s, IfcPropertyType.IDENTIFIER.getStepTypeName());
    }

    @NotNull
    private Supplier<IllegalArgumentException> supplyWrongValueTypeException(
            String featureType, String expectedValueType, MappingExecutionValue value) {
        return () ->
                new IllegalArgumentException(
                        String.format(
                                "When generating a %s feature, Action's value is expected to be a %s, but %s was provided",
                                featureType, expectedValueType, value));
    }

    private StepValueAndType getBooleanValue(Feature feature, MappingExecutionValue value) {
        Optional<Boolean> v = value.getBooleanValue();
        Boolean b = v.orElseThrow(supplyWrongValueTypeException("StringValue", "String", value));
        return new StepValueAndType(b, IfcPropertyType.BOOL.getStepTypeName());
    }

    private StepValueAndType getStringValue(Feature feature, MappingExecutionValue value) {
        Optional<String> v = value.getStringValue();
        String s = v.orElseThrow(supplyWrongValueTypeException("StringValue", "String", value));
        String type =
                s.length() > 255
                        ? IfcPropertyType.TEXT.getStepTypeName()
                        : IfcPropertyType.LABEL.getStepTypeName();
        return new StepValueAndType(s, type);
    }
}
