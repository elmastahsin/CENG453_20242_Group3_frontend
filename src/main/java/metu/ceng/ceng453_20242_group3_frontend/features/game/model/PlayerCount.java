package metu.ceng.ceng453_20242_group3_frontend.features.game.model;

/**
 * Enum representing the number of players in a game.
 */
public enum PlayerCount {
    TWO(2),
    THREE(3),
    FOUR(4);
    
    private final int count;
    
    PlayerCount(int count) {
        this.count = count;
    }
    
    public int getCount() {
        return count;
    }
} 