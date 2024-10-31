/*
 * Authors: Sierra Jackson, Kaleb Helton, Taylor Oxley
 * Reviewers: Emily Krugman, Taylor Oxley, Luke Graham
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ParserProject {
    private final Iterator<ScannerProject.Token> tokens;  // Iterator for Token Stream
    private ScannerProject.Token currentToken;  // Current Token being Processed
    private final List<AtomOperations> atom = new ArrayList<>();  // List to Store Generated Atoms

    public ParserProject(List<ScannerProject.Token> tokens) {
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
        if (currentToken != null && currentToken.getType() == type) {
            advance();
            return true;
        }

        return false;
    }

    private void expect(TokenType type) {  //  If Token Doesn't Match Expected Type -> Throws Error
        if (!accept(type)) {
            throw new RuntimeException("Expected " + type + " but found " + currentToken);
        }
    }

    private boolean peek(TokenType type) {
        return currentToken != null && currentToken.getType() == type;
    }

    public List<AtomOperations> parse() throws Exception {  // Start Parsing -> Returns List of Generated Atoms
        parseStatements();
        parseBlock();
        // if input is empty -> return
        return atom;

        // else -> throw an exception
    }

    private void parseStatements() throws Exception {  // Parses a Sequence of Statements
        while (currentToken != null) {
            parseStatement();
        }
    }

    private void parseStatement() throws Exception {  // Parses a Single Statement Based on Current Token Type
        if (accept(TokenType.KeywordIf)) {
            parseIf();
        } else if (accept(TokenType.KeywordWhile)) {
            parseWhile();
        } else if (accept(TokenType.KeywordFor)) {

            parseFor();
        } else if (accept(TokenType.KeywordInt) || accept(TokenType.KeywordDouble)) {
            parseAssignment();
        } else {
            throw new RuntimeException("Unexpected Token: " + currentToken);
        }
    }

    private void parseIf() throws Exception {
        expect(TokenType.OpeningParenthesis);
        parseExpression();
        expect(TokenType.ClosingParenthesis);
        parseBlock();

        if (accept(TokenType.KeywordElse)) {
            parseBlock();
        }

        else{
            throw new Exception("Reject parseIf");
        }
    }

    private void parseWhile() throws Exception {
        // if accept 'while'
        expect(TokenType.OpeningParenthesis);
        parseExpression();
        expect(TokenType.ClosingParenthesis);
        parseBlock();

        // else -> throw new Exception("Reject parseWhile");
    }

    private void parseFor() throws Exception {
        // if accept 'for'
        expect(TokenType.OpeningParenthesis);
        parseAssignment();
        expect(TokenType.Semicolon);
        parseExpression();
        expect(TokenType.Semicolon);
        parseExpression();
        expect(TokenType.ClosingParenthesis);
        parseBlock();

        // else -> throw new Exception("Reject ParseFor")
    }

    private void parseAssignment() {
        expect(TokenType.Identifier);
        expect(TokenType.Assign);
        parseExpression();
        atom.add(new AtomOperations(Operation.MOV, currentToken.getValue(), null, "result", null, null));
        expect(TokenType.Semicolon);
    }

    private void parseExpression() {
        if (accept(TokenType.Identifier) || accept(TokenType.KeywordInt) || accept(TokenType.KeywordDouble)) {
            atom.add(new AtomOperations(Operation.ADD, "left", "right", "result", null, null));
        }
    }

    private void parseBlock() throws Exception {
        expect(TokenType.OpeningCurlyBracket);
        parseStatements();
        expect(TokenType.ClosingCurlyBracket);
    }
}

