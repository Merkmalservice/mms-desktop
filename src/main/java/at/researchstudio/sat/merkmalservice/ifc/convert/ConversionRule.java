package at.researchstudio.sat.merkmalservice.ifc.convert;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface ConversionRule {
    /**
     * Returns the order of the rule. The conversion engine executes rules in ascending order. The
     * ordering of rules with same <code>order</code> is undefined.
     *
     * @return the rule's order.
     */
    int getOrder();

    /**
     * Determines whether the rule applies to the given <code>line</code> in the given <code>
     * ifcModel</code>.
     */
    boolean appliesTo(IfcLine line, ParsedIfcFile ifcModel);

    /**
     * Applies the rule to the specified <code>line</code> in the specified <code>ifcModel</code>,
     * returning a {@link ParsedIfcFileModification}, which contains the code of the actual
     * modification.
     */
    List<ParsedIfcFileModification> applyTo(IfcLine line, ParsedIfcFile ifcModel);

    /**
     * Rules are only applied to IfcLine (sub)types returned by this method.
     *
     * @return
     */
    default Set<Class<? extends IfcLine>> getIfcTypeRestrictions() {
        return Collections.emptySet();
    }
}
