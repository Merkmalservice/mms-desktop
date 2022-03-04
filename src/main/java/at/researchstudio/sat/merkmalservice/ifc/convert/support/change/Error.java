package at.researchstudio.sat.merkmalservice.ifc.convert.support.change;

import java.util.Optional;

public class Error {
    private String message;
    private Throwable exception;

    public Error(String message, Throwable exception) {
        this.message = message;
        this.exception = exception;
    }

    public Error(String message) {
        this.message = message;
    }

    public Error(Throwable exception) {
        this.exception = exception;
    }

    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

    public Optional<Throwable> getException() {
        return Optional.ofNullable(exception);
    }

    @Override public String toString() {
        return "Error{" +
                        "message='" + message + '\'' +
                        ", exception=" + exception +
                        '}';
    }
}
