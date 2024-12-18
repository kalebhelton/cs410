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

    // private static void removeBetweenJumpAndLabel(List<AtomOperation> atoms) {
    //     int i = 0;

    //     while(i < atoms.size()) {
    //         if(atoms.get(i).getOp() == Operation.JMP) {
    //             while (i < atoms.size() && atoms.get(i).getOp() != Operation.LBL) {
    //                 atoms.remove(i);
    //             }
    //         }

    //         i++;
    //     }
    // }
    private static void removeBetweenJumpAndLabel(List<AtomOperation> atoms) {
        int i = 0;
        boolean jumpFound = false;

        while(i < atoms.size()) {
            //if a jump operation is found set the flag but continue to look at the next op
            if(atoms.get(i).getOp() == Operation.JMP) {
                jumpFound = true;
                i++;
                continue;
            }
            //if the flag is set and the next op is lbl, turn the flag off and continue bc correct
            if(jumpFound && atoms.get(i).getOp() == Operation.LBL) {
                jumpFound = false;
                i++;
                continue;
            }
            //if jump was never found continue
            if(!jumpFound){
                i++;
                continue;
            }
            //otherwise code between jump and lbl so remove until lbl is found and flag is reset
            else{
                atoms.remove(i);
            }
            i++;
        }
    }

    private static void removeUnreachableConditions(List<AtomOperation> atoms) {
        HashMap<String, Double> variables = new HashMap<>();
        int i = 0;

        while(i < atoms.size()) {
            AtomOperation atom = atoms.get(i);
            Operation op = atom.getOp();

            double left = parseFactor(atom.getLeft(), variables);
            double right = parseFactor(atom.getRight(), variables);

            switch (op) {
                case ADD, SUB, MUL, DIV, MOV:
                    assignment(atom, variables);
                    break;
                case TST:
                    if(!testComparison(left, right, atom.getCmp())) {
                        AtomOperation previous = i > 0 ? atoms.get(i - 1) : null;
                        boolean isLoop = previous != null && previous.getOp() == Operation.LBL && (previous.getDest().contains("while") || previous.getDest().contains("for"));

                        if(isLoop) {
                            // Verify the label matches the test
                            String labelNumber = previous.getDest().substring(previous.getDest().lastIndexOf("_") + 1);

                            if(atom.getDest().contains(labelNumber)) {
                                atoms.remove(i - 1);
                                i--;
                            }
                        }

                        while(i < atoms.size() && atoms.get(i).getOp() != Operation.LBL) {
                            atoms.remove(i);
                        }

                        if(i < atoms.size()) {
                            atoms.remove(i);
                        }

                        // prevent incrementing i
                        continue;
                    } else {
                        i = simulateLoop(atoms, variables, i - 1);
                    }
            }

            i++;
        }
    }

    private static int simulateLoop(List<AtomOperation> atoms, HashMap<String, Double> variables, int labelIndex) {
        AtomOperation beforeLabel = atoms.get(labelIndex);
        boolean isLoop = beforeLabel != null && beforeLabel.getOp() == Operation.LBL && (beforeLabel.getDest().contains("while") || beforeLabel.getDest().contains("for"));

        if (!isLoop) {
            return labelIndex;
        }

        String labelNumber = beforeLabel.getDest().substring(beforeLabel.getDest().lastIndexOf("_") + 1);
        AtomOperation test = atoms.get(labelIndex + 1);
        double left = parseFactor(test.getLeft(), variables);
        double right = parseFactor(test.getRight(), variables);
        int i = labelIndex + 2;

        while (i < atoms.size() && testComparison(left, right, test.getCmp())) {
            if (atoms.get(i).getOp() == Operation.LBL || atoms.get(i).getOp() == Operation.JMP) {
                if (atoms.get(i).getDest().substring(atoms.get(i).getDest().lastIndexOf("_") + 1).equals(labelNumber)) {
                    i = labelIndex + 2;
                    continue;
                } else if (atoms.get(i).getOp() == Operation.LBL && atoms.get(i).getDest().startsWith("before")) {
                    i = simulateLoop(atoms, variables, i) + 1;
                }
            }

            assignment(atoms.get(i), variables);

            left = parseFactor(test.getLeft(), variables);
            right = parseFactor(test.getRight(), variables);
            i++;
        }

        return i;
    }

    private static void assignment(AtomOperation atom, HashMap<String, Double> variables) {
        double left = parseFactor(atom.getLeft(), variables);
        double right = parseFactor(atom.getRight(), variables);

        switch (atom.getOp()) {
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
                variables.put(atom.getResult(), left);
                break;
        }
    }

    private static boolean testComparison(double left, double right, String cmp) {
        return switch (cmp) {
            case "0" -> true;
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
