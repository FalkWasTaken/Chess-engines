
import java.util.LinkedList;

public class Chess {
    public byte[][] board;
    public int currentPlayer;
    Position kingW;
    Position kingB;
    Position enpassant;
    boolean canCastleSW;
    boolean canCastleLW;
    boolean canCastleSB;
    boolean canCastleLB;
    public int numPieces;

    public void printBoard() {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 7; x++) {
                System.out.print(board[y][x] + ", ");
            }
            System.out.println(board[y][7]);
        }
    }

    public Chess() {
        currentPlayer = 1;
        canCastleSW = true; 
        canCastleLW = true;
        canCastleSB = true;
        canCastleLB = true;
        byte[][] temp = {
            {-4, -2, -3, -5, -6, -3, -2, -4},
            {-1, -1, -1, -1, -1, -1, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 1, 1, 1},
            {4, 2, 3, 5, 6, 3, 2, 4}
        };
        board = temp;
        kingW = new Position(4, 7);
        kingB = new Position(4, 0);
        enpassant = new Position(-1, -1);
    }

    public Chess(String[] fen) {
        String[] pos = fen[0].split("/");
        board = new byte[8][8];
        for (int i = 0; i < 8; i++) {
            LinkedList<Character> rowInfo = new LinkedList<>();
            for (char ch : pos[i].toCharArray()) {
                rowInfo.addLast(ch);
            }
            for (int j = 0; j < 8; j++) {
                char piece = rowInfo.pop();
                byte player = (byte) (Character.isUpperCase(piece) ? 1 : -1);
                switch (piece) {
                    case 'P':
                    case 'p':
                        board[i][j] = 1;
                        break;
                    case 'N':
                    case 'n':
                        board[i][j] = 2;
                        break;
                    case 'B':
                    case 'b':
                        board[i][j] = 3;
                        break;
                    case 'R':
                    case 'r':
                        board[i][j] = 4;
                        break;
                    case 'Q':
                    case 'q':
                        board[i][j] = 5;
                        break;
                    case 'K':
                        kingW = new Position(j, i);
                        board[i][j] = 6;
                        break;
                    case 'k':
                        kingB = new Position(j, i);
                        board[i][j] = 6;
                        break;
                    default:
                        j += Character.digit(piece, 10) - 1;
                        continue;
                }
                board[i][j] *= player;
            }
        }
        if (fen[1].equals("w")) {
            currentPlayer = 1;
        } else {
            currentPlayer = -1;
        }
        String castle = fen[2];
        canCastleSW = castle.contains("K");
        canCastleSB = castle.contains("k");
        canCastleLW = castle.contains("Q");
        canCastleLB = castle.contains("q");

        enpassant = new Position(-1, -1);
        if (!fen[3].equals("-")) {
            enpassant.x = fen[3].charAt(0) - 'a';
            enpassant.y = Character.digit(fen[3].charAt(1), 10);
        }
    }

    public Triplet<Boolean, Integer, Integer> tryMove(Move move) {
        if(validMove(move)) {
            Triplet<Boolean, Integer, Integer> res = makeMove(move);
            currentPlayer *= -1;
            return res;
        }
        return new Triplet<>(false, 0, -1);
    }

    public Triplet<Boolean, Integer, Integer> makeMove(Move move) {
        return makeMove(move.from.x, move.from.y, move.to.x, move.to.y);
    }

    public Triplet<Boolean, Integer, Integer> makeMove(int x, int y, int xNew, int yNew) {
        byte piece = board[y][x];
        board[yNew][xNew] = piece;
        board[y][x] = 0;
        if (Math.abs(piece) == 1) {
            if (Math.abs(yNew-y) == 2) {
                enpassant = new Position(xNew, yNew + piece);
                return new Triplet<>(true, 0, -1);
            } else if (xNew == enpassant.x && yNew == enpassant.y) {
                board[yNew+piece][xNew] = 0;
                enpassant = new Position(-1, -1);
                return new Triplet<>(true, yNew + piece, -1);
            } else if (yNew == 0 || piece * yNew == -7) {
                board[yNew][xNew] = (byte) (currentPlayer * 5);
            }
        }
        enpassant = new Position(-1, -1);
        if (Math.abs(piece) == 4) {
            if (currentPlayer == 1) {
                if (x == 7) canCastleSW = false;
                else if (x == 0) canCastleLW = false;
            } else {
                if (x == 7) canCastleSB = false;
                else if (x == 0) canCastleLB = false;
            }
        } else if (Math.abs(piece) == 6) {
            setKingPos(xNew, yNew);
            if (currentPlayer == 1) {
                canCastleSW = false;
                canCastleLW = false;
                if (xNew == x + 2) {
                    board[7][7] = 0;
                    board[7][5] = 4;
                    return new Triplet<>(true, 0, xNew + 1);
                }
                if (xNew == x - 2) {
                    board[7][0] = 0;
                    board[7][3] = 4;
                    return new Triplet<>(true, 0, xNew - 2);
                }
            } else {
                canCastleSB = false;
                canCastleLB = false;
                if (xNew == x + 2) {
                    board[0][7] = 0;
                    board[0][5] = -4;
                    return new Triplet<>(true, 0, xNew + 1);
                }
                if (xNew == x - 2) {
                    board[0][0] = 0;
                    board[0][3] = -4;
                    return new Triplet<>(true, 0, xNew - 2);
                }
            }
        }
        return new Triplet<>(true, 0, -1);
    }

    public boolean validPLayer(int x, int y) {
        return Integer.signum(board[y][x]) == Integer.signum(currentPlayer);
    }

    private boolean validMove(Move move) {
        if (move.from.equals(move.to)) return false;
        switch(Math.abs(board[move.from.y][move.from.x])) {
            case 1:
                return pawn(move.from.x, move.from.y, move.to.x, move.to.y);
            case 2:
                return knight(move.from.x, move.from.y, move.to.x, move.to.y);
            case 3:
                return bishop(move.from.x, move.from.y, move.to.x, move.to.y);
            case 4:
                return rook(move.from.x, move.from.y, move.to.x, move.to.y);
            case 5:
                return queen(move.from.x, move.from.y, move.to.x, move.to.y);
            case 6:
                return king(move.from.x, move.from.y, move.to.x, move.to.y);
        }
        return true;
    }

    public boolean pawn(int x, int y, int xNew, int yNew) {
        if (Math.min(xNew, yNew) < 0 || Math.max(xNew, yNew) > 7) return false;
        byte piece = board[y][x];
        byte target = board[yNew][xNew];
        if ((xNew == x && (yNew == y - piece || (yNew == y - 2*piece && board[y-piece][x] == 0 && y == (piece > 0 ? 6 : 1))) && target == 0) || (yNew == y - piece && Math.abs(xNew-x) == 1 && (target * piece < 0 || (enpassant.x == xNew && enpassant.y == yNew))))
            return checkKing(x, y, xNew, yNew);
        return false;
    }

    public boolean knight(int x, int y, int xNew, int yNew) {
        if (Math.min(xNew, yNew) < 0 || Math.max(xNew, yNew) > 7) return false;
        if (Math.abs(xNew-x) * Math.abs(yNew-y) == 2 && (board[yNew][xNew] == 0 || Integer.signum(board[yNew][xNew]) != Integer.signum(board[y][x])))
            return checkKing(x, y, xNew, yNew);
        return false;
    }

    public boolean bishop(int x, int y, int xNew, int yNew) {
        if (Math.min(xNew, yNew) < 0 || Math.max(xNew, yNew) > 7) return false;
        int xDif = Math.abs(xNew-x);
        if (xDif != Math.abs(yNew-y) || Integer.signum(board[yNew][xNew]) == Integer.signum(board[y][x])) 
            return false;
        int xSign = Integer.signum(xNew-x);
        int ySign = Integer.signum(yNew-y);
        for (int i = 1; i < Math.abs(xNew-x); i++)
            if (board[y+i*ySign][x+i*xSign] != 0) return false;
        return checkKing(x, y, xNew, yNew);
    }

    public boolean rook(int x, int y, int xNew, int yNew) {
        if (Math.min(xNew, yNew) < 0 || Math.max(xNew, yNew) > 7) return false;
        byte piece1 = board[y][x];
        byte piece2 = board[yNew][xNew];
        if ((xNew != x && yNew != y) || Integer.signum(piece2) == Integer.signum(piece1)) 
            return false;
        int xSign = Integer.signum(xNew-x);
        int ySign = Integer.signum(yNew-y);
        for (int i = 1; i < Math.abs(xNew-x) + Math.abs(yNew-y); i++)
            if (board[y+i*ySign][x+i*xSign] != 0) return false;
        return checkKing(x, y, xNew, yNew);
    }

    public boolean queen(int x, int y, int xNew, int yNew) {
        return rook(x, y, xNew, yNew) || bishop(x, y, xNew, yNew);
    }

    public boolean king(int x, int y, int xNew, int yNew) {
        if (Math.min(xNew, yNew) < 0 || Math.max(xNew, yNew) > 7) return false;
        if (Math.abs(xNew-x) <= 1 && Math.abs(yNew-y) <= 1 && board[yNew][xNew] * board[y][x] <= 0) {
            boolean canMove = checkMove(x, y, xNew, yNew) == 0;
            return canMove;
        }
        if ((yNew == y && xNew == x + 2) && (((currentPlayer == 1 && canCastleSW && board[7][7] == 4) || (currentPlayer == -1 && canCastleSB && board[0][7] == -4)) && board[y][x+1] == 0 && board[y][xNew] == 0)) {
            boolean canMove = checkMove(x, y, xNew, yNew) == 0 && checkMove(x, y, xNew - 1, yNew) == 0 && checkPos(x, y, false) == 0;
            return canMove;
        }
        if ((yNew == y && xNew == x - 2) && (((currentPlayer == 1 && canCastleLW && board[7][0] == 4) || (currentPlayer == -1 && canCastleLB && board[0][0] == -4)) && board[y][x-1] == 0 && board[y][xNew] == 0 && board[y][xNew-1] == 0)) {
            boolean canMove = checkMove(x, y, xNew, yNew) == 0 && checkMove(x, y, xNew + 1, yNew) == 0 && checkPos(x, y, false) == 0;
            return canMove;
        }
        return false;
    }

    public int checkMove(Move move) {
        return checkMove(move.from.x, move.from.y, move.to.x, move.to.y);
    }

    public int checkMove(int x, int y, int xNew, int yNew) {
        byte piece1 = board[y][x];
        byte piece2 = board[yNew][xNew];
        board[yNew][xNew] = piece1;
        board[y][x] = 0;
        int attackerValue = checkSquare(xNew, yNew, false);
        board[yNew][xNew] = piece2;
        board[y][x] = piece1;
        return attackerValue;
    }

    public int checkPos(int x, int y, boolean defender) {
        return checkSquare(x, y, defender);
    }

    public int checkPos(Position pos, boolean defender) {
        return checkSquare(pos.x, pos.y, defender);
    }

    public boolean checkKing(int x, int y, int xNew, int yNew) {
        byte piece1 = board[y][x];
        byte piece2 = board[yNew][xNew];
        board[yNew][xNew] = piece1;
        board[y][x] = 0;
        int attacker = checkSquare(getKingPos().x, getKingPos().y, false);
        board[yNew][xNew] = piece2;
        board[y][x] = piece1;
        return attacker == 0;
    }

    private int checkSquare(int x, int y, boolean defender) {
        int player = Integer.signum(board[y][x]);
        if (defender) player *= -1;
        // Pawn check
        if ((validPos(x+1, y-player) && board[y-player][x+1] * player == -1) || (validPos(x-1, y-player) && board[y-player][x-1] * player == -1))
            return 1;
        // Knight check
        if ((validPos(x+2, y+1) && board[y+1][x+2]*player == -2) || (validPos(x+2, y-1) && board[y-1][x+2]*player == -2) || (validPos(x-2, y+1) && board[y+1][x-2]*player == -2) || (validPos(x-2, y-1) && board[y-1][x-2]*player == -2) || (validPos(x+1, y+2) && board[y+2][x+1]*player == -2) || (validPos(x-1, y+2) && board[y+2][x-1]*player == -2) || (validPos(x+1, y-2) && board[y-2][x+1]*player == -2) || (validPos(x-1, y-2) && board[y-2][x-1]*player == -2))
            return 3;
        // Rook & Queen check
        for (int j = x + 1; j < 8; j++) {
            byte piece = board[y][j];
            if (piece != 0 && (piece * player != -4 && piece * player != -5)) break;
            if (Math.abs(piece) == 4) return 5;
            if (Math.abs(piece) == 5) return 9;
        }
        for (int j = x - 1; j >= 0; j--) {
            byte piece = board[y][j];
            if (piece != 0 && (piece * player != -4 && piece * player != -5)) break;
            if (Math.abs(piece) == 4) return 5;
            if (Math.abs(piece) == 5) return 9;
        }
        for (int i = y + 1; i < 8; i++) {
            byte piece = board[i][x];
            if (piece != 0 && (piece * player != -4 && piece * player != -5)) break;
            if (Math.abs(piece) == 4) return 5;
            if (Math.abs(piece) == 5) return 9;
        }
        for (int i = y - 1; i >= 0; i--) {
            byte piece = board[i][x];
            if (piece != 0 && (piece * player != -4 && piece * player != -5)) break;
            if (Math.abs(piece) == 4) return 5;
            if (Math.abs(piece) == 5) return 9;
        }
        // Bishop & Queen check
        for (int k = 1; validPos(x+k, y+k); k++) {
            byte piece = board[y+k][x+k];
            if (piece != 0 && (piece * player != -3 && piece * player != -5)) break;
            if (Math.abs(piece) == 3) return 3;
            if (Math.abs(piece) == 5) return 9;
        }
        for (int k = 1; validPos(x+k, y-k); k++) {
            byte piece = board[y-k][x+k];
            if (piece != 0 && (piece * player != -3 && piece * player != -5)) break;
            if (Math.abs(piece) == 3) return 3;
            if (Math.abs(piece) == 5) return 9;
        }
        for (int k = 1; validPos(x-k, y+k); k++) {
            byte piece = board[y+k][x-k];
            if (piece != 0 && (piece * player != -3 && piece * player != -5)) break;
            if (Math.abs(piece) == 3) return 3;
            if (Math.abs(piece) == 5) return 9;
        }
        for (int k = 1; validPos(x-k, y-k); k++) {
            byte piece = board[y-k][x-k];
            if (piece != 0 && (piece * player != -3 && piece * player != -5)) break;
            if (Math.abs(piece) == 3) return 3;
            if (Math.abs(piece) == 5) return 9;
        }
        // King "check"
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (validPos(x+j, y+i) && board[y+i][x+j] * player == -6) return 100;
            }
        }
        return 0;
    }

    public Position getKingPos() {
        if (currentPlayer == 1)
            return kingW;
        return kingB;
    }

    private void setKingPos(int x, int y) {
        if (currentPlayer == 1) {
            kingW.x = x;
            kingW.y = y;
        } else {
            kingB.x = x;
            kingB.y = y;
        }
    }

    public boolean validPos(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }

    public int valueAt(int x, int y) {
        switch (Math.abs(board[y][x])) {
            case 1:
                return 1;
            case 2:
            case 3:
                return 3;
            case 4:
                return 5;
            case 5:
                return 9;
            case 6:
                return 100;
            default:
                return 0;
        }
    }

    public Chess clone() {
        Chess clone = new Chess();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                clone.board[i][j] = board[i][j];
            }
        }
        clone.currentPlayer = currentPlayer;
        clone.kingW = kingW.clone();
        clone.kingB = kingB.clone();
        clone.enpassant = enpassant.clone();
        clone.canCastleSW = canCastleSW;
        clone.canCastleLW = canCastleLW;
        clone.canCastleSB = canCastleSB;
        clone.canCastleLB = canCastleLW;
        return clone;
    } 
}
