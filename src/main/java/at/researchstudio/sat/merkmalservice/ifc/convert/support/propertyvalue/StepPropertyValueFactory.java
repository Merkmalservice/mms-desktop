package at.researchstudio.sat.merkmalservice.ifc.convert.support.propertyvalue;

import at.researchstudio.sat.merkmalservice.model.mapping.MappingExecutionValue;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.featuretype.FeatureType;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcPropertyType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class StepPropertyValueFactory {
    public static class StepValueAndType {
        private Object value;
        private String type;

        public StepValueAndType(Object value, String type) {
            this.value = value;
            this.type = type;
        }

        public Object getValue() {
            return value;
        }

        public String getType() {
            return type;
        }
    }

    public static StepValueAndType toStepPropertyValue(Feature feature, MappingExecutionValue value) {
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
                        String.format("Cannot generate STEP value for feature %s with value %s", feature, value));
    }

    private static StepValueAndType getNumericValue(Feature feature, MappingExecutionValue value) {
        unsupported(feature, value);
        return null;
    }

    private static void unsupported(Feature feature, MappingExecutionValue value) {
        throw new UnsupportedOperationException(
                        String.format("STEP value generation not yet implemented for feature %s with value %s",
                                        feature, value));
    }

    private static StepValueAndType getEnumerationValue(Feature feature, MappingExecutionValue value) {
        Object v = value.getValue();
        if (value.getStringValue().isPresent()) {
            return new  StepValueAndType(value.getStringValue().get(), IfcPropertyType.LABEL.getStepTypeName());
        }
        unsupported(feature, value);
        return null;
    }

    private static StepValueAndType getReferenceValue(Feature feature, MappingExecutionValue value) {
        Optional<String> v = value.getStringValue();
        String s = v.orElseThrow(supplyWrongValueTypeException("ReferenceValue", "String", value));
        return new StepValueAndType(s, IfcPropertyType.IDENTIFIER.getStepTypeName());
    }

    @NotNull private static Supplier<IllegalArgumentException> supplyWrongValueTypeException(
                    String featureType, String expectedValueType, MappingExecutionValue value) {
        return () -> new IllegalArgumentException(
                        String.format("When generating a %s feature, Action's value is expected to be a %s, but %s was provided",
                                        featureType, expectedValueType, value));
    }

    private static StepValueAndType getBooleanValue(Feature feature, MappingExecutionValue value) {
        Optional<Boolean> v = value.getBooleanValue();
        Boolean b = v.orElseThrow(supplyWrongValueTypeException("StringValue", "String", value));
        return new StepValueAndType(b, IfcPropertyType.BOOL.getStepTypeName());
    }

    private static StepValueAndType getStringValue(Feature feature, MappingExecutionValue value) {
        Optional<String> v = value.getStringValue();
        String s = v.orElseThrow(supplyWrongValueTypeException("StringValue", "String", value));
        String type = s.length() > 255 ?
                        IfcPropertyType.TEXT.getStepTypeName() :
                        IfcPropertyType.LABEL.getStepTypeName();
        return new StepValueAndType(s, type);
    }
}
