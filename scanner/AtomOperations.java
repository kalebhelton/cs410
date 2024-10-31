public class AtomOperations{  // Individual Operations in Atom Form
    private final Operation op;
    private final String left;
    private final String right;
    private final String result;
    private final String cmp;
    private final String dest;

    public AtomOperations(Operation op, String left, String right, String result, String cmp, String dest){
        this.op = op;
        this.left = left;
        this.right = right;
        this.result = result;
        this.cmp = cmp;
        this.dest = dest;
    }

    public String toString(){  // String for Dubugging + Output
        return "Atom{" + "op = " + op + ", left = " + left + ", right = " + right + ", result = " + result + ", cmp = " + cmp + ", dest = " + dest + "}";
    }
}