package metu.ceng.ceng453_20242_group3_frontend.features.game.model.card;

/**
 * Represents a number card in the UNO game.
 */
public class NumberCard extends Card {
    
    private int number;
    
    public NumberCard(CardColor color, int number) {
        super(color, CardType.NUMBER);
        this.number = number;
        
        // Load the appropriate image based on color and number
        String imagePath = String.format("/metu/ceng/ceng453_20242_group3_frontend/images/cards/%s_%d.png", 
                color.name().toLowerCase(), number);
        loadImages(imagePath);
    }
    
    public int getNumber() {
        return number;
    }
    
    @Override
    public boolean canBePlayedOn(Card discardPileCard) {
        // A number card can be played if:
        // 1. It has the same color as the discard pile card
        // 2. It has the same number as the discard pile card (if it's a number card)
        
        if (discardPileCard.getColor() == this.getColor()) {
            return true;
        }
        
        if (discardPileCard instanceof NumberCard) {
            NumberCard otherCard = (NumberCard) discardPileCard;
            return otherCard.getNumber() == this.number;
        }
        
        return false;
    }
    
    @Override
    public String toString() {
        return getColor() + " " + number;
    }
} 