package at.researchstudio.sat.merkmalservice.ifc.convert.support.modification;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.change.HighlevelChange;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.change.HighlevelChangeBuilder;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.change.HighlevelChangeType;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;

import java.util.List;
import java.util.function.Predicate;

public class RemovePropertyOrQuantityModification<T extends IfcLine>
        extends ElementModification<T> {
    private Predicate<IfcLine> predicate;

    public RemovePropertyOrQuantityModification(
            Object modificationSource, Predicate<IfcLine> predicate, T element) {
        super(modificationSource, element);
        this.predicate = predicate;
    }

    @Override protected HighlevelChangeType getHighlevelChangeType() {
        return HighlevelChangeType.DELETE_PROPERTY;
    }

    @Override
    protected List<HighlevelChange> modify(T element, ParsedIfcFile ifcModel) {
        HighlevelChangeBuilder changeBuilder = new HighlevelChangeBuilder(getModificationSource(), getHighlevelChangeType(), element.getId());
        ifcModel.removeProperty(element, predicate, changeBuilder);
        return List.of(changeBuilder.build());
    }
}
