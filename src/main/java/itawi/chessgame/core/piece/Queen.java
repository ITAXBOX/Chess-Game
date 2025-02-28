package itawi.chessgame.core.piece;

import itawi.chessgame.core.enums.PieceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Queen extends Piece {

    public Queen(String color, String position) {
        super(color, position, PieceType.QUEEN);
    }

    @Override
    public List<String> getPossibleMoves(Map<String, Piece> board) {
        List<String> possibleMoves = new ArrayList<>();

        // Rook directions
        possibleMoves.addAll(getMovesInDirection(1, 0, board));
        possibleMoves.addAll(getMovesInDirection(-1, 0, board));
        possibleMoves.addAll(getMovesInDirection(0, 1, board));
        possibleMoves.addAll(getMovesInDirection(0, -1, board));

        // Bishop directions
        possibleMoves.addAll(getMovesInDirection(1, 1, board));
        possibleMoves.addAll(getMovesInDirection(1, -1, board));
        possibleMoves.addAll(getMovesInDirection(-1, 1, board));
        possibleMoves.addAll(getMovesInDirection(-1, -1, board));

        return possibleMoves;
    }
}