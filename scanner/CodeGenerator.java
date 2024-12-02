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
        int mode = atom.isDisplacementMode() ? 1 : 0; // Displacement mode
        int cmp = 0; // Default comparison
        int r = Integer.parseInt(atom.getResult()); // Default register
        int a = 0; // Default address
        int rx = 0;  // For Displacement Mode
        int ry = 0;  // For Displacement Mode
        int d = 0;  // For Displacement Mode

        switch (atom.getOp()) {
            case CLR:
                return encodeInstruction(opcode, mode, cmp, r, a);

            case ADD:
                if (mode == 0){  // Absolute Mode
                    a = Integer.parseInt(atom.getRight());
                    return encodeInstruction(opcode, mode, cmp, r, a);
                }

                else{  // Displacement Mode
                    rx = Integer.parseInt(atom.getResult());
                    ry = Integer.parseInt(atom.getLeft());
                    d = Integer.parseInt(atom.getRight());
                    return encodeInstruction(opcode, mode, cmp, rx, ry, d);
                }
               
            case SUB:
                if (mode == 0){
                    a = Integer.parseInt(atom.getRight());
                    return encodeInstruction(opcode, mode, cmp, r, a);
                }

                else{
                    rx = Integer.parseInt(atom.getResult());
                    ry = Integer.parseInt(atom.getLeft());
                    d = Integer.parseInt(atom.getRight());
                    return encodeInstruction(opcode, mode, cmp, rx, ry, d);
                }
                
            case MUL:
                if (mode == 0){
                    a = Integer.parseInt(atom.getRight());
                    return encodeInstruction(opcode, mode, cmp, r, a);
                }

                else{
                    rx = Integer.parseInt(atom.getResult());
                    ry = Integer.parseInt(atom.getLeft());
                    d = Integer.parseInt(atom.getRight());
                    return encodeInstruction(opcode, mode, cmp, rx, ry, d);
                }
               
            case DIV:
                if (mode == 0){
                    a = Integer.parseInt(atom.getRight());
                    return encodeInstruction(opcode, mode, cmp, r, a);
                }

                else{
                    rx = Integer.parseInt(atom.getResult());
                    ry = Integer.parseInt(atom.getLeft());
                    d = Integer.parseInt(atom.getRight());
                    return encodeInstruction(opcode, mode, cmp, rx, ry, d);
                }

            case JMP:
                if (mode == 0){
                        a = Integer.parseInt(atom.getRight());
                        return encodeInstruction(opcode, mode, cmp, r, a);
                    }

                    else{
                        rx = Integer.parseInt(atom.getResult());
                        ry = Integer.parseInt(atom.getLeft());
                        d = Integer.parseInt(atom.getRight());
                        return encodeInstruction(opcode, mode, cmp, rx, ry, d);
                    }

            case CMP:
                cmp = getComparison(atom.getCmp());
                if (mode == 0){
                    a = Integer.parseInt(atom.getRight());
                    return encodeInstruction(opcode, mode, cmp, r, a);
                }

                else{
                    rx = Integer.parseInt(atom.getResult());
                    ry = Integer.parseInt(atom.getLeft());
                    d = Integer.parseInt(atom.getRight());
                    return encodeInstruction(opcode, mode, cmp, rx, ry, d);
                }

            case LOD:
                if (mode == 0){
                    a = Integer.parseInt(atom.getRight());
                    return encodeInstruction(opcode, mode, cmp, r, a);
                }

                else{
                    rx = Integer.parseInt(atom.getResult());
                    ry = Integer.parseInt(atom.getLeft());
                    d = Integer.parseInt(atom.getRight());
                    return encodeInstruction(opcode, mode, cmp, rx, ry, d);
                }

            case STO:
                if (mode == 0){
                    a = Integer.parseInt(atom.getRight());
                    return encodeInstruction(opcode, mode, cmp, r, a);
                }

                else{
                    rx = Integer.parseInt(atom.getResult());
                    ry = Integer.parseInt(atom.getLeft());
                    d = Integer.parseInt(atom.getRight());
                    return encodeInstruction(opcode, mode, cmp, rx, ry, d);
                }

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
            case "lesser or equal": return 4;
            case "greater or equal": return 5;
            case "unequal": return 6;
            default: throw new IllegalArgumentException("Unknown comparison: " + cmp);
        }
    }

    // Translates the Instruction to a Byte Array -> Absolute Mode
    private static byte[] encodeInstruction(int opcode, int mode, int cmp, int r, int a){
        int instruction = (opcode << 28) | (mode << 27) | (cmp << 24) | (r << 20) | a;
        return new byte[] {
            (byte) (instruction >> 24), 
            (byte) (instruction >> 16), 
            (byte) (instruction >> 8),
            (byte) instruction
        };
    }

    // Translates the instruction to a byte array for Displacement Mode
    private static byte[] encodeInstruction(int opcode, int mode, int cmp, int rx, int ry, int d) {
        int instruction = (opcode << 28) | (mode << 27) | (cmp << 24) | (rx << 16) | (ry << 12) | d;
        return new byte[] {
            (byte) (instruction >> 24),
            (byte) (instruction >> 16),
            (byte) (instruction >> 8),
            (byte) instruction
        };
    }
}