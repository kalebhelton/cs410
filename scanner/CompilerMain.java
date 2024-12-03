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
        byte[] machineCode = CodeGenerator.generateMachineCode(atoms);
        for (byte b: machineCode) {
            System.out.print(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
    }
}