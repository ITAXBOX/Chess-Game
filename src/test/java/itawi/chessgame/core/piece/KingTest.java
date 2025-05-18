package itawi.chessgame.core.piece;

import itawi.chessgame.core.enums.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KingTest {

    private Map<String, Piece> board;

    @BeforeEach
    void setUp() {
        board = new HashMap<>();
    }

    @Test
    void testKingBasicMoves() {
        // King in the middle of the board
        King king = new King("white", "d4");
        board.put("d4", king);

        List<String> possibleMoves = king.getPossibleMoves(board);

        // King should be able to move one square in all eight directions
        assertTrue(possibleMoves.contains("c3")); // Down-left
        assertTrue(possibleMoves.contains("c4")); // Left
        assertTrue(possibleMoves.contains("c5")); // Up-left
        assertTrue(possibleMoves.contains("d3")); // Down
        assertTrue(possibleMoves.contains("d5")); // Up
        assertTrue(possibleMoves.contains("e3")); // Down-right
        assertTrue(possibleMoves.contains("e4")); // Right
        assertTrue(possibleMoves.contains("e5")); // Up-right

        assertEquals(8, possibleMoves.size());
    }

    @Test
    void testKingMovesFromCorner() {
        // King in the corner of the board
        King king = new King("white", "a1");
        board.put("a1", king);

        List<String> possibleMoves = king.getPossibleMoves(board);

        // King should have limited moves from the corner
        assertTrue(possibleMoves.contains("a2")); // Up
        assertTrue(possibleMoves.contains("b1")); // Right
        assertTrue(possibleMoves.contains("b2")); // Up-right

        assertEquals(3, possibleMoves.size());
    }

    @Test
    void testKingCantMoveToAllyOccupiedSquares() {
        // King with all pieces nearby
        King king = new King("white", "d4");
        board.put("d4", king);

        // Place all pieces
        board.put("c3", new Pawn("white", "c3"));
        board.put("d5", new Pawn("white", "d5"));
        board.put("e4", new Pawn("white", "e4"));

        List<String> possibleMoves = king.getPossibleMoves(board);

        // King shouldn't be able to move to squares occupied by allies
        assertFalse(possibleMoves.contains("c3"));
        assertFalse(possibleMoves.contains("d5"));
        assertFalse(possibleMoves.contains("e4"));

        // Should still be able to move to empty squares
        assertTrue(possibleMoves.contains("c4"));
        assertTrue(possibleMoves.contains("c5"));
        assertTrue(possibleMoves.contains("d3"));
        assertTrue(possibleMoves.contains("e3"));
        assertTrue(possibleMoves.contains("e5"));

        assertEquals(5, possibleMoves.size());
    }

    @Test
    void testKingCanCaptureEnemyPieces() {
        // King with enemy pieces nearby
        King king = new King("white", "d4");
        board.put("d4", king);

        // Place enemy pieces
        board.put("c3", new Pawn("black", "c3"));
        board.put("d5", new Pawn("black", "d5"));
        board.put("e4", new Pawn("black", "e4"));

        List<String> possibleMoves = king.getPossibleMoves(board);

        // King should be able to capture enemy pieces
        assertTrue(possibleMoves.contains("c3"));
        assertTrue(possibleMoves.contains("d5"));
        assertTrue(possibleMoves.contains("e4"));

        assertEquals(8, possibleMoves.size());
    }

    @Test
    void testKingCastling() {
        // Set up initial positions for castling
        King king = new King("white", "e1");
        Rook rookKingSide = new Rook("white", "h1");
        Rook rookQueenSide = new Rook("white", "a1");

        board.put("e1", king);
        board.put("h1", rookKingSide);
        board.put("a1", rookQueenSide);

        List<String> possibleMoves = king.getPossibleMoves(board);

        // King should be able to castle both kingside and queenside
        assertTrue(possibleMoves.contains("g1")); // Kingside castling
        assertTrue(possibleMoves.contains("c1")); // Queenside castling

        // Mark king as moved - castling should no longer be possible
        king.setHasMoved(true);
        possibleMoves = king.getPossibleMoves(board);

        assertFalse(possibleMoves.contains("g1"));
        assertFalse(possibleMoves.contains("c1"));
    }

    @Test
    void testKingCastlingBlockedByPieces() {
        // Set up positions for castling with blocking pieces
        King king = new King("white", "e1");
        Rook rookKingSide = new Rook("white", "h1");
        Rook rookQueenSide = new Rook("white", "a1");

        board.put("e1", king);
        board.put("h1", rookKingSide);
        board.put("a1", rookQueenSide);

        // Add blocking pieces
        board.put("f1", new Bishop("white", "f1")); // Blocks kingside castling
        board.put("b1", new Knight("white", "b1")); // Blocks queenside castling

        List<String> possibleMoves = king.getPossibleMoves(board);

        // Castling should not be possible with pieces in the way
        assertFalse(possibleMoves.contains("g1"));
        assertFalse(possibleMoves.contains("c1"));
    }

    @Test
    void testKingHasMoved() {
        // Test that the hasMoved property works correctly
        King king = new King("white", "e1");
        assertFalse(king.getHasMoved());

        king.setHasMoved(true);
        assertTrue(king.getHasMoved());
    }

    @Test
    void testKingType() {
        King king = new King("white", "e1");
        assertEquals(PieceType.KING, king.getType());
    }
}
