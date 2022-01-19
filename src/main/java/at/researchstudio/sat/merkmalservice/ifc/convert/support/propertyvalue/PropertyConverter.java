package at.researchstudio.sat.merkmalservice.ifc.convert.support.propertyvalue;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;

public interface PropertyConverter {
    StepValueAndTypeAndIfcUnit convert(
            StepValueAndTypeAndIfcUnit stepValueAndType, ParsedIfcFile parsedIfcFile);
}
