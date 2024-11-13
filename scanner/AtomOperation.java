public class AtomOperation {  // Individual Operations in Atom Form
    private Operation op;
    private String left;
    private String right;
    private String result;
    private String cmp;
    private String dest;

    public AtomOperation(Operation op) {
        this(op, null, null, null, null, null);
    }

    public AtomOperation(Operation op, String left, String right, String result, String cmp, String dest) {
        this.op = op;
        this.left = left;
        this.right = right;
        this.result = result;
        this.cmp = cmp;
        this.dest = dest;
    }

    public Operation getOp() {
        return op;
    }

    public void setOp(Operation op) {
        this.op = op;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCmp() {
        return cmp;
    }

    public void setCmp(String cmp) {
        this.cmp = cmp;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(op.toString());

        if(left != null) sb.append(", ").append(left);
        if(right != null) sb.append(", ").append(right);
        if(result != null) sb.append(", ").append(result);
        if(cmp != null) sb.append(", ").append(cmp);
        if(dest != null) sb.append(", ").append(dest);

        return sb.toString();
    }
}