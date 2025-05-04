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
        this.direction = Direction.COUNTER_CLOCKWISE; // FORCE COUNTER_CLOCKWISE DIRECTION
        this.currentPlayerIndex = 0;
        this.gameStarted = false;
        this.gameEnded = false;
        this.winner = null;
        this.random = new Random();
        this.currentColor = null; // Will be set when first card is played

        System.out.println("### GAME CREATED WITH DIRECTION: " + this.direction + " ###");
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
            CardColor oldColor = this.currentColor;
            this.currentColor = color;
            System.out.println("Game color changed from: " + oldColor + " to: " + color +
                    " [Current player: " + (getCurrentPlayer() != null ? getCurrentPlayer().getName() : "None") +
                    ", Top card: " + (discardPile.peekCard() != null ? discardPile.peekCard() : "None") + "]");
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

        // First check if the player has any cards matching the current color
        // This is needed for Wild Draw Four validation
        boolean hasMatchingColorCard = false;
        for (Card card : currentPlayer.getHand()) {
            if (card.getColor() == currentColor && card.getColor() != CardColor.MULTI) {
                hasMatchingColorCard = true;
                System.out.println("Player has matching color card: " + card);
                break;
            }
        }

        // Then determine which cards are playable
        for (Card card : currentPlayer.getHand()) {
            boolean playable = false;

            if (card.getAction() == CardAction.WILD_DRAW_FOUR) {
                // Wild Draw Four can only be played if the player has no cards matching the current color
                playable = !hasMatchingColorCard;
                if (!playable) {
                    System.out.println("Card not playable (has matching color cards): " + card);
                }
            } else if (card.getAction() == CardAction.WILD) {
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

            // Update the card's playable status
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

        // Always start with human player (index 0)
        currentPlayerIndex = 0;
        System.out.println("Game starting with player: " + players.get(currentPlayerIndex).getName());
        System.out.println("Initial direction: " + direction);

        // Set the game as started
        gameStarted = true;

        return true;
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
        System.out.println("===== MOVING TO NEXT PLAYER =====");
        System.out.println("Current direction: " + direction);
        System.out.println("Current player index: " + currentPlayerIndex + " (" + getCurrentPlayer().getName() + ")");

        int oldIndex = currentPlayerIndex;

        if (direction == Direction.CLOCKWISE) {
            currentPlayerIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
        } else {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }

        System.out.println("===== NEXT PLAYER MOVEMENT COMPLETE =====");
        return getCurrentPlayer();
    }

    /**
     * Skips the next player's turn.
     *
     * @return The new current player
     */
    public Player skipPlayer() {
        // Skip once to move past the next player
        System.out.println("Skipping player - first move from index: " + currentPlayerIndex);
        Player skippedPlayer = nextPlayer();
        System.out.println("Skipped to index: " + currentPlayerIndex + " (player: " + skippedPlayer.getName() + ")");

        // Skip again to move to the player after the skipped one
        Player finalPlayer = nextPlayer();
        System.out.println("Continued to next player index: " + currentPlayerIndex + " (player: " + finalPlayer.getName() + ")");

        return finalPlayer;
    }

    /**
     * Reverses the direction of play.
     */
    public void reverseDirection() {
        Direction oldDirection = direction;

        if (direction == Direction.CLOCKWISE) {
            direction = Direction.COUNTER_CLOCKWISE;
        } else {
            direction = Direction.CLOCKWISE;
        }

        System.out.println("!!! DIRECTION REVERSED from " + oldDirection + " to " + direction + " !!!");
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

        System.out.println("Player " + currentPlayer.getName() + " is playing: " + card +
                ", Current color before play: " + currentColor);

        // Make sure the card is playable according to UNO rules
        if (!isCardPlayable(card)) {
            System.out.println("Card is not playable: " + card);
            return false;
        }

        // Remove the card from player's hand
        if (!currentPlayer.removeCard(card)) {
            return false;
        }

        // Add the card to the discard pile
        discardPile.addCard(card);

        // Handle the color based on card type
        if (card.isWildCard()) {
            // Wild cards - color should already be set by the UI
            // For AI players, choose a random color if not already set
            if (currentPlayer.isAI()) {
                CardColor[] possibleColors = {
                        CardColor.RED, CardColor.BLUE, CardColor.GREEN, CardColor.YELLOW
                };

                // If color hasn't been set yet, set it now
                if (this.currentColor == null || this.currentColor == CardColor.MULTI) {
                    CardColor randomColor = possibleColors[random.nextInt(possibleColors.length)];
                    System.out.println("AI player setting wild card color to: " + randomColor);
                    setCurrentColor(randomColor);
                }
            }
            // Note: For human players the color is set in the UI before calling playCard
        } else {
            // For regular cards (including action cards), set current color to card's color
            System.out.println("Setting color to card's color: " + card.getColor());
            setCurrentColor(card.getColor());
        }

        // Check if the player has won
        if (currentPlayer.getCardCount() == 0) {
            gameEnded = true;
            winner = currentPlayer;
            return true;
        }

        // Automatically call UNO when player has one card left
        if (currentPlayer.getCardCount() == 1) {
            currentPlayer.setHasCalledUno(true);
        }

        // Handle special cards
        handleSpecialCard(card);

        // Update which cards are playable for the new current player
        updatePlayableCards();

        System.out.println("After play: Current color is now: " + currentColor +
                ", Current player is now: " + getCurrentPlayer().getName());
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
                if (playerCard != card && playerCard.getColor() == currentColor && playerCard.getColor() != CardColor.MULTI) {
                    System.out.println("Cannot play Wild Draw Four - player has a card matching the current color: " + playerCard);
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
        System.out.println("Handling special card: " + card +
                ", Current color: " + currentColor +
                ", Current player: " + getCurrentPlayer().getName());

        // If it's a number card or a wild card without draw, just move to the next player
        if (card.isNumberCard() || card.getAction() == CardAction.WILD) {
            Player nextP = nextPlayer();
            System.out.println("Moving to next player: " + nextP.getName());
            return;
        }

        // Handle action cards
        switch (card.getAction()) {
            case SKIP:
                // Skip next player's turn
                Player skippedTo = skipPlayer();
                System.out.println("Skipped to player: " + skippedTo.getName() +
                        ", Current color remains: " + currentColor);
                break;

            case REVERSE:
                // Reverse the direction of play
                Direction oldDirection = direction;
                reverseDirection();
                System.out.println("Direction changed from " + oldDirection + " to " + direction);

                // In a two-player game, Reverse acts like Skip
                if (players.size() == 2) {
                    // After reversing, we need to do a nextPlayer to skip the other player
                    Player reversedTo = nextPlayer();
                    System.out.println("Two players only, Reverse acts like Skip. Now at player: " + reversedTo.getName());
                } else {
                    // In games with more than 2 players, just move to the next player
                    // (which will be different due to reversed direction)
                    Player reversedTo = nextPlayer();
                    System.out.println("Moved to player in new direction: " + reversedTo.getName());
                }
                break;

            case DRAW_TWO:
                Player nextPlayer = nextPlayer();
                System.out.println("Draw Two: Next player " + nextPlayer.getName() + " will draw 2 cards");
                // Draw two cards for the next player
                for (int i = 0; i < 2; i++) {
                    Card drawnCard = drawCard();
                    if (drawnCard != null) {
                        nextPlayer.addCard(drawnCard);
                        System.out.println("  - Drew card: " + drawnCard);
                    }
                }
                // Skip the player who drew cards
                Player skippedPlayer = nextPlayer();
                System.out.println("Skipped player " + nextPlayer.getName() + ", new current player: " + skippedPlayer.getName());
                break;

            case WILD_DRAW_FOUR:
                nextPlayer = nextPlayer();
                System.out.println("Wild Draw Four: Next player " + nextPlayer.getName() + " will draw 4 cards, current color: " + currentColor);
                // Draw four cards for the next player
                for (int i = 0; i < 4; i++) {
                    Card drawnCard = drawCard();
                    if (drawnCard != null) {
                        nextPlayer.addCard(drawnCard);
                        System.out.println("  - Drew card: " + drawnCard);
                    }
                }
                // Skip the player who drew cards
                skippedPlayer = nextPlayer();
                System.out.println("Skipped player " + nextPlayer.getName() + ", new current player: " + skippedPlayer.getName());
                break;

            default:
                // For any other card types, just move to the next player
                Player next = nextPlayer();
                System.out.println("Moved to next player for other action: " + next.getName());
                break;
        }

        System.out.println("After handling special card, current color is: " + currentColor);
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

    /**
     * Gets the player who would be next in turn.
     * Useful for determining who is affected by action cards.
     *
     * @return The next player in turn sequence
     */
    public Player getNextPlayer() {
        if (players.isEmpty()) {
            return null;
        }

        // Calculate the next player index without changing the current player
        int nextPlayerIndex;

        if (direction == Direction.CLOCKWISE) {
            nextPlayerIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
        } else {
            nextPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }

        System.out.println("Next player would be index: " + nextPlayerIndex);
        return players.get(nextPlayerIndex);
    }
} 