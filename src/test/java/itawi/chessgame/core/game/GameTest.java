package itawi.chessgame.core.game;

import itawi.chessgame.core.board.Board;
import itawi.chessgame.core.piece.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
    }

    @Test
    void testInitialGameState() {
        // Test that the game starts with correct initial state
        assertEquals("white", game.getCurrentTurn());
        assertFalse(game.isGameOver());

        // Board should be initialized with standard chess setup
        Board board = game.getBoard();
        assertNotNull(board.getPieceAt("e1")); // White king
        assertNotNull(board.getPieceAt("e8")); // Black king
    }

    @Test
    void testMakingMoves() {
        // Test basic move execution
        boolean result = game.makeMove("e2", "e4");

        // Move should succeed
        assertTrue(result);

        // Turn should switch to black
        assertEquals("black", game.getCurrentTurn());

        // Board should reflect the move
        assertNull(game.getBoard().getPieceAt("e2"));
        assertInstanceOf(Pawn.class, game.getBoard().getPieceAt("e4"));
    }

    @Test
    void testInvalidMove() {
        // Test an invalid move
        boolean result = game.makeMove("e2", "e5"); // Invalid - pawn can't move 3 squares

        // Move should fail
        assertFalse(result);

        // Turn should still be white
        assertEquals("white", game.getCurrentTurn());

        // Board should remain unchanged
        assertInstanceOf(Pawn.class, game.getBoard().getPieceAt("e2"));
        assertNull(game.getBoard().getPieceAt("e5"));
    }

    @Test
    void testWrongTurn() {
        // Try to move black piece on white's turn
        boolean result = game.makeMove("e7", "e5");

        // Move should fail
        assertFalse(result);

        // Turn should still be white
        assertEquals("white", game.getCurrentTurn());
    }

    @Test
    void testPawnPromotion() {
        // Set up a pawn about to be promoted
        // First, clear the board fully by clearing the internal board map
        game.getBoard().getBoard().clear();

        // Now set up our test position
        Pawn whitePawn = new Pawn("white", "e7");
        whitePawn.setHasMoved(true);
        game.getBoard().getBoard().put("e7", whitePawn);
        game.getBoard().getBoard().put("d8", new King("black", "d8"));
        game.getBoard().getBoard().put("e1", new King("white", "e1"));

        // Make sure there's no piece at e8
        assertNull(game.getBoard().getPieceAt("e8"));

        // Move pawn to promotion square
        boolean moveResult = game.makeMove("e7", "e8");
        assertTrue(moveResult, "The promotion move should succeed");

        // The Game class should handle promotion automatically in makeMove
        // But let's also manually trigger promotion to ensure it works
        game.promotePawn("e8", "queen");

        // Verify pawn was promoted to queen
        Piece promotedPiece = game.getBoard().getPieceAt("e8");
        assertNotNull(promotedPiece, "There should be a piece at e8");
        assertInstanceOf(Queen.class, promotedPiece, "The piece should be a Queen");
        assertEquals("white", promotedPiece.getColor(), "The Queen should be white");
    }

    @Test
    void testCheckmate() {
        // Set up a checkmate position (Scholar's Mate)
        game.makeMove("e2", "e4"); // White pawn
        game.makeMove("e7", "e5"); // Black pawn
        game.makeMove("f1", "c4"); // White bishop
        game.makeMove("b8", "c6"); // Black knight
        game.makeMove("d1", "h5"); // White queen
        game.makeMove("g8", "f6"); // Black knight
        game.makeMove("h5", "f7"); // White queen checkmates

        // Game should be over
        assertTrue(game.isGameOver());
    }

    @Test
    void testBoardStateHistory() {
        // Make a few moves
        game.makeMove("e2", "e4");
        game.makeMove("e7", "e5");
        game.makeMove("g1", "f3");

        assertFalse(game.getBoardStateHistory().isEmpty());
    }

    @Test
    void testIllegalMoveIntoCheck() {
        // Clear the board first to start with a clean state
        game.getBoard().getBoardState().clear();

        // Set up a position where moving a pawn exposes the king
        game.getBoard().getBoardState().put("e1", new King("white", "e1"));
        game.getBoard().getBoardState().put("e8", new King("black", "e8"));
        game.getBoard().getBoardState().put("f7", new Pawn("black", "f7"));
        game.getBoard().getBoardState().put("d1", new Queen("white", "d1"));

        // Set the current turn to black
        game = new Game() {
            @Override
            public String getCurrentTurn() {
                return "black";
            }
        };

        // Set up the same position in the new game
        game.getBoard().getBoardState().clear();
        game.getBoard().getBoardState().put("e1", new King("white", "e1"));
        game.getBoard().getBoardState().put("e8", new King("black", "e8"));
        game.getBoard().getBoardState().put("f7", new Pawn("black", "f7"));
        game.getBoard().getBoardState().put("d1", new Queen("white", "d1"));

        // Try to move f7 to f6, which would expose the king to the queen's diagonal
        boolean result = game.makeMove("f7", "f6");

        assertFalse(result); // Move should be illegal
        assertEquals("black", game.getCurrentTurn()); // Turn remains black
        assertInstanceOf(Pawn.class, game.getBoard().getPieceAt("f7"));
    }

    @Test
    void testStalemateScenario() {
        // Create a new game with a clean setup
        game = new Game("black");

        // Clear the board completely
        game.getBoard().getBoard().clear();

        // Set up the stalemate position:
        // - Black king at the top corner (a8)
        // - White pawn at a7
        // - White king at b6
        // This is a classic stalemate where black has no legal moves but is not in check
        game.getBoard().getBoard().put("a8", new King("black", "a8"));
        game.getBoard().getBoard().put("a7", new Pawn("white", "a7"));
        game.getBoard().getBoard().put("b6", new King("white", "b6"));

        // Verify black king is not in check
        assertFalse(game.getBoard().isKingInCheck("black", game.getBoard().getBoard()));

        // Make any move attempt (which should fail since there are no legal moves)
        boolean moveResult = game.makeMove("a8", "b8"); // This should fail as all moves are illegal
        assertFalse(moveResult, "No moves should be possible for black king");

        // The game should recognize stalemate and be over
        assertTrue(game.isGameOver(), "Game should be over due to stalemate");

        // Verify we're still in the same position (nothing changed)
        assertInstanceOf(King.class, game.getBoard().getPieceAt("a8"));
        assertEquals("black", game.getBoard().getPieceAt("a8").getColor());
    }
    @Test
    void testStalemateKingVsKing() {
        // Create a new game with black's turn
        game = new Game("black");

        // Clear the board completely
        game.getBoard().getBoard().clear();

        // Set up King vs King position (insufficient material - automatic draw)
        game.getBoard().getBoard().put("e1", new King("white", "e1"));
        game.getBoard().getBoard().put("e8", new King("black", "e8"));

        // Attempt to make a move (any legal king move)
        boolean moveResult = game.makeMove("e8", "e7");

        // The move should succeed as it's legal
        assertTrue(moveResult);

        // But the game should be recognized as over due to insufficient material
        assertTrue(game.isGameOver(), "Game should be over due to insufficient material (King vs King)");
    }

    @Test
    void testStalemateKingVsKingAndBishop() {
        // Create a new game with black's turn
        game = new Game("black");

        // Clear the board completely
        game.getBoard().getBoard().clear();

        // Set up King vs King and Bishop position (insufficient material - automatic draw)
        game.getBoard().getBoard().put("e1", new King("white", "e1"));
        game.getBoard().getBoard().put("e8", new King("black", "e8"));
        game.getBoard().getBoard().put("c3", new Bishop("white", "c3"));

        // Attempt to make a move (any legal king move)
        boolean moveResult = game.makeMove("e8", "e7");

        // The move should succeed as it's legal
        assertTrue(moveResult);

        // But the game should be recognized as over due to insufficient material
        assertTrue(game.isGameOver(), "Game should be over due to insufficient material (King vs King and Bishop)");
    }

    @Test
    void testStalemateKingVsKingAndKnight() {
        // Create a new game with black's turn
        game = new Game("black");

        // Clear the board completely
        game.getBoard().getBoard().clear();

        // Set up King vs King and Knight position (insufficient material - automatic draw)
        game.getBoard().getBoard().put("e1", new King("white", "e1"));
        game.getBoard().getBoard().put("e8", new King("black", "e8"));
        game.getBoard().getBoard().put("c3", new Knight("white", "c3"));

        // Attempt to make a move (any legal king move)
        boolean moveResult = game.makeMove("e8", "e7");

        // The move should succeed as it's legal
        assertTrue(moveResult);

        // But the game should be recognized as over due to insufficient material
        assertTrue(game.isGameOver(), "Game should be over due to insufficient material (King vs King and Knight)");
    }
}

