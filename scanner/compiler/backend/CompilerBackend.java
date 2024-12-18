package compiler.backend;

import compiler.common.AtomOperation;
import compiler.common.CommandLineArguments;

import java.io.FileOutputStream;
import java.util.List;

public class CompilerBackend {
    public static void main(String[] args) throws Exception {
        CommandLineArguments commandLineArguments = new CommandLineArguments(args);

        AtomReader atomReader = new AtomReader(commandLineArguments.getInput());
        List<AtomOperation> atoms = atomReader.readAtoms();
        atomReader.close();

        // Code Generator Implementation
        Memory memory = new Memory();
        CodeGenerator codeGenerator = new CodeGenerator(memory);

        if(commandLineArguments.getDoLocalOptimization()) {
            System.out.println("Do local optimization");
        }

        codeGenerator.generateMachineCode(atoms);
        byte[] memoryBytes = memory.encode();

        FileOutputStream writer = new FileOutputStream(commandLineArguments.getOutput());
        writer.write(memoryBytes);
        writer.close();
    }

}
