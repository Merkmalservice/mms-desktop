package at.researchstudio.sat.merkmalservice.ifc.convert.support.modification;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.change.HighlevelChange;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.change.HighlevelChangeBuilder;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.change.HighlevelChangeType;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.ifc.support.IfcElementValueExtractor;
import at.researchstudio.sat.merkmalservice.model.mapping.action.convert.ExtractionSource;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature;

import java.util.List;

public class ExtractValueIntoPropertyModification<T extends IfcLine>
        extends ElementModification<T> {

    private String propertySetName;
    private Feature outputFeature;
    private IfcElementValueExtractor extractor;

    public ExtractValueIntoPropertyModification(
            Object modificationSource,
            T element,
            ExtractionSource source,
            Feature outputFeature,
            String propertySetName) {
        super(modificationSource, element);
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

    @Override protected HighlevelChangeType getHighlevelChangeType() {
        return HighlevelChangeType.ADD_PROPERTY;
    }

    @Override
    protected List<HighlevelChange> modify(T element, ParsedIfcFile ifcModel) {
        HighlevelChangeBuilder changeBuilder = new HighlevelChangeBuilder(getModificationSource(), getHighlevelChangeType(), element.getId());
        ifcModel.extractElementValueIntoProperty(
                element, outputFeature, propertySetName, extractor, changeBuilder);
        return List.of(changeBuilder.build());
    }
}
