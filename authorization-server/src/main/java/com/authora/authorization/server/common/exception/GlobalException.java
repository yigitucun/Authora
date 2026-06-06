package com.authora.authorization.server.common.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GlobalException extends RuntimeException {
    private final HttpStatus status;
    private final Object data;

    public GlobalException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.data=null;
    }

    public GlobalException(String message, HttpStatus status, Object data) {
        super(message);
        this.status = status;
        this.data = data;
    }


}
