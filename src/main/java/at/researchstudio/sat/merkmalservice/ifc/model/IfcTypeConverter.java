package at.researchstudio.sat.merkmalservice.ifc.model;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class IfcTypeConverter {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static <F extends IfcLine, T extends IfcLine> Optional<T> castIfPossible(
            F ifcLine, Class<T> toType, String reasonForCast) {
        Objects.requireNonNull(ifcLine);
        Objects.requireNonNull(toType);
        Objects.requireNonNull(reasonForCast);
        if (toType.isAssignableFrom(ifcLine.getClass())) {
            return Optional.of((T) ifcLine);
        }
        logger.info(
                "Failed cast from {} (ifc type: {}) to {}, trying to {}",
                new Object[] {
                    ifcLine.getClass().getSimpleName(),
                    ifcLine.getType(),
                    toType.getSimpleName(),
                    reasonForCast
                });
        return Optional.empty();
    }
}
