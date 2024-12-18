package compiler.frontend;

import compiler.common.AtomOperation;
import compiler.common.Operation;

import java.util.HashMap;
import java.util.List;

public class GlobalOptimizer {

    public static void run(List<AtomOperation> atoms) {
        removeBetweenJumpAndLabel(atoms);
        removeUnreachableConditions(atoms);
    }

    private static void removeBetweenJumpAndLabel(List<AtomOperation> atoms) {
        int i = 0;
        boolean jumpFlag = false;

        while(i < atoms.size()) {
            if(atoms.get(i).getOp() == Operation.JMP) {
                jumpFlag = true;
            }

            while (jumpFlag && atoms.get(i).getOp() != Operation.LBL) {
                atoms.remove(i);
                jumpFlag = false;
            }

            i++;
        }
    }

    private static void removeUnreachableConditions(List<AtomOperation> atoms) {
        HashMap<String, Double> variables = new HashMap<>();
        int i = 0;

        while(i < atoms.size()) {
            // TODO: if variable is an integer do not allow floating point values
            AtomOperation atom = atoms.get(i);
            Operation op = atom.getOp();

            double left = parseFactor(atom.getLeft(), variables);
            double right = parseFactor(atom.getRight(), variables);

            switch (op) {
                case ADD:
                    variables.put(atom.getResult(), left + right);
                    break;
                case SUB:
                    variables.put(atom.getResult(), left - right);
                    break;
                case MUL:
                    variables.put(atom.getResult(), left * right);
                    break;
                case DIV:
                    variables.put(atom.getResult(), left / right);
                    break;
                case MOV:
                    variables.put(atom.getDest(), left);
                    break;
                case TST:
                    if(!testComparison(left, right, atom.getCmp())) {
                        while(atoms.get(i).getOp() != Operation.LBL) {
                            atoms.remove(i);
                        }
                    }
            }

            i++;
        }
    }

    private static boolean testComparison(double left, double right, String cmp) {
        return switch (cmp) {
            case "0" -> false;
            case "1" -> left != right;
            case "2" -> left >= right;
            case "3" -> left <= right;
            case "4" -> left > right;
            case "5" -> left < right;
            case "6" -> left == right;
            default -> false;
        };
    }

    private static double parseFactor(String factor, HashMap<String, Double> variables) {
        if(factor.isEmpty()) return 0;
        if(Character.isDigit(factor.charAt(0))) return Double.parseDouble(factor);

        return variables.getOrDefault(factor, 0.0);
    }

}
