package compiler;

import compiler.backend.CompilerBackend;
import compiler.common.CommandLineArguments;
import compiler.frontend.CompilerFrontend;

public class CompilerMain {
    public static void main(String[] args) throws Exception {
        CommandLineArguments commandLineArguments = new CommandLineArguments(args);

        switch (commandLineArguments.getMode()) {
            case 0:
                CompilerFrontend.main(args);
                break;
            case 1:
                CompilerBackend.main(args);
                break;
            case 2:
                // TODO: call virtual machine
                break;
            default:
        }
    }
}