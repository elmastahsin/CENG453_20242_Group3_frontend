package metu.ceng.ceng453_20242_group3_frontend.features.game.model;

import java.util.Random;

/**
 * Computer AI player that handles automated opponent actions
 */
public class ComputerAIPlayer {
    
    private final String name;
    private boolean isThinking = false;
    private final Random random = new Random();
    
    public ComputerAIPlayer(String name) {
        this.name = name;
    }
    
    /**
     * Makes the AI player wait for a short time before taking action 
     * to simulate thinking
     * 
     * @return The response time in milliseconds
     */
    public int takeTurn() {
        this.isThinking = true;
        int thinkTime = calculateThinkTime();
        return thinkTime;
    }
    
    /**
     * Calculates AI thinking time
     */
    private int calculateThinkTime() {
        // Base thinking time of 1.5 seconds
        return 3000;
    }
    
    /**
     * Indicates the AI player has finished their turn
     */
    public void finishTurn() {
        this.isThinking = false;
    }
    
    /**
     * Selects a random color for a wild card.
     * 
     * @return The selected color
     */
    public CardColor selectWildCardColor() {
        CardColor[] possibleColors = {
            CardColor.RED,
            CardColor.BLUE,
            CardColor.GREEN,
            CardColor.YELLOW
        };
        
        return possibleColors[random.nextInt(possibleColors.length)];
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isThinking() {
        return isThinking;
    }
} 