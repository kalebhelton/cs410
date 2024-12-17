package compiler;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.sql.SQLOutput;
import java.util.List;
import java.util.Scanner;

public class CompilerMain {
    public static void main(String[] args) throws Exception {
        ScannerProject.initializeStates();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter an input file path:");
        String inputPath = scanner.nextLine();
        System.out.println("Enter an output file name:");
        String outputPath = scanner.nextLine();

        List<Token> tokens = ScannerProject.tokenizeInput(inputPath);
        ParserProject parser = new ParserProject(tokens);
        List<AtomOperation> atoms = parser.parse();
        FileWriter atomFile = new FileWriter(outputPath);

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