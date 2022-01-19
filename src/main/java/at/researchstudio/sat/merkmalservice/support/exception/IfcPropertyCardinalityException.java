package at.researchstudio.sat.merkmalservice.support.exception;

public class IfcPropertyCardinalityException extends MMSException {
    public IfcPropertyCardinalityException() {}

    public IfcPropertyCardinalityException(String message) {
        super(message);
    }

    public IfcPropertyCardinalityException(String message, Throwable cause) {
        super(message, cause);
    }

    public IfcPropertyCardinalityException(Throwable cause) {
        super(cause);
    }

    public IfcPropertyCardinalityException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
