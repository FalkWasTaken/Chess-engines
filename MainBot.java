import java.util.ArrayList;

public class MainBot extends ChessBot {

    Move prevMove;

    public MainBot(Chess game, int player) {
        super(game, player);
        prevMove = new Move(new Position(-1, -1), new Position(-1, -1));
    }

    @Override
    public Move chooseMove(ArrayList<Move> bestMoves) {
        ArrayList<Move> filtered = new ArrayList<>();
        for (Move move : bestMoves) {
            if (game.valueAt(move.from.x, move.from.y) == 100) {
                if (Math.abs(move.to.x - move.from.x) == 2) {
                    return move;
                }
            } else if (!move.from.equals(prevMove.to)) filtered.add(move);
        }
        if (!filtered.isEmpty()) {
            bestMoves = filtered;
        }
        ArrayList<Move> res = new ArrayList<>();
        for (Move move : bestMoves) {
            if (distanceToKing(move.from) > distanceToKing(move.to)) {
                res.add(move);
            }
        }
        if (!res.isEmpty()) {
            bestMoves = res;
        }
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    @Override
    public Tuple<Triplet<Boolean, Integer, Integer>, Move> makeMove() {
        Tuple<Triplet<Boolean, Integer, Integer>, Move> res = super.makeMove();
        prevMove = res.snd;
        return res;

    }

    public double scoreFunction(Move move, int player) {
        double score = 0;
        Position from = move.from;
        Position to = move.to;
        score += game.valueAt(to.x, to.y);
        if (game.valueAt(from.x, from.y) == 1 && (to.y == 0 || to.y == 7)) score += 9;
        int attacker = game.checkMove(from.x, from.y, to.x, to.y);
        game.currentPlayer *= -1;
        int defender = game.checkMove(from.x, from.y, to.x, to.y);
        game.currentPlayer *= -1;
        if (isUnprotected(game.valueAt(from.x, from.y), attacker, defender)) {
            if (game.valueAt(from.x, from.y) == 1 && (to.y == 0 || to.y == 7)) score -= 5;
            else score -= game.valueAt(from.x, from.y);
        }
        byte piece1 = game.board[from.y][from.x];
        byte piece2 = game.board[to.y][to.x];
        game.board[from.y][from.x] = 0;
        game.board[to.y][to.x] = piece1;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                //if (game.board[y][x] * player > 0) score += game.valueAt(x, y);
                //else score -= game.valueAt(x, y);
                if ((x == from.x && y == from.y) || (x == to.x && y == to.y) || game.board[y][x] * player <= 0) continue;
                attacker = game.checkPos(x, y, false);
                game.currentPlayer *= -1;
                defender = game.checkPos(x, y, false);
                game.currentPlayer *= -1;
                if (isUnprotected(game.valueAt(x, y), attacker, defender)) {
                    score -= game.valueAt(x, y);
                }
            }
        }
        if (piece1 * player == 4) {
            int countPawn = 0;
            for (int i = 0; i < 8; i++) {
                countPawn += (game.valueAt(to.x, i) == 1 ? 1 : 0);
            }
            if (countPawn >= 2) score -= 0.5;
        }
        game.board[from.y][from.x] = piece1;
        game.board[to.y][to.x] = piece2;
        return score;
    }

    private boolean isUnprotected(int piece, int attacker, int defender) {
        return attacker != 0 && (defender == 0 || attacker < piece);
    }
}
