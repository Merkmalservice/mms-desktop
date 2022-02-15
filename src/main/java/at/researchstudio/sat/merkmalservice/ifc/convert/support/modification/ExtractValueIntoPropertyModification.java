package at.researchstudio.sat.merkmalservice.ifc.convert.support.modification;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.ifc.support.IfcElementValueExtractor;
import at.researchstudio.sat.merkmalservice.model.mapping.action.convert.ExtractionSource;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature;

public class ExtractValueIntoPropertyModification<T extends IfcLine>
        extends ElementModification<T> {

    private String propertySetName;
    private Feature outputFeature;
    private IfcElementValueExtractor extractor;

    public ExtractValueIntoPropertyModification(
            T element, ExtractionSource source, Feature outputFeature, String propertySetName) {
        super(element);
        this.propertySetName = propertySetName;
        this.outputFeature = outputFeature;
        this.extractor = IfcElementValueExtractor.ifcElementValueExtractors.get(source);
        if (this.extractor == null) {
            this.extractor = IfcElementValueExtractor.forSource(source);
        }
        if (this.extractor == null) {
            throw new IllegalArgumentException(
                    "Failed to instantiate IfcElementValueExtractor for source " + source);
        }
    }

    @Override
    protected void modify(T element, ParsedIfcFile ifcModel) {
        ifcModel.extractElementValueIntoProperty(
                element, outputFeature, propertySetName, extractor);
    }
}
