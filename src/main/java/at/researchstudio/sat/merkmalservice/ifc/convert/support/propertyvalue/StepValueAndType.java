package at.researchstudio.sat.merkmalservice.ifc.convert.support.propertyvalue;

import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcPropertyType;

public class StepValueAndType {
    private final Object value;
    private final String type;

    public StepValueAndType(Object value, String type) {
        this.value = value;
        this.type = type;
    }

    public static StepValueAndType fromStingValue(String value, String type) {
        return new StepValueAndType(IfcPropertyType.parsePropertyValue(value, type), type);
    }

    public Object getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "StepValueAndType{" + "value=" + value + ", type='" + type + '\'' + '}';
    }
}
