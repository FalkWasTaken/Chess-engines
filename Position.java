public class Position {
    public int x;
    public int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public boolean equals(Position other) {
        return x == other.x && y == other.y;
    }

    public Position clone() {
        return new Position(x, y);
    }
}
