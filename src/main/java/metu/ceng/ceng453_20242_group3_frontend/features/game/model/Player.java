package metu.ceng.ceng453_20242_group3_frontend.features.game.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player in the UNO game.
 */
public class Player {
    private final String name;
    private final List<Card> hand;
    private boolean isAI;
    private boolean hasCalledUno;
    private boolean shouldShowUnoIndicator;

    /**
     * Constructor for creating a player.
     *
     * @param name The name of the player
     * @param isAI Whether the player is an AI
     */
    public Player(String name, boolean isAI) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.isAI = isAI;
        this.hasCalledUno = false;
        this.shouldShowUnoIndicator = false;
    }

    /**
     * Constructor for creating a human player.
     *
     * @param name The name of the player
     */
    public Player(String name) {
        this(name, false);
    }

    public String getName() {
        return name;
    }

    public List<Card> getHand() {
        return hand;
    }

    public boolean isAI() {
        return isAI;
    }
    
    public boolean hasCalledUno() {
        return hasCalledUno;
    }
    
    public void setHasCalledUno(boolean hasCalledUno) {
        this.hasCalledUno = hasCalledUno;
    }
    
    /**
     * Checks if the UNO indicator should be displayed for this player.
     * 
     * @return true if the UNO indicator should be shown, false otherwise
     */
    public boolean shouldShowUnoIndicator() {
        return shouldShowUnoIndicator;
    }
    
    /**
     * Updates whether the UNO indicator should be shown.
     * Will automatically set to true if the player has exactly one card.
     */
    public void updateUnoIndicator() {
        shouldShowUnoIndicator = (hand.size() == 1);
    }
    
    /**
     * Manually control the UNO indicator visibility.
     * 
     * @param show Whether to show the UNO indicator
     */
    public void setShouldShowUnoIndicator(boolean show) {
        this.shouldShowUnoIndicator = show;
    }

    /**
     * Adds a card to the player's hand.
     *
     * @param card The card to add
     */
    public void addCard(Card card) {
        hand.add(card);
        
        // Reset UNO declaration if player has more than 1 card
        if (hand.size() > 1) {
            hasCalledUno = false;
            shouldShowUnoIndicator = false;
        }
        
        // Update UNO indicator
        updateUnoIndicator();
    }

    /**
     * Removes a card from the player's hand.
     *
     * @param card The card to remove
     * @return true if the card was removed, false otherwise
     */
    public boolean removeCard(Card card) {
        boolean removed = hand.remove(card);
        
        // Reset UNO declaration if player has more than 1 card
        if (hand.size() > 1) {
            hasCalledUno = false;
            shouldShowUnoIndicator = false;
        } else if (hand.size() == 1) {
            // Automatically call UNO when down to one card
            hasCalledUno = true;
            shouldShowUnoIndicator = true;
        }
        
        return removed;
    }

    /**
     * Gets the number of cards in the player's hand.
     *
     * @return The number of cards
     */
    public int getCardCount() {
        return hand.size();
    }
    
    /**
     * Checks if the player should declare UNO (has exactly 1 card).
     *
     * @return true if the player should declare UNO, false otherwise
     */
    public boolean shouldDeclareUno() {
        return hand.size() == 1 && !hasCalledUno;
    }
    
    /**
     * Automatically declares UNO for the player.
     * This should be called when the player gets to one card.
     * 
     * @return true if UNO was declared, false if player already declared or doesn't have one card
     */
    public boolean declareUno() {
        if (hand.size() != 1 || hasCalledUno) {
            return false;
        }
        
        hasCalledUno = true;
        shouldShowUnoIndicator = true;
        return true;
    }

    /**
     * Checks if the player has any playable cards.
     * This is a convenience method that checks if any card in the hand is marked as playable.
     *
     * @return true if the player has at least one playable card, false otherwise
     */
    public boolean hasPlayableCards() {
        return hand.stream().anyMatch(Card::isPlayable);
    }

    /**
     * Gets a list of cards that are currently marked as playable.
     * Note: Cards must be marked as playable by calling Game.updatePlayableCards() first.
     *
     * @return A list of playable cards
     */
    public List<Card> getPlayableCards() {
        List<Card> playableCards = new ArrayList<>();
        for (Card card : hand) {
            if (card.isPlayable()) {
                playableCards.add(card);
            }
        }
        return playableCards;
    }

    @Override
    public String toString() {
        return name + " (" + (isAI ? "AI" : "Human") + "): " + hand.size() + " cards";
    }
} 