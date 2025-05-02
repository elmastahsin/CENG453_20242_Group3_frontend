package metu.ceng.ceng453_20242_group3_frontend.features.game.model;

import metu.ceng.ceng453_20242_group3_frontend.config.AppConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a deck of UNO cards.
 */
public class Deck {
    private final List<Card> cards;

    /**
     * Constructor for creating an empty deck.
     */
    public Deck() {
        this.cards = new ArrayList<>();
    }

    /**
     * Constructor for creating a deck with initial cards.
     *
     * @param cards The initial list of cards
     */
    public Deck(List<Card> cards) {
        this.cards = new ArrayList<>(cards);
    }

    /**
     * Gets all cards in the deck.
     *
     * @return The list of cards
     */
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    /**
     * Gets the number of cards in the deck.
     *
     * @return The number of cards
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
     * Adds a card to the top of the deck.
     *
     * @param card The card to add
     */
    public void addCard(Card card) {
        cards.add(card);
    }

    /**
     * Adds cards to the top of the deck.
     *
     * @param newCards The cards to add
     */
    public void addCards(List<Card> newCards) {
        cards.addAll(newCards);
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
        return cards.remove(cards.size() - 1);
    }

    /**
     * Gets the top card without removing it.
     *
     * @return The top card, or null if the deck is empty
     */
    public Card peekCard() {
        if (isEmpty()) {
            return null;
        }
        return cards.get(cards.size() - 1);
    }

    /**
     * Shuffles the deck.
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Creates and returns a standard UNO deck.
     * A deck of 120 cards consists of:
     * - Two sets of numbered cards (0-9)
     * - Skip, Reverse, and Draw Two cards in four colors (red, yellow, green, blue)
     * - 8 Wild cards
     * - 8 Wild Draw Four cards
     *
     * @return A new standard UNO deck
     */
    public static Deck createStandardDeck() {
        List<Card> cards = new ArrayList<>();
        CardColor[] colors = new CardColor[]{CardColor.RED, CardColor.BLUE, CardColor.GREEN, CardColor.YELLOW};

        // For each color, add:
        for (CardColor color : colors) {
            // Two of each '0' card (making 8 zero cards total across 4 colors)
            cards.add(new Card(color, CardType.NUMBER, 0));
            cards.add(new Card(color, CardType.NUMBER, 0));

            // Two of each number card 1-9 (making 72 number cards)
            for (int i = 1; i <= 9; i++) {
                cards.add(new Card(color, CardType.NUMBER, i));
                cards.add(new Card(color, CardType.NUMBER, i));
            }

            // Two of each action card: Skip, Reverse, Draw Two (making 24 action cards)
            for (int i = 0; i < 2; i++) {
                cards.add(new Card(color, CardType.SKIP));
                cards.add(new Card(color, CardType.REVERSE));
                cards.add(new Card(color, CardType.DRAW_TWO));
            }
        }

        // 8 Wild cards and 8 Wild Draw Four cards (making 16 wild cards)
        for (int i = 0; i < 8; i++) {
            cards.add(new Card(CardColor.WILD, CardType.WILD));
            cards.add(new Card(CardColor.WILD, CardType.WILD_DRAW_FOUR));
        }

        // Verify we have 120 cards as per the requirement
        if (cards.size() != AppConfig.UNO_DECK_SIZE) {
            System.err.println("Warning: Deck created with " + cards.size() + 
                " cards instead of the expected " + AppConfig.UNO_DECK_SIZE);
        }

        Deck deck = new Deck(cards);
        deck.shuffle();
        return deck;
    }

    @Override
    public String toString() {
        return "Deck with " + cards.size() + " cards";
    }
} 