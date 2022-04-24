import java.util.ArrayList;
import java.util.Comparator;
//import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class Rec3 extends ChessBot {

    private Move prevMove;
    private final int depth;
    //private ConcurrentHashMap<byte[][], Double> transpositions;


    public Rec3(Chess game, int depth, int player) {
        super(game, player);
        this.depth = 2*depth;
        prevMove = new Move();
        //transpositions = new ConcurrentHashMap<>();
    }

    @Override
    public Tuple<Triplet<Boolean, Integer, Integer>, Move> makeMove() {
        Tuple<Triplet<Boolean, Integer, Integer>, Move> res = super.makeMove();
        prevMove = res.snd;
        //transpositions = new ConcurrentHashMap<>();
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

    public double scoreFunction(Move move, int player, Chess game) {
        double score = countMaterial(player, game);
        int maxUndefended = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (game.board[y][x] * player <= 0) continue;
                int piece = game.valueAt(x, y);
                int attacker = game.checkPos(x, y, false);
                int defender = game.checkPos(x, y, true);
                if (isUnprotected(piece, attacker, defender)) {
                    maxUndefended = Math.max(maxUndefended, piece - (defender != 0 ? attacker : 0));
                }
            }
        }
        score += badPlacement(player, game);
        return score - maxUndefended;
    }

    private boolean isUnprotected(int piece, int attacker, int defender) {
        return attacker != 0 && (defender == 0 || attacker < piece);
    }

    private double badPlacement(int player, Chess game) {
        double res = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x ++) {
                int piece = game.board[y][x];
                if (game.valueAt(x, y) == 5) {
                    for (int k = 0; k < 8; k++) {
                        if (game.valueAt(x, k) == 1) res += Integer.signum(piece) * 0.1;
                    }
                } else if ((y == 0 || y == 7) && game.valueAt(x, y) == 3) res += Integer.signum(piece) * 0.1;
                else if ((x == 0 || x == 7) && Math.abs(piece) == 2) res += Integer.signum(piece) * 0.1;
            }
        }
        return res;
    }

    private int countMaterial(int player, Chess game) {
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
        ArrayList<Move> currentValid = getValid(player, game);
        ArrayList<Tuple<Move, Double>> branches = new ArrayList<>(currentValid.size());
        ArrayList<Thread> threads = new ArrayList<>(currentValid.size());
        Semaphore mutex = new Semaphore(1);
        for (Move move : currentValid) {
            Thread t = new Thread() {
                public void run() {
                    mutex.acquireUninterruptibly();
                    Chess clone = game.clone();
                    mutex.release();
                    clone.makeMove(move);
                    clone.currentPlayer *= -1;
                    Double score = -recursiveBest(depth-1, player * -1, Double.POSITIVE_INFINITY, clone).snd;
                    mutex.acquireUninterruptibly();
                    branches.add(new Tuple<>(move, score));
                    mutex.release();
                }
            };
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ArrayList<Move> best = new ArrayList<>();
        double maxScore = Double.NEGATIVE_INFINITY;
        for (Tuple<Move, Double> t : branches) {
            if (t.snd > maxScore) {
                maxScore = t.snd;
                best = new ArrayList<>();
                best.add(t.fst);
            } else if (t.snd == maxScore) {
                best.add(t.fst);
            }
        }
        System.out.println(maxScore);
        return best;
        //return recursiveBest(depth, player, Double.POSITIVE_INFINITY, game).fst;
    }

    private Tuple<ArrayList<Move>, Double> recursiveBest(int depth, int player, Double alpha, Chess game) {
        ArrayList<Move> best = new ArrayList<>();
        boolean csw = game.canCastleSW;
        boolean clw = game.canCastleLW;
        boolean csb = game.canCastleSB;
        boolean clb = game.canCastleLB;
        ArrayList<Move> currentValid = getValid(player, game);
        if (currentValid.isEmpty() && game.checkPos(game.getKingPos().x, game.getKingPos().y, false) == 0) {
            return new Tuple<>(best, 0.0);
        }
        double maxScore = Double.NEGATIVE_INFINITY;
        currentValid = (ArrayList<Move>) currentValid.stream().sorted(Comparator.comparingInt(move -> -game.valueAt(move.to.x, move.to.y))).collect(Collectors.toList());
        for (Move move : currentValid) {
            double score = 0;
            byte[][] boardCopy = copyBoard(game);
            Position kw = game.kingW.clone();
            Position kb = game.kingB.clone();
            Position en = game.enpassant.clone();
            game.makeMove(move);
            if (depth > 0) {
                game.currentPlayer *= -1;
                score = -recursiveBest(depth-1, player * -1, -maxScore, game).snd;
                game.currentPlayer *= -1;
                //transpositions.put(game.board, score);
            } else {
                score = scoreFunction(move, player, game);
                //transpositions.put(game.board, score);
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
            if (score > alpha) {
                return new Tuple<>(best, maxScore);
            }
        }
        return new Tuple<>(best, maxScore);
    }
}
