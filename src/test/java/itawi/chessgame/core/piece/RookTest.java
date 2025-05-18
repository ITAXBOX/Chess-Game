package itawi.chessgame.core.piece;

import itawi.chessgame.core.enums.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RookTest {

    private Map<String, Piece> board;

    @BeforeEach
    void setUp() {
        board = new HashMap<>();
    }

    @Test
    void testRookMovesFromCenter() {
        // Rook in the middle of the board
        Rook rook = new Rook("white", "d4");
        board.put("d4", rook);

        List<String> possibleMoves = rook.getPossibleMoves(board);

        // Horizontal moves (left and right)
        assertTrue(possibleMoves.contains("a4"));
        assertTrue(possibleMoves.contains("b4"));
        assertTrue(possibleMoves.contains("c4"));
        assertTrue(possibleMoves.contains("e4"));
        assertTrue(possibleMoves.contains("f4"));
        assertTrue(possibleMoves.contains("g4"));
        assertTrue(possibleMoves.contains("h4"));

        // Vertical moves (up and down)
        assertTrue(possibleMoves.contains("d1"));
        assertTrue(possibleMoves.contains("d2"));
        assertTrue(possibleMoves.contains("d3"));
        assertTrue(possibleMoves.contains("d5"));
        assertTrue(possibleMoves.contains("d6"));
        assertTrue(possibleMoves.contains("d7"));
        assertTrue(possibleMoves.contains("d8"));

        assertEquals(14, possibleMoves.size());
    }

    @Test
    void testRookMovesFromCorner() {
        // Rook in the corner of the board
        Rook rook = new Rook("white", "a1");
        board.put("a1", rook);

        List<String> possibleMoves = rook.getPossibleMoves(board);

        // Horizontal moves
        assertTrue(possibleMoves.contains("b1"));
        assertTrue(possibleMoves.contains("c1"));
        assertTrue(possibleMoves.contains("d1"));
        assertTrue(possibleMoves.contains("e1"));
        assertTrue(possibleMoves.contains("f1"));
        assertTrue(possibleMoves.contains("g1"));
        assertTrue(possibleMoves.contains("h1"));

        // Vertical moves
        assertTrue(possibleMoves.contains("a2"));
        assertTrue(possibleMoves.contains("a3"));
        assertTrue(possibleMoves.contains("a4"));
        assertTrue(possibleMoves.contains("a5"));
        assertTrue(possibleMoves.contains("a6"));
        assertTrue(possibleMoves.contains("a7"));
        assertTrue(possibleMoves.contains("a8"));

        assertEquals(14, possibleMoves.size());
    }

    @Test
    void testRookBlockedByPieces() {
        // Rook with pieces blocking its path
        Rook rook = new Rook("white", "d4");
        board.put("d4", rook);

        // Place blocking pieces
        board.put("d6", new Pawn("white", "d6")); // Ally piece blocking up
        board.put("b4", new Pawn("black", "b4")); // Enemy piece blocking left

        List<String> possibleMoves = rook.getPossibleMoves(board);

        // Upward direction should be blocked after d5
        assertTrue(possibleMoves.contains("d5"));
        assertFalse(possibleMoves.contains("d6"));
        assertFalse(possibleMoves.contains("d7"));

        // Leftward direction should include capturing the enemy piece but nothing beyond
        assertTrue(possibleMoves.contains("b4"));
        assertFalse(possibleMoves.contains("a4"));

        // Other directions should be unaffected
        assertTrue(possibleMoves.contains("d3"));
        assertTrue(possibleMoves.contains("d2"));
        assertTrue(possibleMoves.contains("d1"));
        assertTrue(possibleMoves.contains("e4"));
        assertTrue(possibleMoves.contains("f4"));
        assertTrue(possibleMoves.contains("g4"));
        assertTrue(possibleMoves.contains("h4"));
    }

    @Test
    void testRookCaptureEnemyPiece() {
        // Rook with enemy pieces to capture
        Rook rook = new Rook("white", "d4");
        board.put("d4", rook);

        // Place enemy pieces
        board.put("d7", new Pawn("black", "d7"));
        board.put("g4", new Pawn("black", "g4"));

        List<String> possibleMoves = rook.getPossibleMoves(board);

        // Should be able to capture both enemy pieces
        assertTrue(possibleMoves.contains("d7"));
        assertTrue(possibleMoves.contains("g4"));

        // Paths should be blocked after capturing
        assertFalse(possibleMoves.contains("d8"));
        assertFalse(possibleMoves.contains("h4"));
    }

    @Test
    void testRookHasMoved() {
        // Test that the hasMoved property works correctly
        Rook rook = new Rook("white", "a1");
        assertFalse(rook.getHasMoved());

        rook.setHasMoved(true);
        assertTrue(rook.getHasMoved());
    }

    @Test
    void testRookType() {
        Rook rook = new Rook("white", "a1");
        assertEquals(PieceType.ROOK, rook.getType());
    }
}
