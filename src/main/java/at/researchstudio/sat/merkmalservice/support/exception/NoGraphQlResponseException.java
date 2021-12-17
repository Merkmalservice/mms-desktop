package at.researchstudio.sat.merkmalservice.support.exception;

public class NoGraphQlResponseException extends MMSException {
    public NoGraphQlResponseException() {}

    public NoGraphQlResponseException(String message) {
        super(message);
    }

    public NoGraphQlResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoGraphQlResponseException(Throwable cause) {
        super(cause);
    }

    public NoGraphQlResponseException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
