// Chess Game JavaScript

document.addEventListener('DOMContentLoaded', () => {
    // Game state variables
    let selectedSquare = null;
    let validMoves = [];
    let currentTurn = 'white';
    let gameOver = false;
    let promotionPosition = null;

    // DOM elements
    const chessboard = document.getElementById('chessboard');
    const statusDisplay = document.getElementById('status');
    const newGameBtn = document.getElementById('new-game-btn');
    const gameResultDisplay = document.getElementById('game-result');
    const promotionModal = document.getElementById('promotion-modal');

    // Initialize promotion pieces images
    function initPromotionImages() {
        const color = currentTurn;
        const queenImg = document.getElementById('promotion-queen');
        const rookImg = document.getElementById('promotion-rook');
        const bishopImg = document.getElementById('promotion-bishop');
        const knightImg = document.getElementById('promotion-knight');

        if (queenImg && rookImg && bishopImg && knightImg) {
            queenImg.src = `/images/${color[0]}-queen.png`;
            rookImg.src = `/images/${color[0]}-rook.png`;
            bishopImg.src = `/images/${color[0]}-bishop.png`;
            knightImg.src = `/images/${color[0]}-knight.png`;
        }
    }

    // Create the chessboard UI
    function createBoard() {
        if (!chessboard) {
            console.error('Chessboard element not found');
            return;
        }

        chessboard.innerHTML = '';

        // Letters for columns and numbers for rows
        const cols = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'];
        const rows = [8, 7, 6, 5, 4, 3, 2, 1];

        // Create board squares without any coordinate labels inside
        for (let row = 0; row < 8; row++) {
            for (let col = 0; col < 8; col++) {
                const square = document.createElement('div');
                const position = cols[col] + rows[row];

                square.className = `square ${(row + col) % 2 === 0 ? 'white' : 'black'}`;
                square.dataset.position = position;

                square.addEventListener('click', () => handleSquareClick(position));
                chessboard.appendChild(square);
            }
        }

        // No need to create coordinate labels here as they're already in the HTML
    }

    // Update board with current pieces
    function updateBoard(pieces) {
        if (!Array.isArray(pieces)) {
            console.error('Expected pieces to be an array');
            return;
        }

        // Clear pieces
        document.querySelectorAll('.piece').forEach(piece => {
            if (piece.parentNode) {
                piece.parentNode.removeChild(piece);
            }
        });

        // Place pieces on the board
        pieces.forEach(piece => {
            if (!piece || !piece.position || !piece.color || !piece.type) {
                console.warn('Invalid piece data', piece);
                return;
            }

            const square = document.querySelector(`.square[data-position="${piece.position}"]`);
            if (square) {
                const pieceElement = document.createElement('img');
                pieceElement.className = 'piece';
                pieceElement.src = `/${piece.color[0]}-${piece.type.toLowerCase()}.png`;
                pieceElement.alt = `${piece.color} ${piece.type}`;
                pieceElement.draggable = false; // Disable dragging for simplicity

                // Add error handling for image loading
                pieceElement.onerror = () => {
                    console.warn(`Failed to load image for ${piece.color} ${piece.type}`);
                    pieceElement.src = '/placeholder-piece.png'; // Fallback image
                };

                square.appendChild(pieceElement);
            }
        });
    }

    // Highlight valid moves
    function highlightSquares(validMovesList = []) {
        if (!Array.isArray(validMovesList)) {
            console.warn('Expected validMovesList to be an array');
            validMovesList = [];
        }

        // Clear previous highlights
        document.querySelectorAll('.square').forEach(square => {
            square.classList.remove('selected', 'valid-move', 'check');
        });

        // Highlight selected square
        if (selectedSquare) {
            const squareElement = document.querySelector(`.square[data-position="${selectedSquare}"]`);
            if (squareElement) {
                squareElement.classList.add('selected');
            }
        }

        // Highlight valid moves
        validMovesList.forEach(position => {
            if (typeof position !== 'string') {
                console.warn('Invalid position', position);
                return;
            }

            const square = document.querySelector(`.square[data-position="${position}"]`);
            if (square) {
                square.classList.add('valid-move');
            }
        });
    }

    // Update game status display
    function updateGameStatus(status) {
        if (!status || !statusDisplay) return;

        currentTurn = status.currentTurn || 'white';
        gameOver = Boolean(status.isGameOver);

        // Update turn display
        statusDisplay.textContent = `Current Turn: ${currentTurn.charAt(0).toUpperCase() + currentTurn.slice(1)}`;

        // Show check status
        if (status.inCheck) {
            statusDisplay.textContent += " (In Check)";

            // Highlight king in check
            const kingPiece = document.querySelector(`.piece[alt="${currentTurn} KING"]`);
            if (kingPiece && kingPiece.parentElement) {
                kingPiece.parentElement.classList.add('check');
            }
        }

        // Show game result if game is over
        if (gameOver && gameResultDisplay) {
            gameResultDisplay.textContent = status.result || "Game Over";
            gameResultDisplay.classList.remove('hidden');
        } else if (gameResultDisplay) {
            gameResultDisplay.classList.add('hidden');
        }
    }

    // Handle square click
    async function handleSquareClick(position) {
        if (gameOver || !position) return;

        try {
            const response = await fetch('/api/v1/chess/square-click', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    position,
                    selectedPosition: selectedSquare
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();

            // Update selected square and valid moves
            selectedSquare = data.selectedPosition || null;
            validMoves = Array.isArray(data.validMoves) ? data.validMoves : [];

            // Highlight valid moves
            highlightSquares(validMoves);

            // If move was successful, update the board
            if (data.moveSuccess) {
                updateBoard(data.newBoardState || []);
                updateGameStatus(data.gameStatus || {});

                // Add the move to history
                if (selectedSquare && position) {
                    const moveNotation = `${selectedSquare} → ${position}`;
                    // Use the addMoveToHistory function from index.html
                    if (typeof addMoveToHistory === 'function') {
                        addMoveToHistory(moveNotation);
                    } else {
                        console.warn('addMoveToHistory function not found');
                    }

                    // Check if a piece was captured (by comparing old and new board states)
                    if (data.capturedPiece) {
                        if (typeof addCapturedPiece === 'function') {
                            addCapturedPiece(data.capturedPiece.type, data.capturedPiece.color);
                        } else {
                            console.warn('addCapturedPiece function not found');
                        }
                    }
                }

                // Check for pawn promotion
                if (data.pawnPromotion && promotionModal) {
                    promotionPosition = data.promotionPosition || null;
                    initPromotionImages();
                    promotionModal.style.display = 'flex';
                }
            }
        } catch (error) {
            console.error('Error handling square click:', error);
            if (statusDisplay) {
                statusDisplay.textContent = 'An error occurred. Please try again.';
            }
        }
    }

    // Handle pawn promotion
    async function promotePawn(pieceType) {
        if (!promotionPosition || !pieceType || !promotionModal) return;

        try {
            const response = await fetch('/api/v1/chess/promote', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    position: promotionPosition,
                    pieceType
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();

            // Update board after promotion
            updateBoard(data.board || []);
            updateGameStatus(data.gameStatus || {});

            // Close promotion modal
            promotionModal.style.display = 'none';
            promotionPosition = null;
        } catch (error) {
            console.error('Error promoting pawn:', error);
            if (statusDisplay) {
                statusDisplay.textContent = 'Failed to promote pawn. Please try again.';
            }
            if (promotionModal) {
                promotionModal.style.display = 'none';
            }
        }
    }

    // Start a new game
    async function startNewGame() {
        try {
            console.log("Starting new game...");
            const newGameResponse = await fetch('/api/v1/chess/new-game', {
                method: 'POST'
            });

            if (!newGameResponse.ok) {
                console.error(`Failed to start new game: ${newGameResponse.status} ${newGameResponse.statusText}`);
                throw new Error(`HTTP error! status: ${newGameResponse.status}`);
            }
            console.log("New game created successfully");

            console.log("Fetching board data...");
            const boardResponse = await fetch('/api/v1/chess/board');
            if (!boardResponse.ok) {
                console.error(`Failed to get board: ${boardResponse.status} ${boardResponse.statusText}`);
                throw new Error(`HTTP error! status: ${boardResponse.status}`);
            }
            const boardData = await boardResponse.json();
            console.log("Board data received:", boardData);

            console.log("Fetching game status...");
            const statusResponse = await fetch('/api/v1/chess/status');
            if (!statusResponse.ok) {
                console.error(`Failed to get status: ${statusResponse.status} ${statusResponse.statusText}`);
                throw new Error(`HTTP error! status: ${statusResponse.status}`);
            }
            const statusData = await statusResponse.json();
            console.log("Status data received:", statusData);

            if (Array.isArray(boardData)) {
                updateBoard(boardData);
            } else {
                console.error("Board data is not an array:", boardData);
            }

            if (statusData) {
                updateGameStatus(statusData);
            } else {
                console.error("Status data is invalid:", statusData);
            }

            // Reset game state
            selectedSquare = null;
            validMoves = [];
            gameOver = false;

            // Clear highlights
            highlightSquares([]);

            // Hide game result display
            gameResultDisplay.classList.add('hidden');

            // Clear any error messages
            statusDisplay.textContent = `Current Turn: ${currentTurn.charAt(0).toUpperCase() + currentTurn.slice(1)}`;
        } catch (error) {
            console.error('Error starting new game:', error);
            statusDisplay.textContent = 'Failed to start new game. Please refresh the page.';
        }
    }

    // Add keyboard accessibility
    function handleKeyboardEvent(event) {
        if (event.key === 'Escape') {
            // Clear selection on Escape key
            selectedSquare = null;
            highlightSquares([]);

            // Close promotion modal if open
            if (promotionModal && promotionModal.style.display === 'flex') {
                promotionModal.style.display = 'none';
            }
        }
    }

    // Initialize the game
    function initializeGame() {
        // Check if required elements exist
        if (!chessboard || !statusDisplay || !newGameBtn || !gameResultDisplay) {
            console.error('Required DOM elements not found');
            return;
        }

        // Attach event listeners
        newGameBtn.addEventListener('click', function(e) {
            e.preventDefault();
            console.log("New Game button clicked!");
            startNewGame();
        });
        document.addEventListener('keydown', handleKeyboardEvent);

        // Attach event listeners to promotion pieces
        const promotionPieces = document.querySelectorAll('.promotion-piece');
        if (promotionPieces.length > 0) {
            promotionPieces.forEach(piece => {
                piece.addEventListener('click', function() {
                    const pieceType = this.dataset.piece;
                    if (pieceType) {
                        promotePawn(pieceType);
                    }
                });
            });
        }

        createBoard();
        startNewGame();
    }

    initializeGame();
});

