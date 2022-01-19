package at.researchstudio.sat.merkmalservice.ifc.convert.support.modification;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.ParsedIfcFileModification;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;

public abstract class ElementModification<T extends IfcLine> implements ParsedIfcFileModification {
    private T element;

    public ElementModification(T element) {
        this.element = element;
    }

    @Override
    public final void accept(ParsedIfcFile parsedIfcFile) {
        modify(element, parsedIfcFile);
    }

    protected abstract void modify(T element, ParsedIfcFile ifcModel);
}
