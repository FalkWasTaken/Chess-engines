type Pos = (usize, usize);
type Move = (Pos, Pos);

struct CastlingInfo {
    kw: bool,
    qw: bool,
    kb: bool,
    qb: bool
}

impl CastlingInfo {
    fn init() -> CastlingInfo {
        CastlingInfo {kw: true, qw: true, kb: true, qb: true}
    }

    fn from_FEN(fen: &str) -> CastlingInfo {
        CastlingInfo {
            kw: fen.contains("K"),
            qw: fen.contains("Q"),
            kb: fen.contains("k"),
            qb: fen.contains("q")
        }
    }
}

struct Game {
    board: [[i8; 8]; 8],
    castling: CastlingInfo,
    enpessant: Option<Pos>,
    player: i8
}

impl Game {
    fn new() -> Game {
        Game {
            board: [
                [-4,-2,-3,-5,-6,-3,-2,-4],
                [-1,-1,-1,-1,-1,-1,-1,-1],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [0, 0, 0, 0, 0, 0, 0, 0],
                [1, 1, 1, 1, 1, 1, 1, 1],
                [4, 2, 3, 5, 6, 3, 2, 4]
            ],
            castling: CastlingInfo::init(),
            enpessant: None,
            player: 1
        }
    }

    fn from_FEN(fen: String) -> Game {
        let fen: Vec<&str> = fen.split_whitespace().collect();
        let mut board = [[0; 8]; 8];
        for (i, line) in fen[0].split_terminator("/").enumerate() {
            let mut j = 0;
            for piece in line.chars() {
                match piece {
                    'P' | 'p' => board[i][j] = 1,
                    'N' | 'n' => board[i][j] = 2,
                    'B' | 'b' => board[i][j] = 3,
                    'R' | 'r' => board[i][j] = 4,
                    'Q' | 'q' => board[i][j] = 5,
                    'K' | 'k' => board[i][j] = 6,
                    _ => j += piece.to_digit(10).unwrap() as usize
                }
                if piece.is_uppercase() {
                    board[i][j] *= -1;
                }
                j += 1;
            }
        }
        let player = match fen[1] {
            "w" => 1,
            _ => -1
        };
        let enpessant = match fen[3] {
            "-" => None,
            _ => Some((fen[3].chars().next().unwrap() as usize - 'a' as usize, fen[3].chars().last().unwrap().to_digit(10).unwrap() as usize))
        };
        Game {
            board,
            castling: CastlingInfo::from_FEN(fen[2]),
            enpessant,
            player
        }
    }

    fn valid_move(&self, mov: Move) -> bool {
        let (from, to) = mov;
        let piece = self.board[from.1][from.0];
        if from == to || piece.signum() != self.player || piece.signum() == self.board[to.1][to.0].signum() {
            return false;
        }
        match piece.abs() {
            1 => self.pawn(mov),
            2 => self.knight(mov),
            3 => self.bishop(mov),
            4 => self.rook(mov),
            5 => self.queen(mov),
            _ => self.king(mov)
        }
    }

    fn pawn(&self, ((x, y), (xn, yn)): Move) -> bool {
        let piece = self.board[y][x];
        let target = self.board[yn][xn];
        (xn == x && (yn == y - piece || (yn == y - 2*piece && board[y-piece][x] == 0 && y == if piece > 0 (piece > 0 ? 6 : 1))) && target == 0) || (yn == y - piece && Math.abs(xn-x) == 1 && (target * piece < 0 || (enpassant.x == xn && enpassant.y == yn)))
        true
    }

    fn knight(&self, ((x, y), (xn, yn)): Move) -> bool {
        true
    }

    fn bishop(&self, ((x, y), (xn, yn)): Move) -> bool {
        true
    }

    fn rook(&self, ((x, y), (xn, yn)): Move) -> bool {
        true
    }   

    fn queen(&self, ((x, y), (xn, yn)): Move) -> bool {
        true
    }

    fn king(&self, ((x, y), (xn, yn)): Move) -> bool {
        true
    }

    fn check_king(&self, mov: Move) -> bool {

    }
}

fn main() {
    println!("Hello, world!");
}
