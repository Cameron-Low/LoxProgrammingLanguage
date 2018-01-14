package com.lox;

/**
 * @author Cameron
 * @version 1.0
 * @since 28/12/2017
 */
public class Return extends RuntimeException {
    final Object value;

    Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
