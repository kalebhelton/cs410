public class AtomOperation {  // Individual Operations in Atom Form
    private final Operation op;
    String left;
    String right;
    String result;
    String cmp;
    String source;
    String dest;

    public AtomOperation(Operation op, String left, String right, String result, String cmp, String source, String dest){
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
        if(result != null) sb.append(", ").append(result);
        if(cmp != null) sb.append(", ").append(cmp);
        if(source != null) sb.append(", ").append(source);
        if(dest != null) sb.append(", ").append(dest);

        return sb.toString();
    }
}