package metu.ceng.ceng453_20242_group3_frontend.features.game.model;

import java.util.Objects;
import javafx.scene.image.Image;

/**
 * Represents a UNO card with a color, type, and value.
 * Includes UI representation with front and back images.
 */
public class Card {
    private final CardColor color;
    private final CardType type;
    private final int value; // Relevant for number cards (0-9)
    private Image frontImage;
    private Image backImage;
    private boolean playable;

    private static final String IMAGE_PATH = "/metu/ceng/ceng453_20242_group3_frontend/images/cards/";
    private static final String CARD_BACK_IMAGE = IMAGE_PATH + "card_back.png";

    /**
     * Constructor for creating a card.
     *
     * @param color The color of the card
     * @param type  The type of the card
     * @param value The value of the card (for number cards)
     */
    public Card(CardColor color, CardType type, int value) {
        this.color = color;
        this.type = type;
        this.value = value;
        this.playable = false;
        loadImages();
    }

    /**
     * Constructor for creating a non-number card.
     *
     * @param color The color of the card
     * @param type  The type of the card
     */
    public Card(CardColor color, CardType type) {
        this(color, type, -1);
    }

    /**
     * Loads the appropriate card images based on the card's color, type, and value.
     */
    private void loadImages() {
        // Always load the back image
        try {
            backImage = new Image(getClass().getResourceAsStream(CARD_BACK_IMAGE));
        } catch (Exception e) {
            System.err.println("Failed to load card back image: " + e.getMessage());
        }

        // Load the front image based on card properties
        String frontImagePath = determineFrontImagePath();
        try {
            frontImage = new Image(getClass().getResourceAsStream(frontImagePath));
        } catch (Exception e) {
            System.err.println("Failed to load card front image: " + e.getMessage());
        }
    }

    /**
     * Determines the front image path based on the card's properties.
     *
     * @return The path to the appropriate image
     */
    private String determineFrontImagePath() {
        String colorName = color.toString().toLowerCase();
        
        if (type == CardType.NUMBER) {
            return IMAGE_PATH + "card_" + value + "_" + colorName + ".png";
        } else if (type == CardType.WILD || type == CardType.WILD_DRAW_FOUR) {
            return IMAGE_PATH + "card_" + type.toString().toLowerCase() + ".png";
        } else {
            // Action cards: Skip, Reverse, Draw Two
            return IMAGE_PATH + "card_" + type.toString().toLowerCase() + "_" + colorName + ".png";
        }
    }

    public CardColor getColor() {
        return color;
    }

    public CardType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public Image getFrontImage() {
        return frontImage;
    }

    public Image getBackImage() {
        return backImage;
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
        if (this.type == CardType.WILD || this.type == CardType.WILD_DRAW_FOUR) {
            return true;
        }

        // Cards with the same color can be played
        if (this.color == topCard.color) {
            return true;
        }

        // Cards with the same type or value can be played
        if (this.type == topCard.type) {
            return true;
        }

        // Number cards with the same value can be played
        return this.type == CardType.NUMBER && topCard.type == CardType.NUMBER && this.value == topCard.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return value == card.value && color == card.color && type == card.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type, value);
    }

    @Override
    public String toString() {
        if (type == CardType.NUMBER) {
            return color + " " + value;
        } else {
            return color + " " + type;
        }
    }
} 