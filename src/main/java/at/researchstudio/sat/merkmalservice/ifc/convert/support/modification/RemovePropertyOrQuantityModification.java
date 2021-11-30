package at.researchstudio.sat.merkmalservice.ifc.convert.support.modification;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import java.util.function.Predicate;

public class RemovePropertyOrQuantityModification<T extends IfcLine>
        extends ElementModification<T> {
    private Predicate<IfcLine> predicate;

    public RemovePropertyOrQuantityModification(Predicate<IfcLine> predicate, T element) {
        super(element);
        this.predicate = predicate;
    }

    @Override
    protected void modify(T element, ParsedIfcFile ifcModel) {
        ifcModel.removeProperty(element, predicate);
    }
}
