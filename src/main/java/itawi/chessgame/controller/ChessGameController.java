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
    public ResponseEntity<Void> newGame(@RequestBody(required = false) Map<String, Integer> gameSettings) {
        if (gameSettings != null && gameSettings.containsKey("timeMinutes")) {
            // Start a new game with specified time control
            chessGameService.newGame(gameSettings.get("timeMinutes"));
        } else {
            // Start new game with default time (5 minutes)
            chessGameService.newGame();
        }
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

    @PostMapping("/timer/pause")
    public ResponseEntity<Map<String, Object>> pauseTimer() {
        // Only pause if the game has started
        if (chessGameService.getCurrentGame().getTimer().isTimerRunning()) {
            chessGameService.getCurrentGame().getTimer().stopTimer();
        }

        return ResponseEntity.ok(Map.of(
                "timerRunning", chessGameService.getCurrentGame().getTimer().isTimerRunning(),
                "gameStatus", chessGameService.getGameStatus()
        ));
    }

    @PostMapping("/timer/resume")
    public ResponseEntity<Map<String, Object>> resumeTimer() {
        // Only resume if the game is not over
        if (!chessGameService.getCurrentGame().isGameOver()) {
            chessGameService.getCurrentGame().getTimer().startTimer();
        }

        return ResponseEntity.ok(Map.of(
                "timerRunning", chessGameService.getCurrentGame().getTimer().isTimerRunning(),
                "gameStatus", chessGameService.getGameStatus()
        ));
    }
}
