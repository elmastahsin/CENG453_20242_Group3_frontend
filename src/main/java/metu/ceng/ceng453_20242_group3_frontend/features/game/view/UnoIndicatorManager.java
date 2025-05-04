package metu.ceng.ceng453_20242_group3_frontend.features.game.view;

import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.List;

import metu.ceng.ceng453_20242_group3_frontend.features.game.model.Player;

/**
 * Manages UNO indicators for player name labels.
 */
public class UnoIndicatorManager {
    
    // Player name labels
    private final Label topPlayerNameLabel;
    private final Label leftPlayerNameLabel;
    private final Label rightPlayerNameLabel;
    private final Label bottomPlayerNameLabel;
    
    /**
     * Creates a new UNO indicator manager.
     * 
     * @param topPlayerNameLabel The top player's name label
     * @param leftPlayerNameLabel The left player's name label
     * @param rightPlayerNameLabel The right player's name label
     * @param bottomPlayerNameLabel The bottom player's name label
     */
    public UnoIndicatorManager(
            Label topPlayerNameLabel,
            Label leftPlayerNameLabel,
            Label rightPlayerNameLabel,
            Label bottomPlayerNameLabel) {
        this.topPlayerNameLabel = topPlayerNameLabel;
        this.leftPlayerNameLabel = leftPlayerNameLabel;
        this.rightPlayerNameLabel = rightPlayerNameLabel;
        this.bottomPlayerNameLabel = bottomPlayerNameLabel;
    }
    
    /**
     * Updates UNO indicators for all players based on their UNO status.
     * 
     * @param players The list of players
     */
    public void updateUnoIndicators(List<Player> players) {
        // Remove any existing UNO indicators first
        removeExistingUnoIndicators();
        
        // Check each player's UNO status
        int totalPlayers = players.size();
        
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            
            // Update the player's UNO indicator status first
            player.updateUnoIndicator();
            
            // Show UNO indicator if player has indicator enabled
            if (player.shouldShowUnoIndicator()) {
                if (i == 0) {
                    // Human player (always at bottom)
                    addUnoIndicatorToLabel(bottomPlayerNameLabel);
                } else if (totalPlayers == 2) {
                    // 2 players: human at bottom, AI at top
                    if (i == 1) {
                        addUnoIndicatorToLabel(topPlayerNameLabel);
                    }
                } else if (totalPlayers == 3) {
                    // 3 players: human at bottom, opponent 1 at right, opponent 2 at top
                    if (i == 1) {
                        addUnoIndicatorToLabel(rightPlayerNameLabel);
                    } else if (i == 2) {
                        addUnoIndicatorToLabel(topPlayerNameLabel);
                    }
                } else if (totalPlayers == 4) {
                    // 4 players: human at bottom, opponent 1 at right, opponent 2 at top, opponent 3 at left
                    if (i == 1) {
                        addUnoIndicatorToLabel(rightPlayerNameLabel);
                    } else if (i == 2) {
                        addUnoIndicatorToLabel(topPlayerNameLabel);
                    } else if (i == 3) {
                        addUnoIndicatorToLabel(leftPlayerNameLabel);
                    }
                }
            }
        }
    }
    
    /**
     * Removes any existing UNO indicator badges from all player name labels.
     */
    public void removeExistingUnoIndicators() {
        // Remove graphics from all name labels
        for (Label nameLabel : new Label[]{bottomPlayerNameLabel, topPlayerNameLabel, leftPlayerNameLabel, rightPlayerNameLabel}) {
            if (nameLabel != null) {
                // Remove the UNO indicator graphic
                nameLabel.setGraphic(null);
                nameLabel.setGraphicTextGap(0);
                nameLabel.setContentDisplay(ContentDisplay.LEFT);
            }
        }
    }
    
    /**
     * Adds a UNO indicator badge DIRECTLY INSIDE the player name label.
     * 
     * @param nameLabel The name label to add the indicator to
     */
    private void addUnoIndicatorToLabel(Label nameLabel) {
        if (nameLabel == null) return;
        
        // Create the UNO indicator as a StackPane
        StackPane indicator = new StackPane();
        indicator.setMaxWidth(30);
        indicator.setMaxHeight(30);
        indicator.setPrefWidth(30);
        indicator.setPrefHeight(30);
        
        // Create the background circle
        Circle badge = new Circle(15);
        badge.setFill(Color.rgb(227, 35, 45)); // UNO red
        badge.setStroke(Color.WHITE);
        badge.setStrokeWidth(2);
        
        // Create the "UNO" text
        Label unoText = new Label("UNO");
        unoText.setTextFill(Color.WHITE);
        unoText.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
        
        // Add components to the indicator
        indicator.getChildren().addAll(badge, unoText);
        
        // Add pulsing animation
        ScaleTransition pulse = new ScaleTransition(Duration.millis(800), indicator);
        pulse.setFromX(0.8);
        pulse.setFromY(0.8);
        pulse.setToX(1.2);
        pulse.setToY(1.2);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        
        // Set the indicator as the graphic of the name label
        // Position depends on whether it's a side player or top/bottom player
        if (nameLabel == leftPlayerNameLabel) {
            nameLabel.setGraphic(indicator);
            nameLabel.setGraphicTextGap(10);
            nameLabel.setContentDisplay(ContentDisplay.RIGHT); // Add graphic to the right of text
        } else if (nameLabel == rightPlayerNameLabel) {
            nameLabel.setGraphic(indicator);
            nameLabel.setGraphicTextGap(10);
            nameLabel.setContentDisplay(ContentDisplay.LEFT); // Add graphic to the left of text
        } else { // Top or bottom player
            nameLabel.setGraphic(indicator);
            nameLabel.setGraphicTextGap(10);
            nameLabel.setContentDisplay(ContentDisplay.RIGHT); // Add graphic to the right of text
        }
        
        // Start the animation
        pulse.play();
    }
    
    /**
     * Shows UNO indicators for all positions for testing purposes.
     */
    public void showTestUnoIndicators() {
        // First remove any existing indicators
        removeExistingUnoIndicators();
        
        // Then add indicators for all player positions
        addUnoIndicatorToLabel(bottomPlayerNameLabel);
        addUnoIndicatorToLabel(topPlayerNameLabel);
        addUnoIndicatorToLabel(leftPlayerNameLabel);
        addUnoIndicatorToLabel(rightPlayerNameLabel);
    }
} 