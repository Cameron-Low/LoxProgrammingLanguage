package com.loxChallenges;

import java.util.List;

/**
 * @author Cameron
 * @version 1.0
 * @since 28/12/2017
 */
interface LoxCallable {
    // arity is the number of passable arguments in a function/class call
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
