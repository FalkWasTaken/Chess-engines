import java.awt.*;
import java.awt.Color;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.*;
import javax.swing.*;

public class GameWithBot extends JFrame implements MouseListener, MouseMotionListener {
    JLayeredPane layeredPane;
    JPanel boardUI;
    JLabel chessPiece;
    int xAdjustment;
    int yAdjustment;
    int step;

    Chess chess;
    ChessBot bot;
    boolean canMove;
    int yCurr;
    int xCurr;

    Clip soundMove;
    Clip soundCapture;

    public GameWithBot(String[] fen) {
        Dimension boardSize = new Dimension(600, 600);

        //  Use a Layered Pane for this this application
        layeredPane = new JLayeredPane();
        getContentPane().add(layeredPane);
        layeredPane.setPreferredSize(boardSize);
        layeredPane.addMouseListener(this);
        layeredPane.addMouseMotionListener(this);

        //Add a chess board to the Layered Pane
        boardUI = new JPanel();
        layeredPane.add(boardUI, JLayeredPane.DEFAULT_LAYER);
        boardUI.setLayout(new GridLayout(8, 8));
        boardUI.setPreferredSize(boardSize);
        boardUI.setBounds(0, 0, boardSize.width, boardSize.height);
        step = (boardUI.getHeight()/8);

        for (int i = 0; i < 64; i++) {
            JPanel square = new JPanel(new BorderLayout());
            boardUI.add(square);

            int row = (i / 8) % 2;
            if (row == 0)
                square.setBackground(i % 2 == 0 ? new Color(255, 250, 224) : new Color(83, 145, 88));
            else
                square.setBackground(i % 2 == 0 ? new Color(83, 145, 88) : new Color(255, 250, 224));
        }

        if (fen.length == 0) {
            chess = new Chess();
        } else {
            chess = new Chess(fen);
        }

        //Add pieces to the board
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                String player = (chess.board[i][j] > 0 ? "_w" : "_b");
                String piece;
                switch (Math.abs(chess.board[i][j])) {
                    case 1:
                        piece = "pawn";
                        break;
                    case 2:
                        piece = "knight";
                        break;
                    case 3:
                        piece = "bishop";
                        break;
                    case 4:
                        piece = "rook";
                        break;
                    case 5:
                        piece = "queen";
                        break;
                    case 6:
                        piece = "king";
                        break;
                    default:
                        continue;
                }
                placePiece(i, j, piece + player);
            }
        }

        initAudio();

        bot = new Rec3(chess, 2, chess.currentPlayer * -1);
        canMove = false;
    }

    private void initAudio() {
        try {
            // Open an audio input stream.
            URL moveURL = this.getClass().getClassLoader().getResource("assets/move.wav");
            URL captureURL = this.getClass().getClassLoader().getResource("assets/capture.wav");
            AudioInputStream audioInMove = AudioSystem.getAudioInputStream(moveURL);
            AudioInputStream audioInCapture = AudioSystem.getAudioInputStream(captureURL);
            // Get a sound clip resource.
            soundMove = AudioSystem.getClip();
            soundCapture = AudioSystem.getClip();
            // Open audio clip and load samples from the audio input stream.
            soundMove.open(audioInMove);
            soundCapture.open(audioInCapture);
         } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         } catch (LineUnavailableException e) {
            e.printStackTrace();
         }
    }

    private void placePiece(int i, int j, String piece) {
        JLabel pieceUI = new JLabel(new ImageIcon("assets/" + piece + ".png", piece.substring(0, piece.length()-2)));
        int index = 8 * i + j;
        JPanel panel = (JPanel)boardUI.getComponent(index);
        panel.add(pieceUI);
    }

    private int quantize(int x) {
        return x / step;
    }

    public void mousePressed(MouseEvent e){
        chessPiece = null;
        Component c = boardUI.findComponentAt(e.getX(), e.getY());

        if (c instanceof JPanel)
            return;

        Point parentLocation = c.getParent().getLocation();
        xAdjustment = parentLocation.x - e.getX();
        yAdjustment = parentLocation.y - e.getY();
        xCurr = e.getX();
        yCurr = e.getY();

        if(chess.validPLayer(quantize(xCurr), quantize(yCurr))) {
            chessPiece = (JLabel)c;
            chessPiece.setLocation(e.getX() + xAdjustment, e.getY() + yAdjustment);
            chessPiece.setSize(chessPiece.getWidth(), chessPiece.getHeight());
            layeredPane.add(chessPiece, JLayeredPane.DRAG_LAYER);
            canMove = true;
        }
    }

    //Move the chess piece around

    public void mouseDragged(MouseEvent me) {
        if (chessPiece == null || !canMove) return;
        chessPiece.setLocation(me.getX() + xAdjustment, me.getY() + yAdjustment);
    }

    //Drop the chess piece back onto the chess board

    public void mouseReleased(MouseEvent e) {
        if (chessPiece == null) return;

        chessPiece.setVisible(false);
        Component c =  boardUI.findComponentAt(e.getX(), e.getY());
        Move move = new Move(new Position(quantize(xCurr), quantize(yCurr)), new Position(quantize(e.getX()), quantize(e.getY())));
        Triplet<Boolean, Integer, Integer> res = chess.tryMove(move);
        if (res.fst) {
            if ((e.getY() < step || e.getY() > 7*step) && ((ImageIcon) chessPiece.getIcon()).getDescription().equals("pawn")) {
                String player = (chess.currentPlayer == -1 ? "_w" : "_b"); 
                chessPiece.setIcon(new ImageIcon("assets/queen" + player + ".png", "queen"));
                //System.out.println("Promote by player!");
            }
            if (c instanceof JLabel) {
                Container parent = c.getParent();
                parent.remove(0);
                parent.add(chessPiece);
                playCapture();
            } else {
                Container parent = (Container)c;
                parent.add(chessPiece);
                playMove();
            }
            if (res.snd != 0) {
                //System.out.println("En passant!");
                c = boardUI.findComponentAt(e.getX(), res.snd*step);
                Container parent = c.getParent();
                parent.remove(0);
                parent.repaint();
            } else if (res.thd >= 0) {
                //System.out.println("Castle!");
                int xRook = res.thd*step;
                JLabel rook = (JLabel)boardUI.findComponentAt(xRook, e.getY());
                rook.setVisible(false);
                Container parent = rook.getParent();
                parent.repaint();
                parent.remove(0);
                int xNew = (xRook < e.getX() ? e.getX() + step : e.getX() - step);
                parent = (Container)boardUI.findComponentAt(xNew, e.getY());
                parent.add(rook);
                rook.setVisible(true);
            }
        } else {
            c = boardUI.findComponentAt(xCurr, yCurr);
            Container parent = (Container)c;
            parent.add(chessPiece);
        }
        canMove = false;
        chessPiece.setVisible(true);

        if (res.fst) {
            new Thread(){
                public void run() {
                    respond();
                }
        }.start();
        }
    }

    private void respond() {
        //try {
        //    Thread.sleep(1500);
        //} catch (InterruptedException e) {
        //    e.printStackTrace();
        //}
        Tuple<Triplet<Boolean, Integer, Integer>, Move> res2 = bot.makeMove();
        if (!res2.fst.fst) {
            System.out.println("Could not make move!");
            System.out.println("You won!");
            return;
        }
        Position pos = res2.snd.from;
        Position newPos = res2.snd.to;
        Triplet<Boolean, Integer, Integer> res = res2.fst;
        JPanel parent = (JPanel)boardUI.getComponentAt(pos.x*step, pos.y*step);
        JLabel piece = (JLabel)boardUI.findComponentAt(pos.x*step, pos.y*step);
        piece.setVisible(false);
        if ((newPos.y == 0 || newPos.y == 7) && chess.board[newPos.y][newPos.x] == 5*bot.player) {
            String player = (chess.currentPlayer == -1 ? "_w" : "_b"); 
            piece.setIcon(new ImageIcon("assets/queen" + player + ".png", "queen"));
            //System.out.println("Promote by computer!");
        }
        JPanel target = (JPanel)boardUI.getComponentAt(newPos.x*step, newPos.y*step);
        if (boardUI.findComponentAt(newPos.x*step, newPos.y*step) instanceof JLabel) {
            playCapture();
        } else {
            playMove();
        }
        target.removeAll();
        target.add(piece);
        parent.removeAll();
        if (res.snd != 0) {
            //System.out.println("En passant!");
            Component c = boardUI.findComponentAt(newPos.x*step, res.snd*step);
            Container p = c.getParent();
            p.remove(0);
            p.repaint();
        } else if (res.thd >= 0) {
            //System.out.println("Castle!");
            int xRook = res.thd;
            JLabel rook = (JLabel)boardUI.findComponentAt(xRook*step, newPos.y*step);
            rook.setVisible(false);
            Container p = rook.getParent();
            parent.removeAll();
            parent.repaint();
            int xNew = (xRook < newPos.x ? newPos.x*step + step : newPos.x*step - step);
            p = (Container)boardUI.findComponentAt(xNew+1, newPos.y*step);
            p.add(rook);
            rook.setVisible(true);
        }
        piece.setVisible(true);
    }

    public void mouseClicked(MouseEvent e) {

    }
    public void mouseMoved(MouseEvent e) {
    }
    public void mouseEntered(MouseEvent e){

    }
    public void mouseExited(MouseEvent e) {

    }

    private void playMove() {
        new Thread(){
            public void run() {
                try {
                    soundMove.start();
                    Thread.sleep(50);
                    soundMove.stop();
                    soundMove.setMicrosecondPosition(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {}
            }
        }.start();
    }

    private void playCapture() {
        new Thread(){
            public void run() {
                try {
                    soundCapture.start();
                    Thread.sleep(50);
                    soundCapture.stop();
                    soundCapture.setMicrosecondPosition(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {}
            }
        }.start();
    }

    public static void main(String[] args) {
        JFrame frame = new GameWithBot(args);
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE );
        frame.pack();
        frame.setResizable(true);
        frame.setLocationRelativeTo( null );
        frame.setVisible(true);
    }
}