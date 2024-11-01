public class AtomOperation {  // Individual Operations in Atom Form
    private final Operation op;
    private final String left;
    private final String right;
    private final String result;
    private final String cmp;
    private final String source;
    private final String dest;

    public AtomOperation(String dest) {
        this(Operation.JMP, null, null, null, dest);
    }

    public AtomOperation(String source, String dest) {
        this(Operation.MOV, null, null, null, null, source, dest);
    }

    public AtomOperation(Operation op, String left, String right, String cmp, String dest) {
        this(op, left, right, null, cmp, null, dest);
    }

    public AtomOperation(Operation op, String left, String result) {
        this(op, left, null, result);
    }

    public AtomOperation(Operation op, String left, String right, String result) {
        this(op, left, right, result, null, null, null);
    }

    private AtomOperation(Operation op, String left, String right, String result, String cmp, String source, String dest){
        this.op = op;
        this.left = left;
        this.right = right;
        this.result = result;
        this.cmp = cmp;
        this.source = source;
        this.dest = dest;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(op.toString());

        if(left != null) sb.append(", ").append(left);
        if(right != null) sb.append(", ").append(right);
        if(cmp != null) sb.append(", ").append(cmp);
        if(source != null) sb.append(", ").append(source);
        if(dest != null) sb.append(", ").append(dest);

        return sb.toString();
    }
}