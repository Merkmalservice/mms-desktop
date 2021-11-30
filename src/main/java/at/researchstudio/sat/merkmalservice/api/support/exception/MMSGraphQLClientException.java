package at.researchstudio.sat.merkmalservice.api.support.exception;

import at.researchstudio.sat.merkmalservice.support.exception.MMSException;

public class MMSGraphQLClientException extends MMSException {
    public MMSGraphQLClientException() {}

    public MMSGraphQLClientException(String message) {
        super(message);
    }

    public MMSGraphQLClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public MMSGraphQLClientException(Throwable cause) {
        super(cause);
    }

    public MMSGraphQLClientException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
