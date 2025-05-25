package itawi.chessgame.core.game;

import itawi.chessgame.core.board.Board;
import itawi.chessgame.core.piece.*;
import itawi.chessgame.core.timer.ChessTimer;
import itawi.chessgame.core.util.Utils;
import lombok.Getter;
import lombok.Setter;

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
    @Setter
    private int halfMoveCounter; // Counter for the 50-move rule

    private final ChessTimer timer; // Chess timer for the game
    private String timeoutPlayer; // Player who ran out of time, if any

    public Game() {
        this(5); // Default to 5 minutes per player
    }

    public Game(int timeMinutes) {
        this.board = new Board();
        this.currentTurn = "white"; // White starts first
        this.isGameOver = false;
        boardStateHistory = new ArrayList<>();
        this.halfMoveCounter = 0; // Initialize the counter
        this.timer = new ChessTimer(timeMinutes);
        this.timeoutPlayer = null;
    }

    // Constructor for testing purposes
    public Game(String currentTurn) {
        this.board = new Board();
        this.currentTurn = currentTurn;
        this.isGameOver = false;
        boardStateHistory = new ArrayList<>();
        this.halfMoveCounter = 0; // Initialize the counter
        this.timer = new ChessTimer(5); // Default 5 minutes
        this.timeoutPlayer = null;
    }

    public boolean makeMove(String fromPosition, String toPosition) {
        if (isGameOver) {
            return false; // Game is already over
        }

        // Check for timeout before making move
        checkForTimeout();
        if (isGameOver) {
            return false; // Game ended due to timeout
        }

        // Attempt to move the piece
        Piece piece = board.getPieceAt(fromPosition);

        // Check if there is a piece at the starting position
        if (piece == null) {
            // Check for stalemate if there's no piece (could indicate no valid moves)
            if (checkForStalemateCondition()) {
                isGameOver = true;
                System.out.println("Stalemate detected! The game is a draw.");
            }
            return false;
        }

        // Check if the piece belongs to the current player
        if (!piece.getColor().equalsIgnoreCase(currentTurn)) {
            return false; // Not this player's turn
        }

        // Start the game timer when first move is made
        if (!timer.isTimerRunning()) {
            timer.startTimer();
        }

        // Simulate the move to check if it would leave the king in check
        Map<String, Piece> simulatedBoard = new HashMap<>();
        for (Map.Entry<String, Piece> entry : board.getBoardState().entrySet()) {
            Piece p = entry.getValue();
            Piece copy = Utils.copyPiece(p);
            simulatedBoard.put(entry.getKey(), copy);
        }

        // Make sure we have a valid piece to move in the simulation
        Piece movingPiece = simulatedBoard.get(fromPosition);
        if (movingPiece == null) {
            return false; // Can't move a non-existent piece
        }

        // If en passant, also remove the captured pawn from simulation
        boolean isEnPassant = piece instanceof Pawn && toPosition.equals(board.getEnPassantTarget());
        if (isEnPassant) {
            int[] toCoords = Utils.getCoordinates(toPosition);
            int capturedY = piece.getColor().equals("white") ? toCoords[1] - 1 : toCoords[1] + 1;
            String capturedPawnPosition = Utils.getPosition(toCoords[0], capturedY);
            simulatedBoard.remove(capturedPawnPosition);
        }

        simulatedBoard.remove(fromPosition);
        movingPiece.setPosition(toPosition);
        simulatedBoard.put(toPosition, movingPiece);

        // Verify the king is still on the board before checking if it's in check
        try {
            // Check if the king would be in check after the move
            if (board.isKingInCheck(currentTurn, simulatedBoard)) {
                // Check for stalemate when a move fails due to king being in check
                if (checkForStalemateCondition()) {
                    isGameOver = true;
                    System.out.println("Stalemate detected! The game is a draw.");
                }
                return false; // Move would leave king in check
            }
        } catch (IllegalStateException e) {
            // If king not found, this is likely an invalid move
            System.out.println("Invalid move: " + e.getMessage());
            return false;
        }

        // Now actually perform the move on the real board
        boolean moveSuccess = board.movePiece(fromPosition, toPosition, currentTurn);
        if (!moveSuccess) {
            // Check for stalemate when a move fails for other reasons
            if (checkForStalemateCondition()) {
                isGameOver = true;
                System.out.println("Stalemate detected! The game is a draw.");
            }
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
        } else {
            board.setEnPassantTarget(null);
        }

        // Move was successful - update game state and switch turns
        if (board.getPieceAt(board.getLastMoveTo()) instanceof Pawn || board.wasCaptureMade()) {
            boardStateHistory.clear();
            halfMoveCounter = 0; // Reset the counter when a pawn is moved or a capture is made
        } else {
            halfMoveCounter++; // Increment the counter for moves without pawn movement or capture

            // Immediate check for fifty-move rule right after incrementing the counter
            if (halfMoveCounter >= 100) { // 50 full moves = 100 half-moves
                System.out.println("Fifty-move rule! The game is a draw.");
                isGameOver = true;

                // Add current board state to history before returning
                boardStateHistory.add(new HashMap<>(board.getBoardState()));

                // Switch turns before returning
                currentTurn = currentTurn.equals("white") ? "black" : "white";

                return true; // End the method here, as the game is over
            }
        }

        // Add current board state to history
        boardStateHistory.add(new HashMap<>(board.getBoardState()));

        // Switch turns
        currentTurn = currentTurn.equals("white") ? "black" : "white";

        // Check for perpetual draw (threefold repetition)
        if (isPerpetualDraw()) {
            System.out.println("Perpetual draw! The game is a draw due to threefold repetition.");
            isGameOver = true;
            return true; // End the method here, as the game is over
        }

        // Check for checkmate or stalemate
        if (isCheckmate()) {
            System.out.println("Checkmate! " + (currentTurn.equals("white") ? "Black" : "White") + " wins!");
            isGameOver = true;
        } else if (isStalemate()) {
            System.out.println("Stalemate! The game is a draw.");
            isGameOver = true;
        }

        // Update the timer after a successful move
        timer.switchTurn();

        return true;
    }

    private void checkForTimeout() {
        if (timer.isTimeout()) {
            isGameOver = true;
            timeoutPlayer = currentTurn;
            System.out.println("Time's up! " + (currentTurn.equals("white") ? "Black" : "White") + " wins!");
        }
    }

    private boolean isCheckmate() {
        // First check for timeout
        checkForTimeout();
        if (isGameOver && timeoutPlayer != null) {
            return false; // Game ended due to timeout, not checkmate
        }

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
        // Check if the board has insufficient material for checkmate (automatic draw)
        if (hasInsufficientMaterial()) {
            return true;
        }

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

    /**
     * Checks if the board has insufficient material to deliver checkmate.
     * Cases of insufficient material:
     * 1. King vs King
     * 2. King + Bishop vs King
     * 3. King + Knight vs King
     *
     * @return true if there is insufficient material for a checkmate
     */
    public boolean hasInsufficientMaterial() {
        Map<String, Piece> pieces = board.getBoardState();

        // Count pieces by type
        int totalPieces = pieces.size();
        int bishops = 0;
        int knights = 0;
        int otherPieces = 0; // pawns, queens, rooks

        for (Piece piece : pieces.values()) {
            if (piece instanceof Bishop) {
                bishops++;
            } else if (piece instanceof Knight) {
                knights++;
            } else if (!(piece instanceof King)) {
                otherPieces++;
            }
        }

        // Case 1: Only kings remain
        if (totalPieces == 2) {
            return true; // King vs King
        }

        // Case 2: King + Bishop vs King
        if (totalPieces == 3 && bishops == 1 && knights == 0 && otherPieces == 0) {
            return true;
        }

        // Case 3: King + Knight vs King
        return totalPieces == 3 && knights == 1 && bishops == 0 && otherPieces == 0;
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

    public boolean isPerpetualDraw() {
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

    private boolean checkForStalemateCondition() {
        return isStalemate();
    }
}
