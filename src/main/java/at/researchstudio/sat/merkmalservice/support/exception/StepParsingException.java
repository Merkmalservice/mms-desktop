package at.researchstudio.sat.merkmalservice.support.exception;

public class StepParsingException extends MMSException {
    public StepParsingException() {}

    public StepParsingException(String message) {
        super(message);
    }

    public StepParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public StepParsingException(Throwable cause) {
        super(cause);
    }

    public StepParsingException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
