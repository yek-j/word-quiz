package com.jyk.wordquiz.wordquiz.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
public class ApiResponseWrapper<T> {
    private final String status;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T result;

    private ApiResponseWrapper(String status, String message, T result) {
        this.status = status;
        this.message = message;
        this.result = result;
    }

    public static <T> ApiResponseWrapper<T> success(String message, T result) {
        return new ApiResponseWrapper<>("success", message, result);
    }

    public static ApiResponseWrapper<Void> success(String message) {
        return new ApiResponseWrapper<>("success", message, null);
    }

    public static <T> ApiResponseWrapper<T> fail(String message) {
        return new ApiResponseWrapper<>("fail", message, null);
    }
}
