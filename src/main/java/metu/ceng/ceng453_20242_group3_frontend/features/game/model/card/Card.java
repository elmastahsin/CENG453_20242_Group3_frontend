package metu.ceng.ceng453_20242_group3_frontend.features.game.model.card;

import javafx.scene.image.Image;

/**
 * Abstract base class for all UNO cards.
 */
public abstract class Card {
    
    public enum CardColor {
        RED, BLUE, GREEN, YELLOW, WILD
    }
    
    public enum CardType {
        NUMBER, SKIP, REVERSE, DRAW_TWO, WILD, WILD_DRAW_FOUR
    }
    
    private CardColor color;
    private CardType type;
    private Image frontImage;
    private Image backImage;
    private boolean playable;
    
    // Constructor
    public Card(CardColor color, CardType type) {
        this.color = color;
        this.type = type;
        this.playable = false;
    }
    
    // Abstract method to determine if this card can be played on top of the discard pile card
    public abstract boolean canBePlayedOn(Card discardPileCard);
    
    // Getters and Setters
    public CardColor getColor() {
        return color;
    }
    
    public void setColor(CardColor color) {
        this.color = color;
    }
    
    public CardType getType() {
        return type;
    }
    
    public void setType(CardType type) {
        this.type = type;
    }
    
    public Image getFrontImage() {
        return frontImage;
    }
    
    public void setFrontImage(Image frontImage) {
        this.frontImage = frontImage;
    }
    
    public Image getBackImage() {
        return backImage;
    }
    
    public void setBackImage(Image backImage) {
        this.backImage = backImage;
    }
    
    public boolean isPlayable() {
        return playable;
    }
    
    public void setPlayable(boolean playable) {
        this.playable = playable;
    }
    
    // Load the card images from resources
    protected void loadImages(String frontImagePath) {
        this.frontImage = new Image(getClass().getResourceAsStream(frontImagePath));
        this.backImage = new Image(getClass().getResourceAsStream("/metu/ceng/ceng453_20242_group3_frontend/images/cards/card_back.png"));
    }
} 