package at.researchstudio.sat.merkmalservice.ifc.convert.support.modification;

import static at.researchstudio.sat.merkmalservice.ifc.convert.support.ConversionRuleUtils.getEffectiveActionValue;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.change.HighlevelChange;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.change.HighlevelChangeBuilder;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.change.HighlevelChangeType;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.model.mapping.MappingExecutionValue;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature;
import java.util.List;

public class AddPropertyModification<T extends IfcLine> extends ElementModification<T> {

    private String propertySetName;
    private Feature feature;
    private MappingExecutionValue value;
    private MappingExecutionValue effectiveValue;

    public AddPropertyModification(
            Object modificationSource,
            T element,
            Feature feature,
            MappingExecutionValue value,
            String propertySetName) {
        super(modificationSource, element);
        this.feature = feature;
        this.propertySetName = propertySetName;
        this.value = value;
        this.effectiveValue = getEffectiveActionValue(feature, value);
    }

    @Override
    protected HighlevelChangeType getHighlevelChangeType() {
        return HighlevelChangeType.ADD_PROPERTY;
    }

    @Override
    protected List<HighlevelChange> modify(T element, ParsedIfcFile ifcModel) {
        HighlevelChangeBuilder changeBuilder =
                new HighlevelChangeBuilder(
                        this.getModificationSource(), getHighlevelChangeType(), element.getId());
        ifcModel.addProperty(element, feature, effectiveValue, propertySetName, changeBuilder);
        return List.of(changeBuilder.build());
    }
}
