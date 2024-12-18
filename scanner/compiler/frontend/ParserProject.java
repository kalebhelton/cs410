/*
 * Authors: Sierra Jackson, Kaleb Helton, Taylor Oxley
 * Reviewers: Emily Krugman, Taylor Oxley, Luke Graham
 */

package compiler.frontend;

import compiler.common.AtomOperation;
import compiler.common.Operation;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ParserProject {
    private final LinkedList<Token> tokens;
    private final LinkedList<AtomOperation> atomQueue = new LinkedList<>();
    private boolean optimizeGlobal;  // flag to control global optimization
    private Token currentToken;  // Current Token being Processed

    public ParserProject(List<Token> tokens) {
        this.tokens = new LinkedList<>(tokens);
        this.optimizeGlobal = optimizeGlobal;  // set optimization flag
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
        if (expectedTypes.length == 0) {
            System.out.printf("Unexpected token: %s%n", currentToken.toString());
        } else if (expectedTypes.length == 1) {
            System.out.println("Expected " + expectedTypes[0] + " but found " + currentToken.toString());
        } else {
            System.out.printf("Expected one of %s but found %s%n", String.join(", ", Arrays.stream(expectedTypes).map(Enum::toString).toArray(String[]::new)), currentToken.toString());
        }

        System.exit(1);
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

        if (optimizeGlobal){  // remove unreachable code during parsing
            removeUnreachableCode();
        }

        return atomQueue;
    }

    private void removeUnreachableCode(){
        LinkedList <AtomOperation> optimizedAtomQueue = new LinkedList<>();
        boolean reachable = true;

        for (AtomOperation atomOP : atomQueue) {  // iterate through atomQueue + skip unreachable code
            if (atomOP.getOp() == Operation.JMP) {
                optimizedAtomQueue.add(atomOP);  // Add JMP and mark as unreachable
                reachable = false;
            } else if (atomOP.getOp() == Operation.LBL) { // Labels make code reachable again
                reachable = true;
                optimizedAtomQueue.add(atomOP);
            } else if (reachable) { // Only add operations if reachable
                optimizedAtomQueue.add(atomOP);
            }
        }

        atomQueue.clear();
        atomQueue.addAll(optimizedAtomQueue);  // replace the original atomQueue w/ optimized one
    }


    private boolean block() {
        return expect(TokenType.OPENING_CURLY_BRACKET) &&
                statements() &&
                expect(TokenType.CLOSING_CURLY_BRACKET);
    }

    private boolean statements() {
        boolean statementIsValid =  statement();

        if (statementIsValid && currentToken != null) {
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

            atomQueue.offer(beforeLabel);
            atomQueue.offer(new AtomOperation(Operation.TST));
            atomQueue.peekLast().setResult("");
            atomQueue.peekLast().setDest(afterLabel.getDest());

            if (condition()) {
                expect(TokenType.SEMICOLON);
                assignment();
                AtomOperation assignment = atomQueue.pollLast();
                expect(TokenType.CLOSING_PARENTHESIS);

                boolean blockIsValid = block();

                atomQueue.offer(assignment);
                atomQueue.offer(new AtomOperation(Operation.JMP, null, null, null, null, beforeLabel.getDest()));
                atomQueue.offer(afterLabel);

                return blockIsValid;
            }
        }

        return false;
    }


    private boolean whileLoop() {
        if(accept(TokenType.KEYWORD_WHILE) && expect(TokenType.OPENING_PARENTHESIS)) {
            AtomOperation beforeLabel = generateLabel("before_while");
            AtomOperation afterLabel = generateLabel("after_while");

            atomQueue.offer(beforeLabel);
            atomQueue.offer(new AtomOperation(Operation.TST));
            atomQueue.peekLast().setResult("");
            atomQueue.peekLast().setDest(afterLabel.getDest());

            if(condition()) {
                expect(TokenType.CLOSING_PARENTHESIS);
                boolean blockIsValid = block();

                atomQueue.offer(new AtomOperation(Operation.JMP, "", "", "", "", beforeLabel.getDest()));
                atomQueue.offer(afterLabel);

                return blockIsValid;
            }
        }

        return false;
    }

    private boolean ifStatement() {
        if(accept(TokenType.KEYWORD_IF) && expect(TokenType.OPENING_PARENTHESIS)) {
            AtomOperation afterLabel = generateLabel("after_if");

            atomQueue.offer(new AtomOperation(Operation.TST));
            atomQueue.peekLast().setResult("");
            atomQueue.peekLast().setDest(afterLabel.getDest());

            if(condition()) {
                expect(TokenType.CLOSING_PARENTHESIS);

                boolean blockIsValid = block();

                atomQueue.offer(new AtomOperation(Operation.JMP, "", "", "", "", afterLabel.getDest()));

                if(!elseStatement()) {
                    for(int i = atomQueue.size() - 1; i >= 0; i--) {
                        if(atomQueue.get(i).getOp() == Operation.TST) {
                            atomQueue.get(i).setDest(afterLabel.getDest());
                            break;
                        }
                    }
                }

                atomQueue.offer(afterLabel);

                return blockIsValid;
            }
        }

        return false;
    }

    private boolean elseStatement() {
        if(accept(TokenType.KEYWORD_ELSE)) {
            AtomOperation beforeLabel = generateLabel("before_else");

            atomQueue.offer(beforeLabel);

            for(int i = atomQueue.size() - 1; i >= 0; i--) {
                if(atomQueue.get(i).getOp() == Operation.TST) {
                    atomQueue.get(i).setDest(beforeLabel.getDest());
                    break;
                }
            }

            return ifStatement() || block();
        }

        return false;
    }

    private boolean assignment() {
        type();

        if (peek(TokenType.IDENTIFIER)) {
            atomQueue.offer(new AtomOperation(null, null, null, currentToken.value(), null, null));
            accept(TokenType.IDENTIFIER);

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
        AtomOperation atom = atomQueue.peekLast();

        if(accept(TokenType.IDENTIFIER)) {
            if(atom.getLeft() == null) {
                atom.setLeft(factorToken.value());
            } else {
                atom.setRight(factorToken.value());
            }

            if(peek(TokenType.SEMICOLON)) {
                if(atom.getRight() == null) {
                    atom.setOp(Operation.MOV);
                    atom.setRight("");
                }
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
                    atom.setRight("");
                }
            }

            return true;
        }

        return opNegate();
    }

    private boolean opMath() {  // Outputs Correctly
        if (accept(TokenType.ADD)) {
            atomQueue.peekLast().setOp(Operation.ADD);

            return true;
        } else if (accept(TokenType.SUBTRACT)) {
            atomQueue.peekLast().setOp(Operation.SUB);

            return true;
        } else if (accept(TokenType.MULTIPLY)) {
            atomQueue.peekLast().setOp(Operation.MUL);

            return true;
        } else if (accept(TokenType.DIVIDE)) {
            atomQueue.peekLast().setOp(Operation.DIV);

            return true;
        }

        return false;
    }

    private boolean opUnaryMath() {
        AtomOperation atom = atomQueue.peekLast();

        if (accept(TokenType.INCREMENT)) {
            atom.setOp(Operation.ADD);
            atom.setRight("1");
            atom.setLeft(atom.getResult());

            return true;
        } else if (accept(TokenType.DECREMENT)) {
            atom.setOp(Operation.SUB);
            atom.setRight("1");
            atom.setLeft(atom.getResult());

            return true;
        }

        return false;
    }

    private boolean opNegate() {
        if(peek(TokenType.IDENTIFIER)) {
            AtomOperation atom = atomQueue.peekLast();

            atom.setOp(Operation.NEG);
            atom.setLeft(currentToken.value());
            atom.setRight("");
            accept(TokenType.IDENTIFIER);

            return true;
        }

        return false;
    }

    private boolean opComparison() {
        AtomOperation atom = atomQueue.peekLast();

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
        return new AtomOperation(Operation.LBL, "", "" , "", "", "%s_%d".formatted(baseName, atomQueue.size()));
    }

}