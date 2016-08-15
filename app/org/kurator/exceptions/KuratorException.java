package org.kurator.exceptions;

public class KuratorException extends Exception {

    public KuratorException() {
        super();
    }

    public KuratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public KuratorException(Throwable cause) {
        super(cause);
    }

    protected KuratorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
