package compiler.backend;

import compiler.common.AtomOperation;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CodeGenerator {
    private final HashMap<Integer, String> labelReferences = new HashMap<>();
    private final HashMap<String, Integer> labelTable = new HashMap<>();
    private final Memory memory;

    public CodeGenerator(Memory memory) {
        this.memory = memory;
    }

    /**
     * Prints the generated machine code
     */
    public void printMachineCode(byte[] machineCode) {
        for (int i = 0; i < machineCode.length; i++) {
            if (i % 4 == 0) {
                System.out.println();
            }

            System.out.print(String.format("%8s", Integer.toBinaryString(machineCode[i] & 0xFF)).replace(' ', '0'));
        }

        System.out.println();
    }


    private void optimizeLoadStore() {
        int[] programMemory = memory.getProgramMemory();
        int programMemorySize = memory.getProgramMemorySize();
    
        for (int i = 0; i < programMemorySize - 1; i++) {
            //Grabs the instructions, operations, and addresses
            int instruction1 = programMemory[i];
            int instruction2 = programMemory[i + 1];
    
            MachineOperation operation1 = MachineOperation.values()[(instruction1 >> 28) & 0xF];
            MachineOperation operation2 = MachineOperation.values()[(instruction2 >> 28) & 0xF];

            int address1 = instruction1 & 0xFFFFF;
            int address2 = instruction2 & 0xFFFFF;
    
            //Load and a store to the same address
            if (operation1 == MachineOperation.LOD && operation2 == MachineOperation.STO && address1 == address2) {
                //Remove redundant load
                for (int j = i; j < programMemorySize - 1; j++) {
                    programMemory[j] = programMemory[j + 1];
                }
                programMemorySize--; //Removes the load
                i--; //Recheck index
            }
            //Store and then load to the same address
            if (operation1 == MachineOperation.STO && operation2 == MachineOperation.LOD && address1 == address2) {
                //Remove redundant store and load
                for (int j = i; j < programMemorySize - 2; j++) {
                    programMemory[j] = programMemory[j + 2];
                }
                programMemorySize -= 2; //Removes the load and store
                i--; //Recheck index
            }
        }
    
        //Resets memory size
        memory.setProgramMemorySize(programMemorySize);
    }


    /**
     * Converts a list of atoms into machine code
     * @param atoms the atoms to convert
     */
    public void generateMachineCode(List<AtomOperation> atoms, boolean optimizeFlag) {
        memory.putInMemory(null, 1, true);

        for (AtomOperation atom : atoms) {
            translateAtomToMachineCode(atom);
        }

        encodeInstruction(MachineOperation.HLT, 0, 0, 0);
        secondPass();
        if(optimizeFlag){
        optimizeLoadStore();
        }
    }

    /**
     * Generates machine code given an atom
     * @param atom the atom to translate into machine code
     */
    private void translateAtomToMachineCode(AtomOperation atom) {

        switch (atom.getOp()) {
            case ADD:
                encodeMathOperation(MachineOperation.ADD, atom.getLeft(), atom.getRight(), atom.getResult());
                break;
            case SUB:
                encodeMathOperation(MachineOperation.SUB, atom.getLeft(), atom.getRight(), atom.getResult());
                break;
            case MUL:
                encodeMathOperation(MachineOperation.MUL, atom.getLeft(), atom.getRight(), atom.getResult());
                break;
            case DIV:
                encodeMathOperation(MachineOperation.DIV, atom.getLeft(), atom.getRight(), atom.getResult());
                break;
            case JMP:
                // Encode an always true cmp to set the flag to true before jumping
                encodeBooleanOperation("0", "0", "0");
                // Set program counter (register 1) to memory address of the instruction to jump to
                labelReferences.put(memory.getProgramMemorySize(), atom.getDest());
                encodeInstruction(MachineOperation.JMP, 0, 1, 0);
                break;
            case NEG:
                encodeMathOperation(MachineOperation.SUB, "0", atom.getLeft(), atom.getResult());
                break;
            case LBL:
                labelTable.put(atom.getDest(), memory.getProgramMemorySize());
                break;
            case TST:
                encodeBooleanOperation(atom.getLeft(), atom.getRight(), atom.getCmp());
                // Set program counter (register 1) to memory address of the instruction to jump to
                labelReferences.put(memory.getProgramMemorySize(), atom.getDest());
                encodeInstruction(MachineOperation.JMP, 0, 1, 0);
                break;
            case MOV:
                if (Objects.equals(atom.getLeft(), "0")) {
                    // If setting a value to 0 -> CLR
                    encodeInstruction(MachineOperation.CLR, 0, 0, 0);
                } else {
                    storeOperationConstants(atom.getLeft(), null);
                    encodeInstruction(MachineOperation.LOD, 0, 0, memory.getMemoryAddress(atom.getLeft()));
                }

                encodeInstruction(MachineOperation.STO, 0, 0, memory.getMemoryAddress(atom.getResult()));
                break;
            default:
                throw new UnsupportedOperationException("Unknown operation: " + atom.getOp());
        }
    }

    /**
     * Encodes an instruction into a 32-bit integer
     * @param operation the operation to encode
     * @param cmp the comparison to encode from 0 to 6 inclusive
     * @param r the register to encode from 0 to 15 inclusive
     * @param a the memory address to encode from 0 to 2^20 - 1 inclusive
     */
    private void encodeInstruction(MachineOperation operation, int cmp, int r, int a) {
        int instruction = (operation.ordinal() << 28) | (cmp << 24) | (r << 20) | a;

        memory.putInMemory(null, instruction, true);
    }

    /**
     * Encodes machine instructions for a math operation
     * @param operation the operation to perform
     * @param left the left side of the operation
     * @param right the right side of the operation
     * @param result the symbol that the result is stored in
     */
    private void encodeMathOperation(MachineOperation operation, String left, String right, String result) {
        if (operation != MachineOperation.ADD &&
                operation != MachineOperation.SUB &&
                operation != MachineOperation.MUL &&
                operation != MachineOperation.DIV) {
            throw new IllegalArgumentException("Invalid math operation: " + operation);
        }

        storeOperationConstants(left, right);
        encodeInstruction(MachineOperation.LOD, 0, 0, memory.getMemoryAddress(left));
        encodeInstruction(operation, 0, 0, memory.getMemoryAddress(right));
        encodeInstruction(MachineOperation.STO, 0, 0, memory.getMemoryAddress(result));
    }

    /**
     * Encodes machine instructions for a comparison
     * @param left the left side of the comparison
     * @param right the right side of the comparison
     * @param cmp the code for the operation to perform
     */
    private void encodeBooleanOperation(String left, String right, String cmp) {
        int cmpInt = Integer.parseInt(cmp);

        storeOperationConstants(left, right);
        encodeInstruction(MachineOperation.LOD, 0, 0, memory.getMemoryAddress(left));
        encodeInstruction(MachineOperation.CMP, cmpInt, 0, memory.getMemoryAddress(right));
    }

    /**
     * Stores constant values in an arbitrary location in memory
     * @param left a constant to store
     * @param right a constant to store
     */
    private void storeOperationConstants(String left, String right) {
        if (left != null && Character.isDigit(left.charAt(0))) {
            memory.putInMemory(left, Double.parseDouble(left), false);
        }

        if (right != null && Character.isDigit(right.charAt(0))) {
            memory.putInMemory(right, Double.parseDouble(right), false);
        }
    }

    public void secondPass() {
        for (int i = 0; i < memory.getProgramMemorySize(); i++) {
            int instruction = memory.getProgramMemory()[i];
            MachineOperation operation = MachineOperation.values()[(instruction >> 28) & 0xF];

            if (operation == MachineOperation.JMP) {
                String label = labelReferences.get(i);
                int address = labelTable.get(label) * 4;
                instruction = (instruction & 0xFFF00000) | (address & 0xFFFFF);
                memory.replaceProgramMemory(i, instruction);
            }
        }
    }
}