import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Objects;

public class CodeGenerator {
    public static byte[] generateMachineCode(List<AtomOperation> atoms) {
        ByteArrayOutputStream machineCode = new ByteArrayOutputStream();

        for (AtomOperation atom : atoms) {
            machineCode.writeBytes(translateAtomToMachineCode(atom));
        }

        return machineCode.toByteArray();
    }

    private static byte[] translateAtomToMachineCode(AtomOperation atom) {
        int r = 0;

        switch (atom.getOp()) {
            case Operation.MOV:
                if(atom.getRight().isEmpty()) {
                    if(Objects.equals(atom.getLeft(), "0")) {
                        // If setting a value to 0 -> CLR
                        return encodeInstruction(MachineOperation.CLR, 0, r, 0);
                    } else {
                        // Otherwise STO
                        return encodeInstruction(MachineOperation.STO, 0, r, 0);
                    }
                }
//            case ADD:
//                a = Integer.parseInt(atom.getRight());
//                return encodeInstruction(opcode, cmp, r, a);
//            case SUB:
//                a = Integer.parseInt(atom.getRight());
//                return encodeInstruction(opcode, cmp, r, a);
//            case MUL:
//                a = Integer.parseInt(atom.getRight());
//                return encodeInstruction(opcode, cmp, r, a);
//            case DIV:
//                a = Integer.parseInt(atom.getRight());
//                return encodeInstruction(opcode, cmp, r, a);
//            case JMP:
//                a = Integer.parseInt(atom.getRight());
//                return encodeInstruction(opcode, cmp, r, a);
//            case CMP:
//                cmp = getComparison(atom.getCmp());
//                a = Integer.parseInt(atom.getRight());
//                return encodeInstruction(opcode, cmp, r, a);
//            case LOD:
//                a = Integer.parseInt(atom.getRight());
//                return encodeInstruction(opcode, cmp, r, a);
//            case STO:
//                a = Integer.parseInt(atom.getRight());
//                return encodeInstruction(opcode, cmp, r, a);
//            case HLT:
//                return encodeInstruction(opcode, cmp, r, 0);
            default:
                throw new UnsupportedOperationException("Unknown operation: " + atom.getOp());
        }
    }

    private static int getComparison(String cmp) {
        switch (cmp) {
            case "always true":
                return 0;
            case "equal":
                return 1;
            case "lesser":
                return 2;
            case "greater":
                return 3;
            case "lesser or equal":
                return 4;
            case "greater or equal":
                return 5;
            case "unequal":
                return 6;
            default:
                throw new IllegalArgumentException("Unknown comparison: " + cmp);
        }
    }

    // Translates the Instruction to a Byte Array -> Absolute Mode
    private static byte[] encodeInstruction(MachineOperation operation, int cmp, int r, int a) {
        int instruction = (operation.ordinal() << 28) | (cmp << 24) | (r << 20) | a;
        return new byte[]{
                (byte) (instruction >> 24),
                (byte) (instruction >> 16),
                (byte) (instruction >> 8),
                (byte) instruction
        };
    }
}