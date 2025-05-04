package metu.ceng.ceng453_20242_group3_frontend.features.game.controller;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
     * @param game                The game model
     * @param notificationManager The notification manager
     * @param gameTableController The game table controller
     * @param cardPlayCallback    Callback for card play events
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
        if (game == null || !game.isGameStarted() || game.isGameEnded()) {
            return;
        }
        
        // Debug information
        System.out.println("Checking for AI turns, current player index: " + game.getCurrentPlayerIndex());
        
        // If it's AI's turn, trigger AI move with a delay
        int currentPlayerIndex = game.getCurrentPlayerIndex();
        Player currentPlayer = game.getCurrentPlayer();
        
        if (currentPlayerIndex > 0 && currentPlayer != null && currentPlayer.isAI()) {
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
     * Implements a simple AI turn with basic decision-making
     * 
     * @param aiIndex The index of the AI player taking the turn
     */
    private void simpleAITurn(int aiIndex) {
        Player aiPlayer = game.getPlayerByIndex(aiIndex);
        if (aiPlayer == null) {
            System.out.println("Invalid AI player index: " + aiIndex);
            return;
        }
        
        ComputerAIPlayer aiInstance = aiPlayers.get(aiIndex - 1);
        
        // Check if it's still AI's turn
        if (game.getCurrentPlayerIndex() != aiIndex) {
            System.out.println("Not AI's turn anymore, skipping turn for AI " + aiIndex);
            return;
        }
        
        // Make sure playable cards are up-to-date
        game.updatePlayableCards();
        
        // Check if AI should draw or play
        if (aiInstance.shouldDraw(aiPlayer.getHand())) {
            System.out.println("AI has no playable cards, will draw a card");
            
            PauseTransition pause = new PauseTransition(Duration.millis(2000));
            pause.setOnFinished(e -> {
                // Draw a card without advancing turn
                Card drawnCard = game.drawCardWithoutAdvancingTurn();
                System.out.println("AI drew: " + (drawnCard != null ? drawnCard.toString() : "null"));
                
                // Handle the drawn card
                handleAIDrawnCard(aiIndex, drawnCard);
            });
            pause.play();
            return;
        }
        
        // Find a playable card
        CardColor currentColor = game.getCurrentColor();
        Card cardToPlay = selectCardToPlay(aiPlayer.getHand(), currentColor);

        if (cardToPlay != null) {
            System.out.println("AI found a playable card: " + cardToPlay);
            // Play the card
            final Card selectedCard = cardToPlay;
            PauseTransition pause = new PauseTransition(Duration.millis(2000));
            pause.setOnFinished(e -> playAICard(aiIndex, selectedCard));
            pause.play();
        } else {
            // No playable card found, draw instead
            System.out.println("AI found no playable cards, drawing a card...");
            
            // Draw a card without advancing turn
            PauseTransition pause = new PauseTransition(Duration.millis(2000));
            pause.setOnFinished(e -> {
                Card drawnCard = game.drawCardWithoutAdvancingTurn();
                System.out.println("AI drew: " + (drawnCard != null ? drawnCard.toString() : "null"));
                
                // Handle the drawn card
                handleAIDrawnCard(aiIndex, drawnCard);
            });
            pause.play();
        }
    }

    /**
     * Plays a card for an AI player.
     * 
     * @param aiIndex The index of the AI player
     * @param card    The card to play
     */
    private void playAICard(int aiIndex, Card card) {
        Player aiPlayer = game.getPlayerByIndex(aiIndex);
        if (aiPlayer == null) {
            System.out.println("Invalid AI player index: " + aiIndex);
            return;
        }
        
        // Make sure it's still this AI's turn
        if (game.getCurrentPlayerIndex() != aiIndex) {
            System.out.println("Not AI player's turn anymore, skipping card play");
            return;
        }
        
        // Double-check the card is playable
        if (!card.isPlayable()) {
            System.out.println("ERROR: Attempting to play an unplayable card: " + card + ". Drawing instead.");
            
            // Draw a card instead
            PauseTransition pause = new PauseTransition(Duration.millis(2000));
            pause.setOnFinished(e -> {
                Card drawnCard = game.drawCardWithoutAdvancingTurn();
                System.out.println("AI drew: " + (drawnCard != null ? drawnCard.toString() : "null"));
                
                // Handle the drawn card
                handleAIDrawnCard(aiIndex, drawnCard);
            });
            pause.play();
            return;
        }
        
        // For WILD cards, select a color before playing
        if (card.isWildCard()) {
            // Select a color based on AI's cards
            ComputerAIPlayer aiInstance = aiPlayers.get(aiIndex - 1);
            CardColor selectedColor = aiInstance.makeWildCardDecision(aiPlayer.getHand());
            System.out.println("AI selected color for " + card.getAction() + ": " + selectedColor);
            
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
            if (card.isWildCard()) {
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
            
            // For DRAW_TWO and WILD_DRAW_FOUR cards, force an immediate callback update
            // to ensure the UI gets updated to show the drawn cards
            if (card.getAction() == CardAction.DRAW_TWO || card.getAction() == CardAction.WILD_DRAW_FOUR) {
                // Additional delay to ensure cards are drawn before update
                PauseTransition updateDelay = new PauseTransition(Duration.millis(500));
                updateDelay.setOnFinished(e -> cardPlayCallback.onCardPlayed(aiIndex, card));
                updateDelay.play();
            } else {
                // Notify card played immediately for other card types
                cardPlayCallback.onCardPlayed(aiIndex, card);
            }
            
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
                Card drawnCard = game.drawCardWithoutAdvancingTurn();
                System.out.println("AI drew: " + (drawnCard != null ? drawnCard.toString() : "null"));
                
                // Handle the drawn card
                handleAIDrawnCard(aiIndex, drawnCard);
            });
            pause.play();
        }
    }

    /**
     * Handles a card drawn by an AI player, checking if it's playable and playing
     * it if possible.
     * 
     * @param aiIndex   The index of the AI player
     * @param drawnCard The card that was drawn
     */
    private void handleAIDrawnCard(int aiIndex, Card drawnCard) {
        Player aiPlayer = game.getPlayerByIndex(aiIndex);
        if (aiPlayer == null) {
            System.out.println("Invalid AI player index: " + aiIndex);
            return;
        }
        
        // Check if the drawn card is playable
        if (drawnCard != null && drawnCard.isPlayable()) {
            System.out.println("AI drew a playable card: " + drawnCard + ". Playing it now.");
            
            // Update the UI immediately to show the drawn card
            cardPlayCallback.onCardDrawn();
            
            // Show a special notification for drawn playable card using a highlighted style
            notificationManager.showActionNotification(aiPlayer.getName(), "drew a card and can play it");
            
            // Use a longer delay before playing the drawn card so players can clearly see what was drawn
            PauseTransition pause = new PauseTransition(Duration.millis(3500));
            pause.setOnFinished(e -> {
                // Show another notification when the AI actually plays the card
                notificationManager.showActionNotification(aiPlayer.getName(), "plays drawn card: " + drawnCard.toString());
                
                // Add a small delay after the notification before playing the card
                PauseTransition playDelay = new PauseTransition(Duration.millis(1000));
                playDelay.setOnFinished(event -> playAICard(aiIndex, drawnCard));
                playDelay.play();
            });
            pause.play();
        } else {
            System.out.println("AI drew a card that is not playable. Passing turn.");
            
            // Show notification about drawing a non-playable card
            notificationManager.showActionNotification(aiPlayer.getName(), "drew a card and passed");
            
            // Advance to the next player's turn
            game.advanceTurnAfterDraw();
            
            // Notify card drawn
            cardPlayCallback.onCardDrawn();
            
            // Continue game flow
            if (game.getCurrentPlayer().isAI()) {
                handleAITurns();
            } else {
                gameTableController.updatePlayerAreaAnimations(game);
            }
        }
    }

    /**
     * Finds a card for the AI to play from its hand.
     * Randomly selects from the available playable cards.
     * 
     * @param playerHand   The AI player's hand
     * @param currentColor The current game color
     * @return The card to play, or null if no playable card
     */
    public Card selectCardToPlay(List<Card> playerHand, CardColor currentColor) {
        // First collect all playable cards
        List<Card> playableCards = new ArrayList<>();
        
        for (Card card : playerHand) {
            if (card.isPlayable()) {
                playableCards.add(card);
                System.out.println("AI found playable card: " + card);
            }
        }
        
        // If no playable cards, return null
        if (playableCards.isEmpty()) {
            System.out.println("AI couldn't find any playable cards");
            return null;
        }
        
        // Select a random card from the playable cards
        Random random = new Random();
        int randomIndex = random.nextInt(playableCards.size());
        Card selectedCard = playableCards.get(randomIndex);
        
        System.out.println("AI randomly selected: " + selectedCard + " from " + playableCards.size() + " playable cards");
        return selectedCard;
    }
}