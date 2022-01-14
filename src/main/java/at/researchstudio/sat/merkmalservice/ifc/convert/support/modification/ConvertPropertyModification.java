package at.researchstudio.sat.merkmalservice.ifc.convert.support.modification;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.propertyvalue.FeatureBasedPropertyConverter;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature;

public class ConvertPropertyModification<T extends IfcLine> extends ElementModification<T> {

    private String propertySetName;
    private Feature inputFeature;
    private Feature outputFeature;
    private boolean deleteInputProperty = true;

    public ConvertPropertyModification(
            T element, Feature inputFeature, Feature outputFeature, String propertySetName) {
        super(element);
        this.propertySetName = propertySetName;
        this.inputFeature = inputFeature;
        this.outputFeature = outputFeature;
    }

    @Override
    protected void modify(T element, ParsedIfcFile ifcModel) {
        FeatureBasedPropertyConverter converter =
                new FeatureBasedPropertyConverter(inputFeature, outputFeature);
        ifcModel.convertProperty(
                element,
                inputFeature,
                outputFeature,
                propertySetName,
                converter,
                deleteInputProperty);
    }
}
