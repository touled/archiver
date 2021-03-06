package ru.tulokhonov.arch.exceptions;

/**
 * Исключение архивации
 */
public class ArchivingException extends RuntimeException {
    public ArchivingException() {}

    public ArchivingException(String message) {
        super(message);
    }

    public ArchivingException(String message, Throwable cause) {
        super(message, cause);
    }
}
