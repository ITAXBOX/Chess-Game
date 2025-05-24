document.addEventListener("DOMContentLoaded", () => {
    // Game state variables
    let selectedSquare = null
    let validMoves = []
    let currentTurn = "white"
    let gameOver = false
    let promotionPosition = null

    // Function to add a move to the history
    function addMoveToHistory(notation) {
        const movesList = document.getElementById("moves-list")
        if (!movesList) {
            console.error("Error: moves-list element not found")
            return
        }
        const moveItem = document.createElement("div")
        moveItem.className = "move-item"
        moveItem.innerHTML = notation // Changed from textContent to innerHTML to support HTML
        movesList.appendChild(moveItem)
        movesList.scrollTop = movesList.scrollHeight
        console.log(`Move added to history: ${notation}`)
    }

    // Function to add a captured piece
    function addCapturedPiece(piece, color) {
        const containerId = `${color.toLowerCase()}-captured`
        const container = document.getElementById(containerId)
        if (!container) {
            console.error(`Error: ${containerId} element not found`)
            return
        }

        // Convert piece type to lowercase for consistency
        const pieceType = piece.toLowerCase()
        const pieceColor = color.toLowerCase()
        const pieceKey = `${pieceColor}-${pieceType}`

        // Check if this piece type already exists in captured pieces
        const existingPieceGroup = container.querySelector(`.piece-group[data-piece="${pieceKey}"]`)

        if (existingPieceGroup) {
            // Increment counter if piece already exists
            const counter = existingPieceGroup.querySelector(".piece-counter")
            if (counter) {
                const count = Number.parseInt(counter.textContent) + 1
                counter.textContent = count
                console.log(`Incremented captured ${pieceColor} ${pieceType} count to ${count}`)
            }
        } else {
            // Create new piece group with image and counter
            const pieceGroup = document.createElement("div")
            pieceGroup.className = "piece-group"
            pieceGroup.dataset.piece = pieceKey

            const pieceElement = document.createElement("img")
            pieceElement.src = `/${color[0]}-${pieceType}.png`
            pieceElement.alt = pieceType
            pieceElement.className = "captured-piece"

            const counterElement = document.createElement("span")
            counterElement.className = "piece-counter"
            counterElement.textContent = "1"

            pieceGroup.appendChild(pieceElement)
            pieceGroup.appendChild(counterElement)
            container.appendChild(pieceGroup)

            console.log(`Added new captured piece: ${pieceType} (${pieceColor})`)
        }
    }

    // Get Unicode symbol for chess pieces with proper color
    function getPieceSymbol(pieceType, color) {
        const isWhite = color && color.toLowerCase() === "white"

        switch (pieceType.toUpperCase()) {
            case "KING":
                return isWhite ? "♔" : "♚"
            case "QUEEN":
                return isWhite ? "♕" : "♛"
            case "ROOK":
                return isWhite ? "♖" : "♜"
            case "BISHOP":
                return isWhite ? "♗" : "♝"
            case "KNIGHT":
                return isWhite ? "♘" : "♞"
            case "PAWN":
                return isWhite ? "♙" : "♟"
            default:
                return ""
        }
    }

    // Create colored piece symbol for move history
    function createColoredPieceSymbol(pieceType, color) {
        const symbol = getPieceSymbol(pieceType, color)
        const colorClass = color && color.toLowerCase() === "white" ? "white-piece" : "black-piece"
        return `<span class="${colorClass}">${symbol}</span>`
    }

    // DOM elements
    const chessboard = document.getElementById("chessboard")
    const statusDisplay = document.getElementById("status")
    const newGameBtn = document.getElementById("new-game-btn")
    const gameResultDisplay = document.getElementById("game-result")
    const promotionModal = document.getElementById("promotion-modal")

    // Initialize promotion pieces images
    function initPromotionImages() {
        const color = currentTurn;
        const queenImg = document.getElementById("promotion-queen");
        const rookImg = document.getElementById("promotion-rook");
        const bishopImg = document.getElementById("promotion-bishop");
        const knightImg = document.getElementById("promotion-knight");

        console.log("Initializing promotion images for color:", color);

        if (queenImg && rookImg && bishopImg && knightImg) {
            // Use relative paths since images are in the same directory
            const prefix = color[0].toLowerCase() === "w" ? "b" : "w";

            // Set image sources
            queenImg.src = `/${prefix}-queen.png`;
            rookImg.src = `/${prefix}-rook.png`;
            bishopImg.src = `/${prefix}-bishop.png`;
            knightImg.src = `/${prefix}-knight.png`;

            // Add error handlers for debugging
            const handleImageError = (img, piece) => (e) => {
                console.error(`Failed to load ${piece} image:`, img.src);
                console.log("Current location:", window.location.href);
                console.log("Attempted to load from:", img.src);
            };

            queenImg.onerror = handleImageError(queenImg, 'queen');
            rookImg.onerror = handleImageError(rookImg, 'rook');
            bishopImg.onerror = handleImageError(bishopImg, 'bishop');
            knightImg.onerror = handleImageError(knightImg, 'knight');

            console.log("Promotion images initialized with sources:", {
                queen: queenImg.src,
                rook: rookImg.src,
                bishop: bishopImg.src,
                knight: knightImg.src
            });
        } else {
            console.error("Promotion image elements not found");
        }
    }

    // Create the chessboard UI
    function createBoard() {
        if (!chessboard) {
            console.error("Chessboard element not found")
            return
        }

        chessboard.innerHTML = ""

        // Letters for columns and numbers for rows
        const cols = ["a", "b", "c", "d", "e", "f", "g", "h"]
        const rows = [8, 7, 6, 5, 4, 3, 2, 1]

        // Create board squares without any coordinate labels inside
        for (let row = 0; row < 8; row++) {
            for (let col = 0; col < 8; col++) {
                const square = document.createElement("div")
                const position = cols[col] + rows[row]

                square.className = `square ${(row + col) % 2 === 0 ? "white" : "black"}`
                square.dataset.position = position

                square.addEventListener("click", () => handleSquareClick(position))
                chessboard.appendChild(square)
            }
        }
    }

    // Update board with current pieces
    function updateBoard(pieces) {
        if (!Array.isArray(pieces)) {
            console.error("Expected pieces to be an array")
            return
        }

        // Clear pieces
        document.querySelectorAll(".piece").forEach((piece) => {
            if (piece.parentNode) {
                piece.parentNode.removeChild(piece)
            }
        })

        // Place pieces on the board
        pieces.forEach((piece) => {
            if (!piece || !piece.position || !piece.color || !piece.type) {
                console.warn("Invalid piece data", piece)
                return
            }

            const square = document.querySelector(`.square[data-position="${piece.position}"]`)
            if (square) {
                const pieceElement = document.createElement("img")
                pieceElement.className = "piece"
                pieceElement.src = `/${piece.color[0]}-${piece.type.toLowerCase()}.png`
                pieceElement.alt = `${piece.color} ${piece.type}`
                pieceElement.draggable = false

                // Add error handling for image loading
                pieceElement.onerror = () => {
                    console.warn(`Failed to load image for ${piece.color} ${piece.type}`)
                    pieceElement.src = "/placeholder-piece.png"
                }

                square.appendChild(pieceElement)
            }
        })
    }

    // Highlight valid moves
    function highlightSquares(validMovesList = []) {
        if (!Array.isArray(validMovesList)) {
            console.warn("Expected validMovesList to be an array")
            validMovesList = []
        }

        // Clear previous highlights
        document.querySelectorAll(".square").forEach((square) => {
            square.classList.remove("selected", "valid-move", "check")
        })

        // Highlight selected square
        if (selectedSquare) {
            const squareElement = document.querySelector(`.square[data-position="${selectedSquare}"]`)
            if (squareElement) {
                squareElement.classList.add("selected")
            }
        }

        // Highlight valid moves
        validMovesList.forEach((position) => {
            if (typeof position !== "string") {
                console.warn("Invalid position", position)
                return
            }

            const square = document.querySelector(`.square[data-position="${position}"]`)
            if (square) {
                square.classList.add("valid-move")
            }
        })
    }

    // Update game status display
    function updateGameStatus(status) {
        if (!status || !statusDisplay) return

        currentTurn = status.currentTurn || "white"
        gameOver = Boolean(status.isGameOver)

        // Update turn display
        statusDisplay.textContent = `Current Turn: ${currentTurn.charAt(0).toUpperCase() + currentTurn.slice(1)}`

        // Show check status
        if (status.inCheck) {
            statusDisplay.textContent += " (In Check)"

            // Highlight king in check
            const kingPiece = document.querySelector(`.piece[alt="${currentTurn} KING"]`)
            if (kingPiece && kingPiece.parentElement) {
                kingPiece.parentElement.classList.add("check")
            }
        }

        // Show game result if game is over
        if (gameOver && gameResultDisplay) {
            gameResultDisplay.textContent = status.result || "Game Over"
            gameResultDisplay.classList.remove("hidden")
        } else if (gameResultDisplay) {
            gameResultDisplay.classList.add("hidden")
        }
    }

    // Handle square click
    async function handleSquareClick(position) {
        if (gameOver || !position) return

        try {
            const response = await fetch("/api/v1/chess/square-click", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    position,
                    selectedPosition: selectedSquare,
                }),
            })

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`)
            }

            const data = await response.json()

            // Update selected square and valid moves
            selectedSquare = data.selectedPosition || null
            validMoves = Array.isArray(data.validMoves) ? data.validMoves : []

            // Highlight valid moves
            highlightSquares(validMoves)

            // If move was successful, update the board
            if (data.moveSuccess) {
                let toPiece = null

                if (data.newBoardState) {
                    // Find the piece that moved to the destination
                    toPiece = data.newBoardState.find((piece) => piece.position === position)

                    // Update the board with the new state
                    updateBoard(data.newBoardState || [])
                    updateGameStatus(data.gameStatus || {})

                    // Create notation for the move history with colored piece symbols
                    let moveNotation = ""

                    if (toPiece) {
                        // Create colored piece symbol
                        const coloredPieceSymbol = createColoredPieceSymbol(toPiece.type, toPiece.color)

                        // Format as "♔ e2→e4" with colored piece symbol
                        moveNotation = `${coloredPieceSymbol} ${selectedSquare}→${position}`

                        // Add capture indication if a piece was captured
                        if (data.capturedPiece) {
                            const capturedSymbol = createColoredPieceSymbol(data.capturedPiece.type, data.capturedPiece.color)
                            moveNotation += ` ×${capturedSymbol}` // Add captured piece symbol
                        }

                        // Add check or checkmate symbol if applicable
                        if (data.gameStatus && data.gameStatus.inCheck) {
                            if (data.gameStatus.isGameOver) {
                                moveNotation += " #" // Checkmate symbol
                            } else {
                                moveNotation += " +" // Check symbol
                            }
                        }

                        console.log("Adding move to history:", moveNotation)
                        addMoveToHistory(moveNotation)

                        // Also dispatch an event so other components can react
                        document.dispatchEvent(
                            new CustomEvent("chess-move", {
                                detail: {
                                    from: selectedSquare,
                                    to: position,
                                    piece: toPiece.type,
                                    color: toPiece.color,
                                    notation: moveNotation,
                                    capturedPiece: data.capturedPiece ? data.capturedPiece.type : null,
                                    capturedColor: data.capturedPiece ? data.capturedPiece.color : null,
                                },
                            }),
                        )

                        // Check if a piece was captured
                        if (data.capturedPiece) {
                            addCapturedPiece(data.capturedPiece.type, data.capturedPiece.color)
                        }
                    }
                }

                // Check for pawn promotion
                if (data.pawnPromotion && promotionModal) {
                    promotionPosition = data.promotionPosition || null
                    initPromotionImages()
                    promotionModal.style.display = "flex"
                }
            }
        } catch (error) {
            console.error("Error handling square click:", error)
            if (statusDisplay) {
                statusDisplay.textContent = "An error occurred. Please try again."
            }
        }
    }

    // Handle pawn promotion
    async function promotePawn(pieceType) {
        if (!promotionPosition || !pieceType || !promotionModal) return

        try {
            const response = await fetch("/api/v1/chess/promote", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    position: promotionPosition,
                    pieceType,
                }),
            })

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`)
            }

            const data = await response.json()

            // Update board after promotion
            updateBoard(data.board || [])
            updateGameStatus(data.gameStatus || {})

            // Close promotion modal
            promotionModal.style.display = "none"
            promotionPosition = null
        } catch (error) {
            console.error("Error promoting pawn:", error)
            if (statusDisplay) {
                statusDisplay.textContent = "Failed to promote pawn. Please try again."
            }
            if (promotionModal) {
                promotionModal.style.display = "none"
            }
        }
    }

    // Start a new game
    async function startNewGame() {
        try {
            console.log("Starting new game...")
            const newGameResponse = await fetch("/api/v1/chess/new-game", {
                method: "POST",
            })

            if (!newGameResponse.ok) {
                console.error(`Failed to start new game: ${newGameResponse.status} ${newGameResponse.statusText}`)
                throw new Error(`HTTP error! status: ${newGameResponse.status}`)
            }
            console.log("New game created successfully")

            console.log("Fetching board data...")
            const boardResponse = await fetch("/api/v1/chess/board")
            if (!boardResponse.ok) {
                console.error(`Failed to get board: ${boardResponse.status} ${boardResponse.statusText}`)
                throw new Error(`HTTP error! status: ${boardResponse.status}`)
            }
            const boardData = await boardResponse.json()
            console.log("Board data received:", boardData)

            console.log("Fetching game status...")
            const statusResponse = await fetch("/api/v1/chess/status")
            if (!statusResponse.ok) {
                console.error(`Failed to get status: ${statusResponse.status} ${statusResponse.statusText}`)
                throw new Error(`HTTP error! status: ${statusResponse.status}`)
            }
            const statusData = await statusResponse.json()
            console.log("Status data received:", statusData)

            if (Array.isArray(boardData)) {
                updateBoard(boardData)
            } else {
                console.error("Board data is not an array:", boardData)
            }

            if (statusData) {
                updateGameStatus(statusData)
            } else {
                console.error("Status data is invalid:", statusData)
            }

            // Reset game state
            selectedSquare = null
            validMoves = []
            gameOver = false

            // Clear highlights
            highlightSquares([])

            // Hide game result display
            gameResultDisplay.classList.add("hidden")

            // Clear any error messages
            statusDisplay.textContent = `Current Turn: ${currentTurn.charAt(0).toUpperCase() + currentTurn.slice(1)}`
        } catch (error) {
            console.error("Error starting new game:", error)
            statusDisplay.textContent = "Failed to start new game. Please refresh the page."
        }
    }

    // Add keyboard accessibility
    function handleKeyboardEvent(event) {
        if (event.key === "Escape") {
            // Clear selection on Escape key
            selectedSquare = null
            highlightSquares([])

            // Close promotion modal if open
            if (promotionModal && promotionModal.style.display === "flex") {
                promotionModal.style.display = "none"
            }
        }
    }

    // Initialize the game
    function initializeGame() {
        // Check if required elements exist
        if (!chessboard || !statusDisplay || !newGameBtn || !gameResultDisplay) {
            console.error("Required DOM elements not found")
            return
        }

        // Attach event listeners
        newGameBtn.addEventListener("click", (e) => {
            e.preventDefault()
            console.log("New Game button clicked!")
            startNewGame()
        })
        document.addEventListener("keydown", handleKeyboardEvent)

        // Attach event listeners to promotion pieces
        const promotionPieces = document.querySelectorAll(".promotion-piece")
        if (promotionPieces.length > 0) {
            promotionPieces.forEach((piece) => {
                piece.addEventListener("click", function () {
                    const pieceType = this.dataset.piece
                    if (pieceType) {
                        promotePawn(pieceType)
                    }
                })
            })
        }

        createBoard()
        startNewGame()
    }

    initializeGame()
})
