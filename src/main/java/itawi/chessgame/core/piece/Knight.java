package itawi.chessgame.core.piece;

import itawi.chessgame.core.enums.PieceType;
import itawi.chessgame.core.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Knight extends Piece {

    public Knight(String color, String position) {
        super(color, position, PieceType.KNIGHT);
    }

    @Override
    public List<String> getPossibleMoves(Map<String, Piece> board) {
        List<String> possibleMoves = new ArrayList<>();
        int[] currentCoords = Utils.getCoordinates(this.getPosition());
        int currentX = currentCoords[0];
        int currentY = currentCoords[1];

        // All 8 possible "L"-shaped moves
        int[] dx = {2, 2, 1, 1, -1, -1, -2, -2};
        int[] dy = {1, -1, 2, -2, 2, -2, 1, -1};

        for (int i = 0; i < 8; i++) {
            String newPosition = Utils.getPosition(currentX + dx[i], currentY + dy[i]);
            if (Utils.isValidPosition(newPosition)) {
                Piece target = board.get(newPosition);
                if (target == null || !target.getColor().equals(this.getColor())) {
                    possibleMoves.add(newPosition);
                }
            }
        }

        return possibleMoves;
    }
}