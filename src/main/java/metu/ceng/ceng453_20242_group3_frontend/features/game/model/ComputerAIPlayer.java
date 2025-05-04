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
     * Finds the best card for the AI to play from its hand.
     * Current implementation simply returns the first playable card.
     * 
     * @param playerHand The AI player's hand
     * @param currentColor The current game color
     * @return The card to play, or null if no playable card
     */
    public Card selectCardToPlay(List<Card> playerHand, CardColor currentColor) {
        // For WILD_DRAW_FOUR, we need to verify the player DOESN'T have matching color cards
        // This is an extra precaution on top of the game's isPlayable logic
        
        // First look for non-wild cards matching current color
        boolean hasMatchingColorCard = false;
        for (Card card : playerHand) {
            if (card.getColor() == currentColor && card.getColor() != CardColor.MULTI) {
                hasMatchingColorCard = true;
                break;
            }
        }
        
        // First try to play a non-wild card that matches the current color
        for (Card card : playerHand) {
            if (card.isPlayable() && !card.isWildCard() && card.getColor() == currentColor) {
                return card;
            }
        }
        
        // Then try to play a number card that matches the top card's number
        for (Card card : playerHand) {
            if (card.isPlayable() && card.isNumberCard()) {
                return card;
            }
        }
        
        // Then try action cards
        for (Card card : playerHand) {
            if (card.isPlayable() && card.isActionCard() && !card.isWildCard()) {
                return card;
            }
        }
        
        // Try regular wild cards
        for (Card card : playerHand) {
            if (card.isPlayable() && card.getAction() == CardAction.WILD) {
                return card;
            }
        }
        
        // Only try WILD_DRAW_FOUR if we don't have matching color cards
        if (!hasMatchingColorCard) {
            for (Card card : playerHand) {
                if (card.isPlayable() && card.getAction() == CardAction.WILD_DRAW_FOUR) {
                    return card;
                }
            }
        } else {
            System.out.println("AI has matching color cards, skipping WILD_DRAW_FOUR");
        }
        
        // No playable card
        return null;
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
        boolean hasUnplayableWildDrawFour = false;
        
        for (Card card : playerHand) {
            if (card.isPlayable()) {
                hasPlayableCards = true;
            }
            
            // Also check specifically for WILD_DRAW_FOUR that might be incorrectly marked as playable
            if (card.getAction() == CardAction.WILD_DRAW_FOUR && !card.isPlayable()) {
                hasUnplayableWildDrawFour = true;
            }
        }
        
        // Log detailed information about the decision
        if (!hasPlayableCards) {
            System.out.println("AI has no playable cards, should draw");
        } else if (hasUnplayableWildDrawFour) {
            System.out.println("AI has playable cards but also has unplayable WILD_DRAW_FOUR, checking carefully");
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