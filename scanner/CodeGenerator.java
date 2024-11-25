import java.io.ByteArrayOutputStream;
import java.util.List;

public class CodeGenerator {
    public static byte[] generateMachineCode(List<AtomOperation> atoms) {
        ByteArrayOutputStream machineCode = new ByteArrayOutputStream();

        for (AtomOperation atom : atoms) {
            machineCode.writeBytes(translateAtomToMachineCode(atom));
        }

        return machineCode.toByteArray();
    }

    private static byte[] translateAtomToMachineCode(AtomOperation atom) {
        int opcode = getOpcode(atom.getOp());
        int mode = 0; // Absolute mode
        int cmp = 0; // Default comparison
        int r = 0; // Default register
        int a = 0; // Default address

        switch (atom.getOp()) {
            case CLR:
                return encodeInstruction(opcode, mode, cmp, r, 0);
            case ADD:
                return encodeInstruction(opcode, mode, cmp, r, Integer.parseInt(atom.getRight()));
            case SUB:
                return encodeInstruction(opcode, mode, cmp, r, Integer.parseInt(atom.getRight()));
            case MUL:
                return encodeInstruction(opcode, mode, cmp, r, Integer.parseInt(atom.getRight()));
            case DIV:
                return encodeInstruction(opcode, mode, cmp, r, Integer.parseInt(atom.getRight()));
            case JMP:
                return encodeInstruction(opcode, mode, cmp, r, Integer.parseInt(atom.getDest()));
            case CMP:
                cmp = getComparison(atom.getCmp());
                return encodeInstruction(opcode, mode, cmp, r, Integer.parseInt(atom.getRight()));
            case LOD:
                return encodeInstruction(opcode, mode, cmp, r, Integer.parseInt(atom.getRight()));
            case STO:
                return encodeInstruction(opcode, mode, cmp, r, Integer.parseInt(atom.getRight()));
            case HLT:
                return encodeInstruction(opcode, mode, cmp, r, 0);
            default:
                throw new UnsupportedOperationException("Unknown operation: " + atom.getOp());
        }
    }

    private static int getOpcode(Operation op) {
        switch (op) {
            case CLR: return 0;
            case ADD: return 1;
            case SUB: return 2;
            case MUL: return 3;
            case DIV: return 4;
            case JMP: return 5;
            case CMP: return 6;
            case LOD: return 7;
            case STO: return 8;
            case HLT: return 9;
            default: throw new IllegalArgumentException("Unknown operation: " + op);
        }
    }

    private static int getComparison(String cmp) {
        switch (cmp) {
            case "always true": return 0;
            case "equal": return 1;
            case "lesser": return 2;
            case "greater": return 3;
            // case "lesser or equal": return 4;
            // case "greater or equal": return 5;
            // case "unequal": return 6;
            default: throw new IllegalArgumentException("Unknown comparison: " + cmp);
        }
    }

    // Translates the instruction to a byte array
    private static byte[] encodeInstruction(int opcode, int mode, int cmp, int r, int a) {
        int instruction = (opcode << 28) | (mode << 27) | (cmp << 24) | (r << 20) | a;
        return new byte[] {
            (byte) (instruction >> 24),
            (byte) (instruction >> 16),
            (byte) (instruction >> 8),
            (byte) instruction
        };
    }
}