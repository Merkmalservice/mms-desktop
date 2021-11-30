package at.researchstudio.sat.mmsdesktop.support.exception;

public class MMSDesktopException extends RuntimeException {
    public MMSDesktopException() {}

    public MMSDesktopException(String message) {
        super(message);
    }

    public MMSDesktopException(String message, Throwable cause) {
        super(message, cause);
    }

    public MMSDesktopException(Throwable cause) {
        super(cause);
    }

    public MMSDesktopException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
