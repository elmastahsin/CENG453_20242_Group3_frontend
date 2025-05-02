package metu.ceng.ceng453_20242_group3_frontend.features.game.model;

import java.util.Objects;

/**
 * Represents a UNO card with a color, type, and value.
 */
public class Card {
    private final CardColor color;
    private final CardType type;
    private final CardAction action;
    private final int value; // Relevant for number cards (0-9)
    private boolean playable;

    /**
     * Constructor for creating a number card.
     *
     * @param color The color of the card
     * @param value The value of the card (for number cards)
     */
    public Card(CardColor color, int value) {
        this.color = color;
        this.type = CardType.STANDARD;
        this.action = CardAction.NONE;
        this.value = value;
        this.playable = false;
    }

    /**
     * Constructor for creating an action card (Skip, Reverse, Draw Two).
     *
     * @param color The color of the card
     * @param action The action of the card
     */
    public Card(CardColor color, CardAction action) {
        this.color = color;
        this.type = action.isWildAction() ? CardType.WILDCARD : CardType.STANDARD;
        this.action = action;
        this.value = -1; // Action cards don't have a value
        this.playable = false;
    }

    public CardColor getColor() {
        return color;
    }

    public CardType getType() {
        return type;
    }
    
    public CardAction getAction() {
        return action;
    }

    public int getValue() {
        return value;
    }
    
    public boolean isNumberCard() {
        return action == CardAction.NONE && value >= 0;
    }
    
    public boolean isActionCard() {
        return action != CardAction.NONE;
    }
    
    public boolean isWildCard() {
        return type == CardType.WILDCARD;
    }

    public boolean isPlayable() {
        return playable;
    }

    public void setPlayable(boolean playable) {
        this.playable = playable;
    }

    /**
     * Checks if this card can be played on top of the given card.
     *
     * @param topCard The card on top of the discard pile
     * @return true if this card can be played, false otherwise
     */
    public boolean canPlayOn(Card topCard) {
        // Wild cards can be played on any card
        if (this.type == CardType.WILDCARD) {
            return true;
        }

        // Cards with the same color can be played
        if (this.color == topCard.color) {
            return true;
        }

        // Cards with the same action can be played
        if (this.action != CardAction.NONE && this.action == topCard.action) {
            return true;
        }

        // Number cards with the same value can be played
        return this.isNumberCard() && topCard.isNumberCard() && this.value == topCard.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return value == card.value && color == card.color && type == card.type && action == card.action;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type, action, value);
    }

    @Override
    public String toString() {
        if (isNumberCard()) {
            return color + " " + value;
        } else {
            return color + " " + action;
        }
    }
} 