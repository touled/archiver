package ru.tulokhonov.arch.exceptions;

/**
 * Исключение извлечения из архива
 */
public class ExtractionException extends RuntimeException {
    public ExtractionException() {}

    public ExtractionException(String message) {
        super(message);
    }

    public ExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
