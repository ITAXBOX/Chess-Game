package itawi.chessgame.core.piece;

import itawi.chessgame.core.enums.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BishopTest {

    private Map<String, Piece> board;

    @BeforeEach
    void setUp() {
        board = new HashMap<>();
    }

    @Test
    void testBishopMovesFromCenter() {
        // Bishop in the middle of the board
        Bishop bishop = new Bishop("white", "d4");
        board.put("d4", bishop);

        List<String> possibleMoves = bishop.getPossibleMoves(board);

        // Bishop should move diagonally in all four directions

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

        assertEquals(13, possibleMoves.size());
    }

    @Test
    void testBishopMovesFromCorner() {
        // Bishop in the corner of the board
        Bishop bishop = new Bishop("white", "a1");
        board.put("a1", bishop);

        List<String> possibleMoves = bishop.getPossibleMoves(board);

        // Bishop should only move in one diagonal from corner
        assertTrue(possibleMoves.contains("b2"));
        assertTrue(possibleMoves.contains("c3"));
        assertTrue(possibleMoves.contains("d4"));
        assertTrue(possibleMoves.contains("e5"));
        assertTrue(possibleMoves.contains("f6"));
        assertTrue(possibleMoves.contains("g7"));
        assertTrue(possibleMoves.contains("h8"));

        assertEquals(7, possibleMoves.size());
    }

    @Test
    void testBishopBlockedByPieces() {
        // Bishop with pieces blocking its path
        Bishop bishop = new Bishop("white", "d4");
        board.put("d4", bishop);

        // Place blocking pieces
        board.put("f6", new Pawn("white", "f6")); // Ally piece blocking upper-right
        board.put("b2", new Pawn("black", "b2")); // Enemy piece blocking lower-left

        List<String> possibleMoves = bishop.getPossibleMoves(board);

        // Upper-right diagonal should be blocked after e5
        assertTrue(possibleMoves.contains("e5"));
        assertFalse(possibleMoves.contains("f6"));
        assertFalse(possibleMoves.contains("g7"));

        // Lower-left diagonal should include capturing the enemy piece but nothing beyond
        assertTrue(possibleMoves.contains("b2"));
        assertFalse(possibleMoves.contains("a1"));

        // Other diagonals should be unaffected
        assertTrue(possibleMoves.contains("c5"));
        assertTrue(possibleMoves.contains("b6"));
        assertTrue(possibleMoves.contains("a7"));
        assertTrue(possibleMoves.contains("e3"));
        assertTrue(possibleMoves.contains("f2"));
        assertTrue(possibleMoves.contains("g1"));
    }

    @Test
    void testBishopCaptureEnemyPiece() {
        // Bishop with enemy pieces to capture
        Bishop bishop = new Bishop("white", "d4");
        board.put("d4", bishop);

        // Place enemy pieces
        board.put("f6", new Pawn("black", "f6"));
        board.put("b2", new Pawn("black", "b2"));

        List<String> possibleMoves = bishop.getPossibleMoves(board);

        // Should be able to capture both enemy pieces
        assertTrue(possibleMoves.contains("f6"));
        assertTrue(possibleMoves.contains("b2"));

        // Paths should be blocked after capturing
        assertFalse(possibleMoves.contains("g7"));
        assertFalse(possibleMoves.contains("a1"));
    }

    @Test
    void testBishopType() {
        Bishop bishop = new Bishop("white", "c1");
        assertEquals(PieceType.BISHOP, bishop.getType());
    }
}
