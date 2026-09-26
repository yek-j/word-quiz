package com.jyk.wordquiz.wordquiz.common.exception;

/**
 * 로그인 실패 횟수 초과로 일시 잠금된 상태에서 로그인을 시도한 경우.
 * 자격 증명이 틀린 것(401)이 아니라 요청이 너무 잦은 것이므로 429로 매핑한다.
 */
public class TooManyLoginAttemptsException extends RuntimeException {
    public TooManyLoginAttemptsException(String message) {
        super(message);
    }
}
