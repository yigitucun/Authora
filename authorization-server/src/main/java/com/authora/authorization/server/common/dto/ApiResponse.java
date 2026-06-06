package com.authora.authorization.server.common.dto;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        LocalDateTime timestamp,
        int status
) {
    public static <T> ApiResponse<T> success(T data, String message,int status) {
        return new ApiResponse<>(true, message,data, LocalDateTime.now(),status);
    }

    public static <T> ApiResponse<T> success(String message,int status) {
        return new ApiResponse<>(true, message, null, LocalDateTime.now(),status);
    }

    public static <T> ApiResponse<T> error(String message,int status) {
        return new ApiResponse<>(false, message,null, LocalDateTime.now(),status);
    }

    public static <T> ApiResponse<T> error(String message, T data,int status) {
        return new ApiResponse<>(false, message ,data, LocalDateTime.now(),status);
    }
}