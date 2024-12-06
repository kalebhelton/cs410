import java.util.ArrayList;
import java.util.HashMap;

public class Memory {
    private static final long MAX_MEMORY_SIZE = (long) Math.pow(2, 20) - 1;
    private static final int PROGRAM_MEMORY_SIZE = 10000;

    private final int[] programMemory = new int[PROGRAM_MEMORY_SIZE];
    private int programMemoryIndex = 0;
    private final ArrayList<Double> generalMemory = new ArrayList<>();
    private final HashMap<String, Integer> memoryMap = new HashMap<>();

    public Memory() {
    }

    /**
     * Inserts a value into memory
     * @param symbol the symbol representing the value, only used for general memory
     * @param value the value to insert
     * @param isProgramMemory whether the value should go into program or general memory
     */
    public void putInMemory(String symbol, double value, boolean isProgramMemory) {
        if(isProgramMemory) {
            addProgramMemory((int) value);
        } else {
            memoryMap.put(symbol, addGeneralMemory(value));
        }
    }

    /**
     * Gets the memory address of a symbol
     * @param symbol the symbol to get the address of -> variable
     * @return the memory address the symbol is stored at
     */
    public int getMemoryAddress(String symbol) {
        if(!memoryMap.containsKey(symbol)) {
            putInMemory(symbol, 0, false);
        }

        return memoryMap.get(symbol) * 4 + PROGRAM_MEMORY_SIZE;
    }

    public void replaceProgramMemory(int address, int value) {
        programMemory[address] = value;
    }

    /**
     * Sequentially adds a value into program memory
     * @param value the value to insert into program memory -> instructions stored here
     */
    private void addProgramMemory(int value) {
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
    private int addGeneralMemory(double value) {
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
}
