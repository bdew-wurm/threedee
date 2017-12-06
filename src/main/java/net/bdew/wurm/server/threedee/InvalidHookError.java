package net.bdew.wurm.server.threedee;

public class InvalidHookError extends Exception {
    public InvalidHookError(String message) {
        super(message);
    }

    public InvalidHookError(String message, Throwable exception) {
        super(message, exception);
    }
}
