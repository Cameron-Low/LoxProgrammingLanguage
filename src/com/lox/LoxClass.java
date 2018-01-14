package com.lox;

import java.util.List;
import java.util.Map;

/**
 * @author Cameron
 * @version 1.0
 * @since 29/12/2017
 */
public class LoxClass implements LoxCallable{
    final String name;
    private final Map<String, LoxFunction> methods;
    final LoxClass superClass;


    LoxClass(String name, Map<String, LoxFunction> methods, LoxClass superClass) {
        this.name = name;
        this.methods = methods;
        this.superClass = superClass;
    }

    @Override
    public String toString() {
        return "<cl " + name + ">";
    }

    @Override
    public int arity() {
        LoxFunction initializer = methods.get("init");
        if (initializer != null) return initializer.arity();
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = methods.get("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    LoxFunction findMethod(LoxInstance instance, String name) {
        if (methods.containsKey(name)) {
            return methods.get(name).bind(instance);
        }

        if (superClass != null) {
            superClass.findMethod(instance, name);
        }

        return null;
    }
}
