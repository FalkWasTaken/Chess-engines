import java.util.ArrayList;

public class RecBot extends ChessBot {

    private Move prevMove;
    private final int depth;

    public RecBot(Chess game, int depth, int player) {
        super(game, player);
        this.depth = depth*2;
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
        double score = 0;
        Position from = move.from;
        Position to = move.to;
        score += game.valueAt(to.x, to.y);
        byte piece1 = game.board[from.y][from.x];
        byte piece2 = game.board[to.y][to.x];
        game.board[from.y][from.x] = 0;
        game.board[to.y][to.x] = piece1;
        if (Math.abs(piece1) == 1 && (to.y == 0 || to.y == 7)) game.board[to.y][to.x] = (byte) (player * 5);
        score += countMaterial(player);
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (game.board[y][x] * player > 0) {
                    int attacker = game.checkPos(x, y, false);
                    game.currentPlayer *= -1;
                    int defender = game.checkPos(x, y, false);
                    game.currentPlayer *= -1;
                    if (isUnprotected(game.valueAt(x, y), attacker, defender)) {
                        score -= game.valueAt(x, y);
                    }
                } else if (game.board[y][x] != 0) {
                    game.currentPlayer *= -1;
                    int attacker = game.checkPos(x, y, false);
                    game.currentPlayer *= -1;
                    int defender = game.checkPos(x, y, false);
                    if (isUnprotected(game.valueAt(x, y), attacker, defender)) {
                        score += game.valueAt(x, y);
                    }
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

    private int countMaterial(int player) {
        int res = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (game.board[y][x] * player > 0) res += game.valueAt(x, y);
                else res -= game.valueAt(x, y);
            }
        }
        return res;
    }

    private boolean isUnprotected(int piece, int attacker, int defender) {
        return attacker != 0 && (defender == 0 || attacker < piece);
    }

    @Override
    public ArrayList<Move> getBestMoves() {
        Tuple<ArrayList<Move>, Double> res =  recursiveBest(depth, player, Double.POSITIVE_INFINITY);
        System.out.println(res.snd);
        //System.out.println(res.fst);
        return res.fst;
    }

    private Tuple<ArrayList<Move>, Double> recursiveBest(int depth, int player, Double alpha) {
        ArrayList<Move> best = new ArrayList<>();
        boolean csw = game.canCastleSW;
        boolean clw = game.canCastleLW;
        boolean csb = game.canCastleSB;
        boolean clb = game.canCastleLB;
        ArrayList<Move> currentValid = getValid(player);
        if (currentValid.isEmpty() && game.checkPos(game.getKingPos().x, game.getKingPos().y, false) == 0) {
            return new Tuple<>(best, 0.0);
        }
        //System.out.println("Depth = " + depth + ", number of moves = " + currentValid.size());
        double maxScore = Double.NEGATIVE_INFINITY;
        for (Move move : currentValid) {
            double score = 0;
            if (depth > 0) {
                byte[][] boardCopy = copyBoard();
                Position kw = game.kingW.clone();
                Position kb = game.kingB.clone();
                Position en = game.enpassant.clone();
                game.makeMove(move);
                game.currentPlayer = player * -1;
                score = -recursiveBest(depth-1, player * -1, -maxScore).snd;
                game.board = boardCopy;
                game.kingW = kw;
                game.kingB = kb;
                game.enpassant = en;
                game.canCastleSW = csw;
                game.canCastleLW = clw;
                game.canCastleSB = csb;
                game.canCastleLB = clb;
                game.currentPlayer = player;
            } else {
                //game.currentPlayer *= (player == this.player ? 1 : -1);
                score = scoreFunction(move, player);
                //game.currentPlayer *= (player == this.player ? 1 : -1);
            }
            if (score >= alpha) {
                best = new ArrayList<>();
                best.add(move);
                return new Tuple<>(best, score);
            } else if (maxScore < score) {
                maxScore = score;
                best = new ArrayList<>();
                best.add(move);
            } else if (maxScore == score) {
                best.add(move);
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
