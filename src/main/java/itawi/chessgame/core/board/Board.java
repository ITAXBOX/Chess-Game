package itawi.chessgame.core.board;

import itawi.chessgame.core.piece.*;
import itawi.chessgame.core.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {
    private final Map<String, Piece> board; // Maps positions (e.g., "a1") to pieces

    public Board() {
        this.board = new HashMap<>();
        initializeBoard();
    }

    // Initialize the board with the standard chess setup
    private void initializeBoard() {
        // Place pawns
        for (char file = 'a'; file <= 'h'; file++) {
            board.put(file + "2", new Pawn("white", file + "2"));
            board.put(file + "7", new Pawn("black", file + "7"));
        }

        // Place rooks
        board.put("a1", new Rook("white", "a1"));
        board.put("h1", new Rook("white", "h1"));
        board.put("a8", new Rook("black", "a8"));
        board.put("h8", new Rook("black", "h8"));

        // Place knights
        board.put("b1", new Knight("white", "b1"));
        board.put("g1", new Knight("white", "g1"));
        board.put("b8", new Knight("black", "b8"));
        board.put("g8", new Knight("black", "g8"));

        // Place bishops
        board.put("c1", new Bishop("white", "c1"));
        board.put("f1", new Bishop("white", "f1"));
        board.put("c8", new Bishop("black", "c8"));
        board.put("f8", new Bishop("black", "f8"));

        // Place queens
        board.put("d1", new Queen("white", "d1"));
        board.put("d8", new Queen("black", "d8"));

        // Place kings
        board.put("e1", new King("white", "e1"));
        board.put("e8", new King("black", "e8"));
    }

    // Get the piece at a specific position
    public Piece getPieceAt(String position) {
        return board.get(position);
    }

    // Get the current board state
    public Map<String, Piece> getBoardState() {
        return new HashMap<>(board); // Return a copy to prevent external modifications
    }

    // Move a piece from one position to another
    public boolean movePiece(String fromPosition, String toPosition, String currentTurn) {
        Piece piece = getPieceAt(fromPosition);

        // Check if there is a piece at the starting position
        if (piece == null) {
            return false;
        }

        // Check if it's the correct player's turn
        if (!piece.getColor().equals(currentTurn)) {
            return false;
        }

        // Check if the move is in the list of possible moves
        List<String> possibleMoves = piece.getPossibleMoves(board);
        if (!possibleMoves.contains(toPosition)) {
            return false;
        }

        // Handle castling
        if (piece instanceof King && Math.abs(Utils.getCoordinates(fromPosition)[0] - Utils.getCoordinates(toPosition)[0]) == 2) {
            return performCastling(fromPosition, toPosition, currentTurn);
        }

        // Simulate the move to check if it leaves the king in check
        Map<String, Piece> simulatedBoard = new HashMap<>(board);
        simulatedBoard.remove(fromPosition);
        simulatedBoard.put(toPosition, piece);

        // Check if the king is in check after the move
        if (isKingInCheck(piece.getColor(), simulatedBoard)) {
            return false; // Move is invalid because it leaves the king in check
        }

        // Move the piece
        board.remove(fromPosition);
        board.put(toPosition, piece);
        piece.setPosition(toPosition);

        // Update hasMoved for pawns, rooks, and kings
        switch (piece) {
            case Pawn pawn -> pawn.setHasMoved(true);
            case Rook rook -> rook.setHasMoved(true);
            case King king -> king.setHasMoved(true);
            default -> {
            }
        }

        return true;
    }

    // Perform castling
    private boolean performCastling(String fromPosition, String toPosition, String currentTurn) {
        // Determine rook positions based on castling type
        String rookPosition;
        String newRookPosition;

        if (toPosition.equals("g1") || toPosition.equals("g8")) {
            // King-side castling
            rookPosition = toPosition.equals("g1") ? "h1" : "h8";
            newRookPosition = toPosition.equals("g1") ? "f1" : "f8";
        } else {
            // Queen-side castling
            rookPosition = toPosition.equals("c1") ? "a1" : "a8";
            newRookPosition = toPosition.equals("c1") ? "d1" : "d8";
        }

        // Validate rook
        Piece rook = board.get(rookPosition);
        if (!(rook instanceof Rook) || ((Rook) rook).getHasMoved()) {
            return false;
        }

        // Validate king
        Piece king = board.get(fromPosition);
        if (!(king instanceof King) || ((King) king).getHasMoved()) {
            return false;
        }

        // Check if squares between king and rook are empty
        String[] squaresToCheck = toPosition.equals("c1")
                ? new String[]{"b1", "c1", "d1"}
                : new String[]{"b8", "c8", "d8"};

        for (String square : squaresToCheck) {
            if (board.get(square) != null) {
                return false;
            }
        }

        // Check if king is in check or squares are under attack
        if (isKingInCheck(currentTurn, board) || isSquareUnderAttack(currentTurn, squaresToCheck, board)) {
            return false;
        }

        // Move the king
        board.remove(fromPosition);
        board.put(toPosition, king);
        king.setPosition(toPosition);
        ((King) king).setHasMoved(true);

        // Move the rook
        board.remove(rookPosition);
        board.put(newRookPosition, rook);
        rook.setPosition(newRookPosition);
        ((Rook) rook).setHasMoved(true);

        return true;
    }

    // Check if the king is in check
    public boolean isKingInCheck(String color, Map<String, Piece> board) {
        String kingPosition = findKingPosition(color, board);

        // Iterate through all pieces on the board
        for (Map.Entry<String, Piece> entry : board.entrySet()) {
            Piece piece = entry.getValue();

            // Skip pieces of the same color
            if (piece.getColor().equals(color)) {
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
    private boolean isSquareUnderAttack(String color, String[] squares, Map<String, Piece> board) {
        // Iterate through all pieces on the board
        for (Map.Entry<String, Piece> entry : board.entrySet()) {
            Piece piece = entry.getValue();

            // Skip pieces of the same color
            if (piece.getColor().equals(color)) {
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

    // Find the king's position
    public String findKingPosition(String color, Map<String, Piece> board) {
        for (Map.Entry<String, Piece> entry : board.entrySet()) {
            Piece piece = entry.getValue();
            if (piece instanceof King && piece.getColor().equals(color)) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("King not found for color: " + color);
    }

    // Print the board for debugging
    public void printBoard() {
        for (int rank = 8; rank >= 1; rank--) {
            for (char file = 'a'; file <= 'h'; file++) {
                String position = file + String.valueOf(rank);
                Piece piece = board.get(position);
                if (piece != null) {
                    System.out.print(piece.getType().toString().charAt(0) + " ");
                } else {
                    System.out.print(". ");
                }
            }
            System.out.println();
        }
    }

    // Find the position of a specific piece on the board
    public String findPiecePosition(Piece targetPiece, Map<String, Piece> board) {
        for (Map.Entry<String, Piece> entry : board.entrySet()) {
            if (entry.getValue() == targetPiece) {
                return entry.getKey(); // Return the position of the piece
            }
        }
        throw new IllegalStateException("Piece not found on the board: " + targetPiece);
    }
}