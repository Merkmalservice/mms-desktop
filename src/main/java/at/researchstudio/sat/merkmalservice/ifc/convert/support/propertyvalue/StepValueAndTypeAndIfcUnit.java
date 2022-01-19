package at.researchstudio.sat.merkmalservice.ifc.convert.support.propertyvalue;

import at.researchstudio.sat.merkmalservice.model.ifc.IfcUnit;

public class StepValueAndTypeAndIfcUnit {
    StepValueAndType stepValueAndType;
    IfcUnit ifcUnit;

    public StepValueAndTypeAndIfcUnit(StepValueAndType stepValueAndType, IfcUnit ifcUnit) {
        this.stepValueAndType = stepValueAndType;
        this.ifcUnit = ifcUnit;
    }

    public StepValueAndType getStepValueAndType() {
        return stepValueAndType;
    }

    public IfcUnit getIfcUnit() {
        return ifcUnit;
    }
}
