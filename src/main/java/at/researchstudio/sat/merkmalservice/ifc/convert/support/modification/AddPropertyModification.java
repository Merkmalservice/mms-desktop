package at.researchstudio.sat.merkmalservice.ifc.convert.support.modification;

import static at.researchstudio.sat.merkmalservice.ifc.convert.support.ConversionRuleUtils.getEffectiveActionValue;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.model.mapping.MappingExecutionValue;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature;

public class AddPropertyModification<T extends IfcLine> extends ElementModification<T> {

    private String propertySetName;
    private Feature feature;
    private MappingExecutionValue value;
    private MappingExecutionValue effectiveValue;

    public AddPropertyModification(
            T element, Feature feature, MappingExecutionValue value, String propertySetName) {
        super(element);
        this.feature = feature;
        this.propertySetName = propertySetName;
        this.value = value;
        this.effectiveValue = getEffectiveActionValue(feature, value);
    }

    @Override
    protected void modify(T element, ParsedIfcFile ifcModel) {
        ifcModel.addProperty(element, feature, effectiveValue, propertySetName);
    }
}
