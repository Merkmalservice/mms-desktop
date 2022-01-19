package at.researchstudio.sat.merkmalservice.support.exception;

public class ActionValueException extends MMSException {
    public ActionValueException() {}

    public ActionValueException(String message) {
        super(message);
    }

    public ActionValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public ActionValueException(Throwable cause) {
        super(cause);
    }

    public ActionValueException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
