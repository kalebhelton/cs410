import java.io.FileNotFoundException;
import java.util.List;

public class CompilerMain {
    public static void main(String[] args) throws FileNotFoundException {
        ScannerProject.initializeStates();
        List<ScannerProject.Token> tokens = ScannerProject.tokenizeInput("input.txt");
        ParserProject parser = new ParserProject(tokens);
        List<AtomOperations> atom = parser.parse();
        System.out.println("Atoms: " + atom);
    }
}
