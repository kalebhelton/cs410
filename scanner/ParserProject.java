/*
 * Authors: Sierra Jackson, Kaleb Helton, Taylor Oxley
 * Reviewers: Emily Krugman, Taylor Oxley, Luke Graham
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ParserProject {
    private final Iterator<Token> tokens;  // Iterator for Token Stream
    private Token currentToken;  // Current Token being Processed
    private final List<AtomOperation> atoms = new ArrayList<>();  // List to Store Generated Atoms

    public ParserProject(List<Token> tokens) {
        this.tokens = tokens.iterator();
        advance();
    }

    private void advance() {  // Advances to Next Token in Input Stream
        if (tokens.hasNext()) {
            currentToken = tokens.next();
        } else {
            currentToken = null;  // End of Tokens
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
        if (expectedTypes.length == 0) throw new RuntimeException("Unexpected token: %s".formatted(currentToken));
        if (expectedTypes.length == 1) throw new RuntimeException("Expected " + expectedTypes[0] + " but found " + currentToken);

        throw new RuntimeException("Expected one of %s but found %s".formatted(String.join(", ", Arrays.stream(expectedTypes).map(Enum::toString).toArray(String[]::new)), currentToken));
    }

    private void expect(TokenType type) {  //  If Token Doesn't Match Expected Type -> Throws Error
        if (!accept(type)) reject(type);
    }

    private boolean peek(TokenType type) {
        return currentToken != null && currentToken.type() == type;
    }

    public List<AtomOperation> parse() throws Exception {  // Start Parsing -> Returns List of Generated Atoms
        parseStatements();
//        parseBlock();
        return atoms;
    }

    private void parseStatements() throws Exception {  // Parses a Sequence of Statements
        while (currentToken != null) {
            parseStatement();
        }
    }

    private void parseStatement() throws Exception {  // Parses a Single Statement Based on Current Token Type
        if (peek(TokenType.KEYWORD_IF)) {
            parseIf();
        } else if (peek(TokenType.KEYWORD_WHILE)) {
            parseWhile();
        } else if (accept(TokenType.KEYWORD_FOR)) {
            parseFor();
        } else if (accept(TokenType.KEYWORD_INT) || accept(TokenType.KEYWORD_DOUBLE) || peek(TokenType.IDENTIFIER)) {
            parseAssignment();
        } else if (peek(TokenType.INTEGER) || peek(TokenType.DOUBLE) || peek(TokenType.OPENING_PARENTHESIS)) {
            parseExpression();
            expect(TokenType.SEMICOLON);
        } else {
            reject();
        }
    }

    // else if ??
    private void parseIf() throws Exception {
        if (accept(TokenType.KEYWORD_IF)) {
            expect(TokenType.OPENING_PARENTHESIS);
            parseExpression();
            expect(TokenType.CLOSING_PARENTHESIS);

            if (accept(TokenType.KEYWORD_ELSE)) {
                parseBlock();
            }
        } else {
            throw new Exception("Reject If");
        }
    }

    private void parseElse() throws Exception {
        if (accept(TokenType.KEYWORD_ELSE)) {
            if (peek(TokenType.KEYWORD_IF)) {
                parseIf();
            } else {
                parseBlock();
            }
        } else {
            throw new Exception("Reject Else");
        }
    }

    private void parseWhile() throws Exception {
        if (accept(TokenType.KEYWORD_WHILE)) {
            expect(TokenType.OPENING_PARENTHESIS);
            parseExpression();
            expect(TokenType.CLOSING_PARENTHESIS);
            parseBlock();
        } else {
            // reject
            throw new Exception("Reject While");
        }
    }

    private void parseFor() throws Exception {
        if (accept(TokenType.KEYWORD_FOR)) {
            expect(TokenType.OPENING_PARENTHESIS);
            parseAssignment();
            expect(TokenType.SEMICOLON);
            parseExpression();
            expect(TokenType.SEMICOLON);
            parseExpression();
            expect(TokenType.CLOSING_PARENTHESIS);
            parseBlock();
        } else {
            // reject
            throw new Exception("Reject For");
        }
    }

    private void parseType() {
        if (accept(TokenType.KEYWORD_DOUBLE)) {
            return;
        }
    }

    private void parseAssignment() {
        Token identifier = currentToken;

        expect(TokenType.IDENTIFIER);
        expect(TokenType.ASSIGN);

        Token factor = currentToken;
        if(accept(TokenType.INTEGER) || accept(TokenType.DOUBLE)) {
            atoms.add(new AtomOperation(factor.value(), identifier.value()));
        } else {
            parseExpression();
            atoms.add(new AtomOperation(currentToken.value(), identifier.value()));
        }

        expect(TokenType.SEMICOLON);
    }

    private void parseExpression() {
//        if (accept(TokenType.Identifier) || accept(TokenType.KeywordInt) || accept(TokenType.KeywordDouble)) {
//            atom.add(new AtomOperations(Operation.ADD, "left", "right", "result", null, null));
//        }
        if (accept(TokenType.OPENING_PARENTHESIS)) {
            parseExpression();
            expect(TokenType.CLOSING_PARENTHESIS);
        } else if (accept(TokenType.IDENTIFIER)) {
            if (peek(TokenType.INCREMENT) || peek(TokenType.DECREMENT)) {
                parseUnaryMath();
            } else if (peek(TokenType.ADD) || peek(TokenType.SUBTRACT) || peek(TokenType.MULTIPLY) || peek(TokenType.DIVIDE)) {
                parseMath();
            }
        } else if (accept(TokenType.KEYWORD_INT) || accept(TokenType.KEYWORD_DOUBLE)) {
            if(peek(TokenType.IDENTIFIER)) {
                parseExpression();
            } else {
                reject(TokenType.IDENTIFIER);
            }
        }
    }

    private void parseUnaryMath() {
        if (accept(TokenType.INCREMENT)) {
            atoms.add(new AtomOperation(Operation.ADD, currentToken.value(), "1", currentToken.value()));
        } else if (accept(TokenType.DECREMENT)) {
            atoms.add(new AtomOperation(Operation.SUB, currentToken.value(), "1", currentToken.value()));
        } else {
            reject(TokenType.INCREMENT, TokenType.DECREMENT);
        }
    }

    private void parseMath() {
        if(accept(TokenType.ADD)) {

        }
    }

    private void parseBlock() throws Exception {
        expect(TokenType.OPENING_CURLY_BRACKET);
        parseStatements();
        expect(TokenType.CLOSING_CURLY_BRACKET);
    }
}


