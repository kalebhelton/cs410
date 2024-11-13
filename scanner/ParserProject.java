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

    public ParserProject(List<Token> tokens) {
        this.tokens = new LinkedList<>(tokens);
        advance();
    }

    private void advance() {  // Advances to Next Token in Input Stream
        if (!tokens.isEmpty()) {
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
        if (expectedTypes.length == 0)
            throw new RuntimeException("Unexpected token: %s".formatted(currentToken.toString()));
        if (expectedTypes.length == 1)
            throw new RuntimeException("Expected " + expectedTypes[0] + " but found " + currentToken.toString());

        throw new RuntimeException("Expected one of %s but found %s".formatted(String.join(", ", Arrays.stream(expectedTypes).map(Enum::toString).toArray(String[]::new)), currentToken.toString()));
    }

    private boolean expect(TokenType type) {  //  If Token Doesn't Match Expected Type -> Throws Error
        if (!accept(type)) {
            reject(type);
        }

        return true;
    }

    private boolean peek(TokenType type) {
        return currentToken != null && currentToken.type() == type;
    }

    public List<AtomOperation> parse() {  // Start Parsing -> Returns List of Generated Atoms
        statements();

        if (currentToken != null) {
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
        if (statement() && currentToken != null) {
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

    // Helper method to generate new labels for atoms
    private String generateNewLabel(String baseName) {
        return baseName + "_" + atoms.size();
    }

    private boolean forLoop() {
        if (accept(TokenType.KEYWORD_FOR) &&
                expect(TokenType.OPENING_PARENTHESIS)) {
            assignment();  //Initialization
            expect(TokenType.SEMICOLON);


            String topLabel = generateNewLabel("top");
            atoms.add(new AtomOperation(Operation.LBL, null, null, null, null, null, topLabel));

            // Condition check 
            if (condition()) {
                AtomOperation conditionAtom = atomStack.peek();
                endAtom();

                expect(TokenType.SEMICOLON);

                // Increment 
                assignment();
                expect(TokenType.CLOSING_PARENTHESIS);

                // Jump back to the start of the loop (top)
                atoms.add(new AtomOperation(Operation.JMP, null, null, null, null, null, topLabel));

                // End of the loop (after_for label)

                return block();
            }
        }
        return false;
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
                expect(TokenType.CLOSING_PARENTHESIS);

        ifIsValid = ifIsValid && block();

        return ifIsValid && elseStatement() || ifIsValid;
    }


    private boolean elseStatement() {
        return accept(TokenType.KEYWORD_ELSE) && (ifStatement() || block());
    }

    private boolean assignment() {
        type(); // Check Type

        if (accept(TokenType.IDENTIFIER)) {
            if (opUnaryMath()) {  // Check Increment Option
                return true;
            } else if (opNegate()) {  // Check Decrement Option
                return true;
            }

            return expect(TokenType.ASSIGN) && expression();  // Check Regular Assignment
        }

        return false;
    }

    private boolean expression() {
        if (factor()) {
            return peek(TokenType.SEMICOLON) || opMath() && factor();
        } else if (accept(TokenType.OPENING_PARENTHESIS)) {
            return expression() && expect(TokenType.CLOSING_PARENTHESIS);
        }
        return false;
    }

    private boolean condition() {
        if (factor() && opComparison() && factor()) {
            return true;
        }
        return false;
    }

    private boolean type() {
        return accept(TokenType.KEYWORD_INT) || accept(TokenType.KEYWORD_DOUBLE);
    }

    private boolean factor() {
        return opNegate() ||
                accept(TokenType.INTEGER) ||
                accept(TokenType.DOUBLE) ||
                accept(TokenType.IDENTIFIER);
    }

    private AtomOperation beginAtom(Operation op) {
        return atomStack.push(new AtomOperation(op, null, null, null, null, null, null));
    }

    private void endAtom() {
        atoms.add(atomStack.pop());
    }

    private boolean opMath() {  // Outputs Correctly
        if (accept(TokenType.ADD)) {
            return true;
        } else if (accept(TokenType.SUBTRACT)) {
            return true;
        } else if (accept(TokenType.MULTIPLY)) {
            return true;
        } else if (accept(TokenType.DIVIDE)) {
            return true;
        }

        return false;
    }

    private boolean opUnaryMath() {
        if (accept(TokenType.INCREMENT)) {
            return atoms.add(new AtomOperation(Operation.ADD, "x", "1", "x", null, null, null));
        } else if (accept(TokenType.DECREMENT)) {
            return atoms.add(new AtomOperation(Operation.SUB, "x", "1", "x", null, null, null));
        }

        return false;
    }

    private boolean opNegate() {
        if (accept(TokenType.DECREMENT)) {
            beginAtom(Operation.SUB);

            if (!atomStack.isEmpty()) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    private boolean opComparison() {
        return accept(TokenType.EQUAL) ||
                accept(TokenType.NOT_EQUAL) ||
                accept(TokenType.LESS_THAN) ||
                accept(TokenType.LESS_THAN_OR_EQUAL) ||
                accept(TokenType.GREATER_THAN) ||
                accept(TokenType.GREATER_THAN_OR_EQUAL);
    }

}