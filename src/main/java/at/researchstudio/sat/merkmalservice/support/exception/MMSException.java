package at.researchstudio.sat.merkmalservice.support.exception;

public class MMSException extends RuntimeException {
    public MMSException() {}

    public MMSException(String message) {
        super(message);
    }

    public MMSException(String message, Throwable cause) {
        super(message, cause);
    }

    public MMSException(Throwable cause) {
        super(cause);
    }

    public MMSException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
