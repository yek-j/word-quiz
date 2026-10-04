package com.jyk.wordquiz.wordquiz.common.exception;

/**
 * 요청 형식은 올바르나 현재 리소스 상태와 충돌해 처리할 수 없는 경우 (HTTP 409).
 *
 * 400(Bad Request)과의 구분 기준:
 *   - 요청을 고치면 성공할 수 있다        → 400 (IllegalArgumentException)
 *   - 서버 상태가 바뀌어야 성공할 수 있다 → 409 (이 예외)
 *
 * 예) 이미 답변한 문제에 다시 답변, 저장 개수 한도 초과, 이미 존재하는 프롬프트 추가
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
