package itawi.chessgame.controller;

import itawi.chessgame.service.ChessGameService;
import itawi.chessgame.dto.PieceDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chess")
@AllArgsConstructor
public class ChessGameController {

    private final ChessGameService chessGameService;

    @PostMapping("/new-game")
    public ResponseEntity<Void> newGame() {
        chessGameService.newGame();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/board")
    public ResponseEntity<List<PieceDTO>> getBoard() {
        return ResponseEntity.ok(chessGameService.getBoardAsPieceDTOs());
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getGameStatus() {
        return ResponseEntity.ok(chessGameService.getGameStatus());
    }

    @GetMapping("/valid-moves/{position}")
    public ResponseEntity<List<String>> getValidMoves(@PathVariable String position) {
        return ResponseEntity.ok(chessGameService.getValidMovesForPiece(position));
    }

    @GetMapping("/highlighted-squares/{position}")
    public ResponseEntity<List<String>> getHighlightedSquares(@PathVariable String position) {
        return ResponseEntity.ok(chessGameService.getHighlightedSquares(position));
    }

    @PostMapping("/move")
    public ResponseEntity<Map<String, Object>> makeMove(@RequestBody Map<String, String> moveRequest) {
        String from = moveRequest.get("from");
        String to = moveRequest.get("to");

        boolean success = chessGameService.makeMove(from, to);

        Map<String, Object> response = Map.of(
                "success", success,
                "board", chessGameService.getBoardAsPieceDTOs(),
                "gameStatus", chessGameService.getGameStatus()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/promote")
    public ResponseEntity<Map<String, Object>> promotePawn(@RequestBody Map<String, String> promotionRequest) {
        String position = promotionRequest.get("position");
        String pieceType = promotionRequest.get("pieceType");

        chessGameService.promotePawn(position, pieceType);

        Map<String, Object> response = Map.of(
                "board", chessGameService.getBoardAsPieceDTOs(),
                "gameStatus", chessGameService.getGameStatus()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/square-click")
    public ResponseEntity<Map<String, Object>> handleSquareClick(@RequestBody Map<String, String> clickRequest) {
        String position = clickRequest.get("position");
        String selectedPosition = clickRequest.get("selectedPosition");

        Map<String, Object> response = chessGameService.handleSquareClick(position, selectedPosition);
        return ResponseEntity.ok(response);
    }
}
