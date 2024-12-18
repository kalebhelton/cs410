package compiler.frontend;

import compiler.common.AtomOperation;
import compiler.common.Operation;

import java.util.List;

public class GlobalOptimizer {

    public static List<AtomOperation> run(List<AtomOperation> atoms) {
        int i = 0;
        boolean jumpFlag = false;

        while(i < atoms.size()) {
            while (jumpFlag && atoms.get(i).getOp() != Operation.LBL) {
                atoms.remove(i);
                jumpFlag = false;
            }

            if(atoms.get(i).getOp() == Operation.JMP) {
                jumpFlag = true;
            }

            i++;
        }

        return atoms;
    }

}
