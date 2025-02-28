package itawi.chessgame.core.piece;

import itawi.chessgame.core.enums.PieceType;
import itawi.chessgame.core.util.Utils;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Setter
public class King extends Piece {
    private boolean hasMoved;

    public boolean getHasMoved() {
        return hasMoved;
    }

    public King(String color, String position) {
        super(color, position, PieceType.KING);
        this.hasMoved = false;
    }

    @Override
    public List<String> getPossibleMoves(Map<String, Piece> board) {
        List<String> possibleMoves = new ArrayList<>();
        int[] currentCoords = Utils.getCoordinates(this.getPosition());
        int currentX = currentCoords[0];
        int currentY = currentCoords[1];

        // All 8 possible directions the king can move
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
            String newPosition = Utils.getPosition(currentX + dx[i], currentY + dy[i]);
            if (Utils.isValidPosition(newPosition)) {
                Piece target = board.get(newPosition);
                if (target == null || !target.getColor().equals(this.getColor())) {
                    possibleMoves.add(newPosition);
                }
            }
        }

        // Add castling moves if eligible
        if (!hasMoved) {
            addCastlingMoves(possibleMoves, board);
        }

        return possibleMoves;
    }

    private void addCastlingMoves(List<String> possibleMoves, Map<String, Piece> board) {
        // Check king-side castling (short castling)
        if (canCastleKingSide(board)) {
            possibleMoves.add(getColor().equals("white") ? "g1" : "g8");
        }

        // Check queen-side castling (long castling)
        if (canCastleQueenSide(board)) {
            possibleMoves.add(getColor().equals("white") ? "c1" : "c8");
        }
    }

    // Check if king-side castling is possible
    private boolean canCastleKingSide(Map<String, Piece> board) {
        String rookPosition = getColor().equals("white") ? "h1" : "h8";

        // Check if the rook exists and hasn't moved
        Piece rook = board.get(rookPosition);
        if (!(rook instanceof Rook) || ((Rook) rook).getHasMoved()) {
            return false;
        }

        // Check if the squares between the king and rook are empty
        String[] squaresToCheck = getColor().equals("white") ? new String[]{"f1", "g1"} : new String[]{"f8", "g8"};
        for (String square : squaresToCheck) {
            if (board.get(square) != null) {
                return false;
            }
        }

        // Check if the king is not in check and the squares are not under attack
        return !isInCheck(board) && !isSquareUnderAttack(board, squaresToCheck);
    }

    // Check if queen-side castling is possible
    private boolean canCastleQueenSide(Map<String, Piece> board) {
        String rookPosition = getColor().equals("white") ? "a1" : "a8";

        // Check if the rook exists and hasn't moved
        Piece rook = board.get(rookPosition);
        if (!(rook instanceof Rook) || ((Rook) rook).getHasMoved()) {
            return false;
        }

        // Check if the squares between the king and rook are empty
        String[] squaresToCheck = getColor().equals("white") ? new String[]{"b1", "c1", "d1"} : new String[]{"b8", "c8", "d8"};
        for (String square : squaresToCheck) {
            if (board.get(square) != null) {
                return false;
            }
        }

        // Check if the king is not in check and the squares are not under attack
        return !isInCheck(board) && !isSquareUnderAttack(board, squaresToCheck);
    }

    // Check if the king is in check
    private boolean isInCheck(Map<String, Piece> board) {
        String kingPosition = this.getPosition();

        // Iterate through all pieces on the board
        for (Map.Entry<String, Piece> entry : board.entrySet()) {
            Piece piece = entry.getValue();

            // Skip pieces of the same color
            if (piece.getColor().equals(this.getColor())) {
                continue;
            }

            // Check if the piece can attack the king's position
            if (piece.getPossibleMoves(board).contains(kingPosition)) {
                return true; // King is in check
            }
        }

        return false; // King is not in check
    }

    // Check if any of the squares are under attack
    private boolean isSquareUnderAttack(Map<String, Piece> board, String[] squares) {
        // Iterate through all pieces on the board
        for (Map.Entry<String, Piece> entry : board.entrySet()) {
            Piece piece = entry.getValue();

            // Skip pieces of the same color
            if (piece.getColor().equals(this.getColor())) {
                continue;
            }

            // Get the possible moves of the opponent's piece
            List<String> possibleMoves = piece.getPossibleMoves(board);

            // Check if any of the squares are in the opponent's possible moves
            for (String square : squares) {
                if (possibleMoves.contains(square)) {
                    return true; // Square is under attack
                }
            }
        }

        return false; // Squares are not under attack
    }
}