public class Move {
    Position from;
    Position to;

    public Move(Position from, Position to) {
        this.from = from;
        this.to = to;
    }

    public Move() {
        from = new Position(-1, -1);
        to = new Position(-1, -1);
    }

    public String toString() {
        return from + " -> " + to;
    }

    public Move clone() {
        return new Move(from, to);
    }
}
