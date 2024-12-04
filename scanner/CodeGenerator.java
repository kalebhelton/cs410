import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CodeGenerator {

    private final HashMap<String, Long> memory = new HashMap<>();

    public CodeGenerator() {}

    public void printMachineCode(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if(i % 4 == 0) {
                System.out.println();
            }

            System.out.print(String.format("%8s", Integer.toBinaryString(bytes[i] & 0xFF)).replace(' ', '0'));
        }
    }

    public byte[] generateMachineCode(List<AtomOperation> atoms) {
        ByteArrayOutputStream machineCode = new ByteArrayOutputStream();

        for (AtomOperation atom : atoms) {
            machineCode.writeBytes(translateAtomToMachineCode(atom));
        }

        return machineCode.toByteArray();
    }

    private byte[] translateAtomToMachineCode(AtomOperation atom) {
        switch (atom.getOp()) {
            case Operation.MOV:
                long r = getMemoryAddress(atom.getResult());

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

    private int getComparison(String cmp) {
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
    private byte[] encodeInstruction(MachineOperation operation, int cmp, long r, int a) {
        int instruction = (operation.ordinal() << 28) | (cmp << 24) | ((int) r << 20) | a;
        return new byte[]{
                (byte) (instruction >> 24),
                (byte) (instruction >> 16),
                (byte) (instruction >> 8),
                (byte) instruction
        };
    }

    private long getMemoryAddress(String symbol) {
        if(memory.containsKey(symbol)) {
            return memory.get(symbol);
        }

        memory.put(symbol, (long) memory.size());

        return memory.size() - 1;
    }
}