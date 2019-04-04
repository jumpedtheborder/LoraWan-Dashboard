package com.uniofsurrey.lorawandashboard.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("You are not authorized do perform this action");
    }
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
    public UnauthorizedException(String message) {
        super(message);
    }
    public UnauthorizedException(Throwable cause) {
        super(cause);
    }
}