package compiler;

public record Token(TokenType type, String value) {

    public String toString() {
        return value + " (" + type + ")";
    }

}