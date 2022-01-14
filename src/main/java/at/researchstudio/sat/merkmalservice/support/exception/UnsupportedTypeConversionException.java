package at.researchstudio.sat.merkmalservice.support.exception;

public class UnsupportedTypeConversionException extends MMSException {
    public UnsupportedTypeConversionException() {}

    public UnsupportedTypeConversionException(String message) {
        super(message);
    }

    public UnsupportedTypeConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedTypeConversionException(Throwable cause) {
        super(cause);
    }

    public UnsupportedTypeConversionException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
