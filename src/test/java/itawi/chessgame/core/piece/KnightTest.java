package itawi.chessgame.core.piece;

import itawi.chessgame.core.enums.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KnightTest {

    private Map<String, Piece> board;

    @BeforeEach
    void setUp() {
        board = new HashMap<>();
    }

    @Test
    void testKnightMovesFromCenter() {
        // Knight in the middle of the board
        Knight knight = new Knight("white", "d4");
        board.put("d4", knight);

        List<String> possibleMoves = knight.getPossibleMoves(board);

        // Knight should be able to move in L-shape (8 possible moves from center)
        assertTrue(possibleMoves.contains("c6")); // Up-left
        assertTrue(possibleMoves.contains("e6")); // Up-right
        assertTrue(possibleMoves.contains("f5")); // Right-up
        assertTrue(possibleMoves.contains("f3")); // Right-down
        assertTrue(possibleMoves.contains("e2")); // Down-right
        assertTrue(possibleMoves.contains("c2")); // Down-left
        assertTrue(possibleMoves.contains("b3")); // Left-down
        assertTrue(possibleMoves.contains("b5")); // Left-up

        assertEquals(8, possibleMoves.size());
    }

    @Test
    void testKnightMovesFromCorner() {
        // Knight in the corner of the board
        Knight knight = new Knight("white", "a1");
        board.put("a1", knight);

        List<String> possibleMoves = knight.getPossibleMoves(board);

        // Knight should have limited moves from the corner
        assertTrue(possibleMoves.contains("b3")); // Up-right
        assertTrue(possibleMoves.contains("c2")); // Right-up

        assertEquals(2, possibleMoves.size());
    }

    @Test
    void testKnightJumpsOverPieces() {
        // Knight surrounded by pieces
        Knight knight = new Knight("white", "d4");
        board.put("d4", knight);

        // Surround knight with other pieces
        board.put("d3", new Pawn("white", "d3"));
        board.put("d5", new Pawn("white", "d5"));
        board.put("c4", new Pawn("white", "c4"));
        board.put("e4", new Pawn("white", "e4"));

        List<String> possibleMoves = knight.getPossibleMoves(board);

        // Knight should still have all its L-moves (jumps over pieces)
        assertEquals(8, possibleMoves.size());
    }

    @Test
    void testKnightCapture() {
        // Knight with opponent pieces
        Knight knight = new Knight("white", "d4");
        board.put("d4", knight);

        // Place opponent pieces at some destinations
        board.put("c6", new Pawn("black", "c6"));
        board.put("f5", new Pawn("black", "f5"));

        List<String> possibleMoves = knight.getPossibleMoves(board);

        // Knight should be able to move to and capture opponent pieces
        assertTrue(possibleMoves.contains("c6"));
        assertTrue(possibleMoves.contains("f5"));
        assertEquals(8, possibleMoves.size());
    }

    @Test
    void testKnightBlockedByAllies() {
        // Knight with ally pieces at destinations
        Knight knight = new Knight("white", "d4");
        board.put("d4", knight);

        // Place ally pieces at some destinations
        board.put("c6", new Pawn("white", "c6"));
        board.put("f5", new Pawn("white", "f5"));

        List<String> possibleMoves = knight.getPossibleMoves(board);

        // Knight should not be able to move to squares occupied by allies
        assertFalse(possibleMoves.contains("c6"));
        assertFalse(possibleMoves.contains("f5"));
        assertEquals(6, possibleMoves.size());
    }

    @Test
    void testKnightType() {
        Knight knight = new Knight("white", "b1");
        assertEquals(PieceType.KNIGHT, knight.getType());
    }
}
