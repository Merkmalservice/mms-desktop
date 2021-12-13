package at.researchstudio.sat.merkmalservice.support.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class ErrorUtils {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static <T> T logThrowableMessage(Supplier<T> supplier) {
        return logThrowableMessage(supplier, Level.INFO);
    }

    public static <T> T logThrowableMessage(Supplier<T> supplier, Level level) {
        Objects.requireNonNull(level);
        Objects.requireNonNull(supplier);
        try {
            return supplier.get();
        } catch (Throwable t) {
            String message = t.getMessage();
            if (level.compareTo(Level.DEBUG) >= 0) {
                logger.debug(message);
            } else if (level == Level.INFO) {
                logger.info(message);
            } else if (level == Level.WARN) {
                logger.warn(message);
            } else {
                logger.error(message);
            }
        }
        return null;
    }
}
