package metu.ceng.ceng453_20242_group3_frontend.features.game.controller;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import metu.ceng.ceng453_20242_group3_frontend.features.game.model.Card;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.CardAction;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.CardColor;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.ComputerAIPlayer;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.Game;
import metu.ceng.ceng453_20242_group3_frontend.features.game.model.Player;
import metu.ceng.ceng453_20242_group3_frontend.features.game.view.NotificationManager;

/**
 * Controller for AI player actions.
 */
public class AIPlayerController {
    
    private final List<ComputerAIPlayer> aiPlayers = new ArrayList<>();
    private final Game game;
    private final NotificationManager notificationManager;
    private final GameTableController gameTableController;
    
    /**
     * Interface for handling card playing callbacks
     */
    public interface CardPlayCallback {
        void onCardPlayed(int aiIndex, Card card);
        void onCardDrawn();
        void onGameEnd(boolean isAIWinner);
    }
    
    private final CardPlayCallback cardPlayCallback;
    
    /**
     * Creates a new AI player controller.
     * 
     * @param game The game model
     * @param notificationManager The notification manager
     * @param gameTableController The game table controller
     * @param cardPlayCallback Callback for card play events
     */
    public AIPlayerController(Game game, NotificationManager notificationManager, 
                              GameTableController gameTableController, CardPlayCallback cardPlayCallback) {
        this.game = game;
        this.notificationManager = notificationManager;
        this.gameTableController = gameTableController;
        this.cardPlayCallback = cardPlayCallback;
        
        // Initialize AI players based on the game model
        initializeAIPlayers();
    }
    
    /**
     * Initialize AI players based on game state.
     */
    private void initializeAIPlayers() {
        aiPlayers.clear();
        
        // Skip the first player (human player)
        List<Player> players = game.getPlayers();
        for (int i = 1; i < players.size(); i++) {
            Player player = players.get(i);
            if (player.isAI()) {
                aiPlayers.add(new ComputerAIPlayer(player.getName()));
            }
        }
    }
    
    /**
     * Returns the list of AI players.
     * 
     * @return The list of AI player instances
     */
    public List<ComputerAIPlayer> getAIPlayers() {
        return aiPlayers;
    }
    
    /**
     * Handles AI turns when it's an AI player's turn
     */
    public void handleAITurns() {
        if (game == null) {
            return;
        }
        
        // Debug information
        System.out.println("Checking for AI turns, current player index: " + game.getCurrentPlayerIndex());
        
        // If it's AI's turn, trigger AI move with a small delay
        int currentPlayerIndex = game.getCurrentPlayerIndex();
        if (currentPlayerIndex > 0) { // Player at index 0 is always human
            // Create a delay so AI doesn't play immediately
            PauseTransition pause = new PauseTransition(Duration.millis(5000));
            pause.setOnFinished(e -> {
                int aiIndex = currentPlayerIndex;
                // Make sure it's still the same AI's turn after the delay
                if (game.getCurrentPlayerIndex() == aiIndex) {
                    System.out.println("AI player " + aiIndex + " is taking their turn");
                    simpleAITurn(aiIndex);
                }
            });
            pause.play();
        }
    }
    
    /**
     * Simulates a simple AI turn.
     *
     * @param aiIndex The index of the AI player in the game's player list
     */
    private void simpleAITurn(int aiIndex) {
        Player aiPlayer = game.getPlayers().get(aiIndex);
        
        // Make sure it's this AI's turn
        if (!game.getCurrentPlayer().equals(aiPlayer)) {
            System.out.println("Not AI player's turn anymore, skipping AI move");
            return;
        }
        
        System.out.println("AI player " + aiPlayer.getName() + " is checking for playable cards");
        
        // Update which cards are playable
        game.updatePlayableCards();
        
        // Log all playable cards for debugging
        System.out.println("Checking AI hand for playable cards:");
        for (Card c : aiPlayer.getHand()) {
            System.out.println("  Card: " + c + ", Playable: " + c.isPlayable());
        }
        
        // Get the corresponding AI instance
        ComputerAIPlayer aiInstance = aiPlayers.get(aiIndex - 1);
        
        // Check if AI should draw or play
        if (aiInstance.shouldDraw(aiPlayer.getHand())) {
            System.out.println("AI has no playable cards, will draw a card");
            
            // Delay before drawing
            PauseTransition pause = new PauseTransition(Duration.millis(2000));
            pause.setOnFinished(e -> {
                // Draw a card
                Card drawnCard = game.drawCardForCurrentPlayer();
                System.out.println("AI drew: " + (drawnCard != null ? drawnCard.toString() : "null"));
                
                // Notify card drawn
                cardPlayCallback.onCardDrawn();
                
                // If it's now the human player's turn, update playable cards
                if (!game.getCurrentPlayer().isAI()) {
                    // Update game table animations for human player's turn
                    gameTableController.updatePlayerAreaAnimations(game);
                } else {
                    // Otherwise, handle the next AI turn
                    handleAITurns();
                }
            });
            pause.play();
            return;
        }
        
        // Find a playable card using AI's decision-making
        Card cardToPlay = aiInstance.selectCardToPlay(aiPlayer.getHand(), game.getCurrentColor());
        
        if (cardToPlay != null) {
            // Double check this card is actually playable according to game rules
            if (!cardToPlay.isPlayable()) {
                System.out.println("WARNING: AI tried to play a card marked as unplayable: " + cardToPlay);
                
                // Draw a card instead
                PauseTransition pause = new PauseTransition(Duration.millis(2000));
                pause.setOnFinished(e -> {
                    Card drawnCard = game.drawCardForCurrentPlayer();
                    System.out.println("AI drew instead: " + (drawnCard != null ? drawnCard.toString() : "null"));
                    
                    // Notify card drawn
                    cardPlayCallback.onCardDrawn();
                    
                    // Continue game flow
                    if (game.getCurrentPlayer().isAI()) {
                        handleAITurns();
                    } else {
                        gameTableController.updatePlayerAreaAnimations(game);
                    }
                });
                pause.play();
                return;
            }
            
            final Card finalCardToPlay = cardToPlay; // Need final var for lambda
            
            // Handle wild card color selection - IMPORTANT: For Wild Draw Four, we don't select
            // the color until we're sure it can be played
            if (cardToPlay.isWildCard() && cardToPlay.getAction() != CardAction.WILD_DRAW_FOUR) {
                // Select a color based on AI's cards
                CardColor selectedColor = aiInstance.makeWildCardDecision(aiPlayer.getHand());
                System.out.println("AI selected color for wild card: " + selectedColor);
                
                // Set the color in the game
                game.setCurrentColor(selectedColor);
                
                // Small delay before playing the card
                PauseTransition pause = new PauseTransition(Duration.millis(2000));
                pause.setOnFinished(e -> {
                    playAICard(aiIndex, finalCardToPlay);
                });
                pause.play();
            } else {
                // Play card directly after a short delay
                PauseTransition pause = new PauseTransition(Duration.millis(2000));
                pause.setOnFinished(e -> {
                    playAICard(aiIndex, finalCardToPlay);
                });
                pause.play();
            }
        } else {
            // No playable card found, draw instead
            System.out.println("AI found no playable cards, drawing a card...");
            
            PauseTransition pause = new PauseTransition(Duration.millis(2000));
            pause.setOnFinished(e -> {
                Card drawnCard = game.drawCardForCurrentPlayer();
                System.out.println("AI drew: " + (drawnCard != null ? drawnCard.toString() : "null"));
                
                // Notify card drawn
                cardPlayCallback.onCardDrawn();
                
                // Continue game flow
                if (game.getCurrentPlayer().isAI()) {
                    handleAITurns();
                } else {
                    gameTableController.updatePlayerAreaAnimations(game);
                }
            });
            pause.play();
        }
    }
    
    /**
     * Plays a card for an AI player.
     * 
     * @param aiIndex The index of the AI player
     * @param card The card to play
     */
    private void playAICard(int aiIndex, Card card) {
        Player aiPlayer = game.getPlayers().get(aiIndex);
        
        // Make sure it's still this AI's turn
        if (!game.getCurrentPlayer().equals(aiPlayer)) {
            System.out.println("Not AI player's turn anymore, skipping card play");
            return;
        }
        
        // Double-check the card is playable
        if (!card.isPlayable()) {
            System.out.println("ERROR: Attempting to play an unplayable card: " + card + ". Drawing instead.");
            
            // Draw a card instead
            Card drawnCard = game.drawCardForCurrentPlayer();
            
            // Notify card drawn
            cardPlayCallback.onCardDrawn();
            
            // Continue game flow
            if (game.getCurrentPlayer().isAI()) {
                handleAITurns();
            } else {
                gameTableController.updatePlayerAreaAnimations(game);
            }
            return;
        }
        
        // For WILD_DRAW_FOUR, we need to check if it can be legally played before setting a color
        if (card.getAction() == CardAction.WILD_DRAW_FOUR) {
            // Check if the player has any cards matching the current color
            boolean hasMatchingColor = false;
            for (Card playerCard : aiPlayer.getHand()) {
                if (playerCard != card && playerCard.getColor() == game.getCurrentColor() && 
                    playerCard.getColor() != CardColor.MULTI) {
                    hasMatchingColor = true;
                    break;
                }
            }
            
            if (hasMatchingColor) {
                System.out.println("AI tried to play WILD_DRAW_FOUR but has matching color cards. Drawing instead.");
                
                // Draw a card instead
                Card drawnCard = game.drawCardForCurrentPlayer();
                
                // Notify card drawn
                cardPlayCallback.onCardDrawn();
                
                // Continue game flow
                if (game.getCurrentPlayer().isAI()) {
                    handleAITurns();
                } else {
                    gameTableController.updatePlayerAreaAnimations(game);
                }
                return;
            }
            
            // If we get here, we can play the WILD_DRAW_FOUR safely
            // Now select a color AFTER we've verified it's legal to play
            ComputerAIPlayer aiInstance = aiPlayers.get(aiIndex - 1);
            CardColor selectedColor = aiInstance.makeWildCardDecision(aiPlayer.getHand());
            System.out.println("AI selected color for WILD_DRAW_FOUR: " + selectedColor);
            
            // Set the color in the game
            game.setCurrentColor(selectedColor);
        }
        
        int originalCardCount = aiPlayer.getCardCount();
        
        // Get the next player who will be affected by the action
        Player nextPlayer = game.getNextPlayer();
        String nextPlayerName = nextPlayer != null ? nextPlayer.getName() : "Unknown";
        
        // Play the card
        boolean success = game.playCard(card);
        System.out.println("AI played card: " + card + ", success: " + success);
        
        if (success) {
            // Show wild card color notification separately to ensure it's always displayed
            if (card.isWildCard() && game.getCurrentColor() != CardColor.MULTI) {
                notificationManager.showColorSelectionNotification(aiPlayer.getName(), game.getCurrentColor());
            }
            
            // Show action notification if needed
            String actionMessage = notificationManager.getActionMessage(card, aiPlayer.getName(), nextPlayerName);
            if (actionMessage != null) {
                notificationManager.showActionNotification(aiPlayer.getName(), actionMessage);
            }
            
            // Check for UNO declaration when player will have 1 card left
            if (originalCardCount == 2 && aiPlayer.getCardCount() == 1) {
                // AI automatically declares UNO
                aiPlayer.declareUno();
                notificationManager.showUnoCallNotification(aiPlayer.getName());
            }
            
            // Notify card played
            cardPlayCallback.onCardPlayed(aiIndex, card);
            
            // Check for game over
            if (game.isGameEnded()) {
                cardPlayCallback.onGameEnd(true); // AI is winner
                return;
            }
            
            // If there are more AI turns, handle them
            if (game.getCurrentPlayer().isAI()) {
                handleAITurns();
            } else {
                // Update game table animations for human player's turn
                gameTableController.updatePlayerAreaAnimations(game);
            }
        } else {
            System.out.println("AI failed to play card: " + card);
            
            // If play failed, try drawing instead
            PauseTransition pause = new PauseTransition(Duration.millis(2000));
            pause.setOnFinished(e -> {
                Card drawnCard = game.drawCardForCurrentPlayer();
                System.out.println("AI is drawing a card instead: " + drawnCard);
                
                // Notify card drawn
                cardPlayCallback.onCardDrawn();
                
                // Continue game flow
                if (game.getCurrentPlayer().isAI()) {
                    handleAITurns();
                } else {
                    // Update game table animations for human player's turn
                    gameTableController.updatePlayerAreaAnimations(game);
                }
            });
            pause.play();
        }
    }
} 