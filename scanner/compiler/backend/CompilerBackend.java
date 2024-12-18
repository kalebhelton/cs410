package compiler.backend;

import compiler.common.AtomOperation;

import java.io.FileOutputStream;
import java.util.List;

public class CompilerBackend {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java -jar CompilerMain <input file> <output file>");
            return;
        }

        AtomReader atomReader = new AtomReader(args[0]);
        List<AtomOperation> atoms = atomReader.readAtoms();
        atomReader.close();

        // Code Generator Implementation
        Memory memory = new Memory();
        CodeGenerator codeGenerator = new CodeGenerator(memory);

        codeGenerator.generateMachineCode(atoms);
        byte[] memoryBytes = memory.encode();

        FileOutputStream writer = new FileOutputStream(args[1]);
        writer.write(memoryBytes);
        writer.close();
    }
}
