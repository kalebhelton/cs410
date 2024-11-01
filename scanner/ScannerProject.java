/*
 * Authors: Sierra Jackson, Kaleb Helton, Simon Hale, Luke Graham
 * Reviewers: Emily Krugman, Taylor Oxley
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ScannerProject {
    // The inputs are all ascii values (0-127) -> 128 inputs
    private static final int INPUTS = 128;
    private static final int STATES = 46;
    private static final TokenType[] ACCEPT = new TokenType[STATES];
    private static final int[][] FSM = new int[STATES][INPUTS];

	public static void initializeStates() {
        ACCEPT[1] = TokenType.IDENTIFIER;
        ACCEPT[2] = TokenType.SEMICOLON;
        ACCEPT[4] = TokenType.NOT_EQUAL;
        ACCEPT[5] = TokenType.ASSIGN;
        ACCEPT[6] = TokenType.EQUAL;
        ACCEPT[7] = TokenType.LESS_THAN;
        ACCEPT[8] = TokenType.LESS_THAN_OR_EQUAL;
        ACCEPT[9] = TokenType.GREATER_THAN;
        ACCEPT[10] = TokenType.GREATER_THAN_OR_EQUAL;
        ACCEPT[11] = TokenType.ADD;
        ACCEPT[12] = TokenType.INCREMENT;
        ACCEPT[13] = TokenType.SUBTRACT;
        ACCEPT[14] = TokenType.DECREMENT;
        ACCEPT[15] = TokenType.MULTIPLY;
        ACCEPT[16] = TokenType.DIVIDE;
        ACCEPT[17] = TokenType.INTEGER;
        ACCEPT[18] = TokenType.CLOSING_PARENTHESIS;
        ACCEPT[19] = TokenType.OPENING_PARENTHESIS;
        ACCEPT[20] = TokenType.CLOSING_CURLY_BRACKET;
        ACCEPT[21] = TokenType.OPENING_CURLY_BRACKET;
        ACCEPT[44] = TokenType.DOUBLE;
        ACCEPT[45] = TokenType.DOUBLE;

        // handle variables
        setStateValues(0, 'a', 'z', 1);
        setStateValues(0, 'A', 'Z', 1);
        setStateValues(1, 'a', 'z', 1);
        setStateValues(1, 'A', 'Z', 1);

        // handle numbers
        setStateValues(0, '0', '9', 17);
        setStateValues(17, '0', '9', 17);

        setStateValues(44, '0', '9', 44);

        // handle keywords
        addKeyword("for", TokenType.KEYWORD_FOR, new int[]{22, 23, 24});
        addKeyword("if", TokenType.KEYWORD_IF, new int[]{25, 26});
        addKeyword("int", TokenType.KEYWORD_INT, new int[]{25, 27, 28});
        addKeyword("while", TokenType.KEYWORD_WHILE, new int[]{29, 30, 31, 32, 33});
        addKeyword("else", TokenType.KEYWORD_ELSE, new int[]{34, 35, 36, 37});
        addKeyword("double", TokenType.KEYWORD_DOUBLE, new int[]{38, 39, 40, 41, 42, 43});

        FSM[0][';'] = 2;

        FSM[0]['!'] = 3;
        FSM[3]['='] = 4;

        FSM[0]['='] = 5;
        FSM[5]['='] = 6;

        FSM[0]['<'] = 7;
        FSM[7]['='] = 8;

        FSM[0]['>'] = 9;
        FSM[9]['='] = 10;

        FSM[0]['+'] = 11;
        FSM[11]['+'] = 12;

        FSM[0]['-'] = 13;
        FSM[13]['-'] = 14;

        FSM[0]['*'] = 15;

        FSM[0]['/'] = 16;

        FSM[17]['.'] = 44;

        FSM[0][')'] = 18;

        FSM[0]['('] = 19;

        FSM[0]['}'] = 20;

        FSM[0]['{'] = 21;
    }

    private static void addKeyword(String keyword, TokenType type, int[] states) {
        if (keyword.length() != states.length) {
            throw new IllegalArgumentException("Keyword and states must have the same length");
        }

        FSM[0][keyword.charAt(0)] = states[0];

        for (int i = 0; i < states.length - 1; i++) {
            setStateValues(states[i], 'a', 'z', 1);
            setStateValues(states[i], 'A', 'Z', 1);

            FSM[states[i]][keyword.charAt(i + 1)] = states[i + 1];
            ACCEPT[states[i]] = TokenType.IDENTIFIER;
        }

        setStateValues(states[states.length - 1], 'a', 'z', 1);
        setStateValues(states[states.length - 1], 'A', 'Z', 1);
        ACCEPT[states[states.length - 1]] = type;
    }

    public static List<Token> tokenizeInput(String filename) throws FileNotFoundException {
        StringBuilder input = new StringBuilder();
        File inputFile = new File(filename);
        Scanner sc = new Scanner(inputFile);

        while (sc.hasNextLine()) {
            input.append(sc.nextLine());
        }
        sc.close();

        List<Token> tokens = new ArrayList<>();
        int state = 0;
        StringBuilder currentToken = new StringBuilder();

        for (char ch : input.toString().toCharArray()){
            if (ch < INPUTS) {
                int oldState = state;
                state = FSM[oldState][ch];

                if (state != 0) {
                    currentToken.append(ch);
                } else {
                    if(!currentToken.isEmpty()){
                        String tokenText = currentToken.toString();

                        if (ACCEPT[oldState] != null) {
                            tokens.add(new Token(ACCEPT[oldState], tokenText));
                        } else {
                            System.out.println("Unaccepted token '" + tokenText + "'");
                        }
                    }

                    currentToken.setLength(0);
                    state = FSM[0][ch];

                    if (state != 0) {
                        currentToken.append(ch);
                    } else if (ch != ' ') {
                        System.out.println("Unaccepted character '" + ch + "'");
                    }
                }
            }
        }

        if (!currentToken.isEmpty()) {
            String tokenText = currentToken.toString();

            if (ACCEPT[state] != null) {
                tokens.add(new Token(ACCEPT[state], tokenText));
            } else {
                System.out.println("Unaccepted token '" + tokenText + "'");
            }
        }

        return tokens;
    }

    private static void setStateValues(int state, int lower, int upper, int value) {
        for (int i = lower; i <= upper; i++) {
            if(FSM[state][i] == 0) {
                FSM[state][i] = value;
            }
        }
    }

    // call the input method + print the results
    public static void main(String[] args) throws FileNotFoundException {
        initializeStates();
        List<Token> tokens = tokenizeInput("input.txt");
        System.out.println("Tokens: " + tokens);
    }
}
