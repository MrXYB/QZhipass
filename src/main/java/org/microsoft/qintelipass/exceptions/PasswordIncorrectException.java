package org.microsoft.qintelipass.exceptions;

public class PasswordIncorrectException extends RuntimeException{
    public PasswordIncorrectException(String message) {
        super(message);
    }
}
