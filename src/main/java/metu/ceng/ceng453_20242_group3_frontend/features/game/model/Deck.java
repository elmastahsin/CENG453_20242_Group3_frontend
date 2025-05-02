package metu.ceng.ceng453_20242_group3_frontend.features.game.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import metu.ceng.ceng453_20242_group3_frontend.config.AppConfig;

/**
 * Represents a deck of UNO cards.
 */
public class Deck {
    private final List<Card> cards;
    private final Random random;

    /**
     * Constructor for creating an empty deck.
     */
    public Deck() {
        this.cards = new ArrayList<>();
        this.random = new Random();
    }

    /**
     * Constructor for creating a deck with a list of cards.
     *
     * @param cards The list of cards to add to the deck
     */
    public Deck(List<Card> cards) {
        this.cards = new ArrayList<>(cards);
        this.random = new Random();
    }

    /**
     * Creates a standard UNO deck with all cards.
     *
     * @return A new deck with standard UNO cards
     */
    public static Deck createStandardDeck() {
        Deck deck = new Deck();
        
        // Create standard colored cards (RED, BLUE, GREEN, YELLOW)
        for (CardColor color : new CardColor[]{CardColor.RED, CardColor.BLUE, CardColor.GREEN, CardColor.YELLOW}) {
            // Add one 0 card
            deck.addCard(new Card(color, 0));
            
            // Add two of each number card 1-9
            for (int i = 1; i <= 9; i++) {
                deck.addCard(new Card(color, i));
                deck.addCard(new Card(color, i));
            }
            
            // Add two of each action card: Skip, Reverse, Draw Two
            for (CardAction action : new CardAction[]{CardAction.SKIP, CardAction.REVERSE, CardAction.DRAW_TWO}) {
                deck.addCard(new Card(color, action));
                deck.addCard(new Card(color, action));
            }
        }
        
        // Add wild cards (4 of each)
        for (int i = 0; i < AppConfig.CARD_WILD_COUNT; i++) {
            deck.addCard(new Card(CardColor.MULTI, CardAction.WILD));
            deck.addCard(new Card(CardColor.MULTI, CardAction.WILD_DRAW_FOUR));
        }
        
        // Shuffle the deck
        deck.shuffle();
        
        return deck;
    }

    /**
     * Gets the list of cards in the deck.
     *
     * @return The list of cards
     */
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    /**
     * Gets the number of cards in the deck.
     *
     * @return The size of the deck
     */
    public int getSize() {
        return cards.size();
    }

    /**
     * Checks if the deck is empty.
     *
     * @return true if the deck is empty, false otherwise
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Adds a card to the deck.
     *
     * @param card The card to add
     */
    public void addCard(Card card) {
        cards.add(card);
    }

    /**
     * Adds multiple cards to the deck.
     *
     * @param cardsToAdd The list of cards to add
     */
    public void addCards(List<Card> cardsToAdd) {
        cards.addAll(cardsToAdd);
    }

    /**
     * Removes a card from the deck.
     *
     * @param card The card to remove
     * @return true if the card was removed, false otherwise
     */
    public boolean removeCard(Card card) {
        return cards.remove(card);
    }

    /**
     * Draws a card from the top of the deck.
     *
     * @return The drawn card, or null if the deck is empty
     */
    public Card drawCard() {
        if (isEmpty()) {
            return null;
        }
        return cards.remove(0);
    }

    /**
     * Peeks at the top card of the deck without removing it.
     *
     * @return The top card, or null if the deck is empty
     */
    public Card peekCard() {
        if (isEmpty()) {
            return null;
        }
        return cards.get(0);
    }

    /**
     * Shuffles the deck.
     */
    public void shuffle() {
        Collections.shuffle(cards, random);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Deck: ").append(getSize()).append(" cards\n");
        for (Card card : cards) {
            sb.append(" - ").append(card.toString()).append("\n");
        }
        return sb.toString();
    }
} 