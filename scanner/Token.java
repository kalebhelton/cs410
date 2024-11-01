public record Token(TokenType type, String value) {

    //controls the output!!
    public String toString() {
        return value + " (" + type + ")";
    }

}