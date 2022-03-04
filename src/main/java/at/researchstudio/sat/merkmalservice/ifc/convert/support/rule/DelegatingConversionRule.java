package at.researchstudio.sat.merkmalservice.ifc.convert.support.rule;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.ConversionRule;
import at.researchstudio.sat.merkmalservice.ifc.convert.ParsedIfcFileModification;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import java.util.List;
import java.util.Set;

public class DelegatingConversionRule implements ConversionRule {
    private ConversionRule delegate;

    public DelegatingConversionRule(ConversionRule delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getOrder() {
        return delegate.getOrder();
    }

    @Override
    public boolean appliesTo(IfcLine line, ParsedIfcFile ifcModel) {
        return delegate.appliesTo(line, ifcModel);
    }

    @Override
    public List<ParsedIfcFileModification> applyTo(IfcLine line, ParsedIfcFile ifcModel) {
        return delegate.applyTo(line, ifcModel);
    }

    @Override
    public Set<Class<? extends IfcLine>> getIfcTypeRestrictions() {
        return delegate.getIfcTypeRestrictions();
    }

    protected ConversionRule getDelegate() {
        return delegate;
    }
}
