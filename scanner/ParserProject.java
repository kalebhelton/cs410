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

    private boolean forLoop() {
        if (accept(TokenType.KEYWORD_FOR) && expect(TokenType.OPENING_PARENTHESIS)) {
            assignment();
            expect(TokenType.SEMICOLON);
            AtomOperation beforeLabel = generateLabel("before_for");
            AtomOperation afterLabel = generateLabel("after_for");

            atoms.add(beforeLabel);
            atomStack.push(new AtomOperation(Operation.TST));
            atomStack.peek().setDest(afterLabel.getDest());

            if (condition()) {
                expect(TokenType.SEMICOLON);
                assignment();
                AtomOperation assignment = atoms.removeLast();
                expect(TokenType.CLOSING_PARENTHESIS);

                boolean blockIsValid = block();

                atoms.add(assignment);
                atoms.add(new AtomOperation(Operation.JMP, null, null, null, null, beforeLabel.getDest()));
                atoms.add(afterLabel);

                return blockIsValid;
            }
        }

        return false;
    }


    private boolean whileLoop() {
        if(accept(TokenType.KEYWORD_WHILE) && expect(TokenType.OPENING_PARENTHESIS)) {
            AtomOperation beforeLabel = generateLabel("before_while");
            AtomOperation afterLabel = generateLabel("after_while");

            atoms.add(beforeLabel);
            atomStack.push(new AtomOperation(Operation.TST));
            atomStack.peek().setDest(afterLabel.getDest());

            if(condition()) {
                atoms.add(atomStack.pop());
                expect(TokenType.CLOSING_PARENTHESIS);
                boolean blockIsValid = block();

                atoms.add(new AtomOperation(Operation.JMP, null, null, null, null, beforeLabel.getDest()));
                atoms.add(afterLabel);

                return blockIsValid;
            }
        }

        return false;
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
        type();

        atomStack.push(new AtomOperation(null, null, null, currentToken.value(), null, null));

        if (accept(TokenType.IDENTIFIER)) {
            if (opUnaryMath()) {
                return true;
            }

            return expect(TokenType.ASSIGN) && expression();
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
        if(!factor() || !opComparison()) {
            return false;
        }

        return factor();
    }

    private boolean type() {
        return accept(TokenType.KEYWORD_INT) || accept(TokenType.KEYWORD_DOUBLE);
    }

    private boolean factor() {
        Token factorToken = currentToken;
        AtomOperation atom = atomStack.peek();

        if(accept(TokenType.IDENTIFIER)) {
            if(atom.getLeft() == null) {
                atom.setLeft(factorToken.value());
            } else {
                atom.setRight(factorToken.value());
            }

            if(peek(TokenType.SEMICOLON)) {
                if(atom.getRight() == null) {
                    atom.setOp(Operation.MOV);
                }

                atoms.add(atomStack.pop());
            }

            return true;
        }

        boolean isNegative = accept(TokenType.SUBTRACT);
        factorToken = currentToken;

        if(accept(TokenType.INTEGER) || accept(TokenType.DOUBLE)) {
            String value = isNegative ? "-%s".formatted(factorToken.value()) : factorToken.value();

            if(atom.getLeft() == null) {
                atom.setLeft(value);
            } else {
                atom.setRight(value);
            }

            if(peek(TokenType.SEMICOLON)) {
                if(atom.getRight() == null) {
                    atom.setOp(Operation.MOV);
                }

                atoms.add(atomStack.pop());
            }

            return true;
        }

        return opNegate();
    }

    private boolean opMath() {  // Outputs Correctly
        if (accept(TokenType.ADD)) {
            atomStack.peek().setOp(Operation.ADD);

            return true;
        } else if (accept(TokenType.SUBTRACT)) {
            atomStack.peek().setOp(Operation.SUB);

            return true;
        } else if (accept(TokenType.MULTIPLY)) {
            atomStack.peek().setOp(Operation.MUL);

            return true;
        } else if (accept(TokenType.DIVIDE)) {
            atomStack.peek().setOp(Operation.DIV);

            return true;
        }

        return false;
    }

    private boolean opUnaryMath() {
        AtomOperation atom = atomStack.peek();

        if (accept(TokenType.INCREMENT)) {
            atom.setOp(Operation.ADD);
            atom.setRight("1");
            atom.setLeft(atom.getResult());

            return atoms.add(atomStack.pop());
        } else if (accept(TokenType.DECREMENT)) {
            atom.setOp(Operation.SUB);
            atom.setRight("1");
            atom.setLeft(atom.getResult());

            return atoms.add(atomStack.pop());
        }

        return false;
    }

    private boolean opNegate() {
        if(peek(TokenType.IDENTIFIER)) {
            AtomOperation atom = atomStack.pop();

            atom.setOp(Operation.NEG);
            atom.setLeft(currentToken.value());
            accept(TokenType.IDENTIFIER);

            return atoms.add(atom);
        }

        return false;
    }

    private boolean opComparison() {
        AtomOperation atom = atomStack.peek();

        if(accept(TokenType.EQUAL)) {
            atom.setCmp("6");
        } else if(accept(TokenType.LESS_THAN)) {
            atom.setCmp("5");
        } else if(accept(TokenType.GREATER_THAN)) {
            atom.setCmp("4");
        } else if(accept(TokenType.LESS_THAN_OR_EQUAL)) {
            atom.setCmp("3");
        } else if(accept(TokenType.GREATER_THAN_OR_EQUAL)) {
            atom.setCmp("2");
        } else if(accept(TokenType.NOT_EQUAL)) {
            atom.setCmp("1");
        }

        return atom.getCmp() != null;
    }

    private AtomOperation generateLabel(String baseName) {
        return new AtomOperation(Operation.LBL, null, null , null, null, "%s_%d".formatted(baseName, atoms.size()));
    }

}