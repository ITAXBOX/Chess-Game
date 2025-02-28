package itawi.chessgame.core.piece;

import itawi.chessgame.core.enums.PieceType;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class Bishop extends Piece {

    public Bishop(String color, String position) {
        super(color, position, PieceType.BISHOP);
    }

    @Override
    public List<String> getPossibleMoves(Map<String, Piece> board) {
        List<String> possibleMoves = new ArrayList<>();

        getBishopPossibleMoves(possibleMoves, board);

        return possibleMoves;
    }

    public void getBishopPossibleMoves(List<String> possibleMoves, Map<String, Piece> board) {
        // Diagonal moves
        possibleMoves.addAll(getMovesInDirection(1, 1, board));   // Top-right
        possibleMoves.addAll(getMovesInDirection(1, -1, board));  // Bottom-right
        possibleMoves.addAll(getMovesInDirection(-1, 1, board));  // Top-left
        possibleMoves.addAll(getMovesInDirection(-1, -1, board)); // Bottom-left
    }
}