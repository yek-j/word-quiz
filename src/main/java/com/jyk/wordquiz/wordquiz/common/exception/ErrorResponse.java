package com.jyk.wordquiz.wordquiz.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "에러 응답")
public class ErrorResponse {
    @Schema(description = "HTTP 상태 코드", example = "409")
    private final int code;
    @Schema(description = "에러 상태", example = "DUPLICATION_WORD")
    private final String status;
    @Schema(description = "에러 메시지", example = "이미 존재하는 단어 입니다.")
    private final String message;
    @Schema(description = "발생 시간", example = "2024-01-15 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;

    public ErrorResponse(int code, String status, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
