package itawi.chessgame.core.piece;

import itawi.chessgame.core.enums.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QueenTest {

    private Map<String, Piece> board;

    @BeforeEach
    void setUp() {
        board = new HashMap<>();
    }

    @Test
    void testQueenMovesFromCenter() {
        // Queen in the middle of the board
        Queen queen = new Queen("white", "d4");
        board.put("d4", queen);

        List<String> possibleMoves = queen.getPossibleMoves(board);

        // Horizontal moves (left and right) - like a Rook
        assertTrue(possibleMoves.contains("a4"));
        assertTrue(possibleMoves.contains("b4"));
        assertTrue(possibleMoves.contains("c4"));
        assertTrue(possibleMoves.contains("e4"));
        assertTrue(possibleMoves.contains("f4"));
        assertTrue(possibleMoves.contains("g4"));
        assertTrue(possibleMoves.contains("h4"));

        // Vertical moves (up and down) - like a Rook
        assertTrue(possibleMoves.contains("d1"));
        assertTrue(possibleMoves.contains("d2"));
        assertTrue(possibleMoves.contains("d3"));
        assertTrue(possibleMoves.contains("d5"));
        assertTrue(possibleMoves.contains("d6"));
        assertTrue(possibleMoves.contains("d7"));
        assertTrue(possibleMoves.contains("d8"));

        // Diagonal moves - like a Bishop
        // Upper-right diagonal
        assertTrue(possibleMoves.contains("e5"));
        assertTrue(possibleMoves.contains("f6"));
        assertTrue(possibleMoves.contains("g7"));
        assertTrue(possibleMoves.contains("h8"));

        // Upper-left diagonal
        assertTrue(possibleMoves.contains("c5"));
        assertTrue(possibleMoves.contains("b6"));
        assertTrue(possibleMoves.contains("a7"));

        // Lower-right diagonal
        assertTrue(possibleMoves.contains("e3"));
        assertTrue(possibleMoves.contains("f2"));
        assertTrue(possibleMoves.contains("g1"));

        // Lower-left diagonal
        assertTrue(possibleMoves.contains("c3"));
        assertTrue(possibleMoves.contains("b2"));
        assertTrue(possibleMoves.contains("a1"));

        // Queen can move in 8 directions (horizontals, verticals, diagonals)
        assertEquals(27, possibleMoves.size());
    }

    @Test
    void testQueenBlockedByPieces() {
        // Queen with pieces blocking its path
        Queen queen = new Queen("white", "d4");
        board.put("d4", queen);

        // Place blocking pieces
        board.put("d6", new Pawn("white", "d6")); // Ally piece blocking vertical up
        board.put("f6", new Pawn("white", "f6")); // Ally piece blocking diagonal up-right
        board.put("b4", new Pawn("black", "b4")); // Enemy piece blocking horizontal left
        board.put("b2", new Pawn("black", "b2")); // Enemy piece blocking diagonal down-left

        List<String> possibleMoves = queen.getPossibleMoves(board);

        // Upward direction should be blocked after d5
        assertTrue(possibleMoves.contains("d5"));
        assertFalse(possibleMoves.contains("d6"));
        assertFalse(possibleMoves.contains("d7"));

        // Up-right diagonal should be blocked after e5
        assertTrue(possibleMoves.contains("e5"));
        assertFalse(possibleMoves.contains("f6"));
        assertFalse(possibleMoves.contains("g7"));

        // Left horizontal should include capturing the enemy piece but nothing beyond
        assertTrue(possibleMoves.contains("b4"));
        assertFalse(possibleMoves.contains("a4"));

        // Down-left diagonal should include capturing the enemy piece but nothing beyond
        assertTrue(possibleMoves.contains("b2"));
        assertFalse(possibleMoves.contains("a1"));

        // Unblocked directions should still be available
        assertTrue(possibleMoves.contains("d3"));
        assertTrue(possibleMoves.contains("e4"));
        assertTrue(possibleMoves.contains("c5"));
    }

    @Test
    void testQueenCaptureEnemyPiece() {
        // Queen with enemy pieces to capture
        Queen queen = new Queen("white", "d4");
        board.put("d4", queen);

        // Place enemy pieces
        board.put("d7", new Pawn("black", "d7")); // Vertical up
        board.put("g7", new Pawn("black", "g7")); // Diagonal up-right
        board.put("g4", new Pawn("black", "g4")); // Horizontal right

        List<String> possibleMoves = queen.getPossibleMoves(board);

        // Should be able to capture all enemy pieces
        assertTrue(possibleMoves.contains("d7"));
        assertTrue(possibleMoves.contains("g7"));
        assertTrue(possibleMoves.contains("g4"));

        // Paths should be blocked after capturing
        assertFalse(possibleMoves.contains("d8"));
        assertFalse(possibleMoves.contains("h8"));
        assertFalse(possibleMoves.contains("h4"));
    }

    @Test
    void testQueenType() {
        Queen queen = new Queen("white", "d1");
        assertEquals(PieceType.QUEEN, queen.getType());
    }
}
