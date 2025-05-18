package itawi.chessgame.core.util;

import itawi.chessgame.core.piece.*;

public class Utils {
    public static int[] getCoordinates(String position) {
        if (position == null || position.length() != 2) {
            throw new IllegalArgumentException("Invalid position: " + position);
        }
        int x = position.charAt(0) - 'a'; // Convert 'a'-'h' to 0-7
        int y = Character.getNumericValue(position.charAt(1)) - 1; // Convert '1'-'8' to 0-7
        return new int[]{x, y};
    }

    public static String getPosition(int x, int y) {
        if (x < 0 || x > 7 || y < 0 || y > 7) {
            return null;
        }
        char file = (char) ('a' + x); // Convert 0-7 to 'a'-'h'
        char rank = (char) ('1' + y); // Convert 0-7 to '1'-'8'
        return String.valueOf(file) + rank;
    }

    public static boolean isValidPosition(String position) {
        if (position == null || position.length() != 2) {
            return false;
        }
        int x = position.charAt(0) - 'a';
        int y = Character.getNumericValue(position.charAt(1)) - 1;
        return x >= 0 && x <= 7 && y >= 0 && y <= 7;
    }

    public static boolean isValidPosition(int x, int y) {
        return x >= 0 && x <= 7 && y >= 0 && y <= 7;
    }

    // Deep copy a piece (for move simulation)
    public static Piece copyPiece(Piece piece) {
        if (piece == null) return null;
        String color = piece.getColor();
        String position = piece.getPosition();
        switch (piece.getType()) {
            case PAWN -> {
                Pawn pawn = new Pawn(color, position);
                pawn.setHasMoved(((Pawn) piece).isHasMoved());
                return pawn;
            }
            case ROOK -> {
                return new Rook(color, position);
            }
            case KNIGHT -> {
                return new Knight(color, position);
            }
            case BISHOP -> {
                return new Bishop(color, position);
            }
            case QUEEN -> {
                return new Queen(color, position);
            }
            case KING -> {
                return new King(color, position);
            }
            default -> throw new IllegalArgumentException("Unknown piece type");
        }
    }
}
