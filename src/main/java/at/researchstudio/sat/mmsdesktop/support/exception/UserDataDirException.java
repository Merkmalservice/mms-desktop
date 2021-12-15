package at.researchstudio.sat.mmsdesktop.support.exception;

import at.researchstudio.sat.merkmalservice.support.exception.MMSException;

public class UserDataDirException extends MMSException {
    public UserDataDirException() {}

    public UserDataDirException(String message) {
        super(message);
    }

    public UserDataDirException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserDataDirException(Throwable cause) {
        super(cause);
    }

    public UserDataDirException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
