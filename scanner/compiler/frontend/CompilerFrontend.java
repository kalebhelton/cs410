package compiler.frontend;

import compiler.common.AtomOperation;
import compiler.common.CommandLineArguments;

import java.io.FileWriter;
import java.util.List;

public class CompilerFrontend {
    public static void main(String[] args) throws Exception {
        CommandLineArguments commandLineArguments = new CommandLineArguments(args);
        ScannerProject.initializeStates();

        List<Token> tokens = ScannerProject.tokenizeInput(commandLineArguments.getInput());
        ParserProject parser = new ParserProject(tokens);
        List<AtomOperation> atoms = parser.parse();

        if(commandLineArguments.getDoGlobalOptimization()) {
            GlobalOptimizer.run(atoms);
        }

        FileWriter atomFile = new FileWriter(commandLineArguments.getOutput());

        for (AtomOperation atom : atoms) {
            atomFile.write(atom.toString());
            atomFile.write(System.lineSeparator());
        }
        atomFile.close();
    }
}
