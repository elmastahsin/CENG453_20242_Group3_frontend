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
    
    // Track stacked Draw Two cards for the stacking mechanic
    private int drawTwoStack = 0;

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
        this.direction = Direction.COUNTER_CLOCKWISE;
        this.currentPlayerIndex = 0;
        this.gameStarted = false;
        this.gameEnded = false;
        this.winner = null;
        this.random = new Random();
        this.currentColor = null;
        this.drawTwoStack = 0;

        System.out.println("### GAME CREATED WITH DIRECTION: " + this.direction + " ###");
    }

    /* === Getters and basic methods === */

    public String getGameId() {
        return gameId;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public PlayerCount getPlayerCount() {
        return playerCount;
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public Deck getDrawPile() {
        return drawPile;
    }

    public Deck getDiscardPile() {
        return discardPile;
    }

    public boolean isDiscardPileEmpty() {
        return discardPile == null || discardPile.isEmpty();
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public Player getCurrentPlayer() {
        if (players.isEmpty()) {
            return null;
        }
        return players.get(currentPlayerIndex);
    }

    public Player getPlayerByIndex(int index) {
        if (index < 0 || index >= players.size()) {
            System.out.println("Invalid player index: " + index);
            return null;
        }
        return players.get(index);
    }

    public CardColor getCurrentColor() {
        Card topCard = discardPile.peekCard();
        // For wild cards, use the color that was explicitly set
        if (topCard != null && topCard.isWildCard()) {
            return currentColor;
        } else if (topCard != null) {
            // For regular cards, use the card's color
            return topCard.getColor();
        }
        // If no card has been played yet, return the current color
        return currentColor;
    }

    public void setCurrentColor(CardColor color) {
        if (color != CardColor.MULTI) {
            CardColor oldColor = this.currentColor;
            this.currentColor = color;
            System.out.println("Game color changed from: " + oldColor + " to: " + color +
                    " [Current player: " + (getCurrentPlayer() != null ? getCurrentPlayer().getName() : "None") +
                    ", Top card: " + (discardPile.peekCard() != null ? discardPile.peekCard() : "None") + "]");
        }
    }

    public boolean addPlayer(Player player) {
        if (players.size() >= playerCount.getCount()) {
            return false;
        }
        return players.add(player);
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public Player getWinner() {
        return winner;
    }

    /**
     * Gets the current Draw Two stack count.
     * 
     * @return The number of Draw Two cards in the current stack
     */
    public int getDrawTwoStackCount() {
        return drawTwoStack;
    }

    /* === Game state helper methods === */

    /**
     * Checks if the game is currently active (started but not ended)
     * 
     * @return true if the game is active, false otherwise
     */
    private boolean isGameActive() {
        return gameStarted && !gameEnded;
    }

    /**
     * Declares a player as the winner and ends the game.
     * 
     * @param player The winning player
     */
    private void declareWinner(Player player) {
        gameEnded = true;
        winner = player;
        System.out.println("Player " + player.getName() + " has won the game!");
    }

    /* === Card playability methods === */

    /**
     * Updates which cards in the current player's hand are playable
     * based on the top card of the discard pile.
     */
    public void updatePlayableCards() {
        if (!isGameActive() || players.isEmpty()) {
            return;
        }
        
        Player currentPlayer = getCurrentPlayer();
        Card topCard = discardPile.peekCard();
        CardColor currentGameColor = getCurrentColor();
        
        System.out.println("Updating playable cards for " + currentPlayer.getName());
        System.out.println("Top card: " + topCard + ", Current color: " + currentGameColor);
        
        // Special case: If Draw Two stack is active, only Draw Two cards are playable
        if (drawTwoStack > 0) {
            for (Card card : currentPlayer.getHand()) {
                // Only Draw Two cards are playable when responding to a Draw Two
                    card.setPlayable(card.getAction() == CardAction.DRAW_TWO);

            }
            return;
        }
        
        // Standard playability rules for normal game state
        // Special case: If no cards in discard pile yet
        if (topCard == null) {
            setAllCardsPlayableExceptWildDrawFour(currentPlayer);
            return;
        }
        
        // Check if the player has any cards matching the current color
        boolean hasMatchingColorCard = checkForMatchingColorCards(currentPlayer, currentGameColor);
        
        // Determine which cards are playable
        updateCardPlayability(currentPlayer, topCard, currentGameColor, hasMatchingColorCard);
    }

    /**
     * Sets all cards playable except Wild Draw Four.
     * Used when the discard pile is empty.
     * 
     * @param player The player whose cards to update
     */
    private void setAllCardsPlayableExceptWildDrawFour(Player player) {
        for (Card card : player.getHand()) {
            card.setPlayable(card.getAction() != CardAction.WILD_DRAW_FOUR);
        }
    }

    /**
     * Checks if the player has any cards matching the current color.
     * 
     * @param player       The player to check
     * @param currentColor The current game color
     * @return true if the player has matching color cards, false otherwise
     */
    private boolean checkForMatchingColorCards(Player player, CardColor currentColor) {
        for (Card card : player.getHand()) {
            if (card.getColor() != CardColor.MULTI && card.getColor() == currentColor) {
                System.out.println("Player has matching color card: " + card);
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the playability of each card in the player's hand.
     * 
     * @param player               The player whose cards to update
     * @param topCard              The top card of the discard pile
     * @param currentColor         The current game color
     * @param hasMatchingColorCard Whether the player has cards matching the current
     *                             color
     */
    private void updateCardPlayability(Player player, Card topCard, CardColor currentColor,
            boolean hasMatchingColorCard) {
        for (Card card : player.getHand()) {
            boolean playable = determineIfCardIsPlayable(card, topCard, currentColor, hasMatchingColorCard);
            card.setPlayable(playable);
        }
    }

    /**
     * Determines if a specific card is playable.
     * 
     * @param card                 The card to check
     * @param topCard              The top card of the discard pile
     * @param currentColor         The current game color
     * @param hasMatchingColorCard Whether the player has cards matching the current
     *                             color
     * @return true if the card is playable, false otherwise
     */
    private boolean determineIfCardIsPlayable(Card card, Card topCard, CardColor currentColor,
            boolean hasMatchingColorCard) {
        // Wild and Wild Draw Four cards have special playability rules

        // Regular WILD cards can always be played, regardless of the current color or top card
        if (card.getAction() == CardAction.WILD) {
            System.out.println("WILD card is always playable: " + card);
            return true;
        }
        
        // Wild Draw Four can only be played if the player has no matching color cards
        if (card.getAction() == CardAction.WILD_DRAW_FOUR) {
            boolean playable = !hasMatchingColorCard;
            if (!playable) {
                System.out.println("WILD_DRAW_FOUR not playable - player has matching color cards: " + card);
            }
            return playable;
        }

        // Cards matching the current color can be played
        if (card.getColor() == currentColor) {
            System.out.println("Card matches current color (" + currentColor + "): " + card);
            return true;
        }

        // Number cards matching the top card's value can be played
        if (topCard.isNumberCard() && card.isNumberCard() && topCard.getValue() == card.getValue()) {
            System.out.println("Card matches top card value (" + topCard.getValue() + "): " + card);
            return true;
        }

        // Action cards matching the top card's action can be played (except wilds)
        if (topCard.isActionCard() && card.isActionCard() && topCard.getAction() == card.getAction()
                && !card.isWildCard() && !topCard.isWildCard()) {
            System.out.println("Card matches top card action (" + topCard.getAction() + "): " + card);
            return true;
        }

        // Special case for Draw Two during stacking
        if (drawTwoStack > 0 && card.getAction() == CardAction.DRAW_TWO) {
            System.out.println("Draw Two played during stacking (no color restriction): " + card);
            return true;
        }

        System.out.println("Card is not playable: " + card);
        return false;
    }

    /**
     * Checks if a card would be playable given the current game state.
     * Used for validating cards that are drawn or played.
     *
     * @param card The card to check
     * @return true if the card is playable, false otherwise
     */
    public boolean isCardPlayable(Card card) {
        Card topCard = discardPile.peekCard();
        CardColor currentGameColor = getCurrentColor();

        // If no top card, all non-Wild Draw Four cards are playable
        if (topCard == null) {
            return card.getAction() != CardAction.WILD_DRAW_FOUR;
        }

        // Check if player has any cards of the current color (for Wild Draw Four
        // validation)
        Player currentPlayer = getCurrentPlayer();
        boolean hasMatchingColorCard = false;

        for (Card c : currentPlayer.getHand()) {
            if (c != card && c.getColor() != CardColor.MULTI && c.getColor() == currentGameColor) {
                hasMatchingColorCard = true;
                break;
            }
        }

        return determineIfCardIsPlayable(card, topCard, currentGameColor, hasMatchingColorCard);
    }

    /* === Game flow control methods === */

    /**
     * Starts the game.
     *
     * @return true if the game was started, false otherwise
     */
    public boolean startGame() {
        if (players.size() < playerCount.getCount() || gameStarted) {
            return false;
        }

        dealInitialCards();

        System.out.println("Game starting with player: " + players.get(currentPlayerIndex).getName());
        System.out.println("Initial direction: " + direction);

        gameStarted = true;
        return true;
    }

    /**
     * Deals initial cards to all players.
     */
    private void dealInitialCards() {
        // dealInitialCardsForStacking();
        for (int i = 0; i < AppConfig.INITIAL_CARDS_PER_PLAYER; i++) {
            for (Player player : players) {
                Card card = drawPile.drawCard();
                if (card != null) {
                    player.addCard(card);
                }
            }
        }
    }

    // private void dealInitialCardsForStacking() {
    //     players.get(0).addCard(new Card(CardColor.RED, CardAction.DRAW_TWO));
    //     players.get(0).addCard(new Card(CardColor.YELLOW, CardAction.DRAW_TWO));
    //     players.get(1).addCard(new Card(CardColor.BLUE, CardAction.DRAW_TWO));
    // }


    /**
     * Moves to the next player in turn.
     *
     * @return The new current player
     */
    public Player nextPlayer() {
        System.out.println("===== MOVING TO NEXT PLAYER =====");
        System.out.println("Current direction: " + direction);

        int previousPlayerIndex = currentPlayerIndex;
        currentPlayerIndex = getNextPlayerIndex();

        System.out.println("Player index changed from: " + previousPlayerIndex + " to: " + currentPlayerIndex);
        System.out.println("===== NEXT PLAYER MOVEMENT COMPLETE =====");
        return getCurrentPlayer();
    }

    /**
     * Calculates the next player index without changing the current player.
     * 
     * @return The next player index
     */
    private int getNextPlayerIndex() {
        if (direction == Direction.CLOCKWISE) {
            return (currentPlayerIndex - 1 + players.size()) % players.size();
        } else {
            return (currentPlayerIndex + 1) % players.size();
        }
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

        int nextPlayerIndex = getNextPlayerIndex();
        System.out.println("Next player would be index: " + nextPlayerIndex);
        return players.get(nextPlayerIndex);
    }

    /**
     * Skips the next player's turn.
     *
     * @return The new current player
     */
    public Player skipPlayer() {
        // Skip once to move past the next player
        Player skippedPlayer = nextPlayer();
        // Skip again to move to the player after the skipped one
        Player finalPlayer = nextPlayer();
        System.out.println("Skipped player: " + skippedPlayer.getName() +
                ", Current player is now: " + finalPlayer.getName());

        return finalPlayer;
    }

    /**
     * Reverses the direction of play.
     */
    public void reverseDirection() {
        Direction oldDirection = direction;
        direction = (direction == Direction.CLOCKWISE) ? Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;
        System.out.println("Direction changed from: " + oldDirection + " to: " + direction);
    }

    /* === Card handling methods === */

    /**
     * Plays a card from the current player's hand.
     *
     * @param card The card to play
     * @return true if the card was played successfully, false otherwise
     */
    public boolean playCard(Card card) {
        Player currentPlayer = getCurrentPlayer();

        // Check if the game is active and player has the card
        if (!isGameActive() || !currentPlayer.getHand().contains(card)) {
            return false;
        }

        // Special handling for Draw Two stacking
        boolean isDrawTwoResponse = drawTwoStack > 0 && card.getAction() == CardAction.DRAW_TWO;
        
        if (!isDrawTwoResponse) {
            // Only update playable cards in normal situations (not during Draw Two stacking)
            updatePlayableCards();
        }

        // Check if the card is playable
        if (!card.isPlayable() && !isDrawTwoResponse) {
            System.out.println("Card is not playable: " + card);
            return false;
        }
        
        // Remove the card from player's hand and add to discard pile
        if (!currentPlayer.removeCard(card)) {
            return false;
        }
        discardPile.addCard(card);

        // Handle card color
        handleCardColor(card, currentPlayer);

        // Check for winner
        if (currentPlayer.getCardCount() == 0) {
            declareWinner(currentPlayer);
            return true;
        }

        // Automatically call UNO when player has one card left
        if (currentPlayer.getCardCount() == 1) {
            currentPlayer.setHasCalledUno(true);
        }

        // Handle special card actions
        handleCardAction(card);

        System.out.println("After play: Current color is now: " + currentColor +
                ", Current player is now: " + getCurrentPlayer().getName());
        return true;
    }

    /**
     * Handles the color logic when a card is played.
     * 
     * @param card   The card that was played
     * @param player The player who played the card
     */
    private void handleCardColor(Card card, Player player) {
        if (card.isWildCard()) {
            // For AI players, choose a random color if not already set
            if (player.isAI()) {
                setRandomColorForAI();
            }
            // For human players, color is set in UI before calling playCard
        } else {
            // For regular cards, set current color to card's color
            System.out.println("Setting color to card's color: " + card.getColor());
            setCurrentColor(card.getColor());
        }
    }

    /**
     * Sets a random color for AI players when they play wild cards.
     */
    private void setRandomColorForAI() {
        CardColor[] possibleColors = {
                CardColor.RED, CardColor.BLUE, CardColor.GREEN, CardColor.YELLOW
        };
        CardColor randomColor = possibleColors[random.nextInt(possibleColors.length)];
        System.out.println("AI player setting wild card color to: " + randomColor);
        setCurrentColor(randomColor);
    }

    /**
     * Handles the effects of special cards.
     *
     * @param card The card that was played
     */
    private void handleCardAction(Card card) {
        System.out.println("Handling special card: " + card +
                ", Current color: " + currentColor +
                ", Current player: " + getCurrentPlayer().getName());

        // If this is not a Draw Two card, reset the stack counter
        // since the chain is broken
        if (card.getAction() != CardAction.DRAW_TWO) {
            if (drawTwoStack > 0) {
                System.out.println("Draw Two stack reset (was " + drawTwoStack + ")");
                drawTwoStack = 0;
            }
        }

        // If it's a number card or a wild card without draw, just move to the next
        // player
        if (card.isNumberCard() || card.getAction() == CardAction.WILD) {
            Player nextP = nextPlayer();
            System.out.println("Moving to next player: " + nextP.getName());
            return;
        }

        // Handle action cards based on their type
        switch (card.getAction()) {
            case SKIP:
                handleSkipCard();
                break;

            case REVERSE:
                handleReverseCard();
                break;

            case DRAW_TWO:
                handleDrawTwoCard();
                break;

            case WILD_DRAW_FOUR:
                handleWildDrawFourCard();
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
     * Handles a Skip card action.
     */
    private void handleSkipCard() {
        Player skippedTo = skipPlayer();
        System.out.println("Skipped to player: " + skippedTo.getName() +
                ", Current color remains: " + currentColor);
    }

    /**
     * Handles a Reverse card action.
     */
    private void handleReverseCard() {
        Direction oldDirection = direction;
        reverseDirection();
        System.out.println("Direction changed from " + oldDirection + " to " + direction);

        // In a two-player game, Reverse acts like Skip
        if (players.size() == 2) {
            // After reversing, skip the other player
            Player reversedTo = nextPlayer();
            System.out.println("Two players only, Reverse acts like Skip. Now at player: " + reversedTo.getName());
        } else {
            // In games with more than 2 players, move to next player in new direction
            Player reversedTo = nextPlayer();
            System.out.println("Moved to player in new direction: " + reversedTo.getName());
        }
    }

    /**
     * Handles a Draw Two card action.
     */
    private void handleDrawTwoCard() {
        // Increment the draw two stack counter
        drawTwoStack += 1;
        
        Player nextPlayer = nextPlayer();
        System.out.println("Draw Two played, stack is now: " + drawTwoStack + " - Next player: " + nextPlayer.getName());
        
        // Check if the next player has a Draw Two card they can play in response
        boolean canRespond = checkForDrawTwoResponse(nextPlayer);
        
        if (!canRespond) {
            // If the player can't respond, they draw cards based on the stack size
            int cardsToDraw = drawTwoStack * 2;
            System.out.println("Player " + nextPlayer.getName() + " must draw " + cardsToDraw + " cards");
            
            // Draw the stacked cards
            drawCardsForPlayer(nextPlayer, cardsToDraw);
            
            // Reset the draw two stack
            drawTwoStack = 0;
            
            // Skip the player who drew cards
            Player skippedPlayer = nextPlayer();
            System.out.println("Skipped player " + nextPlayer.getName() + 
                    ", new current player: " + skippedPlayer.getName());
        }
        // If they can respond, we don't do anything - the stack will continue to increase
        // when they play their Draw Two card in their turn
    }
    
    /**
     * Checks if a player has a Draw Two card they can play in response to a stacked Draw Two.
     * This only applies to AI players - human players will manually select their card.
     * 
     * @param player The player to check
     * @return true if the player can respond with a Draw Two, false otherwise
     */
    private boolean checkForDrawTwoResponse(Player player) {
        // Only AI players should automatically respond - human players will choose manually
        if (!player.isAI()) {
            updatePlayableCards();
            
            // For human players, we'll update card playability to show only Draw Two cards
            // as playable, but we won't automatically respond
            for (Card card : player.getHand()) {
                // Only Draw Two cards are playable when responding to a Draw Two
                card.setPlayable(card.getAction() == CardAction.DRAW_TWO);
            }
            
            // Return if they have any Draw Two card, but don't play it automatically
            boolean hasDrawTwo = player.hasPlayableCards();
            System.out.println("Human player " + player.getName() + 
                    (hasDrawTwo ? " has a Draw Two card they can play" : " has no Draw Two cards"));
            return hasDrawTwo;
        }
        
        // For AI players, check if they have a Draw Two card - no color restriction
        boolean hasDrawTwo = false;
        for (Card card : player.getHand()) {
            if (card.getAction() == CardAction.DRAW_TWO) {
                // Found a Draw Two card, but don't play it yet - AI will play it in their turn
                hasDrawTwo = true;
                break;
            }
        }
        
        if (hasDrawTwo) {
            System.out.println("AI player " + player.getName() + " has a Draw Two card they can play on their turn");
        } else {
            System.out.println("AI player " + player.getName() + " has no Draw Two cards to respond with");
        }
        
        return hasDrawTwo;
    }

    /**
     * Handles a Wild Draw Four card action.
     */
    private void handleWildDrawFourCard() {
        Player nextPlayer = nextPlayer();
        System.out.println("Wild Draw Four: Next player " + nextPlayer.getName() +
                " will draw 4 cards, current color: " + currentColor);

        // Draw four cards for the next player
        drawCardsForPlayer(nextPlayer, 4);

        // Skip the player who drew cards
        Player skippedPlayer = nextPlayer();
        System.out.println("Skipped player " + nextPlayer.getName() +
                ", new current player: " + skippedPlayer.getName());
    }

    /**
     * Draw a specified number of cards for a player.
     * 
     * @param player The player who will receive the cards
     * @param count  Number of cards to draw
     */
    private void drawCardsForPlayer(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Card drawnCard = drawCard();
            if (drawnCard != null) {
                player.addCard(drawnCard);
                System.out.println("  - Drew card: " + drawnCard);
            }
        }
    }

    /**
     * Draws a card from the draw pile.
     *
     * @return The drawn card, or null if no card could be drawn
     */
    public Card drawCard() {
        // If the draw pile is empty, recycle the discard pile
        if (drawPile.isEmpty() && discardPile.getSize() > 1) {
            recycleDiscardPile();
        }

        return drawPile.drawCard();
    }

    /**
     * Recycles the discard pile when the draw pile is empty.
     * Keeps the top card in the discard pile and moves the rest to the draw pile.
     */
    private void recycleDiscardPile() {
        Card topCard = discardPile.drawCard(); // Draw the top card from the discard pile
        List<Card> cards = discardPile.getCards();
        drawPile.addCards(cards);
        drawPile.shuffle();
        discardPile = new Deck(); // Create a new empty discard pile
        discardPile.addCard(topCard); // Put the top card back
    }

    /**
     * Draws a card for the current player and adds it to their hand.
     * For AI players, it will try to play the card if possible and advance the
     * turn.
     * For human players, it will just add the card to their hand and advance the
     * turn.
     *
     * @return The drawn card, or null if no card could be drawn
     */
    public Card drawCardForCurrentPlayer() {
        Player currentPlayer = getCurrentPlayer();

        if (!isGameActive()) {
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
     * Draws a card for the current player without advancing the turn.
     * This is useful for human players when we want to check if the drawn card is
     * playable before deciding whether to advance the turn.
     * 
     * Special handling for Draw Two stacking - if drawTwoStack > 0, this will draw
     * all the required cards (2 × stack size).
     *
     * @return The drawn card, or null if no card could be drawn
     */
    public Card drawCardWithoutAdvancingTurn() {
        Player currentPlayer = getCurrentPlayer();

        if (!isGameActive()) {
            return null;
        }
        
        // Special handling for Draw Two stacking
        if (drawTwoStack > 0) {
            // Calculate total cards to draw (2 × stack size)
            int cardsToDraw = drawTwoStack * 2;
            System.out.println("Player " + currentPlayer.getName() + " is drawing " + 
                    cardsToDraw + " cards due to stacked Draw Two");
            
            // Draw all the required cards
            for (int i = 0; i < cardsToDraw; i++) {
                Card stackCard = drawCard();
                if (stackCard != null) {
                    currentPlayer.addCard(stackCard);
                }
            }
            
            // Reset the stack
            drawTwoStack = 0;
            
            // Update playable status of cards
            updatePlayableCards();
            
            // Return the last drawn card (for UI feedback)
            return currentPlayer.getHand().get(currentPlayer.getHand().size() - 1);
        }
        
        // Normal drawing (single card)
        Card card = drawCard();
        if (card != null) {
            currentPlayer.addCard(card);

            // Update playable status of the drawn card
            boolean playable = isCardPlayable(card);
            card.setPlayable(playable);
            System.out.println("Drawn card is " + (playable ? "playable: " : "not playable: ") + card);

            // Update which cards are playable
            updatePlayableCards();
        }

        return card;
    }

    /**
     * Advances the turn to the next player after drawing a card.
     * This should be called only if the human player decides not to play the drawn
     * card.
     */
    public void advanceTurnAfterDraw() {
        // If we had a Draw Two stack, we should reset it after the player draws
        if (drawTwoStack > 0) {
            drawTwoStack = 0;
        }
        
        nextPlayer();
        updatePlayableCards();
    }

    /* === Debug methods === */

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
        sb.append("Current Player: ").append(getCurrentPlayer() != null ? getCurrentPlayer().getName() : "None")
                .append("\n");
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