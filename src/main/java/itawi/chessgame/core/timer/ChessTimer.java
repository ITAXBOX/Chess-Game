package itawi.chessgame.core.timer;

import lombok.Getter;
import lombok.Setter;

/**
 * Handles chess game timing functionality
 */
@Getter
@Setter
public class ChessTimer {
    private long whiteTimeMillis; // Remaining time for white player in milliseconds
    private long blackTimeMillis; // Remaining time for black player in milliseconds
    private long whiteLastMoveTimestamp; // When white last moved
    private long blackLastMoveTimestamp; // When black last moved
    private boolean timerRunning;
    private String activeTimer; // "white" or "black"

    /**
     * Create a new chess timer with the specified time for each player
     * @param initialTimeMinutes Time in minutes for each player
     */
    public ChessTimer(int initialTimeMinutes) {
        this.whiteTimeMillis = initialTimeMinutes * 60 * 1000L;
        this.blackTimeMillis = initialTimeMinutes * 60 * 1000L;
        this.timerRunning = false;
        this.activeTimer = "white"; // White starts first
    }

    /**
     * Start the timer
     */
    public void startTimer() {
        if (!timerRunning) {
            timerRunning = true;
            if (activeTimer.equals("white")) {
                whiteLastMoveTimestamp = System.currentTimeMillis();
            } else {
                blackLastMoveTimestamp = System.currentTimeMillis();
            }
        }
    }

    /**
     * Stop the timer
     */
    public void stopTimer() {
        timerRunning = false;
    }

    /**
     * Switch the active timer from one player to the other
     * Also calculates and updates the time used by the current player
     */
    public void switchTurn() {
        long currentTime = System.currentTimeMillis();

        if (timerRunning) {
            if (activeTimer.equals("white")) {
                // Calculate how much time white used
                long timeElapsed = currentTime - whiteLastMoveTimestamp;
                whiteTimeMillis -= timeElapsed;

                // Ensure time doesn't go negative
                if (whiteTimeMillis < 0) {
                    whiteTimeMillis = 0;
                }

                // Switch to black's timer
                blackLastMoveTimestamp = currentTime;
                activeTimer = "black";
            } else {
                // Calculate how much time black used
                long timeElapsed = currentTime - blackLastMoveTimestamp;
                blackTimeMillis -= timeElapsed;

                // Ensure time doesn't go negative
                if (blackTimeMillis < 0) {
                    blackTimeMillis = 0;
                }

                // Switch to white's timer
                whiteLastMoveTimestamp = currentTime;
                activeTimer = "white";
            }
        } else {
            // If timer wasn't running, just switch the active timer
            activeTimer = activeTimer.equals("white") ? "black" : "white";
        }
    }

    /**
     * Check if a player has run out of time
     * @return true if the active player has run out of time
     */
    public boolean isTimeout() {
        updateCurrentPlayerTime();

        if (activeTimer.equals("white") && whiteTimeMillis <= 0) {
            return true;
        } else return activeTimer.equals("black") && blackTimeMillis <= 0;
    }

    /**
     * Update the time for the current active player
     */
    private void updateCurrentPlayerTime() {
        if (!timerRunning) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        if (activeTimer.equals("white")) {
            // Calculate how much time has passed
            long timeElapsed = currentTime - whiteLastMoveTimestamp;
            long updatedTime = whiteTimeMillis - timeElapsed;

            // Only update if time has actually passed (prevent time jumping forward due to async calls)
            if (timeElapsed > 0) {
                whiteTimeMillis = Math.max(0, updatedTime);
                whiteLastMoveTimestamp = currentTime;
            }
        } else {
            // Calculate how much time has passed
            long timeElapsed = currentTime - blackLastMoveTimestamp;
            long updatedTime = blackTimeMillis - timeElapsed;

            // Only update if time has actually passed
            if (timeElapsed > 0) {
                blackTimeMillis = Math.max(0, updatedTime);
                blackLastMoveTimestamp = currentTime;
            }
        }
    }

    /**
     * Get the remaining time for the specified player formatted as MM:SS
     * @param color The player's color ("white" or "black")
     * @return Formatted time string
     */
    public String getFormattedTime(String color) {
        updateCurrentPlayerTime();

        long timeMillis = color.equals("white") ? whiteTimeMillis : blackTimeMillis;

        // Calculate minutes and seconds
        long minutes = timeMillis / (60 * 1000);
        long seconds = (timeMillis % (60 * 1000)) / 1000;

        // Format as MM:SS
        return String.format("%02d:%02d", minutes, seconds);
    }
}
