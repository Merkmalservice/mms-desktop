package at.researchstudio.sat.merkmalservice.ifc.convert.support.modification;

import at.researchstudio.sat.merkmalservice.ifc.convert.ParsedIfcFileModification;
import java.util.Objects;

public abstract class BaseModification implements ParsedIfcFileModification {
    private final Object modificationSource;

    public BaseModification(Object modificationSource) {
        Objects.requireNonNull(modificationSource);
        this.modificationSource = modificationSource;
    }

    @Override
    public Object getModificationSource() {
        return modificationSource;
    }
}
