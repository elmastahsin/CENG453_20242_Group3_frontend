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
     * Adds a card to the player's hand.
     *
     * @param card The card to add
     */
    public void addCard(Card card) {
        hand.add(card);
        
        // Reset UNO declaration if player has more than 1 card
        if (hand.size() > 1) {
            hasCalledUno = false;
        }
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
     * Checks if the player has a valid card to play.
     *
     * @param topCard The card on top of the discard pile
     * @return true if the player has a valid card, false otherwise
     */
    public boolean hasValidMove(Card topCard) {
        return hand.stream().anyMatch(card -> card.canPlayOn(topCard));
    }

    /**
     * Gets a list of cards that can be played on the given card.
     *
     * @param topCard The card on top of the discard pile
     * @return A list of playable cards
     */
    public List<Card> getPlayableCards(Card topCard) {
        List<Card> playableCards = new ArrayList<>();
        for (Card card : hand) {
            if (card.canPlayOn(topCard)) {
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