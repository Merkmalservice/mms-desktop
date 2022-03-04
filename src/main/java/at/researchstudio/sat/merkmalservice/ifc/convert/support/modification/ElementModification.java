package at.researchstudio.sat.merkmalservice.ifc.convert.support.modification;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.change.HighlevelChange;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.change.HighlevelChangeBuilder;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.change.HighlevelChangeType;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ElementModification<T extends IfcLine> extends BaseModification {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private T element;

    public ElementModification(Object modificationSource, T element) {
        super(modificationSource);
        this.element = element;
    }

    protected abstract HighlevelChangeType getHighlevelChangeType();

    @Override
    public final List<HighlevelChange> accept(ParsedIfcFile parsedIfcFile) {
        try {
            return modify(element, parsedIfcFile);
        } catch (Exception e) {
            HighlevelChangeBuilder changeBuilder = new HighlevelChangeBuilder(getModificationSource(),getHighlevelChangeType(), element.getId());
            changeBuilder.errorFmt(e,
                    "Error applying modification to element %d",
                    element.getId());
            return List.of(changeBuilder.build());
        }
    }

    public T getElement() {
        return element;
    }

    protected abstract List<HighlevelChange> modify(T element, ParsedIfcFile ifcModel);
}
