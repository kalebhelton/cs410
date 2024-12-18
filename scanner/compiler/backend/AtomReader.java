package compiler.backend;

import compiler.common.AtomOperation;
import compiler.common.Operation;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AtomReader implements Closeable {

    private final BufferedReader bufferedReader;

    public AtomReader(String filename) throws FileNotFoundException {
        this.bufferedReader = new BufferedReader(new FileReader(filename));
    }

    public List<AtomOperation> readAtoms() throws IOException {
        List<AtomOperation> atoms = new ArrayList<>();
        String line;

        while((line = bufferedReader.readLine()) != null) {
            atoms.add(parseAtom(line));
        }

        return atoms;
    }

    public void close() throws IOException {
        bufferedReader.close();
    }

    private AtomOperation parseAtom(String line) {
        String[] atomInfo = line
                .replace("(", "")
                .replace(")", "")
                .split(", ");
        Operation operation = Operation.valueOf(atomInfo[0]);

        return switch (operation) {
            case ADD, SUB, MUL, DIV, NEG, MOV ->
                    new AtomOperation(operation, atomInfo[1], atomInfo[2], atomInfo[3], null, null);
            case TST, JMP, LBL ->
                    new AtomOperation(operation, atomInfo[1], atomInfo[2], atomInfo[3], atomInfo[4], atomInfo[5]);
        };
    }

}
