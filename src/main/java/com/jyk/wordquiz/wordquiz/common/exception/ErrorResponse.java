package com.jyk.wordquiz.wordquiz.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final int code;
    private final String status;
    private final String message;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;

    public ErrorResponse(int code, String status, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
