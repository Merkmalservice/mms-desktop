package at.researchstudio.sat.merkmalservice.ifc.convert;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.change.HighlevelChange;
import java.util.List;

public interface ParsedIfcFileModification<S> {
    /**
     * Applies this modification to the specified model, if possible.
     *
     * @param model
     * @return <code>true</code> if the model was modified, <code>false</code> otherwise.
     */
    public List<HighlevelChange> accept(ParsedIfcFile model);

    /**
     * Returns the object that generated the modification or contains an explantion as to how it was
     * generated.
     */
    public Object getModificationSource();
}
