package metu.ceng.ceng453_20242_group3_frontend.features.game.model;

/**
 * Enum representing the types of UNO cards.
 */
public enum CardType {
    WILDCARD, STANDARD;
    
    /**
     * Check if the card type is a wild card.
     * 
     * @return true if the card is a wild card, false otherwise
     */
    public boolean isWildCard() {
        return this == WILDCARD;
    }
} 