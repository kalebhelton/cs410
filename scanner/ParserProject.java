/*
 * Authors: Sierra Jackson, Kaleb Helton, Taylor Oxley
 * Reviewers: Emily Krugman, Taylor Oxley, Luke Graham
 */


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ParserProject {
    private final LinkedList<Token> tokens;
    private final List<AtomOperation> atoms = new ArrayList<>();  // List to Store Generated Atoms

    private Token currentToken;  // Current Token being Processed
    private Operation operation;
    private String left;
    private String right;
    private String result;
    private String cmp;
    private String source;
    private String dest;

    public ParserProject(List<Token> tokens) {
        this.tokens = new LinkedList<>(tokens);
        advance();
    }

    private void advance() {  // Advances to Next Token in Input Stream
        if(!tokens.isEmpty()) {
            currentToken = tokens.pop();
        } else {
            currentToken = null;
        }
    }

    private boolean accept(TokenType type) {  // If Token Matches the Expected Type -> Advances
        if (currentToken != null && currentToken.type() == type) {
            advance();
            return true;
        }

        return false;
    }

    private void reject(TokenType... expectedTypes) {
        if (expectedTypes.length == 0) throw new RuntimeException("Unexpected token: %s".formatted(currentToken.toString()));
        if (expectedTypes.length == 1) throw new RuntimeException("Expected " + expectedTypes[0] + " but found " + currentToken.toString());

        throw new RuntimeException("Expected one of %s but found %s".formatted(String.join(", ", Arrays.stream(expectedTypes).map(Enum::toString).toArray(String[]::new)), currentToken.toString()));
    }

    private boolean expect(TokenType type) {  //  If Token Doesn't Match Expected Type -> Throws Error
        if (!accept(type)) reject(type);

        return true;
    }

    private boolean peek(TokenType type) {
        return currentToken != null && currentToken.type() == type;
    }

    public List<AtomOperation> parse() {  // Start Parsing -> Returns List of Generated Atoms
        statements();

        if(currentToken != null) {
            reject();
        }

        return atoms;
    }

    private boolean block() {
         return expect(TokenType.OPENING_CURLY_BRACKET) &&
                 statements() &&
                 expect(TokenType.CLOSING_CURLY_BRACKET);
    }

    private boolean statements() {  // Parses a Sequence of Statements
        if(statement() && currentToken != null) {
            statements();
        }

        return true;
    }

    private boolean statement() {  // Parses a Single Statement Based on Current Token Type
        return ifStatement() ||
                whileLoop() ||
                forLoop() ||
                assignment() && expect(TokenType.SEMICOLON);
    }

    private boolean forLoop() {
        return accept(TokenType.KEYWORD_FOR) &&
                expect(TokenType.OPENING_PARENTHESIS) &&
                assignment() &&
                expect(TokenType.SEMICOLON) &&
                condition() &&
                expect(TokenType.SEMICOLON) &&
                assignment() &&
                expect(TokenType.CLOSING_PARENTHESIS) &&
                block();
    }

    private boolean whileLoop() {
        return accept(TokenType.KEYWORD_WHILE) &&
                expect(TokenType.OPENING_PARENTHESIS) &&
                condition() &&
                expect(TokenType.CLOSING_PARENTHESIS) &&
                block();
    }

    private boolean ifStatement() {
        boolean ifIsValid = accept(TokenType.KEYWORD_IF) &&
                expect(TokenType.OPENING_PARENTHESIS) &&
                condition() &&
                expect(TokenType.CLOSING_PARENTHESIS) &&
                block();

        return ifIsValid && elseStatement() || ifIsValid;
    }

    private boolean elseStatement() {
        return accept(TokenType.KEYWORD_ELSE) && (ifStatement() || block());
    }

    private boolean assignment() {
        result = currentToken.value();

        // Read type if it is there
        type();

        return accept(TokenType.IDENTIFIER) &&
                (opUnaryMath() || expect(TokenType.ASSIGN) && expression());
    }

    private boolean expression() {
        left = currentToken.value();

        if(factor()) {
            if(peek(TokenType.SEMICOLON)) {
                operation = Operation.MOV;
                source = left;
                dest = result;
                left = null;
                result = null;

                createAtom();
            }

            return peek(TokenType.SEMICOLON) || opMath() && factor();
        } else if (accept(TokenType.OPENING_PARENTHESIS)) {
            return expression() && expect(TokenType.CLOSING_PARENTHESIS);
        }

        return false;
    }

    private boolean condition() {
        return factor() && opComparison() && factor();
    }

    private boolean type() {
        return accept(TokenType.KEYWORD_INT) || accept(TokenType.KEYWORD_DOUBLE);
    }

    private boolean factor() {
        return opNegate() || accept(TokenType.INTEGER) || accept(TokenType.DOUBLE) || accept(TokenType.IDENTIFIER);
    }

    private boolean opMath() {
        if(accept(TokenType.ADD)) {
            operation = Operation.ADD;
            right = currentToken.value();

            return createAtom();
        } else if(accept(TokenType.SUBTRACT)) {
            operation = Operation.SUB;
            right = currentToken.value();

            return createAtom();
        } else if(accept(TokenType.MULTIPLY)) {
            operation = Operation.MUL;
            right = currentToken.value();

            return createAtom();
        } else if(accept(TokenType.DIVIDE)) {
            operation = Operation.DIV;
            right = currentToken.value();

            return createAtom();
        }

        return false;
    }

    private boolean opUnaryMath() {
        if (accept(TokenType.INCREMENT)) {
            operation = Operation.ADD;
            left = result;
            right = "1";

            return createAtom();
        } else if (accept(TokenType.DECREMENT)) {
            operation = Operation.SUB;
            left = result;
            right = "1";

            return createAtom();
        }

        return false;
    }

    private boolean opNegate() {
        if(accept(TokenType.SUBTRACT)) {
            operation = Operation.NEG;
            left = currentToken.value();

            return factor() && createAtom();
        }

        return false;
    }

    private boolean opComparison() {
        return accept(TokenType.EQUAL) || accept(TokenType.NOT_EQUAL) || accept(TokenType.LESS_THAN) || accept(TokenType.LESS_THAN_OR_EQUAL) || accept(TokenType.GREATER_THAN) || accept(TokenType.GREATER_THAN_OR_EQUAL);
    }

    private boolean createAtom() {
        atoms.add(new AtomOperation(operation, left, right, result, cmp, source, dest));

        // Reset all atom values
        operation = null;
        left = null;
        right = null;
        result = null;
        cmp = null;
        source = null;
        dest = null;

        return true;
    }
}


