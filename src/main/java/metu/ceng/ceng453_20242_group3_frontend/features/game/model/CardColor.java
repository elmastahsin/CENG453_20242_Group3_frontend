package metu.ceng.ceng453_20242_group3_frontend.features.game.model;

/**
 * Enum representing the colors of UNO cards.
 */
public enum CardColor {
    GREEN, RED, BLUE, YELLOW, MULTI;
    
    /**
     * Get the lowercase name of the color for resource paths.
     * 
     * @return The lowercase name of the color
     */
    public String getColorName() {
        return this.name().toLowerCase();
    }
    
    /**
     * Check if the card color is a standard color (not MULTI).
     * 
     * @return true if the color is a standard color, false if it's MULTI
     */
    public boolean isStandardColor() {
        return this != MULTI;
    }
} 