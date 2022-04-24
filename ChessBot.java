import java.util.ArrayList;
import java.util.Random;

public abstract class ChessBot {
    public Chess game;
    public ArrayList<Move> validMoves;
    public Random random; 
    public Position king;
    public Position oppositKing;
    public final int player; 

    public ChessBot(Chess game, int player) {
        this.game = game;
        this.player = player;
        if (player == 1) {
            king = game.kingW;
            oppositKing = game.kingB;
        } else {
            king = game.kingB;
            oppositKing = game.kingW;
        }
        random = new Random();
    }

    public abstract double scoreFunction(Move move, int player, Chess game);

    public Move chooseMove(ArrayList<Move> bestMoves) {
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }
    
    public Tuple<Triplet<Boolean, Integer, Integer>, Move> makeMove() {
        //updateMoves();
        ArrayList<Move> bestMoves = getBestMoves();
        if (bestMoves == null || bestMoves.isEmpty()) return new Tuple<>(new Triplet<>(false, 0, 0), null);
        //Tuple<Position, Position> move = bestMoves.get(random.nextInt(bestMoves.size()));
        Move move = chooseMove(bestMoves);
        System.out.println(move);
        System.out.println();
        Triplet<Boolean, Integer, Integer> res = game.makeMove(move.from.x, move.from.y, move.to.x, move.to.y);
        game.currentPlayer *= -1;
        return new Tuple<>(res, move);    
    }

    public ArrayList<Move> getBestMoves() {
        ArrayList<Move> bestMoves = null;
        double maxScore = Integer.MIN_VALUE;
        //System.out.println("King pos: " + king);
        //System.out.println("Possible moves:");
        for (Move move : validMoves) {
            double score = scoreFunction(move, player, game);
            //System.out.println(from + " -> " + to + " (" + score + ")");
            if (score > maxScore) {
                maxScore = score;
                bestMoves = new ArrayList<>();
                bestMoves.add(move);
            } else if (score == maxScore) {
                bestMoves.add(move);
            }
        }
        //System.out.println("Best moves:");
        //for (Tuple<Position,Position> tuple : bestMoves) {
        //    System.out.println(tuple.fst + " -> " + tuple.snd + " (" + maxScore + ")");
        //}
        System.out.print(maxScore + ": ");
        return bestMoves;
    }

    //private void updateMoves() {
    //    validMoves = new ArrayList<>();
    //    for (int y = 0; y < 8; y++) {
    //        for (int x = 0; x < 8; x++) {
    //            if (game.board[y][x] * player > 0) {
    //                Position pos = new Position(x, y);
    //                switch (game.board[y][x] * player) {
    //                    case 1: // Pawn
    //                        validMoves.addAll(tryPawn(pos));
    //                        break;
    //                    case 2: // Knight
    //                        validMoves.addAll(tryKnight(pos));
    //                        break;
    //                    case 3: // Bishop
    //                        validMoves.addAll(tryBishop(pos));
    //                        break;
    //                    case 4:
    //                        validMoves.addAll(tryRook(pos));
    //                        break;
    //                    case 5:
    //                        validMoves.addAll(tryQueen(pos));
    //                        break;
    //                    default:
    //                        validMoves.addAll(tryKing(pos));
    //                        break;
    //                }
    //            }
    //        }
    //    }
    //}

    private ArrayList<Move> tryPawn(Position pos, Chess game) {
        ArrayList<Move> res = new ArrayList<>();
        if (game.pawn(pos.x, pos.y, pos.x, pos.y - game.currentPlayer)) res.add(new Move(pos, new Position(pos.x, pos.y - game.currentPlayer)));
        if (game.pawn(pos.x, pos.y, pos.x, pos.y - 2*game.currentPlayer)) res.add(new Move(pos, new Position(pos.x, pos.y - 2*game.currentPlayer)));
        if (game.pawn(pos.x, pos.y, pos.x - 1, pos.y - game.currentPlayer)) res.add(new Move(pos, new Position(pos.x - 1, pos.y - game.currentPlayer)));
        if (game.pawn(pos.x, pos.y, pos.x + 1, pos.y - game.currentPlayer)) res.add(new Move(pos, new Position(pos.x + 1, pos.y - game.currentPlayer)));
        return res;
    }

    private ArrayList<Move> tryKnight(Position pos, Chess game) {
        ArrayList<Move> res = new ArrayList<>();
        for (int k = 1; k < 3; k++) {
            if (game.knight(pos.x, pos.y, pos.x + k, pos.y + 3 - k)) res.add(new Move(pos, new Position(pos.x + k, pos.y + 3 - k)));
            if (game.knight(pos.x, pos.y, pos.x + k, pos.y - 3 + k)) res.add(new Move(pos, new Position(pos.x + k, pos.y - 3 + k)));
            if (game.knight(pos.x, pos.y, pos.x - k, pos.y + 3 - k)) res.add(new Move(pos, new Position(pos.x - k, pos.y + 3 - k)));
            if (game.knight(pos.x, pos.y, pos.x - k, pos.y - 3 + k)) res.add(new Move(pos, new Position(pos.x - k, pos.y - 3 + k)));
        }
        return res;
    }

    private ArrayList<Move> tryBishop(Position pos, Chess game) {
        ArrayList<Move> res = new ArrayList<>();
        for (int k = 1; k <= Math.min(pos.x, pos.y); k++) {
            if (game.bishop(pos.x, pos.y, pos.x - k, pos.y - k)) res.add(new Move(pos, new Position(pos.x - k, pos.y - k)));
        }
        for (int k = 1; k <= Math.min(pos.x, 8 - pos.y); k++) {
            if (game.bishop(pos.x, pos.y, pos.x - k, pos.y + k)) res.add(new Move(pos, new Position(pos.x - k, pos.y + k)));
        }
        for (int k = 1; k <= Math.min(8 - pos.x, pos.y); k++) {
            if (game.bishop(pos.x, pos.y, pos.x + k, pos.y - k)) res.add(new Move(pos, new Position(pos.x + k, pos.y - k)));
        }
        for (int k = 1; k <= Math.min(8 - pos.x, 8 - pos.y); k++) {
            if (game.bishop(pos.x, pos.y, pos.x + k, pos.y + k)) res.add(new Move(pos, new Position(pos.x + k, pos.y + k)));
        }
        return res;
    }

    private ArrayList<Move> tryRook(Position pos, Chess game) {
        ArrayList<Move> res = new ArrayList<>();
        for (int k = 0; k < 8; k++) {
            if (game.rook(pos.x, pos.y, pos.x, k)) res.add(new Move(pos, new Position(pos.x, k)));
            if (game.rook(pos.x, pos.y, k, pos.y)) res.add(new Move(pos, new Position(k, pos.y)));
        }
        return res;
    }

    private ArrayList<Move> tryQueen(Position pos, Chess game) {
        ArrayList<Move> res = new ArrayList<>();
        res. addAll(tryBishop(pos, game));
        res.addAll(tryRook(pos, game));
        return res;
    }

    private ArrayList<Move> tryKing(Position pos, Chess game) {
        ArrayList<Move> res = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (game.validPos(pos.x + x, pos.y + y) && game.king(pos.x, pos.y, pos.x + x, pos.y + y)) res.add(new Move(pos, new Position(pos.x + x, pos.y + y)));
            }
        }
        if (game.validPos(pos.x+2, pos.y) && game.king(pos.x, pos.y, pos.x + 2, pos.y)) res.add(new Move(pos, new Position(pos.x + 2, pos.y)));
        if (game.validPos(pos.x-2, pos.y) && game.king(pos.x, pos.y, pos.x - 2, pos.y)) res.add(new Move(pos, new Position(pos.x - 2, pos.y)));
        return res;
    }

    public ArrayList<Move> getMovesFrom(Position from, Chess game) {
        switch (Math.abs(game.board[from.y][from.x])) {
            case 1:
                return tryPawn(from, game);
            case 2:
                return tryKnight(from, game);
            case 3:
                return tryBishop(from, game);
            case 4:
                return tryRook(from, game);
            case 5:
                return tryQueen(from, game);
            case 6:
                return tryKing(from, game);
            default:
                return new ArrayList<>();
        }
    }

    public ArrayList<Move> getValid(int player, Chess game) {
        ArrayList<Move> res = new ArrayList<>();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (game.board[y][x] * player <= 0) continue;
                res.addAll(getMovesFrom(new Position(x, y), game));
            }
        }
        return res;
    }

    public byte[][] copyBoard(Chess game) {
        byte[][] copy = new byte[8][8];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                copy[y][x] = game.board[y][x];
            }
        }
        return copy;
    }

    public int distanceToKing(Position pos) {
        return Math.max(Math.abs(pos.x - oppositKing.x), Math.abs(pos.y - oppositKing.y));
    }
}
