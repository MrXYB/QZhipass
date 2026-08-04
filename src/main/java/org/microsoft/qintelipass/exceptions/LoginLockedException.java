package org.microsoft.qintelipass.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class LoginLockedException extends ApiException {

    private final long retryAfterMinutes;

    public LoginLockedException(String message, long retryAfterMinutes) {
        super(HttpStatus.LOCKED, message);
        this.retryAfterMinutes = retryAfterMinutes;
    }
}
