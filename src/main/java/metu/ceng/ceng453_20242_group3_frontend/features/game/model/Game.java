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
            return;
        }
        
        for (Card card : currentPlayer.getHand()) {
            card.setPlayable(card.canPlayOn(topCard));
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
        // Keep drawing until we get a number card (not an action card)
        while (initialCard != null && !initialCard.isNumberCard()) {
            // Put the action card back in the deck and shuffle
            drawPile.addCard(initialCard);
            drawPile.shuffle();
            initialCard = drawPile.drawCard();
        }
        
        if (initialCard != null) {
            // Create a fresh discard pile to ensure proper ordering
            discardPile = new Deck();
            discardPile.addCard(initialCard);
            System.out.println("Initial card placed on discard pile: " + initialCard);
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

        // Check if the card can be played
        Card topCard = discardPile.peekCard();
        if (topCard != null && !card.canPlayOn(topCard)) {
            return false;
        }

        // Remove the card from player's hand
        if (!currentPlayer.removeCard(card)) {
            return false;
        }

        // Add the card to the discard pile
        discardPile.addCard(card);

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
     * Handles the effects of special cards.
     *
     * @param card The card that was played
     */
    private void handleSpecialCard(Card card) {
        // If it's a number card or a standard wild card, just move to the next player
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
            
            // After drawing, move to the next player
            nextPlayer();
            
            // Update which cards are playable for the new current player
            updatePlayableCards();
        }
        
        return card;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Game ID: ").append(gameId).append("\n");
        sb.append("Game Mode: ").append(gameMode).append("\n");
        sb.append("Player Count: ").append(playerCount.getCount()).append("\n");
        sb.append("Direction: ").append(direction).append("\n");
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