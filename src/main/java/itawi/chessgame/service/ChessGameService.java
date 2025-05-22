package itawi.chessgame.service;

import itawi.chessgame.core.board.Board;
import itawi.chessgame.core.game.Game;
import itawi.chessgame.core.piece.Pawn;
import itawi.chessgame.core.piece.Piece;
import itawi.chessgame.core.util.Utils;
import itawi.chessgame.dto.PieceDTO;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@Getter
public class ChessGameService {
    private Game currentGame;

    public void newGame() {
        this.currentGame = new Game();
    }

    public Map<String, Piece> getBoardState() {
        return this.currentGame.getBoard().getBoardState();
    }

    public String getCurrentTurn() {
        return this.currentGame.getCurrentTurn();
    }

    public boolean isGameOver() {
        return this.currentGame.isGameOver();
    }

    public List<String> getValidMovesForPiece(String position) {
        Piece piece = currentGame.getBoard().getPieceAt(position);
        if (piece == null || !piece.getColor().equals(getCurrentTurn())) {
            return List.of(); // No valid moves for empty square or opponent's piece
        }

        // Pass the actual Board instance instead of just the board state map
        return piece.getPossibleMoves(currentGame.getBoard()).stream()
                .filter(move -> isLegalMove(position, move))
                .toList();
    }

    private boolean isLegalMove(String from, String to) {
        // Create simulated board
        Map<String, Piece> boardState = currentGame.getBoard().getBoardState();
        Piece piece = boardState.get(from);

        if (piece == null) {
            return false;
        }

        // Simulate the move
        Map<String, Piece> simulatedBoard = new HashMap<>(Map.copyOf(boardState));
        simulatedBoard.remove(from);
        simulatedBoard.put(to, piece);

        // Check if the king is in check after the move
        return !currentGame.getBoard().isKingInCheck(piece.getColor(), simulatedBoard);
    }

    public boolean makeMove(String fromPosition, String toPosition) {
        return currentGame.makeMove(fromPosition, toPosition);
    }

    public void promotePawn(String position, String pieceType) {
        currentGame.promotePawn(position, pieceType);
    }

    public List<PieceDTO> getBoardAsPieceDTOs() {
        Map<String, Piece> boardState = getBoardState();
        return boardState.entrySet().stream()
                .map(entry -> {
                    Piece piece = entry.getValue();
                    String position = entry.getKey();
                    return new PieceDTO(
                            position, // Use position as ID
                            piece.getType().toString(),
                            piece.getColor(),
                            position
                    );
                })
                .toList();
    }

    public Map<String, Object> getGameStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("currentTurn", getCurrentTurn());
        status.put("isGameOver", isGameOver());

        // Check for check, checkmate, stalemate
        Board board = currentGame.getBoard();
        String currentPlayer = getCurrentTurn();
        boolean inCheck = board.isKingInCheck(currentPlayer, board.getBoardState());

        status.put("inCheck", inCheck);

        if (isGameOver()) {
            if (inCheck) {
                status.put("result", (currentPlayer.equals("white") ? "Black" : "White") + " wins by checkmate");
            } else {
                status.put("result", "Draw by stalemate");
            }
        }

        return status;
    }

    public Map<String, Object> handleSquareClick(String position, String selectedPosition) {
        Map<String, Object> response = new HashMap<>();

        // If no square is selected or clicking the same square, select/deselect this square
        if (selectedPosition == null || selectedPosition.equals(position)) {
            Piece piece = currentGame.getBoard().getPieceAt(position);

            // Only allow selecting your own pieces
            if (piece != null && piece.getColor().equals(getCurrentTurn())) {
                response.put("selectedPosition", position);
                response.put("validMoves", getValidMovesForPiece(position));
            } else {
                response.put("selectedPosition", null);
                response.put("validMoves", List.of());
            }
        }
        // If a square is already selected, attempt to move there
        else {
            // Store the board state before the move to detect captured pieces
            Map<String, Piece> boardStateBefore = new HashMap<>(currentGame.getBoard().getBoardState());

            // Check if this might be an en passant move
            boolean isPotentialEnPassant = false;
            Piece movingPiece = currentGame.getBoard().getPieceAt(selectedPosition);
            String enPassantTarget = currentGame.getBoard().getEnPassantTarget();

            if (movingPiece instanceof Pawn && position.equals(enPassantTarget)) {
                isPotentialEnPassant = true;
            }

            boolean moveSuccess = makeMove(selectedPosition, position);
            response.put("moveSuccess", moveSuccess);
            response.put("selectedPosition", null); // Clear selection after move attempt

            if (moveSuccess) {
                response.put("newBoardState", getBoardAsPieceDTOs());
                response.put("gameStatus", getGameStatus());

                // Check for captured pieces by comparing board states
                if (isPotentialEnPassant) {
                    // For en passant, the captured pawn is not on the destination square
                    int[] posCoords = Utils.getCoordinates(position);
                    int capturedY = movingPiece.getColor().equals("white") ? posCoords[1] - 1 : posCoords[1] + 1;
                    String capturedPawnPosition = Utils.getPosition(posCoords[0], capturedY);

                    Piece capturedPawn = boardStateBefore.get(capturedPawnPosition);
                    if (capturedPawn != null) {
                        Map<String, String> capturedPieceInfo = new HashMap<>();
                        capturedPieceInfo.put("type", capturedPawn.getType().toString());
                        capturedPieceInfo.put("color", capturedPawn.getColor());
                        response.put("capturedPiece", capturedPieceInfo);
                    }
                } else {
                    // Normal capture check
                    Piece capturedPiece = boardStateBefore.get(position);
                    if (capturedPiece != null) {
                        Map<String, String> capturedPieceInfo = new HashMap<>();
                        capturedPieceInfo.put("type", capturedPiece.getType().toString());
                        capturedPieceInfo.put("color", capturedPiece.getColor());
                        response.put("capturedPiece", capturedPieceInfo);
                    }
                }

                // Check for pawn promotion
                Piece movedPiece = currentGame.getBoard().getPieceAt(position);
                if (movedPiece != null && movedPiece.getType().toString().equals("PAWN")) {
                    int rank = position.charAt(1) - '0';
                    if ((movedPiece.getColor().equals("white") && rank == 8) ||
                            (movedPiece.getColor().equals("black") && rank == 1)) {
                        response.put("pawnPromotion", true);
                        response.put("promotionPosition", position);
                    }
                }
            }
        }

        return response;
    }
}

