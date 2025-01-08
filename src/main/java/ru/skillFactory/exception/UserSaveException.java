package ru.skillFactory.exception;

public class UserSaveException extends RuntimeException {
    public UserSaveException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserSaveException(String message) {
        super(message);
    }
}