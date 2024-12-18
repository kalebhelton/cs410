package compiler;

import compiler.backend.CodeGenerator;
import compiler.backend.Memory;
import compiler.common.AtomOperation;
import compiler.frontend.ParserProject;
import compiler.frontend.ScannerProject;
import compiler.frontend.Token;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.List;

public class CompilerMain {
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

        System.out.printf(
                "Atoms:\n%s\n",
                String.join("\n", atoms.stream().map(AtomOperation::toString).toArray(String[]::new))
        );

        // Code Generator Implementation
        Memory memory = new Memory();
        CodeGenerator codeGenerator = new CodeGenerator(memory);

        codeGenerator.generateMachineCode(atoms);

        byte[] memoryBytes = memory.encode();
        codeGenerator.printMachineCode(memoryBytes);

        FileOutputStream writer = new FileOutputStream("machineCode.bin");
        writer.write(memoryBytes);
        writer.close();
    }
}