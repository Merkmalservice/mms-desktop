package at.researchstudio.sat.merkmalservice.ifc.convert.support.propertyvalue;

import at.researchstudio.sat.merkmalservice.model.mapping.MappingExecutionValue;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.featuretype.FeatureType;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcPropertyType;

public abstract class StepPropertyValueFactory {
    public static class StepValueAndType {
        private String value;
        private String type;

        public StepValueAndType(String value, String type) {
            this.value = value;
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public String getType() {
            return type;
        }
    }

    public static StepValueAndType toStepPropertyValue(Feature feature, MappingExecutionValue value) {
        String featureType = feature.getType().getType();
        FeatureType.Types type =  FeatureType.Types.valueOf(featureType);
        switch(type){
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
        throw new IllegalArgumentException(String.format("Cannot generate STEP value for feature %s with value %s", feature, value));
    }

    private static StepValueAndType getNumericValue(Feature feature, MappingExecutionValue value) {
        unsupported(feature, value);
        return null;
    }

    private static void unsupported(Feature feature, MappingExecutionValue value) {
        throw new UnsupportedOperationException(String.format("STEP value generation not yet implemented for feature %s with value %s",
                        feature, value));
    }

    private static StepValueAndType getEnumerationValue(Feature feature, MappingExecutionValue value) {
        unsupported(feature, value);
        return null;
    }

    private static StepValueAndType getReferenceValue(Feature feature, MappingExecutionValue value) {
        unsupported(feature, value);
        return null;
    }

    private static StepValueAndType getBooleanValue(Feature feature, MappingExecutionValue value) {
        unsupported(feature, value);
        return null;
    }

    private static StepValueAndType getStringValue(Feature feature, MappingExecutionValue value) {
        String s = value.getValue();
        String type = s.length() > 255 ? IfcPropertyType.TEXT.getStepTypeName() : IfcPropertyType.LABEL.getStepTypeName();
        return new StepValueAndType(s, type);
    }
}
