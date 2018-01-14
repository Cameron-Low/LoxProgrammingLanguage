package com.loxChallenges;

/**
 * @author Cameron
 * @version 1.0
 * @since 08/12/2017
 */
public class RuntimeError extends RuntimeException {
    final Token token;

    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
