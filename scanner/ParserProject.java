/*
 * Authors: Sierra Jackson, Kaleb Helton, Taylor Oxley
 * Reviewers: Emily Krugman, Taylor Oxley, Luke Graham
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class ParserProject {
    private final LinkedList<Token> tokens;
    private final List<AtomOperation> atoms = new ArrayList<>();  // List to Store Generated Atoms
    private final Stack<AtomOperation> atomStack = new Stack<>();

    private Token currentToken;  // Current Token being Processed
    private Operation operation; // Stack for atom management
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
        if (factor()) {
            if (peek(TokenType.SEMICOLON)) {
                beginAtom(Operation.MOV);
                AtomOperation atom = atomStack.peek();
                atom.source = left;
                atom.dest = result;
                endAtom();
            }
            return peek(TokenType.SEMICOLON) || opMath() && factor();
        } else if (accept(TokenType.OPENING_PARENTHESIS)) {
            return expression() && expect(TokenType.CLOSING_PARENTHESIS);
        }
        return false;
    }

    private boolean condition() {
        if (factor() && opComparison() && factor()) {
            beginAtom(Operation.TST);
            AtomOperation atom = atomStack.peek();
            atom.left = left;
            atom.source = right;
            atom.source = cmp;
            endAtom();
            return true;
        }
        return false;
    }

    private boolean type() {
        return accept(TokenType.KEYWORD_INT) || accept(TokenType.KEYWORD_DOUBLE);
    }

    private boolean factor() {
        return opNegate() || accept(TokenType.INTEGER) || accept(TokenType.DOUBLE) || accept(TokenType.IDENTIFIER);
    }

    private void beginAtom(Operation op) {
        atomStack.push(new AtomOperation(op, null, null, null, null, null, null));
    }

    private void endAtom() {
        atoms.add(atomStack.pop());
    }

    private boolean opMath() {
        if (accept(TokenType.ADD)) {
            beginAtom(Operation.ADD);
            AtomOperation atom = atomStack.peek();
            atom.left = left;
            atom.source = currentToken.value();
            endAtom();
            return true;
        } else if (accept(TokenType.SUBTRACT)) {
            beginAtom(Operation.SUB);
            AtomOperation atom = atomStack.peek();
            atom.left = left;
            atom.source = currentToken.value();
            endAtom();
            return true;
        } else if (accept(TokenType.MULTIPLY)) {
            beginAtom(Operation.MUL);
            AtomOperation atom = atomStack.peek();
            atom.left = left;
            atom.source = currentToken.value();
            endAtom();
            return true;
        } else if (accept(TokenType.DIVIDE)) {
            beginAtom(Operation.DIV);
            AtomOperation atom = atomStack.peek();
            atom.left = left;
            atom.source = currentToken.value();
            endAtom();
            return true;
        }
        return false;
    }

    private boolean opUnaryMath() {
        if (accept(TokenType.INCREMENT)) {
            beginAtom(Operation.ADD);
            AtomOperation atom = atomStack.peek();
            atom.left = result;
            atom.source = "1";
            endAtom();
            return true;
        } else if (accept(TokenType.DECREMENT)) {
            beginAtom(Operation.SUB);
            AtomOperation atom = atomStack.peek();
            atom.left = result;
            atom.source = "1";
            endAtom();
            return true;
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


