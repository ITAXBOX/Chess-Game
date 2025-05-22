package itawi.chessgame.core.piece;

import itawi.chessgame.core.board.Board;
import itawi.chessgame.core.enums.PieceType;
import itawi.chessgame.core.util.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class Piece {
    private String color;
    private String position;
    private PieceType type;

    public abstract List<String> getPossibleMoves(Map<String, Piece> board);

    // Overloaded method that accepts a Board instead of just a Map
    public List<String> getPossibleMoves(Board board) {
        // By default, just call the Map version with the board state
        return getPossibleMoves(board.getBoardState());
    }

    protected List<String> getMovesInDirection(int dx, int dy, Map<String, Piece> board) {
        List<String> possibleMoves = new ArrayList<>();
        int[] currentCoords = Utils.getCoordinates(this.getPosition());
        int currentX = currentCoords[0];
        int currentY = currentCoords[1];

        int x = currentX + dx;
        int y = currentY + dy;

        while (Utils.isValidPosition(x, y)) {
            String newPosition = Utils.getPosition(x, y);
            Piece pieceAtNewPosition = board.get(newPosition);

            if (pieceAtNewPosition == null) {
                // Empty square, add to possible moves
                possibleMoves.add(newPosition);
            } else {
                // Piece encountered
                if (!pieceAtNewPosition.getColor().equals(this.getColor())) {
                    // Opponent's piece, can capture
                    possibleMoves.add(newPosition);
                }
                break; // Stop further moves in this direction
            }

            x += dx;
            y += dy;
        }

        return possibleMoves;
    }
}
