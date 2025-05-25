package itawi.chessgame.core.timer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChessTimerTest {

    private ChessTimer chessTimer;
    private final int TEST_TIME_MINUTES = 5;
    private final long TEST_TIME_MILLIS = TEST_TIME_MINUTES * 60 * 1000L;

    @BeforeEach
    public void setUp() {
        chessTimer = new ChessTimer(TEST_TIME_MINUTES);
    }

    @Test
    public void testInitialization() {
        assertEquals(TEST_TIME_MILLIS, chessTimer.getWhiteTimeMillis());
        assertEquals(TEST_TIME_MILLIS, chessTimer.getBlackTimeMillis());
        assertEquals("white", chessTimer.getActiveTimer());
        assertFalse(chessTimer.isTimerRunning());
    }

    @Test
    public void testStartTimer() {
        chessTimer.startTimer();
        assertTrue(chessTimer.isTimerRunning());

        // Check that the timestamp was set
        assertTrue(chessTimer.getWhiteLastMoveTimestamp() > 0);
        assertEquals(0, chessTimer.getBlackLastMoveTimestamp()); // Black hasn't moved yet
    }
    
    @Test
    public void testStopTimer() {
        chessTimer.startTimer();
        assertTrue(chessTimer.isTimerRunning());

        chessTimer.stopTimer();
        assertFalse(chessTimer.isTimerRunning());
    }

    @Test
    public void testSwitchTurn() throws InterruptedException {
        // Start the timer
        chessTimer.startTimer();

        // Let some time pass
        Thread.sleep(100);

        // Switch from white to black
        chessTimer.switchTurn();

        // Check that the timer switched to black
        assertEquals("black", chessTimer.getActiveTimer());
        assertTrue(chessTimer.getWhiteTimeMillis() < TEST_TIME_MILLIS); // White's time decreased
        assertEquals(TEST_TIME_MILLIS, chessTimer.getBlackTimeMillis()); // Black's time unchanged
        assertTrue(chessTimer.getBlackLastMoveTimestamp() > 0); // Black's timestamp was set

        // Let some more time pass
        Thread.sleep(100);

        // Switch back to white
        chessTimer.switchTurn();

        // Check that the timer switched to white
        assertEquals("white", chessTimer.getActiveTimer());
        assertTrue(chessTimer.getBlackTimeMillis() < TEST_TIME_MILLIS); // Black's time decreased
        assertTrue(chessTimer.getWhiteLastMoveTimestamp() > chessTimer.getBlackLastMoveTimestamp()); // White's timestamp was updated
    }

    @Test
    public void testIsTimeout() throws InterruptedException {
        // Set very small time for testing timeout
        chessTimer = new ChessTimer(0); // 0 minutes = only seconds to test
        chessTimer.setWhiteTimeMillis(100); // 100 milliseconds
        chessTimer.setBlackTimeMillis(5000); // 5 seconds

        // Start the timer
        chessTimer.startTimer();

        // Let white's time run out
        Thread.sleep(150);

        // Check if white timed out
        assertTrue(chessTimer.isTimeout());

        // Switch to black
        chessTimer.switchTurn();

        // Black should not be timed out yet
        assertFalse(chessTimer.isTimeout());
    }

    @Test
    public void testGetFormattedTime() {
        // Set specific times to test formatting
        chessTimer.setWhiteTimeMillis(62000); // 1 minute 2 seconds
        chessTimer.setBlackTimeMillis(3599000); // 59 minutes 59 seconds

        assertEquals("01:02", chessTimer.getFormattedTime("white"));
        assertEquals("59:59", chessTimer.getFormattedTime("black"));
    }

    @Test
    public void testUpdateCurrentPlayerTime() throws InterruptedException {
        chessTimer.startTimer();

        // Initial time
        long initialWhiteTime = chessTimer.getWhiteTimeMillis();

        // Let some time pass
        Thread.sleep(500);

        // Force an update by getting the formatted time
        chessTimer.getFormattedTime("white");

        // Check that white's time decreased
        assertTrue(chessTimer.getWhiteTimeMillis() < initialWhiteTime);

        // Switch to black and check that black's time starts decreasing
        chessTimer.switchTurn();
        long initialBlackTime = chessTimer.getBlackTimeMillis();

        Thread.sleep(500);

        // Force an update
        chessTimer.getFormattedTime("black");

        // Check that black's time decreased
        assertTrue(chessTimer.getBlackTimeMillis() < initialBlackTime);
    }

    @Test
    public void testTimerDoesNotDecrementWhenStopped() throws InterruptedException {
        chessTimer.startTimer();

        // Initial time
        long initialWhiteTime = chessTimer.getWhiteTimeMillis();

        // Stop the timer
        chessTimer.stopTimer();

        // Let some time pass
        Thread.sleep(500);

        // Force an update
        chessTimer.getFormattedTime("white");

        // Check that white's time did not change
        assertEquals(initialWhiteTime, chessTimer.getWhiteTimeMillis());
    }
}
