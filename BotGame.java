import java.awt.*;
import java.awt.Color;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

public class BotGame extends JFrame implements MouseListener, MouseMotionListener {
    JLayeredPane layeredPane;
    JPanel boardUI;
    JLabel chessPiece;
    int xAdjustment;
    int yAdjustment;
    int step;

    Chess chess;
    ChessBot bot1;
    ChessBot bot2;
    boolean canMove;
    int yCurr;
    int xCurr;

    Clip soundMove;
    Clip soundCapture;

    public BotGame() {
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

        //Add pieces to the board
        placePiece(0, 0, "rook_b");
        placePiece(0, 1, "knight_b");
        placePiece(0, 2, "bishop_b");
        placePiece(0, 3, "queen_b");
        placePiece(0, 4, "king_b");
        placePiece(0, 5, "bishop_b");
        placePiece(0, 6, "knight_b");
        placePiece(0, 7, "rook_b");

        placePiece(7, 0, "rook_w");
        placePiece(7, 1, "knight_w");
        placePiece(7, 2, "bishop_w");
        placePiece(7, 3, "queen_w");
        placePiece(7, 4, "king_w");
        placePiece(7, 5, "bishop_w");
        placePiece(7, 6, "knight_w");
        placePiece(7, 7, "rook_w");

        for (int j = 0; j < 8; j++) {
            placePiece(1, j, "pawn_b");
            placePiece(6, j, "pawn_w");
        }

        initAudio();

        chess = new Chess();
        bot1 = new Rec4(chess, 2, 1);
        bot2 = new Rec3(chess, 2, -1);
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

    public void mousePressed(MouseEvent e){
    }

    //Move the chess piece around

    public void mouseDragged(MouseEvent me) {
    }

    //Drop the chess piece back onto the chess board

    public void mouseReleased(MouseEvent e) {
    }

    private boolean respond() {
        //try {
        //    Thread.sleep(5);
        //} catch (InterruptedException e) {
        //    e.printStackTrace();
        //}
        ChessBot bot = (chess.currentPlayer == 1 ? bot1 : bot2);
        Tuple<Triplet<Boolean, Integer, Integer>, Move> res2 = bot.makeMove();
        if (!res2.fst.fst) {
            String player = (chess.currentPlayer == 1 ? "White" : "Black");
            System.out.println(player + " could not make a move!");
            System.out.println(chess.kingW + ", " + chess.kingB);
            chess.printBoard();
            return false;
        }
        Position pos = res2.snd.from;
        Position newPos = res2.snd.to;
        Triplet<Boolean, Integer, Integer> res = res2.fst;
        JPanel parent = (JPanel)boardUI.getComponentAt(pos.x*step, pos.y*step);
        JLabel piece = (JLabel)boardUI.findComponentAt(pos.x*step, pos.y*step);
        if (boardUI.findComponentAt(newPos.x*step, newPos.y*step) instanceof JLabel) {
            playCapture();
        } else {
            playMove();
        }
        piece.setVisible(false);
        if ((newPos.y == 0 || newPos.y == 7) && chess.board[newPos.y][newPos.x] == 5*bot.player) {
            String player = (chess.currentPlayer == -1 ? "_w" : "_b"); 
            piece.setIcon(new ImageIcon("assets/queen" + player + ".png", "queen"));
        }
        JPanel target = (JPanel)boardUI.getComponentAt(newPos.x*step, newPos.y*step);
        target.removeAll();
        target.add(piece);
        parent.removeAll();
        if (res.snd != 0) {
            Component c = boardUI.findComponentAt(newPos.x*step, res.snd*step);
            Container p = c.getParent();
            p.remove(0);
            p.repaint();
        } else if (res.thd >= 0) {
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
        return true;
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
        JFrame frame = new BotGame();
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE );
        frame.pack();
        frame.setResizable(true);
        frame.setLocationRelativeTo( null );
        frame.setVisible(true);
        while (((BotGame) frame).respond());
    }
}