package metu.ceng.ceng453_20242_group3_frontend.features.game.model;

/**
 * Enum representing the special actions of UNO cards.
 */
public enum CardAction {
    SKIP, REVERSE, DRAW_TWO, WILD, WILD_DRAW_FOUR, NONE;
    
    /**
     * Check if the action is a wild card action.
     * 
     * @return true if the action is WILD or WILD_DRAW_FOUR, false otherwise
     */
    public boolean isWildAction() {
        return this == WILD || this == WILD_DRAW_FOUR;
    }
    
    /**
     * Check if the action is a draw card action.
     * 
     * @return true if the action is DRAW_TWO or WILD_DRAW_FOUR, false otherwise
     */
    public boolean isDrawAction() {
        return this == DRAW_TWO || this == WILD_DRAW_FOUR;
    }
    
    /**
     * Get the symbol representing this action on a card.
     * 
     * @return A symbol or icon for the action
     */
    public String getSymbol() {
        switch (this) {
            case SKIP:
                return "⊘";
            case REVERSE:
                return "↺";
            case DRAW_TWO:
                return "+2";
            case WILD:
                return "★";
            case WILD_DRAW_FOUR:
                return "+4";
            default:
                return "";
        }
    }
} 