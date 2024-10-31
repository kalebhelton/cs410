import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class ParserProject {
    private final Iterator<ScannerProject.Token> tokens;  // Iterator for Token Stream
    private ScannerProject.Token currentToken;  // Current Token being Processed
    private final List<AtomOperations> atom = new ArrayList<>();  // List to Store Generated Atoms

    public ParserProject(List<ScannerProject.Token> tokens){
        this.tokens = tokens.iterator();
        advance();
    }

    private void advance(){  // Advances to Next Token in Input Stream
        if (tokens.hasNext()){
            currentToken = tokens.next();
        }

        else{
            currentToken = null;  // End of Tokens
        }
    }

    private boolean accept(TokenType type){  // If Token Matches the Expected Type -> Advances
        if (currentToken != null && currentToken.getType() == type){
            advance();
            return true;
        }

        return false;
    }

    private void expect(TokenType type){  //  If Token Doesn't Match Expected Type -> Throws Error
        if (!accept(type)){
            throw new RuntimeException("Expected " + type + " but found " + currentToken);
        }
    }

    public List<AtomOperations> parse(){  // Start Parsing -> Returns List of Generated Atoms
        parseStatements();
        return atom;
    }

    private void parseStatements(){  // Parses a Sequence of Statements
        while (currentToken != null){
            parseStatements();
        }
    }

    private void parseStatement(){  // Parses a Single Statement Based on Current Token Type
        if (accept(TokenType.KeywordIf)){
            parseIf();
        }

        else if(accept(TokenType.KeywordWhile)){
            parseWhile();
        }

        else if(accept(TokenType.KeywordFor)){
            parseFor();
        }

        else if(accept(TokenType.Integer) || accept(TokenType.Double)){
            parseAssignment();
        }

        else{
            throw new RuntimeException("Unexpected Token: " + currentToken);
        }
    }

    private void parseIf(){
        expect(TokenType.OpeningParenthesis);
        parseExpression();
        expect(TokenType.ClosingParenthesis);
        parseBlock();

        if (accept(TokenType.KeywordElse)){
            parseBlock();
        }
    }

    private void parseWhile(){
        expect(TokenType.OpeningParenthesis);
        parseExpression();
        expect(TokenType.ClosingParenthesis);
        parseBlock();
    }

    private void parseFor(){
        expect(TokenType.OpeningParenthesis);
        parseAssignment();
        expect(TokenType.Semicolon);
        parseExpression();
        expect(TokenType.Semicolon);
        parseExpression();
        expect(TokenType.ClosingParenthesis);
        parseBlock();
    }

    private void parseAssignment(){
        expect(TokenType.Identifier);
        expect(TokenType.Assign);
        parseExpression();
        atom.add(new AtomOperations(Operation.MOV, currentToken.getValue(), null, "result", null , null));
        expect(TokenType.Semicolon);
    }

    private void parseExpression(){
        if (accept(TokenType.Identifier) || accept(TokenType.Integer) || accept(TokenType.Double)){
            atom.add(new AtomOperations(Operation.ADD, "left", "right", "result", null, null));
        }
    }

    private void parseBlock(){
        expect(TokenType.OpeningCurlyBracket);
        parseStatements();
        expect(TokenType.ClosingCurlyBracket);
    }
}

