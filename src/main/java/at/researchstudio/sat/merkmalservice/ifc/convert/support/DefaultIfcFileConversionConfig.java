package at.researchstudio.sat.merkmalservice.ifc.convert.support;

import at.researchstudio.sat.merkmalservice.ifc.convert.ConversionRule;
import at.researchstudio.sat.merkmalservice.ifc.convert.IfcFileConversionConfig;
import java.util.Collection;
import java.util.Set;

public class DefaultIfcFileConversionConfig implements IfcFileConversionConfig {
    private Set<ConversionRule> ruleSet;

    public DefaultIfcFileConversionConfig(Set<ConversionRule> ruleSet) {
        this.ruleSet = ruleSet;
    }

    @Override
    public Collection<ConversionRule> getConversionRules() {
        return ruleSet;
    }
}
