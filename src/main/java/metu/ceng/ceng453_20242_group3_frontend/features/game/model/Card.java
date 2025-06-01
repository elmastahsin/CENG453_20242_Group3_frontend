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
    private Integer id; // Backend card ID for multiplayer games

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
        this.id = null;
    }

    /**
     * Constructor for creating an action card (Skip, Reverse, Draw Two).
     *
     * @param color  The color of the card
     * @param action The action of the card
     */
    public Card(CardColor color, CardAction action) {
        this.color = color;
        this.type = action.isWildAction() ? CardType.WILDCARD : CardType.STANDARD;
        this.action = action;
        this.value = -1; // Action cards don't have a value
        this.playable = false;
        this.id = null;
    }

    /**
     * Constructor with ID for multiplayer games.
     *
     * @param id The backend card ID
     * @param color The color of the card
     * @param value The value of the card (for number cards)
     */
    public Card(Integer id, CardColor color, int value) {
        this.id = id;
        this.color = color;
        this.type = CardType.STANDARD;
        this.action = CardAction.NONE;
        this.value = value;
        this.playable = false;
    }

    /**
     * Constructor with ID for action cards in multiplayer games.
     *
     * @param id The backend card ID
     * @param color  The color of the card
     * @param action The action of the card
     */
    public Card(Integer id, CardColor color, CardAction action) {
        this.id = id;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return value == card.value &&
               playable == card.playable &&
               color == card.color &&
               type == card.type &&
               action == card.action &&
               Objects.equals(id, card.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type, action, value, playable, id);
    }

    @Override
    public String toString() {
        if (isNumberCard()) {
            return color + " " + value + (id != null ? " (ID:" + id + ")" : "");
        } else {
            return color + " " + action + (id != null ? " (ID:" + id + ")" : "");
        }
    }
}