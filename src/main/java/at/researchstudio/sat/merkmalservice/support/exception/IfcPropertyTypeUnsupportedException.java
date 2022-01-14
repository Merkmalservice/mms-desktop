package at.researchstudio.sat.merkmalservice.support.exception;

public class IfcPropertyTypeUnsupportedException extends MMSException {
    public IfcPropertyTypeUnsupportedException() {}

    public IfcPropertyTypeUnsupportedException(String message) {
        super(message);
    }

    public IfcPropertyTypeUnsupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public IfcPropertyTypeUnsupportedException(Throwable cause) {
        super(cause);
    }

    public IfcPropertyTypeUnsupportedException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
