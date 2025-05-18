package itawi.chessgame.core.game;

import itawi.chessgame.core.board.Board;
import itawi.chessgame.core.piece.*;
import itawi.chessgame.core.util.Utils;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Game {
    private final Board board;
    private String currentTurn; // "white" or "black"
    private boolean isGameOver;
    private final List<Map<String, Piece>> boardStateHistory;

    public Game() {
        this.board = new Board();
        this.currentTurn = "white"; // White starts first
        this.isGameOver = false;
        boardStateHistory = new ArrayList<>();
    }

    public boolean makeMove(String fromPosition, String toPosition) {
        if (isGameOver) {
            return false; // Game is already over
        }

        // Attempt to move the piece
        Piece piece = board.getPieceAt(fromPosition);

        // Check if there is a piece at the starting position
        if (piece == null) {
            return false;
        }

        // Check if it's the correct player's turn
        if (!piece.getColor().equals(currentTurn)) {
            return false;
        }

        // Simulate the move to check if it would leave the king in check
        Map<String, Piece> simulatedBoard = new HashMap<>();
        for (Map.Entry<String, Piece> entry : board.getBoardState().entrySet()) {
            Piece p = entry.getValue();
            Piece copy = Utils.copyPiece(p);
            simulatedBoard.put(entry.getKey(), copy);
        }
        Piece movingPiece = simulatedBoard.remove(fromPosition);
        if (movingPiece != null) {
            movingPiece.setPosition(toPosition);
            simulatedBoard.put(toPosition, movingPiece);
        }

        // Check if the king would be in check after the move
        if (board.isKingInCheck(currentTurn, simulatedBoard)) {
            // Do NOT switch turn if move is illegal
            return false; // Move would leave king in check
        }

        // Now actually perform the move on the real board
        boolean moveSuccess = board.movePiece(fromPosition, toPosition, currentTurn);
        if (!moveSuccess) {
            // Do NOT switch turn if move is illegal
            return false; // Move failed for some other reason
        }

        // Update enPassantTarget after a pawn moves two squares
        Piece movedPiece = board.getPieceAt(toPosition);
        if (movedPiece instanceof Pawn) {
            int[] fromCoords = Utils.getCoordinates(fromPosition);
            int[] toCoords = Utils.getCoordinates(toPosition);
            int deltaY = Math.abs(toCoords[1] - fromCoords[1]);
            if (deltaY == 2) {
                // Set the skipped square as enPassantTarget
                int skippedY = (fromCoords[1] + toCoords[1]) / 2;
                String enPassantSquare = Utils.getPosition(fromCoords[0], skippedY);
                board.setEnPassantTarget(enPassantSquare);
            } else {
                board.setEnPassantTarget(null);
            }

            // Check for pawn promotion
            int promotionRank = movedPiece.getColor().equals("white") ? 7 : 0; // White promotes on rank 8, black on rank 1
            if (toCoords[1] == promotionRank) {
                // Handle pawn promotion
                handlePawnPromotion(toPosition);
            }
        } else {
            board.setEnPassantTarget(null);
        }

        // Move was successful - update game state and switch turns
        if (board.getPieceAt(board.getLastMoveTo()) instanceof Pawn || board.wasCaptureMade()) {
            boardStateHistory.clear();
        }
        boardStateHistory.add(new HashMap<>(board.getBoardState()));

        // Check for perpetual draw (threefold repetition)
        if (isPerpetualDraw()) {
            System.out.println("Perpetual draw! The game is a draw due to threefold repetition.");
            isGameOver = true;
        }

        // Switch turns
        currentTurn = currentTurn.equals("white") ? "black" : "white";

        // Check for checkmate or stalemate
        if (isCheckmate()) {
            System.out.println("Checkmate! " + (currentTurn.equals("white") ? "Black" : "White") + " wins!");
            isGameOver = true;
        } else if (isStalemate()) {
            System.out.println("Stalemate! The game is a draw.");
            isGameOver = true;
        }

        return true;
    }

    // A method to handle the promotion process
    private void handlePawnPromotion(String position) {
        // In a real game with a UI, you would prompt the user to choose the piece
        // For now, let's use a default promotion to Queen
        // This should be replaced with actual user input in your frontend
        String chosenPieceType = "queen"; // Default to queen
        promotePawn(position, chosenPieceType);
    }

    private boolean isCheckmate() {
        // Check if the current player's king is in check
        if (!board.isKingInCheck(currentTurn, board.getBoardState())) {
            return false; // Not in check, so not checkmate
        }

        // Get the king's position
        String kingPosition = board.findKingPosition(currentTurn, board.getBoardState());
        Piece king = board.getPieceAt(kingPosition);

        // Check if the king can escape by moving
        if (hasLegalMoves(king, kingPosition)) {
            return false; // King can escape check
        }

        // Find all pieces attacking the king
        List<Piece> attackers = getAttackers(kingPosition, currentTurn, board.getBoardState());

        // If there are multiple attackers, the only way to escape is by moving the king
        if (attackers.size() > 1) {
            return true; // Double check, king must move (already checked above)
        }

        // If there's only one attacker, check if it can be captured or blocked
        Piece attacker = attackers.getFirst();
        String attackerPosition = board.findPiecePosition(attacker, board.getBoardState());

        // Check if the attacker can be captured
        if (canPiecePerformAction(attackerPosition, currentTurn, board.getBoardState())) {
            return false; // Attacker can be captured
        }

        // Check if the attack can be blocked (only if the attacker is a sliding piece)
        if (attacker instanceof Rook || attacker instanceof Bishop || attacker instanceof Queen) {
            return !canBlockAttack(kingPosition, attackerPosition, currentTurn, board.getBoardState()); // Attack can be blocked
        }

        return true; // No way to escape check, it's checkmate
    }

    private boolean isStalemate() {
        // Check if the current player has no legal moves and is not in check
        if (board.isKingInCheck(currentTurn, board.getBoardState())) {
            return false; // Player is in check, so it's not stalemate
        }

        // Check if any piece has a legal move
        for (Map.Entry<String, Piece> entry : board.getBoardState().entrySet()) {
            Piece piece = entry.getValue();
            if (piece.getColor().equals(currentTurn)) {
                if (hasLegalMoves(piece, entry.getKey())) {
                    return false; // Player has at least one legal move
                }
            }
        }

        return true; // Player has no legal moves and is not in check
    }

    private boolean hasLegalMoves(Piece piece, String position) {
        Map<String, Piece> currentBoard = board.getBoardState();
        List<String> possibleMoves = piece.getPossibleMoves(currentBoard);
        for (String move : possibleMoves) {
            // Simulate the move
            Map<String, Piece> simulatedBoard = new HashMap<>(currentBoard);
            simulatedBoard.remove(position);
            simulatedBoard.put(move, piece);

            // Check if the king is in check after the move
            if (!board.isKingInCheck(currentTurn, simulatedBoard)) {
                return true;
            }
        }
        return false;
    }

    // Find all pieces attacking the king
    private List<Piece> getAttackers(String kingPosition, String color, Map<String, Piece> board) {
        List<Piece> attackers = new ArrayList<>();

        // Iterate through all pieces on the board
        for (Map.Entry<String, Piece> entry : board.entrySet()) {
            Piece piece = entry.getValue();

            // Skip pieces of the same color
            if (piece.getColor().equals(color)) {
                continue;
            }

            // Check if the piece can attack the king's position
            if (piece.getPossibleMoves(board).contains(kingPosition)) {
                attackers.add(piece);
            }
        }

        return attackers;
    }

    private boolean canPiecePerformAction(String targetPosition, String color, Map<String, Piece> boardState) {
        for (Map.Entry<String, Piece> entry : boardState.entrySet()) {
            Piece piece = entry.getValue();
            String fromPosition = entry.getKey();
            if (!piece.getColor().equals(color))
                continue;

            List<String> possibleMoves = piece.getPossibleMoves(boardState);
            if (possibleMoves.contains(targetPosition)) {
                // Simulate the move
                Map<String, Piece> simulatedBoard = new HashMap<>(boardState);
                simulatedBoard.remove(fromPosition);
                simulatedBoard.put(targetPosition, piece);
                if (!board.isKingInCheck(color, simulatedBoard)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Check if the attack can be blocked (only for sliding pieces: rook, bishop, queen)
    private boolean canBlockAttack(String kingPosition, String attackerPosition, String color, Map<String, Piece> boardState) {
        int[] kingCoords = Utils.getCoordinates(kingPosition);
        int[] attackerCoords = Utils.getCoordinates(attackerPosition);

        int dx = Integer.compare(attackerCoords[0], kingCoords[0]);
        int dy = Integer.compare(attackerCoords[1], kingCoords[1]);

        int x = kingCoords[0] + dx;
        int y = kingCoords[1] + dy;
        while (x != attackerCoords[0] || y != attackerCoords[1]) {
            String square = Utils.getPosition(x, y);
            if (canPiecePerformAction(square, color, boardState)) {
                return true;
            }
            x += dx;
            y += dy;
        }
        return false;
    }

    public void promotePawn(String position, String pieceType) {
        Piece pawn = board.getPieceAt(position);
        if (!(pawn instanceof Pawn)) {
            return; // No pawn at the given position
        }

        // Check if the pawn is on the promotion rank
        int[] coords = Utils.getCoordinates(position);
        int promotionRank = pawn.getColor().equals("white") ? 7 : 0; // White promotes on rank 8, black on rank 1
        if (coords[1] != promotionRank) {
            return; // Pawn is not on the promotion rank
        }

        // Create the new piece based on the chosen type
        Piece newPiece = createPromotionPiece(pawn.getColor(), position, pieceType);
        if (newPiece == null) {
            return; // Invalid piece type
        }

        // Replace the pawn with the new piece
        board.getBoard().put(position, newPiece);
    }

    private Piece createPromotionPiece(String color, String position, String pieceType) {
        return switch (pieceType.toLowerCase()) {
            case "queen" -> new Queen(color, position);
            case "rook" -> new Rook(color, position);
            case "bishop" -> new Bishop(color, position);
            case "knight" -> new Knight(color, position);
            default -> null; // Invalid piece type
        };
    }

    private boolean isPerpetualDraw() {
        Map<String, Piece> currentBoardState = board.getBoardState();
        int repetitionCount = 0;

        // Count how many times the current board state has occurred
        for (Map<String, Piece> boardState : boardStateHistory) {
            if (boardState.equals(currentBoardState)) {
                repetitionCount++;
            }
        }

        // If the same position occurs three times, it's a perpetual draw
        return repetitionCount >= 3;
    }
}
