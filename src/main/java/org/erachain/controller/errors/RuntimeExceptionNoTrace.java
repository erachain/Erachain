package org.erachain.controller.errors;

import lombok.Getter;
import lombok.Setter;

public class RuntimeExceptionNoTrace extends RuntimeException {

    @Getter
    @Setter
    protected int logLevel;

    public RuntimeExceptionNoTrace() {
        super();
    }

    public RuntimeExceptionNoTrace(String message) {
        super(message);
    }

    public RuntimeExceptionNoTrace(String message, int logLevel) {
        super(message);
        this.logLevel = logLevel;
    }

    public RuntimeExceptionNoTrace(String message, Throwable cause) {
        super(message, cause);
    }

    public RuntimeExceptionNoTrace(String message, Throwable cause, int logLevel) {
        super(message, cause);
        this.logLevel = logLevel;
    }

    public RuntimeExceptionNoTrace(Throwable cause) {
        super(cause);
    }

    public RuntimeExceptionNoTrace(Throwable cause, int logLevel) {
        super(cause);
        this.logLevel = logLevel;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
