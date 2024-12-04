import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CodeGenerator {

    private final HashMap<String, Long> memory = new HashMap<>();
    private final ByteArrayOutputStream machineCode = new ByteArrayOutputStream();

    public CodeGenerator() {
        initializeRegisterMemory();
    }

    public void printMachineCode(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if (i % 4 == 0) {
                System.out.println();
            }

            System.out.print(String.format("%8s", Integer.toBinaryString(bytes[i] & 0xFF)).replace(' ', '0'));
        }
    }

    public byte[] generateMachineCode(List<AtomOperation> atoms) {
        for (AtomOperation atom : atoms) {
            translateAtomToMachineCode(atom);
        }

        return machineCode.toByteArray();
    }

    private void translateAtomToMachineCode(AtomOperation atom) {
        switch (atom.getOp()) {
            case Operation.ADD:
                encodeMathOperation(MachineOperation.ADD, atom.getLeft(), atom.getRight(), atom.getResult());
                break;
            case Operation.SUB:
                encodeMathOperation(MachineOperation.SUB, atom.getLeft(), atom.getRight(), atom.getResult());
                break;
            case Operation.MUL:
                encodeMathOperation(MachineOperation.MUL, atom.getLeft(), atom.getRight(), atom.getResult());
                break;
            case Operation.DIV:
                encodeMathOperation(MachineOperation.DIV, atom.getLeft(), atom.getRight(), atom.getResult());
                break;
            case Operation.MOV:
                long a = getMemoryAddress(atom.getResult());

                if (Objects.equals(atom.getLeft(), "0")) {
                    // If setting a value to 0 -> CLR
                    encodeInstruction(MachineOperation.CLR, 0, 0, 0);
                }

                encodeInstruction(MachineOperation.STO, 0, 0, a);
                break;
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
    private void encodeInstruction(MachineOperation operation, int cmp, int r, long a) {
        int instruction = (operation.ordinal() << 28) | (cmp << 24) | (r << 20) | (int) a;

        machineCode.writeBytes(new byte[]{
                (byte) (instruction >> 24),
                (byte) (instruction >> 16),
                (byte) (instruction >> 8),
                (byte) instruction
        });
    }

    private void encodeMathOperation(MachineOperation operation, String left, String right, String result) {
        if (operation != MachineOperation.ADD &&
                operation != MachineOperation.SUB &&
                operation != MachineOperation.MUL &&
                operation != MachineOperation.DIV) {
            throw new IllegalArgumentException("Invalid math operation: " + operation);
        }

        if (Character.isDigit(left.charAt(0))) {
            if (Character.isDigit(right.charAt(0))) {
                // number <op> number
                encodeInstruction(MachineOperation.STO, 0, 1, getRegisterMemoryAddress(1));
                encodeInstruction(operation, 0, 0, getRegisterMemoryAddress(1));
                encodeInstruction(MachineOperation.STO, 0, 0, getMemoryAddress(result));
            } else {
                // number <op> variable
                encodeInstruction(operation, 0, 0, getMemoryAddress(right));
                encodeInstruction(MachineOperation.STO, 0, 0, getMemoryAddress(result));
            }
        } else {
            if (Character.isDigit(right.charAt(0))) {
                // variable <op> number
                encodeInstruction(MachineOperation.LOD, 0, 1, getMemoryAddress(left));
                encodeInstruction(MachineOperation.STO, 0, 0, getRegisterMemoryAddress(0));
                encodeInstruction(operation, 0, 1, getRegisterMemoryAddress(0));
                encodeInstruction(MachineOperation.STO, 0, 1, getMemoryAddress(result));
            } else {
                // variable <op> variable
                encodeInstruction(MachineOperation.LOD, 0, 0, getMemoryAddress(left));
                encodeInstruction(operation, 0, 0, getMemoryAddress(right));
                encodeInstruction(MachineOperation.STO, 0, 0, getMemoryAddress(result));
            }
        }
    }

    private void initializeRegisterMemory() {
        for (int i = 0; i < 16; i++) {
            memory.put(String.valueOf(i), (long) Math.pow(2, 20) - 1 - i);
        }
    }

    private long getMemoryAddress(String symbol) {
        if (!memory.containsKey(symbol)) {
            memory.put(symbol, (long) memory.size() - 16);
        }

        return memory.get(symbol);
    }

    private long getRegisterMemoryAddress(int register) {
        return getMemoryAddress(String.valueOf(register));
    }

}