package com.authora.authorization.server.common.exception;

import com.authora.authorization.server.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<?> handleGlobalException(GlobalException e){
        if (e.getData() != null){
            return ResponseEntity
                    .status(e.getStatus().value())
                    .body(ApiResponse.error(e.getMessage(),e.getData(),e.getStatus().value()));
        }
        return ResponseEntity
                .status(e.getStatus().value())
                .body(ApiResponse.error(e.getMessage(),e.getStatus().value()));

    }

}
