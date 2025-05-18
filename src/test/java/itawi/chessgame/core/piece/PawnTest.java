package itawi.chessgame.core.piece;

import itawi.chessgame.core.enums.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PawnTest {

    private Map<String, Piece> board;

    @BeforeEach
    void setUp() {
        board = new HashMap<>();
    }

    @Test
    void testWhitePawnInitialMoves() {
        // White pawn at starting position
        Pawn whitePawn = new Pawn("white", "e2");
        board.put("e2", whitePawn);

        List<String> possibleMoves = whitePawn.getPossibleMoves(board);

        // Should be able to move one or two squares forward
        assertTrue(possibleMoves.contains("e3"));
        assertTrue(possibleMoves.contains("e4"));
        assertEquals(2, possibleMoves.size());
    }

    @Test
    void testBlackPawnInitialMoves() {
        // Black pawn at starting position
        Pawn blackPawn = new Pawn("black", "e7");
        board.put("e7", blackPawn);

        List<String> possibleMoves = blackPawn.getPossibleMoves(board);

        // Should be able to move one or two squares forward
        assertTrue(possibleMoves.contains("e6"));
        assertTrue(possibleMoves.contains("e5"));
        assertEquals(2, possibleMoves.size());
    }

    @Test
    void testPawnCaptureMove() {
        // White pawn with opponent pieces to capture
        Pawn whitePawn = new Pawn("white", "e4");
        board.put("e4", whitePawn);

        // Add opponent pieces diagonal to the pawn
        board.put("d5", new Pawn("black", "d5"));
        board.put("f5", new Pawn("black", "f5"));

        List<String> possibleMoves = whitePawn.getPossibleMoves(board);

        // Should be able to move forward and capture diagonally
        assertTrue(possibleMoves.contains("e5"));
        assertTrue(possibleMoves.contains("d5"));
        assertTrue(possibleMoves.contains("f5"));
        assertEquals(3, possibleMoves.size());
    }

    @Test
    void testPawnBlockedMove() {
        // White pawn blocked by another piece
        Pawn whitePawn = new Pawn("white", "e2");
        board.put("e2", whitePawn);

        // Place a piece directly in front
        board.put("e3", new Pawn("black", "e3"));

        List<String> possibleMoves = whitePawn.getPossibleMoves(board);

        // Should not be able to move forward
        assertTrue(possibleMoves.isEmpty());
    }

    @Test
    void testPawnNoCaptureAllies() {
        // White pawn with all pieces diagonally
        Pawn whitePawn = new Pawn("white", "e4");
        board.put("e4", whitePawn);

        // Add all pieces diagonal to the pawn
        board.put("d5", new Pawn("white", "d5"));
        board.put("f5", new Pawn("white", "f5"));

        List<String> possibleMoves = whitePawn.getPossibleMoves(board);

        // Should only be able to move forward
        assertTrue(possibleMoves.contains("e5"));
        assertEquals(1, possibleMoves.size());
    }

    @Test
    void testPawnType() {
        Pawn pawn = new Pawn("white", "e2");
        assertEquals(PieceType.PAWN, pawn.getType());
    }
}
