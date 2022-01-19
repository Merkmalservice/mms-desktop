package at.researchstudio.sat.merkmalservice.ifc.convert.support.rule;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.ConversionRule;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.ifc.model.element.IfcBuiltElementLine;
import at.researchstudio.sat.merkmalservice.ifc.support.IfcLinePredicates;
import java.util.Set;
import java.util.function.Predicate;

public class IfcElementConversionRule extends DelegatingConversionRule {

    Predicate<IfcLine> predicate = IfcLinePredicates.isIfcLineType(IfcBuiltElementLine.class);

    public IfcElementConversionRule(ConversionRule delegate) {
        super(delegate);
    }

    @Override
    public boolean appliesTo(IfcLine line, ParsedIfcFile ifcModel) {
        return predicate.test(line) && getDelegate().appliesTo(line, ifcModel);
    }

    @Override
    public Set<Class<? extends IfcLine>> getIfcTypeRestrictions() {
        return Set.of(IfcBuiltElementLine.class);
    }

    @Override
    public String toString() {
        // FIXME: Simple way to get Name of Rule (for now)
        return getDelegate().toString();
    }
}
