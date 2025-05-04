package metu.ceng.ceng453_20242_group3_frontend.features.game.controller;

import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import metu.ceng.ceng453_20242_group3_frontend.features.game.model.Card;
import metu.ceng.ceng453_20242_group3_frontend.features.game.view.CardRenderer;

/**
 * Handles card animations during gameplay.
 */
public class CardAnimationController {
    
    private final Pane gamePane;
    private final StackPane discardPileContainer;
    
    /**
     * Interface for animation completion callbacks
     */
    public interface AnimationCallback {
        void onAnimationComplete();
    }
    
    /**
     * Creates a new card animation controller.
     * 
     * @param gamePane The main game pane
     * @param discardPileContainer The discard pile container
     */
    public CardAnimationController(Pane gamePane, StackPane discardPileContainer) {
        this.gamePane = gamePane;
        this.discardPileContainer = discardPileContainer;
    }
    
    /**
     * Creates a card view at the specified position and animates it to the discard pile.
     * 
     * @param card The card model to animate
     * @param sourceX The starting X position
     * @param sourceY The starting Y position
     * @param callback Callback to run when animation completes
     */
    public void animateCardFromPosition(Card card, double sourceX, double sourceY, AnimationCallback callback) {
        // Create card view
        StackPane cardView = CardRenderer.createCardView(card);
        
        // Add to game pane for animation
        gamePane.getChildren().add(cardView);
        
        // Convert from scene coordinates to gamePane coordinates
        Point2D cardPosInGamePane = gamePane.sceneToLocal(sourceX, sourceY);
        
        // Position at the starting location
        cardView.setLayoutX(cardPosInGamePane.getX());
        cardView.setLayoutY(cardPosInGamePane.getY());
        
        // Animate to discard pile
        animateCardToDiscardPile(cardView, callback);
    }
    
    /**
     * Animates a card from its current position to the discard pile.
     * 
     * @param sourceNode The source node to get position from
     * @param card The card model to animate
     * @param callback Callback to run when animation completes
     */
    public void animateCardFromNode(Pane sourceNode, Card card, AnimationCallback callback) {
        double originalX = 0;
        double originalY = 0;
        
        try {
            // Convert the source node's position to scene coordinates
            Bounds nodeBounds = sourceNode.localToScene(sourceNode.getBoundsInLocal());
            originalX = nodeBounds.getMinX();
            originalY = nodeBounds.getMinY();
        } catch (Exception e) {
            System.out.println("Warning: Could not get original node position");
        }
        
        // Animate from the obtained position
        animateCardFromPosition(card, originalX, originalY, callback);
    }
    
    /**
     * Animates a card moving to the discard pile.
     * 
     * @param cardView The card view to animate
     * @param callback Callback to run when animation completes
     */
    private void animateCardToDiscardPile(StackPane cardView, AnimationCallback callback) {
        // Calculate the target position (discard pile's center)
        // Convert discard pile position to scene coordinates
        Bounds discardBounds = discardPileContainer.localToScene(discardPileContainer.getBoundsInLocal());
        double discardCenterX = discardBounds.getMinX() + discardBounds.getWidth()/2;
        double discardCenterY = discardBounds.getMinY() + discardBounds.getHeight()/2;
        
        // Convert from scene coordinates to gamePane coordinates
        Point2D targetPoint = gamePane.sceneToLocal(discardCenterX, discardCenterY);
        
        // Calculate the offset needed to center the card
        double cardWidth = cardView.getBoundsInLocal().getWidth();
        double cardHeight = cardView.getBoundsInLocal().getHeight();
        
        double targetX = targetPoint.getX() - cardWidth/2;
        double targetY = targetPoint.getY() - cardHeight/2;
        
        // Create the animations
        TranslateTransition moveCard = new TranslateTransition(Duration.millis(300), cardView);
        moveCard.setToX(targetX - cardView.getLayoutX());
        moveCard.setToY(targetY - cardView.getLayoutY());
        
        RotateTransition rotateCard = new RotateTransition(Duration.millis(300), cardView);
        rotateCard.setToAngle(-5 + (Math.random() * 10)); // Random angle for natural look
        
        // Play both animations in parallel
        ParallelTransition animation = new ParallelTransition(moveCard, rotateCard);
        
        // When animation completes, update the UI
        animation.setOnFinished(e -> {
            // Remove the animated card
            gamePane.getChildren().remove(cardView);
            
            // Execute callback
            if (callback != null) {
                callback.onAnimationComplete();
            }
        });
        
        // Start the animation
        animation.play();
    }
} 