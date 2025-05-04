package metu.ceng.ceng453_20242_group3_frontend.features.game.model;

import java.util.List;
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
    
    /**
     * Makes a strategic decision on which color to choose for a wild card.
     * Currently just selects a random color, but could be enhanced with
     * more strategic considerations.
     * 
     * @param playerHand The AI player's hand
     * @return The selected color
     */
    public CardColor makeWildCardDecision(List<Card> playerHand) {
        // Count cards of each color to make strategic decision
        int redCount = 0, blueCount = 0, greenCount = 0, yellowCount = 0;
        
        for (Card card : playerHand) {
            switch (card.getColor()) {
                case RED:
                    redCount++;
                    break;
                case BLUE:
                    blueCount++;
                    break;
                case GREEN:
                    greenCount++;
                    break;
                case YELLOW:
                    yellowCount++;
                    break;
                default:
                    // Ignore wild cards and other special colors
                    break;
            }
        }
        
        // Select the color with the most cards
        if (redCount >= blueCount && redCount >= greenCount && redCount >= yellowCount) {
            return CardColor.RED;
        } else if (blueCount >= redCount && blueCount >= greenCount && blueCount >= yellowCount) {
            return CardColor.BLUE;
        } else if (greenCount >= redCount && greenCount >= blueCount && greenCount >= yellowCount) {
            return CardColor.GREEN;
        } else {
            return CardColor.YELLOW;
        }
    }
    
    /**
     * Determines whether AI should play card or draw.
     * 
     * @param playerHand The AI player's hand
     * @return true if the AI should draw a card, false if it should play a card
     */
    public boolean shouldDraw(List<Card> playerHand) {
        boolean hasPlayableCards = false;
        int playableCardCount = 0;
        
        System.out.println("AI checking hand for playable cards:");
        for (Card card : playerHand) {
            if (card.isPlayable()) {
                playableCardCount++;
                hasPlayableCards = true;
                System.out.println("  Playable card: " + card);
            } else {
                System.out.println("  Unplayable card: " + card);
            }
        }
        
        // Log detailed information about the decision
        if (!hasPlayableCards) {
            System.out.println("AI has no playable cards (" + playableCardCount + " of " + playerHand.size() + "), should draw");
        } else {
            System.out.println("AI has " + playableCardCount + " playable cards out of " + playerHand.size() + " total, should play");
        }
        
        return !hasPlayableCards;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isThinking() {
        return isThinking;
    }
} 