package compiler.frontend;

import compiler.common.AtomOperation;

import java.io.FileWriter;
import java.util.List;

public class CompilerFrontend {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java -jar CompilerMain <input file> <output file>");
            return;
        }

        ScannerProject.initializeStates();

        // frontend
        List<Token> tokens = ScannerProject.tokenizeInput(args[0]);
        ParserProject parser = new ParserProject(tokens);
        List<AtomOperation> atoms = parser.parse();
        FileWriter atomFile = new FileWriter(args[1]);

        for (AtomOperation atom : atoms) {
            atomFile.write(atom.toString());
            atomFile.write(System.lineSeparator());
        }
        atomFile.close();
    }
}
