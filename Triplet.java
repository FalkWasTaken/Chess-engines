public class Triplet<T1, T2, T3> {
    public T1 fst;
    public T2 snd;
    public T3 thd;

    public Triplet(T1 fst, T2 snd, T3 thd) {
        this.fst = fst;
        this.snd = snd;
        this.thd = thd;
    }

    public String toString() {
        return "(" + fst.toString() + ", " + snd.toString() + ", " + thd.toString() + ")";
    }
}
