public class Tuple<T1, T2> {
    public T1 fst;
    public T2 snd;
    
    public Tuple(T1 fst, T2 snd) {
        this.fst = fst;
        this.snd = snd;
    }

    public String toString() {
        return "(" + fst.toString() + ", " + snd.toString() + ")";
    }
}
