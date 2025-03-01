package itawi.chessgame.core.piece;

import itawi.chessgame.core.board.Board;
import itawi.chessgame.core.enums.PieceType;
import itawi.chessgame.core.util.Utils;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Setter
public class Pawn extends Piece {
    private final int DIRECTION = this.getColor().equals("white") ? 1 : -1; // White moves up, black moves down
    private boolean hasMoved;

    public Pawn(String color, String position) {
        super(color, position, PieceType.PAWN);
        this.hasMoved = false;
    }

    @Override
    public List<String> getPossibleMoves(Map<String, Piece> board) {
        List<String> possibleMoves = new ArrayList<>();
        int[] currentCoords = Utils.getCoordinates(this.getPosition());
        int currentX = currentCoords[0];
        int currentY = currentCoords[1];

        // Normal move: one square forward
        String oneSquareMove = Utils.getPosition(currentX, currentY + DIRECTION);
        if (Utils.isValidPosition(oneSquareMove) && board.get(oneSquareMove) == null) {
            possibleMoves.add(oneSquareMove);
        }

        // First move: two squares forward
        if (!hasMoved) {
            String twoSquareMove = Utils.getPosition(currentX, currentY + 2 * DIRECTION);
            String intermediateSquare = Utils.getPosition(currentX, currentY + DIRECTION);
            if (Utils.isValidPosition(twoSquareMove) && board.get(twoSquareMove) == null && board.get(intermediateSquare) == null) {
                possibleMoves.add(twoSquareMove);
            }
        }

        // Capture: diagonally forward
        String leftCapture = Utils.getPosition(currentX - 1, currentY + DIRECTION);
        if (Utils.isValidPosition(leftCapture)) {
            Piece pieceAtLeftCapture = board.get(leftCapture);
            if (pieceAtLeftCapture != null && !pieceAtLeftCapture.getColor().equals(this.getColor())) {
                possibleMoves.add(leftCapture);
            }
        }

        String rightCapture = Utils.getPosition(currentX + 1, currentY + DIRECTION);
        if (Utils.isValidPosition(rightCapture)) {
            Piece pieceAtRightCapture = board.get(rightCapture);
            if (pieceAtRightCapture != null && !pieceAtRightCapture.getColor().equals(this.getColor())) {
                possibleMoves.add(rightCapture);
            }
        }

        // En passant logic
        String enPassantTarget = ((Board) board).getEnPassantTarget();
        if (enPassantTarget != null) {
            int[] targetCoords = Utils.getCoordinates(enPassantTarget);
            // Check if the pawn is on the correct rank and adjacent file
            boolean isCorrectRank = (this.getColor().equals("white") && currentY == 4) ||
                                    (this.getColor().equals("black") && currentY == 3);
            boolean isAdjacentFile = Math.abs(targetCoords[0] - currentX) == 1;
            boolean isCorrectDirection = (this.getColor().equals("white") && targetCoords[1] == currentY + 1) ||
                                         (this.getColor().equals("black") && targetCoords[1] == currentY - 1);
            if (isCorrectRank && isAdjacentFile && isCorrectDirection) {
                possibleMoves.add(enPassantTarget);
            }
        }

        return possibleMoves;
    }
}