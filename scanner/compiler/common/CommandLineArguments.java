package compiler.common;

public class CommandLineArguments {

    private String input;
    private String output;
    private boolean doGlobalOptimization;
    private boolean doLocalOptimization;

    public CommandLineArguments(String[] args) {
        this.parseArguments(args);
        this.validateArguments();
    }

    private void parseArguments(String[] args) {
        for(int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-g", "--global" -> doGlobalOptimization = true;
                case "-l", "--local" -> doLocalOptimization = true;
                case "-i", "--input" -> {
                    if (i < args.length - 1 && !args[i + 1].startsWith("-")) {
                        input = args[++i];
                    } else {
                        throw new IllegalArgumentException("Missing value for option %s".formatted(args[i]));
                    }
                }
                case "-o", "--output" -> {
                    if (i < args.length - 1 && !args[i + 1].startsWith("-")) {
                        output = args[++i];
                    } else {
                        throw new IllegalArgumentException("Missing value for option %s".formatted(args[i]));
                    }
                }
            }
        }
    }

    private void validateArguments() {
        if(input == null) {
            throw new IllegalArgumentException("Missing argument: input");
        }

        if(output == null) {
            throw new IllegalArgumentException("Missing argument: output");
        }
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }

    public boolean getDoGlobalOptimization() {
        return doGlobalOptimization;
    }

    public boolean getDoLocalOptimization() {
        return doLocalOptimization;
    }
}
