package com.loxChallenges;

/**
 * @author Cameron
 * @version 1.0
 * @since 06/12/2017
 * Creates an unambiguous, if ugly string representation of AST nodes
 */

//TODO: Implement Stmt.Visitor<String> and create nicer formating for the entire AST tree, this will improve debugging
public class AstPrinter implements Expr.Visitor<String> {
    @Override
    public String visitVariableExpr(Expr.Variable expr) { return parenthesize(expr.name.toString()); }

    @Override
    public String visitAssignExpr(Expr.Assign expr) { return parenthesize(expr.name.toString()); }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {return parenthesize(expr.operator.lexeme, expr.left, expr.right);}

    @Override
    public String visitCallExpr(Expr.Call expr) {
        return null;
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return null;
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    String print(Expr expr) {
        return expr.accept(this);
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
}
