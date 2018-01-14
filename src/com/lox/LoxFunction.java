package com.lox;

import java.util.List;

/**
 * @author Cameron
 * @version 1.0
 * @since 28/12/2017
 */
public class LoxFunction implements LoxCallable {
    private final Environment closure;
    private final Stmt.Function declaration;
    private final boolean isInitializer;

    LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.closure = closure;
        this.declaration = declaration;
        this.isInitializer = isInitializer;
    }

    @Override
    public int arity() {
        return declaration.parameters.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i< declaration.parameters.size(); i++) {
            if (arguments.get(i) instanceof Stmt.Function) {
                environment.define(declaration.parameters.get(i).lexeme, new LoxFunction((Stmt.Function)arguments.get(i), environment, false));
            } else {
                environment.define(declaration.parameters.get(i).lexeme, arguments.get(i));
            }
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        if (isInitializer) return closure.getAt(0, "this");
        return null;
    }

    LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(declaration, environment, isInitializer);
    }

    @Override
    public String toString() {
        if (declaration.name == null) {
            return "<fn nil>";
        }
        return "<fn " + declaration.name.lexeme + ">";
    }
}
