package com.loxChallenges;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Cameron
 * @version 1.0
 * @since 05/12/2017
 * This class is used to run a lox file or open a lox interpreter shell.
 */

public class Lox {
    //Class variables
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        // TODO: Run multiple files
        if (args.length > 1) {
            System.out.println("Usage: lox [script]");
        } else if (args.length == 0) {
            runFile("/Users/Cameron/Documents/Programming Projects/src/com/loxChallenges/Test.lox");
        } else {
            runPrompt();
        }
    }

    // Runs a lox file from a given path
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()), false);

        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    // Runs the lox interpreter indefinitely; or until you escape using Control-C
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            try {
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException ero) {}
            System.out.print(">> ");
            run(reader.readLine(), true);

            hadError = false;
        }
    }



    // Main run function used to begin the interpretation of lox
    private static void run(String source, boolean inREPL) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        //System.out.print(tokens);
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        //System.out.print(new AstPrinter().print(expression));

        // Stop if there was a syntax error
        if (hadError) return;

        // Allow printing of expressions in REPL
        if (inREPL){
            interpreter.setInREPL();
        }


        interpreter.interpret(statements);
    }

    // Basic error handling functions
    static void error(int line, String message) {
        report(line, "", message);
    }

    static void runtimeError(RuntimeError error) {
        System.err.print("[line " + error.token.line + "] " + error.getMessage());
        hadRuntimeError = true;
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
}
