package com.loxChallenges;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author Cameron
 * @version 1.0
 * @since 07/12/2017
 */
class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    // Setup global scope
    final Environment globals = new Environment();
    private Environment environment = globals;

    // Flag used for useful expression printing
    private boolean inREPL = false;

    // Flag used to check if a break has been hit;
    private Token hitBreak = null;

    // Flag used to check whether scope is currently within a loop
    private boolean inLoop = false;

    Interpreter() {
        // Adding a built-in function clock() that returns the number of seconds from a fixed point in time.
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000;
            }
        });


        // Adds basic user input
        globals.define("input", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Scanner input = new Scanner(System.in);
                return input.next();
            }
        });
    }

    // Method used to execute all statements in the AST
    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                // Very messy implementation of nice REPL printing for standalone expressions.
                if (statement instanceof Stmt.Expression && inREPL) {
                    if (!(((Stmt.Expression) statement).expression instanceof Expr.Assign)) {
                        System.out.print(evaluate((Expr)((Stmt.Expression) statement).expression));
                    }
                } else {
                    execute(statement);
                }
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    public void setInREPL() {
        inREPL = true;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
        }

        // Unreachable
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    // Type checking methods

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String) left + (String)right;
                }
                throw new RuntimeError(expr.operator, "Operands must be either two numbers or two strings.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }

        // Unreachable
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {

        Object callee = evaluate(expr.callee);
        List<Object> arguments = new ArrayList<>();
        for (Object argument : expr.arguments) {
            if (argument instanceof Expr) {
                arguments.add(evaluate((Expr)argument));
            } else if (argument instanceof Stmt.Function) {
                arguments.add(argument);
            }
        }

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes");
        }

        LoxCallable function = (LoxCallable)callee;

        if (function.arity() != arguments.size()) {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    // Returns the 'truthiness' of an object
    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        // nil is only equal to nil
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    // Makes nicer printing of values
    private String stringify(Object object) {
        if (object == null) return "nil";

        // Hack. Work around Java adding .0 to integer valued-doubles
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    // Evaluates an expression
    private Object evaluate(Expr expr) {
       return expr.accept(this);
    }

    // Executes a statement and will return if a break has been hit
    private void execute(Stmt stmt) {
        if (hitBreak != null) {
            if (!inLoop) {
                throw new RuntimeError(hitBreak, "Cannot have a break statement outside of a loop");
            }
            return;
        }
        stmt.accept(this);
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        hitBreak = stmt.token;
        return null;
    }

    // Creates a new local scope and executes statements one by one
    void executeBlock(List<Stmt> statememts, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statememts) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        System.out.print(1);
        if (stmt.expression instanceof Stmt.Function) {
            return null;
        }
        evaluate((Expr) stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt, environment);
        if (stmt.name != null) {
            environment.define(stmt.name.lexeme, function);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            if (stmt.initializer instanceof Stmt.Function) {
                environment.define(stmt.name.lexeme, new LoxFunction((Stmt.Function)stmt.initializer , environment));

                return null;
            } else {
                value = evaluate((Expr) stmt.initializer);
            }
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
            if (hitBreak != null) {
                hitBreak = null;
                break;
            }
        }
        inLoop = false;
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        if (expr.value instanceof Stmt.Function) {
            environment.assign(expr.name, new LoxFunction((Stmt.Function)expr.value , environment));
            return null;
        }
        Object value = evaluate((Expr) expr.value);
        environment.assign(expr.name, value);
        return value;
    }
}
