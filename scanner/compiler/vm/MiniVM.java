package compiler.vm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author original by S. Bergmann
 * @author translation by Andrew B.
 * @author revision by R. Reaser
 */
public class MiniVM {
	private static final int MAX_REGISTERS = 16;
	private static final int MAX_MEMORY = 0xFFFF;
	private static final int MAX_CYCLES = 1000;
	private static final int START_ADDRESS = 0;
	private static final int PC_REGISTER = 1;

	private final int[] reg = new int[MAX_REGISTERS];
	private final float[] fpreg = new float[MAX_REGISTERS];
	private final int[] mem = new int[MAX_MEMORY];
	private Instr ir;
	private boolean flag = false;
	private boolean halt = false;
	private int load;
	private int cycle;

	public static void main(String[] args) throws IOException {
		Path input = args.length > 0
			? Paths.get(args[0])
			: Paths.get("input.src");

		new MiniVM(input).execute(true, true);
	}

	public MiniVM(Path input) throws IOException {
		this(Files.readAllBytes(input));
	}

	public MiniVM(String input) throws IOException {
		this(Paths.get(input));
	}

	public MiniVM(ByteBuffer buf) {
		load = Math.min(buf.array().length / 4, MAX_MEMORY);
		buf.asIntBuffer().get(mem, 0, load);
	}

	public MiniVM(byte[] bytes) {
		load = Math.min(bytes.length / 4, MAX_MEMORY);
		ByteBuffer.wrap(bytes).asIntBuffer().get(mem, 0, load);
	}

	public MiniVM(int[] ints) {
		load = Math.min(ints.length, MAX_MEMORY);
		IntBuffer.wrap(ints).get(mem, 0, load);
	}

	public void execute(boolean verbose, boolean stepwise) {
		try {
			reg[PC_REGISTER] = mem[START_ADDRESS];

			if (verbose) {
				System.out.println("================================================================");
				System.out.println("BOOT");
				System.out.println("================================================================");
				System.out.println();

				dumpRegisters();
				dumpMemory(0, load);
			}

			for (cycle = 0; cycle < MAX_CYCLES; cycle++) {
				if (stepwise) {
					System.out.println("Press any key...");
					System.in.read();
					System.out.println();
				}

				ir = new Instr(mem[reg[PC_REGISTER]]);
				executeInstr();

				if (verbose) {
					System.out.println("================================================================");
					System.out.println("CYCLE = " + cycle);
					System.out.println("================================================================");
					System.out.println();

					dumpTrace(3, 7);
					dumpRegisters();
					dumpMemory(0, load);
				}

				if (halt) break;

				reg[PC_REGISTER]++;
			}

			if (verbose) {
				System.out.println("================================================================");
				System.out.println("HALT");
				System.out.println("================================================================");
			}
		}
		catch (Exception e) {
			throw new RuntimeException("Fatal exception", e);
		}
	}

	private void executeInstr() {
		switch (ir.instructionType()) {
			case CLR -> {
				fpreg[ir.r1()] = 0;
			}
			case ADD -> {
				final int dest = absMemoryLocation(ir);
				fpreg[ir.r1()] = fpreg[ir.r1()] + Float.intBitsToFloat(mem[dest]);
			}
			case SUB -> {
				final int dest = absMemoryLocation(ir);
				fpreg[ir.r1()] = fpreg[ir.r1()] - Float.intBitsToFloat(mem[dest]);
			}
			case MUL -> {
				final int dest = absMemoryLocation(ir);
				fpreg[ir.r1()] = fpreg[ir.r1()] * Float.intBitsToFloat(mem[dest]);
			}
			case DIV -> {
				final int dest = absMemoryLocation(ir);
				fpreg[ir.r1()] = fpreg[ir.r1()] / Float.intBitsToFloat(mem[dest]);
			}
			case JMP -> {
				if (flag) {
					reg[PC_REGISTER] = absMemoryLocation(ir);
				}
			}
			case CMP -> {
				final float rhs = Float.intBitsToFloat(mem[absMemoryLocation(ir)]);
				flag = switch (ir.comparisonType()) {
					case ALWAYS -> true;
					case EQUAL -> fpreg[ir.r1()] == rhs;
					case LESSER -> fpreg[ir.r1()] < rhs;
					case LESSER_OR_EQUAL -> fpreg[ir.r1()] <= rhs;
					case GREATER -> fpreg[ir.r1()] > rhs;
					case GREATER_OR_EQUAL -> fpreg[ir.r1()] >= rhs;
					case UNEQUAL -> fpreg[ir.r1()] != rhs;
				};
			}
			case LOD -> {
				fpreg[ir.r1()] = Float.intBitsToFloat(mem[absMemoryLocation(ir)]);
			}
			case STO -> {
				mem[absMemoryLocation(ir)] = Float.floatToIntBits(fpreg[ir.r1()]);
			}
			case HLT -> {
				halt = true;
			}
		}
	}

	private int absMemoryLocation(Instr instr) {
		return instr.memoryMode() == MemoryMode.ABSOLUTE
			? instr.address()
			: instr.address() + reg[instr.r2()];
	}

	public void dumpTrace(int behind, int ahead) {
		System.out.println("-----");
		System.out.println("TRACE");
		System.out.println("-----");
		System.out.println("");

		final int start = Math.max(0, reg[PC_REGISTER] - behind);
		final int end = Math.min(MAX_MEMORY - 1, reg[PC_REGISTER] + ahead);

		for (int i = start; i < end; i++) {
			System.out.printf(
				"mem[%04x]\t%08x\t%s%s\n",
				i, mem[i], new Instr(mem[i]),
				i == reg[PC_REGISTER] ? "\t<--- PC" : ""
			);
		}
		System.out.println();
	}

	public void dumpRegisters() {
		System.out.println("---------");
		System.out.println("REGISTERS");
		System.out.println("---------");
		System.out.println("");

		System.out.printf(
			"ir      = %08x = %s\n\n",
			ir != null ? ir.rawInstruction() : 0,
			ir != null ? ir : 0
		);

		for (int i = 0; i < MAX_REGISTERS; i++) {
			System.out.printf(
				"reg[%2s] = %08x = %d\t\tfpreg[%2s] = %08x = %-8e\n",
				i, reg[i], reg[i],
				i, Float.floatToIntBits(fpreg[i]), fpreg[i]
			);
		}
		System.out.println();
	}

	public void dumpMemory(int low, int high) {
		System.out.println("------");
		System.out.println("MEMORY");
		System.out.println("------");
		System.out.println("");

		final int lo = low / 4 * 4;
		final int hi = (high + 4) / 4 * 4 - 1;
		for (int i = lo; i <= hi; i += 4) {
			System.out.printf(
				"mem[%04x:%04x]\t%08x\t%08x\t%08x\t%08x\n\t\t%-8e\t%-8e\t%-8e\t%-8e\n",
				i, i+3, mem[i], mem[i + 1], mem[i + 2], mem[i + 3],
				Float.intBitsToFloat(mem[i]),
				Float.intBitsToFloat(mem[i + 1]),
				Float.intBitsToFloat(mem[i + 2]),
				Float.intBitsToFloat(mem[i + 3])
			);
		}
		System.out.println();
	}
}

class Instr {
	private static final int TYPE_SHIFT = 28;
	private static final int MODE_SHIFT = 27;
	private static final int CMP_SHIFT = 24;
	private static final int R1_SHIFT = 20;
	private static final int R2_SHIFT = 16;

	private final int raw;

	public Instr(int raw) {
		this.raw = raw;
	}

	public Instr(InstrType type, MemoryMode mode, ComparisonType cmp, int r1, int r2, int a) {
		this(
			(type.ordinal() & 0xFFFF << TYPE_SHIFT) |
			(mode.ordinal() & 0b1 << MODE_SHIFT) |
			(type == InstrType.CMP
				? cmp.ordinal() & 0b111 << CMP_SHIFT
				: 0
			) |
			(r1 & 0xF << R1_SHIFT) |
			(mode == MemoryMode.ABSOLUTE
				? 0
				: (r2 & 0xF) << R2_SHIFT
			) |
			(a & (mode == MemoryMode.ABSOLUTE
				? 0xFFFF
				: 0xFFFFF)
			)
		);
	}

	public int rawInstruction() {
		return raw;
	}

	public InstrType instructionType() {
		return InstrType.values()[raw >>> TYPE_SHIFT];
	}

	public MemoryMode memoryMode() {
		return ((raw >>> MODE_SHIFT & 1) > 0)
			? MemoryMode.REGISTER_DISPLACEMENT
			: MemoryMode.ABSOLUTE;
	}

	public ComparisonType comparisonType() {
		return ComparisonType.values()[raw >>> CMP_SHIFT & 0b111];
	}

	public int r1() {
		return raw >>> R1_SHIFT & 0xF;
	}

	public int r2() {
		return raw >>> R2_SHIFT & 0xF;
	}

	public int address() {
		int mask = memoryMode() == MemoryMode.ABSOLUTE
			? 0xFFFFF
			: 0xFFFF;
		return raw & mask;
	}

	@Override
	public String toString() {
		return instructionType().name() + " "
			+ (instructionType() == InstrType.CMP ? comparisonType().name() + " " : "")
			+ r1() + " "
			+ r2() + " "
			+ address();
	}

//	public static Instr newClr(int r1) {
//		return new Instr(
//			InstrType.CLR, MemoryMode.ABSOLUTE, ComparisonType.ALWAYS, r1, 0, 0
//		);
//	}
//
//	public static Instr newAdd(int r1, int address) {
//		return new Instr(
//			InstrType.ADD, MemoryMode.ABSOLUTE, ComparisonType.ALWAYS, r1, 0, address
//		);
//	}
//
//	public static Instr newSub(int r1, int address) {
//		return new Instr(
//			InstrType.SUB, MemoryMode.ABSOLUTE, ComparisonType.ALWAYS, r1, 0, address
//		);
//	}
//
//	public static Instr newMul(int r1, int address) {
//		return new Instr(
//			InstrType.MUL, MemoryMode.ABSOLUTE, ComparisonType.ALWAYS, r1, 0, address
//		);
//	}
//
//	public static Instr newDiv(int r1, int address) {
//		return new Instr(
//			InstrType.DIV, MemoryMode.ABSOLUTE, ComparisonType.ALWAYS, r1, 0, address
//		);
//	}
//
//	public static Instr newJmp(int address) {
//		return new Instr(
//			InstrType.JMP, MemoryMode.ABSOLUTE, ComparisonType.ALWAYS, 0, 0, address
//		);
//	}
//
//	public static Instr newCmp(ComparisonType comparatorType, int r1, int address) {
//		return new Instr(
//			InstrType.CMP, MemoryMode.ABSOLUTE, comparatorType, r1, 0, address
//		);
//	}
//
//	public static Instr newLod(int r1, int address) {
//		return new Instr(
//			InstrType.LOD, MemoryMode.ABSOLUTE, ComparisonType.ALWAYS, r1, 0, address
//		);
//	}
//
//	public static Instr newSto(int r1, int address) {
//		return new Instr(
//			InstrType.STO, MemoryMode.ABSOLUTE, ComparisonType.ALWAYS, r1, 0, address
//		);
//	}
//
//	public static Instr newHlt() {
//		return new Instr(
//			InstrType.HLT, MemoryMode.ABSOLUTE, ComparisonType.ALWAYS, 0, 0, 0
//		);
//	}
}

enum MemoryMode {
	ABSOLUTE,
	REGISTER_DISPLACEMENT;
}

enum InstrType {
	CLR,
	ADD,
	SUB,
	MUL,
	DIV,
	JMP,
	CMP,
	LOD,
	STO,
	HLT;
}

enum ComparisonType {
	ALWAYS,
	EQUAL,
	LESSER,
	GREATER,
	LESSER_OR_EQUAL,
	GREATER_OR_EQUAL,
	UNEQUAL;

	public ComparisonType negate() {
		try {
			return values()[7 - this.ordinal()];
		}
		catch (ArrayIndexOutOfBoundsException e) {
			throw new UnsupportedOperationException("Unsupported comparator type");
		}
	}
}