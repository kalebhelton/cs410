package compiler;

import java.io.FileOutputStream;
import java.util.List;

public class CompilerMain {
    public static void main(String[] args) throws Exception {
        ScannerProject.initializeStates();
        List<Token> tokens = ScannerProject.tokenizeInput("input.txt");
        ParserProject parser = new ParserProject(tokens);
        List<AtomOperation> atoms = parser.parse();

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