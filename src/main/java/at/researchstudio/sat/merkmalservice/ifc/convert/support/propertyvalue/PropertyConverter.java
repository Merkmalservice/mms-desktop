package at.researchstudio.sat.merkmalservice.ifc.convert.support.propertyvalue;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.change.HighlevelChangeBuilder;

public interface PropertyConverter {
    StepValueAndTypeAndIfcUnit convert(
            StepValueAndTypeAndIfcUnit stepValueAndTypeAndIfcUnit,
            ParsedIfcFile parsedIfcFile,
            HighlevelChangeBuilder changeBuilder);
}
