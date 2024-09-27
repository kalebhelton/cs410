/*
 * Authors: Sierra Jackson, Kaleb Helton, Simon Hale, Luke Graham
 * Reviewers: Emily Krugman, Taylor Oxley*/


import java.util.ArrayList;
import java.util.List;


public class Scanner {

    // The inputs are all ascii values (0-127) -> 128 inputs
    private static final int INPUTS = 128;
    private static final int STATES = 44;
    private static final boolean[] ACCEPT = new boolean[STATES];
    private static final int[][] FSM = new int[STATES][INPUTS];

	public static void initializeStates() {
        ACCEPT[1] = true;
        ACCEPT[2] = true;
        ACCEPT[4] = true;
        ACCEPT[5] = true;
        ACCEPT[6] = true;
        ACCEPT[7] = true;
        ACCEPT[8] = true;
        ACCEPT[9] = true;
        ACCEPT[10] = true;
        ACCEPT[11] = true;
        ACCEPT[12] = true;
        ACCEPT[13] = true;
        ACCEPT[14] = true;
        ACCEPT[15] = true;
        ACCEPT[16] = true;
        ACCEPT[17] = true;
        ACCEPT[18] = true;
        ACCEPT[19] = true;
        ACCEPT[20] = true;
        ACCEPT[21] = true;

        // handle variables
        setStateValues(0, 'a', 'z', 1);
        setStateValues(0, 'A', 'Z', 1);
        setStateValues(1, 'a', 'z', 1);
        setStateValues(1, 'A', 'Z', 1);

        // handle numbers
        setStateValues(0, '0', '9', 17);
        setStateValues(17, '0', '9', 17);

        // handle keywords
        addKeyword("for", new int[]{22, 23, 24});
        addKeyword("if", new int[]{25, 26});
        addKeyword("int", new int[]{25, 27, 28});
        addKeyword("while", new int[]{29, 30, 31, 32, 33});
        addKeyword("else", new int[]{34, 35, 36, 37});
        addKeyword("double", new int[]{38, 39, 40, 41, 42, 43});

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

        FSM[0][')'] = 18;

        FSM[0]['('] = 19;

        FSM[0]['}'] = 20;

        FSM[0]['{'] = 21;
    }

    private static void addKeyword(String keyword, int[] states) {
        if (keyword.length() != states.length) {
            throw new IllegalArgumentException("Keyword and states must have the same length");
        }

        FSM[0][keyword.charAt(0)] = states[0];

        for (int i = 0; i < states.length - 1; i++) {
            setStateValues(states[i], 'a', 'z', 1);
            setStateValues(states[i], 'A', 'Z', 1);

            FSM[states[i]][keyword.charAt(i + 1)] = states[i + 1];
        }

        setStateValues(states[states.length - 1], 'a', 'z', 1);
        setStateValues(states[states.length - 1], 'A', 'Z', 1);

        for (int state : states) {
            ACCEPT[state] = true;
        }
    }

    public static List<String> tokenizeInput(){

        System.out.println("Enter input to tokenize: ");
        String input = System.console().readLine();

        List<String> tokens = new ArrayList<>();
        int state = 0;
        StringBuilder currentToken = new StringBuilder();

        for (char ch : input.toCharArray()){
            if (ch < INPUTS) {
                int oldState = state;
                state = FSM[oldState][ch];

                if (state != 0) {
                    currentToken.append(ch);
                } else {
                    if(!currentToken.isEmpty()){
                        String tokenText = currentToken.toString();

                        if (ACCEPT[oldState]) {
                            tokens.add(tokenText);
                        } else {
                            System.out.println("Unaccepted token '" + tokenText + "'");
                        }

                    }

                    currentToken.setLength(0);

                    if (oldState != 0) {
                        currentToken.append(ch);
                    } else {
                        System.out.println("Unaccepted token '" + ch + "'");
                    }

                    state = FSM[0][ch];
                }

            }
        }

        if (!currentToken.isEmpty()){
            String tokenText = currentToken.toString();

            if (ACCEPT[state]) {
                tokens.add(tokenText);
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
    public static void main(String[] args) {
        initializeStates();
        List<String> tokens = tokenizeInput();
        System.out.println("Tokens: " + tokens);
    }

}
