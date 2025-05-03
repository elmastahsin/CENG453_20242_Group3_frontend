package metu.ceng.ceng453_20242_group3_frontend.features.game.model;

import metu.ceng.ceng453_20242_group3_frontend.config.AppConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Represents the state of an UNO game.
 */
public class Game {
    private final String gameId;
    private final GameMode gameMode;
    private final PlayerCount playerCount;
    private final List<Player> players;
    private final Deck drawPile;
    private Deck discardPile;
    private Direction direction;
    private int currentPlayerIndex;
    private boolean gameStarted;
    private boolean gameEnded;
    private Player winner;
    private final Random random;
    
    // Track the current color for wild cards
    private CardColor currentColor;

    /**
     * Constructor for creating a new game.
     *
     * @param gameMode    The game mode (singleplayer or multiplayer)
     * @param playerCount The number of players in the game
     */
    public Game(GameMode gameMode, PlayerCount playerCount) {
        this.gameId = UUID.randomUUID().toString();
        this.gameMode = gameMode;
        this.playerCount = playerCount;
        this.players = new ArrayList<>();
        this.drawPile = Deck.createStandardDeck();
        this.discardPile = new Deck();
        this.direction = Direction.CLOCKWISE;
        this.currentPlayerIndex = 0;
        this.gameStarted = false;
        this.gameEnded = false;
        this.winner = null;
        this.random = new Random();
        this.currentColor = null; // Will be set when first card is played
    }

    /**
     * Gets the unique ID of the game.
     *
     * @return The game ID
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * Gets the game mode.
     *
     * @return The game mode
     */
    public GameMode getGameMode() {
        return gameMode;
    }

    /**
     * Gets the number of players.
     *
     * @return The player count
     */
    public PlayerCount getPlayerCount() {
        return playerCount;
    }

    /**
     * Gets the list of players.
     *
     * @return The list of players
     */
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * Gets the draw pile.
     *
     * @return The draw pile
     */
    public Deck getDrawPile() {
        return drawPile;
    }

    /**
     * Gets the discard pile.
     *
     * @return The discard pile
     */
    public Deck getDiscardPile() {
        return discardPile;
    }

    /**
     * Checks if the discard pile is empty.
     *
     * @return true if the discard pile is empty, false otherwise
     */
    public boolean isDiscardPileEmpty() {
        return discardPile == null || discardPile.isEmpty();
    }

    /**
     * Gets the current direction of play.
     *
     * @return The direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Sets the direction of play.
     *
     * @param direction The new direction
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    /**
     * Gets the current game color (important for wild cards).
     *
     * @return The current game color
     */
    public CardColor getCurrentColor() {
        return currentColor;
    }

    /**
     * Sets the current game color (used after playing wild cards).
     *
     * @param color The new game color
     */
    public void setCurrentColor(CardColor color) {
        if (color != CardColor.MULTI) {
            this.currentColor = color;
            System.out.println("Game color changed to: " + color);
        }
    }

    /**
     * Gets the index of the current player.
     *
     * @return The current player index
     */
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    /**
     * Gets the current player.
     *
     * @return The current player
     */
    public Player getCurrentPlayer() {
        if (players.isEmpty()) {
            return null;
        }
        return players.get(currentPlayerIndex);
    }

    /**
     * Updates which cards in the current player's hand are playable
     * based on the top card of the discard pile.
     */
    public void updatePlayableCards() {
        if (!gameStarted || gameEnded || players.isEmpty()) {
            return;
        }

        Player currentPlayer = getCurrentPlayer();
        Card topCard = discardPile.peekCard();
        
        if (topCard == null) {
            // If no cards in discard pile yet, all cards are playable
            for (Card card : currentPlayer.getHand()) {
                card.setPlayable(true);
            }
            return;
        }
        
        System.out.println("Updating playable cards for " + currentPlayer.getName());
        System.out.println("Top card: " + topCard + ", Current color: " + currentColor);
        
        boolean hasMatchingColorCard = false;
        
        // First check if the player has any cards matching the current color
        // This is needed for Wild Draw Four validation
        for (Card card : currentPlayer.getHand()) {
            if (card.getColor() == currentColor && card.getColor() != CardColor.MULTI) {
                hasMatchingColorCard = true;
                break;
            }
        }
        
        // Then determine which cards are playable
        for (Card card : currentPlayer.getHand()) {
            boolean playable = false;
            
            if (card.getAction() == CardAction.WILD_DRAW_FOUR) {
                // Wild Draw Four can only be played if the player has no cards matching the current color
                playable = !hasMatchingColorCard;
            } else if (card.isWildCard()) {
                // Regular wild cards can always be played
                playable = true;
            } else if (card.getColor() == currentColor) {
                // Cards matching the current color can be played
                playable = true;
            } else if (topCard.isNumberCard() && card.isNumberCard() && topCard.getValue() == card.getValue()) {
                // Number cards matching the top card's value can be played
                playable = true;
            } else if (topCard.isActionCard() && card.isActionCard() && topCard.getAction() == card.getAction() 
                      && !card.isWildCard() && !topCard.isWildCard()) {
                // Action cards matching the top card's action can be played (except wilds)
                playable = true;
            }
            
            card.setPlayable(playable);
            
            // Debug output
            if (playable) {
                System.out.println("Playable card: " + card);
            }
        }
    }

    /**
     * Adds a player to the game.
     *
     * @param player The player to add
     * @return true if the player was added, false otherwise
     */
    public boolean addPlayer(Player player) {
        if (players.size() >= playerCount.getCount()) {
            return false;
        }
        return players.add(player);
    }

    /**
     * Checks if the game has started.
     *
     * @return true if the game has started, false otherwise
     */
    public boolean isGameStarted() {
        return gameStarted;
    }

    /**
     * Checks if the game has ended.
     *
     * @return true if the game has ended, false otherwise
     */
    public boolean isGameEnded() {
        return gameEnded;
    }

    /**
     * Gets the winner of the game.
     *
     * @return The winner, or null if there is no winner yet
     */
    public Player getWinner() {
        return winner;
    }

    /**
     * Starts the game.
     *
     * @return true if the game was started, false otherwise
     */
    public boolean startGame() {
        // Check if we have enough players
        if (players.size() < playerCount.getCount()) {
            return false;
        }

        // Check if the game has already started
        if (gameStarted) {
            return false;
        }

        // Deal cards to players
        dealInitialCards();

        // Select a random player to start
        currentPlayerIndex = random.nextInt(players.size());
        // TODO: Remove this
        currentPlayerIndex = 0;
        // Set the game as started
        gameStarted = true;

        return true;
    }

    /**
     * Places the first card from the draw pile onto the discard pile to start the game.
     * This should be called after the game has started and players are ready to play.
     * 
     * @return The card placed on the discard pile
     */
    public Card startFirstTurn() {
        // Move one card from draw pile to discard pile to start the game
        Card initialCard = drawPile.drawCard();
        // Keep drawing until we get a valid starting card (not a Wild+4 or action card)
        while (initialCard != null && (initialCard.getAction() == CardAction.WILD_DRAW_FOUR || 
                                      initialCard.isActionCard())) {
            // Put the card back in the deck and shuffle
            drawPile.addCard(initialCard);
            drawPile.shuffle();
            initialCard = drawPile.drawCard();
        }
        
        if (initialCard != null) {
            // Create a fresh discard pile to ensure proper ordering
            discardPile = new Deck();
            discardPile.addCard(initialCard);
            
            // Set the initial color based on the first card
            currentColor = initialCard.getColor();
            
            System.out.println("Initial card placed on discard pile: " + initialCard);
            System.out.println("Initial game color: " + currentColor);
        }
        
        return initialCard;
    }

    /**
     * Deals initial cards to all players.
     */
    private void dealInitialCards() {
        for (int i = 0; i < AppConfig.INITIAL_CARDS_PER_PLAYER; i++) {
            for (Player player : players) {
                Card card = drawPile.drawCard();
                if (card != null) {
                    player.addCard(card);
                }
            }
        }
    }

    /**
     * Moves to the next player in turn.
     *
     * @return The new current player
     */
    public Player nextPlayer() {
        if (direction == Direction.CLOCKWISE) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } else {
            currentPlayerIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
        }
        return getCurrentPlayer();
    }

    /**
     * Skips the next player's turn.
     *
     * @return The new current player
     */
    public Player skipPlayer() {
        // Skip once to move past the next player
        nextPlayer();
        // Skip again to move to the player after the skipped one
        return nextPlayer();
    }

    /**
     * Reverses the direction of play.
     */
    public void reverseDirection() {
        if (direction == Direction.CLOCKWISE) {
            direction = Direction.COUNTER_CLOCKWISE;
        } else {
            direction = Direction.CLOCKWISE;
        }
    }

    /**
     * Plays a card from the current player's hand.
     *
     * @param card The card to play
     * @return true if the card was played successfully, false otherwise
     */
    public boolean playCard(Card card) {
        Player currentPlayer = getCurrentPlayer();
        
        // Check if the game has started
        if (!gameStarted || gameEnded) {
            return false;
        }

        // Check if the player has the card
        if (!currentPlayer.getHand().contains(card)) {
            return false;
        }

        // Make sure the card is playable according to UNO rules
        if (!isCardPlayable(card)) {
            return false;
        }

        // Remove the card from player's hand
        if (!currentPlayer.removeCard(card)) {
            return false;
        }

        // Add the card to the discard pile
        discardPile.addCard(card);
        
        // If it's a wild card, the color should already be set by the UI
        // But for AI players, we select a random color if it hasn't been set yet
        if (card.isWildCard() && currentPlayer.isAI()) {
            // For AI players, choose a random color if not already set
            CardColor[] possibleColors = {
                CardColor.RED, CardColor.BLUE, CardColor.GREEN, CardColor.YELLOW
            };
            
            // If color hasn't been set yet, set it now
            if (this.currentColor == null || this.currentColor == CardColor.MULTI) {
                setCurrentColor(possibleColors[random.nextInt(possibleColors.length)]);
            }
        } else if (!card.isWildCard()) {
            // For regular cards, set the current color to the card's color
            setCurrentColor(card.getColor());
        }

        // Check if the player has won
        if (currentPlayer.getCardCount() == 0) {
            gameEnded = true;
            winner = currentPlayer;
            return true;
        }

        // Handle special cards
        handleSpecialCard(card);
        
        // Update which cards are playable for the new current player
        updatePlayableCards();

        return true;
    }
    
    /**
     * Checks if a card is playable according to UNO rules.
     *
     * @param card The card to check
     * @return true if the card can be played, false otherwise
     */
    private boolean isCardPlayable(Card card) {
        Card topCard = discardPile.peekCard();
        
        // If this is the first card (no cards in discard pile), any card can be played
        if (topCard == null || discardPile.isEmpty()) {
            System.out.println("Discard pile is empty, any card can be played");
            return true;
        }
        
        // Wild cards can always be played except for Wild Draw Four
        if (card.getAction() == CardAction.WILD) {
            return true;
        }
        
        // Wild Draw Four can only be played if the player has no cards matching the current color
        if (card.getAction() == CardAction.WILD_DRAW_FOUR) {
            Player currentPlayer = getCurrentPlayer();
            for (Card playerCard : currentPlayer.getHand()) {
                if (playerCard != card && playerCard.getColor() == currentColor) {
                    return false; // Player has a card matching the current color
                }
            }
            return true;
        }
        
        // Cards of the same color as the current game color can be played
        if (card.getColor() == currentColor) {
            return true;
        }
        
        // Cards with the same number as the top card can be played
        if (topCard.isNumberCard() && card.isNumberCard() && topCard.getValue() == card.getValue()) {
            return true;
        }
        
        // Action cards with the same action can be played (except wild cards)
        if (topCard.isActionCard() && card.isActionCard() && 
            topCard.getAction() == card.getAction() && 
            !topCard.isWildCard() && !card.isWildCard()) {
            return true;
        }
        
        return false;
    }

    /**
     * Handles the effects of special cards.
     *
     * @param card The card that was played
     */
    private void handleSpecialCard(Card card) {
        // If it's a number card or a wild card without draw, just move to the next player
        if (card.isNumberCard() || card.getAction() == CardAction.WILD) {
            nextPlayer();
            return;
        }
        
        // Handle action cards
        switch (card.getAction()) {
            case SKIP:
                skipPlayer();
                break;
                
            case REVERSE:
                reverseDirection();
                // In a two-player game, Reverse acts like Skip
                if (players.size() == 2) {
                    nextPlayer();
                } else {
                    nextPlayer();
                }
                break;
                
            case DRAW_TWO:
                Player nextPlayer = nextPlayer();
                // Draw two cards for the next player
                for (int i = 0; i < 2; i++) {
                    Card drawnCard = drawCard();
                    if (drawnCard != null) {
                        nextPlayer.addCard(drawnCard);
                    }
                }
                // Skip the player who drew cards
                nextPlayer();
                break;
                
            case WILD_DRAW_FOUR:
                nextPlayer = nextPlayer();
                // Draw four cards for the next player
                for (int i = 0; i < 4; i++) {
                    Card drawnCard = drawCard();
                    if (drawnCard != null) {
                        nextPlayer.addCard(drawnCard);
                    }
                }
                // Skip the player who drew cards
                nextPlayer();
                break;
                
            default:
                // For any other card types, just move to the next player
                nextPlayer();
                break;
        }
    }

    /**
     * Draws a card from the draw pile for the current player.
     *
     * @return The drawn card, or null if no card could be drawn
     */
    public Card drawCard() {
        // If the draw pile is empty, shuffle the discard pile (except the top card)
        // and make it the new draw pile
        if (drawPile.isEmpty() && discardPile.getSize() > 1) {
            Card topCard = discardPile.drawCard(); // Save the top card
            List<Card> cards = discardPile.getCards();
            drawPile.addCards(cards);
            drawPile.shuffle();
            discardPile = new Deck(); // Create a new empty discard pile
            discardPile.addCard(topCard); // Put the top card back
        }

        return drawPile.drawCard();
    }

    /**
     * Draws a card for the current player and adds it to their hand.
     *
     * @return The drawn card, or null if no card could be drawn
     */
    public Card drawCardForCurrentPlayer() {
        Player currentPlayer = getCurrentPlayer();
        
        // Check if the game has started
        if (!gameStarted || gameEnded) {
            return null;
        }

        Card card = drawCard();
        if (card != null) {
            currentPlayer.addCard(card);
            
            // For AI players, automatically play the card if possible
            if (currentPlayer.isAI() && isCardPlayable(card)) {
                playCard(card);
                return card;
            }
            
            // After drawing, move to the next player
            nextPlayer();
            
            // Update which cards are playable for the new current player
            updatePlayableCards();
        }
        
        return card;
    }
    
    /**
     * Gets the current UNO state as a string for debugging purposes.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Game ID: ").append(gameId).append("\n");
        sb.append("Game Mode: ").append(gameMode).append("\n");
        sb.append("Player Count: ").append(playerCount.getCount()).append("\n");
        sb.append("Direction: ").append(direction).append("\n");
        sb.append("Current Color: ").append(currentColor).append("\n");
        sb.append("Current Player: ").append(getCurrentPlayer() != null ? getCurrentPlayer().getName() : "None").append("\n");
        sb.append("Draw Pile: ").append(drawPile.getSize()).append(" cards\n");
        sb.append("Discard Pile: ").append(discardPile.getSize()).append(" cards\n");
        sb.append("Top Card: ").append(discardPile.peekCard()).append("\n");
        sb.append("Players: \n");
        
        for (Player player : players) {
            sb.append("  - ").append(player.toString()).append("\n");
        }
        
        if (gameEnded && winner != null) {
            sb.append("Winner: ").append(winner.getName()).append("\n");
        }
        
        return sb.toString();
    }
} 