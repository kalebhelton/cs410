package compiler.backend;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Memory {
    private static final long MAX_MEMORY_SIZE = (long) Math.pow(2, 20) - 1;
    private static final int PROGRAM_MEMORY_SIZE = 512;

    private final int[] programMemory = new int[PROGRAM_MEMORY_SIZE];
    private int programMemoryIndex = 0;
    private final ArrayList<Float> generalMemory = new ArrayList<>();
    private final HashMap<String, Integer> memoryMap = new HashMap<>();

    /**
     * Inserts a value into general memory
     * @param symbol the symbol representing the value
     * @param value the value to insert
     */
    public void putInMemory(String symbol, float value) {
        memoryMap.put(symbol, addGeneralMemory(value));
    }

    /**
     * Gets the memory address of a symbol
     * @param symbol the symbol to get the address of -> variable
     * @return the memory address the symbol is stored at
     */
    public int getMemoryAddress(String symbol) {
        if(!memoryMap.containsKey(symbol)) {
            putInMemory(symbol, 0);
        }

        return memoryMap.get(symbol) + PROGRAM_MEMORY_SIZE;
    }

    public void replaceProgramMemory(int address, int value) {
        programMemory[address] = value;
    }

    /**
     * Sequentially adds a value into program memory
     * @param value the value to insert into program memory -> instructions stored here
     */
    public void addProgramMemory(int value) {
        if(programMemoryIndex >= PROGRAM_MEMORY_SIZE) {
            throw new OutOfMemoryError("Out of program memory");
        }

        programMemory[programMemoryIndex++] = value;
    }

    /**
     * Adds a value into general memory
     * @param value the value to insert into general memory -> variables and constants stored here
     * @return the address the value is stored at
     */
    private int addGeneralMemory(float value) {
        if(generalMemory.size() + PROGRAM_MEMORY_SIZE >= MAX_MEMORY_SIZE) {
            throw new OutOfMemoryError("Out of general memory");
        }

        generalMemory.add(value);

        return generalMemory.size() - 1;
    }

    /**
     * Gets the program memory
     * @return the value of the program memory
     */
    public int[] getProgramMemory() {
        return programMemory;
    }

    public int getProgramMemorySize() {
        return programMemoryIndex;
    }

    public void setProgramMemorySize(int size) {
        this.programMemoryIndex = size;
    }

    /**
     * Encodes the entirety of memory into a byte array
     * @return a byte array representing the machine's initial memory
     */
    public byte[] encode() {
        ByteArrayOutputStream memoryOutput = new ByteArrayOutputStream();

        for (int instruction : programMemory) {
            memoryOutput.write((byte) (instruction >> 24));
            memoryOutput.write((byte) (instruction >> 16));
            memoryOutput.write((byte) (instruction >> 8));
            memoryOutput.write((byte) instruction);
        }

        for (float value : generalMemory) {
            int valueBits = Float.floatToIntBits(value);

            memoryOutput.write((byte) (valueBits >> 24));
            memoryOutput.write((byte) (valueBits >> 16));
            memoryOutput.write((byte) (valueBits >> 8));
            memoryOutput.write((byte) valueBits);
        }

        return memoryOutput.toByteArray();
    }
}
