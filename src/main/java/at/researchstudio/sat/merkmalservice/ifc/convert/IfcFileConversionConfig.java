package at.researchstudio.sat.merkmalservice.ifc.convert;

import java.util.Collection;

public interface IfcFileConversionConfig {
    Collection<ConversionRule> getConversionRules();
}
