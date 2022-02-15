package at.researchstudio.sat.merkmalservice.ifc.convert.support;

import at.researchstudio.sat.merkmalservice.support.exception.MMSException;

public class ConversionException extends MMSException {
    public ConversionException() {}

    public ConversionException(String message) {
        super(message);
    }

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConversionException(Throwable cause) {
        super(cause);
    }

    public ConversionException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
