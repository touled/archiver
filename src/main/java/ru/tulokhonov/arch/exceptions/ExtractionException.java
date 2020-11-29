package ru.tulokhonov.arch.exceptions;

public class ExtractionException extends RuntimeException {
    public ExtractionException() {
    }

    public ExtractionException(String message) {
        super(message);
    }

    public ExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
