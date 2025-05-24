package itawi.chessgame.core.board;

import itawi.chessgame.core.piece.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    void testInitialBoardSetup() {
        // Test that the board is initialized with the correct pieces in the correct positions

        // Check pawns
        for (char file = 'a'; file <= 'h'; file++) {
            assertInstanceOf(Pawn.class, board.getPieceAt(file + "2"));
            assertEquals("white", board.getPieceAt(file + "2").getColor());

            assertInstanceOf(Pawn.class, board.getPieceAt(file + "7"));
            assertEquals("black", board.getPieceAt(file + "7").getColor());
        }

        // Check rooks
        assertInstanceOf(Rook.class, board.getPieceAt("a1"));
        assertInstanceOf(Rook.class, board.getPieceAt("h1"));
        assertInstanceOf(Rook.class, board.getPieceAt("a8"));
        assertInstanceOf(Rook.class, board.getPieceAt("h8"));

        // Check knights
        assertInstanceOf(Knight.class, board.getPieceAt("b1"));
        assertInstanceOf(Knight.class, board.getPieceAt("g1"));
        assertInstanceOf(Knight.class, board.getPieceAt("b8"));
        assertInstanceOf(Knight.class, board.getPieceAt("g8"));

        // Check bishops
        assertInstanceOf(Bishop.class, board.getPieceAt("c1"));
        assertInstanceOf(Bishop.class, board.getPieceAt("f1"));
        assertInstanceOf(Bishop.class, board.getPieceAt("c8"));
        assertInstanceOf(Bishop.class, board.getPieceAt("f8"));

        // Check queens
        assertInstanceOf(Queen.class, board.getPieceAt("d1"));
        assertInstanceOf(Queen.class, board.getPieceAt("d8"));

        // Check kings
        assertInstanceOf(King.class, board.getPieceAt("e1"));
        assertInstanceOf(King.class, board.getPieceAt("e8"));

        // Check empty squares in the middle
        for (char file = 'a'; file <= 'h'; file++) {
            for (int rank = 3; rank <= 6; rank++) {
                assertNull(board.getPieceAt(file + String.valueOf(rank)));
            }
        }
    }

    @Test
    void testBasicMove() {
        // Test a basic pawn move
        board.movePiece("e2", "e4", "white");

        // Verify the pawn moved
        assertNull(board.getPieceAt("e2"));
        assertInstanceOf(Pawn.class, board.getPieceAt("e4"));
        assertEquals("white", board.getPieceAt("e4").getColor());

        // Check that the move was tracked
        assertEquals("e2", board.getLastMoveFrom());
        assertEquals("e4", board.getLastMoveTo());
        assertFalse(board.wasCaptureMade());
    }

    @Test
    void testCapture() {
        // Set up a capture scenario
        board.movePiece("e2", "e4", "white");
        board.movePiece("d7", "d5", "black");

        // Capture black pawn with white pawn
        board.movePiece("e4", "d5", "white");

        // Verify the capture
        assertNull(board.getPieceAt("e4"));
        assertInstanceOf(Pawn.class, board.getPieceAt("d5"));
        assertEquals("white", board.getPieceAt("d5").getColor());

        // Check that the capture was tracked
        assertTrue(board.wasCaptureMade());
    }

    @Test
    void testInvalidMove() {
        // Try to move a piece to an invalid position
        boolean result = board.movePiece("e2", "e5", "white"); // Pawn can't move 3 squares

        // Move should fail
        assertFalse(result);

        // Board should remain unchanged
        assertInstanceOf(Pawn.class, board.getPieceAt("e2"));
        assertNull(board.getPieceAt("e5"));
    }

    @Test
    void testWrongTurn() {
        // Try to move a black piece on white's turn
        boolean result = board.movePiece("e7", "e5", "white");

        // Move should fail
        assertFalse(result);

        // Board should remain unchanged
        assertInstanceOf(Pawn.class, board.getPieceAt("e7"));
        assertNull(board.getPieceAt("e5"));
    }

    @Test
    void testKingInCheck() {
        // Set up a position where the king is in check
        board.getBoard().clear();

        // Place white king and black queen
        King whiteKing = new King("white", "e1");
        board.getBoard().put("e1", whiteKing);
        Queen blackQueen = new Queen("black", "e8");
        board.getBoard().put("e8", blackQueen);

        // Verify king is in check
        boolean isInCheck = board.isKingInCheck("white", board.getBoard());
        assertTrue(isInCheck);

        // Try to move king out of check
        boolean moveResult = board.movePiece("e1", "d2", "white");
        assertTrue(moveResult);

        // King should no longer be in check
        isInCheck = board.isKingInCheck("white", board.getBoard());
        assertFalse(isInCheck);
    }

    @Test
    void testCastlingKingside() {
        // Clear the way for castling
        board.getBoardState().remove("f1");
        board.getBoardState().remove("g1");

        // Directly access and modify the board map
        board.getBoard().remove("f1");
        board.getBoard().remove("g1");

        // Attempt castling
        boolean result = board.movePiece("e1", "g1", "white");

        // Castling should succeed
        assertTrue(result);

        // Verify king position
        assertInstanceOf(King.class, board.getPieceAt("g1"));
        assertNull(board.getPieceAt("e1"));

        // Verify rook position
        assertInstanceOf(Rook.class, board.getPieceAt("f1"));
        assertNull(board.getPieceAt("h1"));

        // Verify king and rook have moved
        King king = (King) board.getPieceAt("g1");
        Rook rook = (Rook) board.getPieceAt("f1");
        assertTrue(king.getHasMoved());
        assertTrue(rook.getHasMoved());
    }

    @Test
    void testCastlingQueenside() {
        // Clear the way for castling
        board.getBoardState().put("b1", null);
        board.getBoardState().put("c1", null);
        board.getBoardState().put("d1", null);

        // Directly access and modify the board map
        board.getBoard().remove("b1");
        board.getBoard().remove("c1");
        board.getBoard().remove("d1");

        // Attempt castling
        boolean result = board.movePiece("e1", "c1", "white");

        // Castling should succeed
        assertTrue(result);

        // Verify king position
        assertInstanceOf(King.class, board.getPieceAt("c1"));
        assertNull(board.getPieceAt("e1"));

        // Verify rook position
        assertInstanceOf(Rook.class, board.getPieceAt("d1"));
        assertNull(board.getPieceAt("a1"));

        // Verify king and rook have moved
        King king = (King) board.getPieceAt("c1");
        Rook rook = (Rook) board.getPieceAt("d1");
        assertTrue(king.getHasMoved());
        assertTrue(rook.getHasMoved());
    }

    @Test
    void testPawnPromotion() {
        // Clear the board to set up a test position
        board.getBoard().clear();

        // Set up a pawn about to be promoted
        Pawn whitePawn = new Pawn("white", "d7");
        whitePawn.setHasMoved(true);
        board.getBoard().put("d7", whitePawn);

        // We need to add kings to the board so the isKingInCheck validation passes
        board.getBoard().put("e1", new King("white", "e1"));
        board.getBoard().put("e8", new King("black", "e8"));

        // Move pawn to promotion square
        boolean result = board.movePiece("d7", "d8", "white");

        // Move should succeed
        assertTrue(result);

        // Verify pawn at promotion square
        Piece promotedPiece = board.getPieceAt("d8");
        assertNotNull(promotedPiece);
        assertInstanceOf(Pawn.class, promotedPiece);
        assertEquals("white", promotedPiece.getColor());

        // In an actual game, the Game class would handle the promotion
        // But we can manually replace the piece to test board behavior
        board.getBoard().put("d8", new Queen("white", "d8"));

        // Verify the replacement worked
        promotedPiece = board.getPieceAt("d8");
        assertInstanceOf(Queen.class, promotedPiece);
        assertEquals("white", promotedPiece.getColor());
    }
}
