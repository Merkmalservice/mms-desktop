package at.researchstudio.sat.merkmalservice.ifc.model;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TypeConverter {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static <F extends IfcLine, T> T castTo(F ifcLine, Class<T> toType) {
        Objects.requireNonNull(ifcLine);
        Objects.requireNonNull(toType);
        return (T) ifcLine;
    }

    public static <F extends IfcLine, T> Optional<T> castToOpt(F ifcLine, Class<T> toType) {
        Objects.requireNonNull(ifcLine);
        Objects.requireNonNull(toType);
        if (toType.isAssignableFrom(ifcLine.getClass())) {
            return Optional.of((T) ifcLine);
        }
        return Optional.empty();
    }

    public static <F extends IfcLine, T> Optional<T> castToOptAndLogFailure(
            F ifcLine, Class<T> toType, String reasonForCast) {
        Objects.requireNonNull(reasonForCast);
        Optional<T> result = castToOpt(ifcLine, toType);
        if (result.isEmpty()) {
            logger.info(
                    "Failed cast from {} (ifc type: {}) to {}, trying to {}",
                    new Object[] {
                        ifcLine.getClass().getSimpleName(),
                        ifcLine.getType(),
                        toType.getSimpleName(),
                        reasonForCast
                    });
        }
        return result;
    }
}
