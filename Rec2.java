import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class Rec2 extends ChessBot {

    private Move prevMove;
    private final int depth;

    public Rec2(Chess game, int depth, int player) {
        super(game, player);
        this.depth = 2*depth;
        prevMove = new Move();
    }

    @Override
    public Tuple<Triplet<Boolean, Integer, Integer>, Move> makeMove() {
        Tuple<Triplet<Boolean, Integer, Integer>, Move> res = super.makeMove();
        prevMove = res.snd;
        return res;
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

    public double scoreFunction(Move move, int player) {
        double score = countMaterial(player);
        int maxUndefended = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (game.board[y][x] * player <= 0) continue;
                int piece = game.valueAt(x, y);
                int attacker = game.checkPos(x, y, false);
                int defender = game.checkPos(x, y, true);
                //System.out.println("Move: " + new Move(from, to));
                //System.out.println("Piece: " + piece + " at " + new Position(x, y) + ", Attacker: " + attacker + ", Defender: " + defender);
                if (isUnprotected(piece, attacker, defender)) {
                    maxUndefended = Math.max(maxUndefended, piece - (defender != 0 ? attacker : 0));
                }
            }
        }
        //System.out.println();
        return score - maxUndefended;
    }

    private boolean isUnprotected(int piece, int attacker, int defender) {
        return attacker != 0 && (defender == 0 || attacker < piece);
    }

    private int countMaterial(int player) {
        int res = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (game.board[y][x] * player > 0) {
                    res += game.valueAt(x, y);
                } else res -= game.valueAt(x, y);
            }
        }
        return res;
    }

    @Override
    public ArrayList<Move> getBestMoves() {
        Tuple<ArrayList<Move>, Double> res =  recursiveBest(depth, player, Double.POSITIVE_INFINITY);
        System.out.println(res.snd);
        return res.fst;
    }

    private Tuple<ArrayList<Move>, Double> recursiveBest(int depth, int player, Double alpha) {
        ArrayList<Move> best = new ArrayList<>();
        boolean csw = game.canCastleSW;
        boolean clw = game.canCastleLW;
        boolean csb = game.canCastleSB;
        boolean clb = game.canCastleLB;
        ArrayList<Move> currentValid = getValid(player);
        //System.out.println(validMoves.size());
        if (currentValid.isEmpty() && game.checkPos(game.getKingPos().x, game.getKingPos().y, false) == 0) {
            return new Tuple<>(best, 0.0);
        }
        //System.out.println("Depth = " + depth + ", number of moves = " + currentValid.size());
        double maxScore = Double.NEGATIVE_INFINITY;
        currentValid = (ArrayList<Move>) currentValid.stream().sorted(Comparator.comparingInt(move -> -game.valueAt(move.to.x, move.to.y))).collect(Collectors.toList());
        for (Move move : currentValid) {
            double score = 0;
            byte[][] boardCopy = copyBoard();
            Position kw = game.kingW.clone();
            Position kb = game.kingB.clone();
            Position en = game.enpassant.clone();
            game.makeMove(move);
            if (depth > 0) {
                game.currentPlayer = player * -1;
                score = -recursiveBest(depth-1, player * -1, -maxScore).snd;
                game.currentPlayer = player;
            } else {
                //game.currentPlayer *= (player == this.player ? 1 : -1);
                score = scoreFunction(move, player);
                //game.currentPlayer *= (player == this.player ? 1 : -1);
            }
            game.board = boardCopy;
            game.kingW = kw;
            game.kingB = kb;
            game.enpassant = en;
            game.canCastleSW = csw;
            game.canCastleLW = clw;
            game.canCastleSB = csb;
            game.canCastleLB = clb;
            if (maxScore < score) {
                maxScore = score;
                best = new ArrayList<>();
                best.add(move);
            } else if (maxScore == score) {
                best.add(move);
            }
            if (maxScore > alpha) {
                return new Tuple<>(best, maxScore);
            }
        }
        return new Tuple<>(best, maxScore);
    }

    private ArrayList<Move> getValid(int player) {
        ArrayList<Move> res = new ArrayList<>();
        //game.currentPlayer *= (this.player == player ? 1 : -1);
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (game.board[y][x] * player <= 0) continue;
                res.addAll(getMovesFrom(new Position(x, y)));
            }
        }
        //game.currentPlayer *= (this.player == player ? 1 : -1);
        return res;
    }
}
