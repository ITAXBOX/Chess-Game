package itawi.chessgame.core.piece;

import itawi.chessgame.core.enums.PieceType;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Setter
public class Rook extends Piece {
    private boolean hasMoved;

    public boolean getHasMoved() {
        return hasMoved;
    }

    public Rook(String color, String position) {
        super(color, position, PieceType.ROOK);
        hasMoved = false;
    }

    @Override
    public List<String> getPossibleMoves(Map<String, Piece> board) {
        List<String> possibleMoves = new ArrayList<>();

        getRookPossibleMoves(possibleMoves, board);

        return possibleMoves;
    }

    public void getRookPossibleMoves(List<String> possibleMoves, Map<String, Piece> board) {
        // Horizontal and vertical moves
        possibleMoves.addAll(getMovesInDirection(1, 0, board));  // Right
        possibleMoves.addAll(getMovesInDirection(-1, 0, board)); // Left
        possibleMoves.addAll(getMovesInDirection(0, 1, board));  // Up
        possibleMoves.addAll(getMovesInDirection(0, -1, board)); // Down
    }
}