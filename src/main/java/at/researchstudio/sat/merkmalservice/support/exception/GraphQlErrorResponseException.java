package at.researchstudio.sat.merkmalservice.support.exception;

public class GraphQlErrorResponseException extends MMSException {
    public GraphQlErrorResponseException() {}

    public GraphQlErrorResponseException(String message) {
        super(message);
    }

    public GraphQlErrorResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public GraphQlErrorResponseException(Throwable cause) {
        super(cause);
    }

    public GraphQlErrorResponseException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
