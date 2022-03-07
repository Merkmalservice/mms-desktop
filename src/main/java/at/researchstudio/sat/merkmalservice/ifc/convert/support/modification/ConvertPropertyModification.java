package at.researchstudio.sat.merkmalservice.ifc.convert.support.modification;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.change.HighlevelChange;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.change.HighlevelChangeBuilder;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.change.HighlevelChangeType;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.propertyvalue.FeatureBasedPropertyConverter;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature;
import java.util.List;

public class ConvertPropertyModification<T extends IfcLine> extends ElementModification<T> {

    private String propertySetName;
    private Feature inputFeature;
    private Feature outputFeature;
    private boolean deleteInputProperty = true;

    public ConvertPropertyModification(
            Object modificationSource,
            T element,
            Feature inputFeature,
            Feature outputFeature,
            String propertySetName) {
        super(modificationSource, element);
        this.propertySetName = propertySetName;
        this.inputFeature = inputFeature;
        this.outputFeature = outputFeature;
    }

    @Override
    protected HighlevelChangeType getHighlevelChangeType() {
        return HighlevelChangeType.CONVERT_PROPERTY;
    }

    @Override
    protected List<HighlevelChange> modify(T element, ParsedIfcFile ifcModel) {
        FeatureBasedPropertyConverter converter =
                new FeatureBasedPropertyConverter(inputFeature, outputFeature);
        HighlevelChangeBuilder changeBuilder =
                new HighlevelChangeBuilder(
                        getModificationSource(), getHighlevelChangeType(), element.getId());
        ifcModel.convertProperty(
                element,
                inputFeature,
                outputFeature,
                propertySetName,
                converter,
                deleteInputProperty,
                changeBuilder);
        return List.of(changeBuilder.build());
    }
}
