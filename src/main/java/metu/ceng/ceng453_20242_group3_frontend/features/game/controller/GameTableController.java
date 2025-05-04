package metu.ceng.ceng453_20242_group3_frontend.features.game.controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.List;

import metu.ceng.ceng453_20242_group3_frontend.features.game.model.Game;

/**
 * Handles game table animations and visual effects.
 */
public class GameTableController {
    
    private final HBox leftPlayerArea;
    private final HBox rightPlayerArea;
    private final VBox topPlayerArea;
    private final VBox bottomPlayerArea;
    private final Pane gamePane;
    
    // Store player area animations for control
    private Timeline[] playerAnimations;
    
    // Add a field to store the game table animation
    private Timeline gameTableAnimation;
    
    /**
     * Creates a new game table controller.
     * 
     * @param gamePane The game pane
     * @param topPlayerArea The top player area
     * @param leftPlayerArea The left player area
     * @param rightPlayerArea The right player area
     * @param bottomPlayerArea The bottom player area
     */
    public GameTableController(Pane gamePane, VBox topPlayerArea, HBox leftPlayerArea, 
                               HBox rightPlayerArea, VBox bottomPlayerArea) {
        this.gamePane = gamePane;
        this.topPlayerArea = topPlayerArea;
        this.leftPlayerArea = leftPlayerArea;
        this.rightPlayerArea = rightPlayerArea;
        this.bottomPlayerArea = bottomPlayerArea;
        
        // Initialize animations
        setupGameTableAnimations();
    }
    
    /**
     * Sets up subtle animations for the game players areas
     */
    private void setupGameTableAnimations() {
        // Create animation timelines for each player area
        Timeline bottomPlayerAnimation = createPulseAnimation(bottomPlayerArea);
        Timeline topPlayerAnimation = createPulseAnimation(topPlayerArea);
        Timeline leftPlayerAnimation = createPulseAnimation(leftPlayerArea);
        Timeline rightPlayerAnimation = createPulseAnimation(rightPlayerArea);
        
        // Store animations for later control
        playerAnimations = new Timeline[] {
            bottomPlayerAnimation, topPlayerAnimation, leftPlayerAnimation, rightPlayerAnimation
        };
        
        // Find the game table element
        Node gameTable = findGameTable();
        
        // Create the table animation if the table was found
        if (gameTable != null) {
            // Use the same animation timing as player animations for sync
            gameTableAnimation = createPulseAnimation(gameTable);
            gameTableAnimation.pause(); // Start paused
        }
    }
    
    /**
     * Finds the game table element in the scene graph
     * 
     * @return The game table node or null if not found
     */
    private Node findGameTable() {
        // Look through the StackPane in the center of the grid
        for (Node node : gamePane.getChildrenUnmodifiable()) {
            if (node instanceof GridPane) {
                GridPane gridPane = (GridPane) node;
                
                for (Node child : gridPane.getChildren()) {
                    // Find the center stack pane
                    Integer colIndex = GridPane.getColumnIndex(child);
                    Integer rowIndex = GridPane.getRowIndex(child);
                    
                    if (colIndex != null && rowIndex != null && colIndex == 1 && rowIndex == 1) {
                        if (child instanceof StackPane) {
                            StackPane centerPane = (StackPane) child;
                            
                            // Find the game table inside the stack pane
                            for (Node tableCandidate : centerPane.getChildren()) {
                                if (tableCandidate instanceof StackPane &&
                                    tableCandidate.getStyleClass().contains("game-table")) {
                                    return tableCandidate;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Creates a pulsing animation for a node
     * 
     * @param node The node to animate
     * @return The animation timeline
     */
    private Timeline createPulseAnimation(Node node) {
        // Gold color for the glow effect
        Color glowColor = Color.rgb(255, 215, 0, 0.7);
        
        // Create a pulsing glow effect with consistent timing for all elements
        Timeline pulseAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(
                    node.effectProperty(),
                    new DropShadow(10, Color.rgb(0, 0, 0, 0.7))
                )
            ),
            new KeyFrame(Duration.seconds(1.0), 
                new KeyValue(
                    node.effectProperty(),
                    new DropShadow(20, glowColor)
                )
            ),
            new KeyFrame(Duration.seconds(2.0), 
                new KeyValue(
                    node.effectProperty(),
                    new DropShadow(10, Color.rgb(0, 0, 0, 0.7))
                )
            )
        );
        
        pulseAnimation.setCycleCount(javafx.animation.Animation.INDEFINITE);
        
        // Make sure the animation is stopped initially but not played
        // This prevents a common JavaFX issue where animations get out of sync
        pulseAnimation.stop();
        
        return pulseAnimation;
    }
    
    /**
     * Updates which player area is pulsing based on the current player.
     */
    public void updatePlayerAreaAnimations(Game game) {
        if (playerAnimations == null || game == null) {
            return;
        }
        
        // Stop all animations
        for (Timeline animation : playerAnimations) {
            animation.stop(); // Use stop() instead of pause() to fully reset
        }
        
        // Stop game table animation and reset it properly
        if (gameTableAnimation != null) {
            gameTableAnimation.stop(); // Use stop() instead of pause() to fully reset
        }
        
        // Set basic shadow for all player areas to reset their appearance
        bottomPlayerArea.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.7)));
        topPlayerArea.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.7)));
        leftPlayerArea.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.7)));
        rightPlayerArea.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.7)));
        
        // Find the game table element and reset its appearance too
        Node gameTable = findGameTable();
        if (gameTable != null) {
            gameTable.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.7)));
        }
        
        // Determine which player area to animate based on player index and total players
        int currentPlayerIndex = game.getCurrentPlayerIndex();
        int totalPlayers = game.getPlayers().size();
        
        if (currentPlayerIndex == 0) {
            // Human player (always at bottom)
            playerAnimations[0].playFromStart(); // Use playFromStart() to ensure proper reset
            
            // Also pulse the game table for human player, properly synchronized
            if (gameTableAnimation != null) {
                gameTableAnimation.playFromStart(); // Use playFromStart() for proper synchronization
            }
        } else {
            // AI player - which area depends on position in the new layout
            if (totalPlayers == 2) {
                // 2 players: human at bottom, AI at top
                if (currentPlayerIndex == 1) {
                    playerAnimations[1].playFromStart(); // Top player
                }
            } else if (totalPlayers == 3) {
                // 3 players: human, right, top
                if (currentPlayerIndex == 1) {
                    playerAnimations[3].playFromStart(); // Right player (Opponent 1)
                } else if (currentPlayerIndex == 2) {
                    playerAnimations[1].playFromStart(); // Top player (Opponent 2)
                }
            } else if (totalPlayers == 4) {
                // 4 players: human, right, top, left
                if (currentPlayerIndex == 1) {
                    playerAnimations[3].playFromStart(); // Right player (Opponent 1)
                } else if (currentPlayerIndex == 2) {
                    playerAnimations[1].playFromStart(); // Top player (Opponent 2)
                } else if (currentPlayerIndex == 3) {
                    playerAnimations[2].playFromStart(); // Left player (Opponent 3)
                }
            }
        }
    }
    
    /**
     * Sets up the player areas based on total player count.
     * 
     * @param game The game instance
     * @param aiPlayerNames Names of AI players
     */
    public void setupPlayerAreas(Game game, List<String> aiPlayerNames) {
        int playerCount = game.getPlayers().size();
        
        // Configure visibility based on player count
        switch (playerCount) {
            case 2: // 2 players: bottom and top
                topPlayerArea.setVisible(true);
                leftPlayerArea.setVisible(false);
                rightPlayerArea.setVisible(false);
                
                if (!aiPlayerNames.isEmpty()) {
                    ((javafx.scene.control.Label)topPlayerArea.lookup(".player-name-label")).setText(aiPlayerNames.get(0));
                } else {
                    ((javafx.scene.control.Label)topPlayerArea.lookup(".player-name-label")).setText("Opponent 1");
                }
                break;
                
            case 3: // 3 players: bottom, right (Opponent 1), top (Opponent 2)
                topPlayerArea.setVisible(true);
                rightPlayerArea.setVisible(true);
                leftPlayerArea.setVisible(false);
                
                if (aiPlayerNames.size() >= 2) {
                    // Player 1 at right, Player 2 at top (counterclockwise from player)
                    ((javafx.scene.control.Label)rightPlayerArea.lookup(".player-name-label")).setText(aiPlayerNames.get(0));
                    ((javafx.scene.control.Label)topPlayerArea.lookup(".player-name-label")).setText(aiPlayerNames.get(1));
                } else {
                    ((javafx.scene.control.Label)rightPlayerArea.lookup(".player-name-label")).setText("Opponent 1");
                    ((javafx.scene.control.Label)topPlayerArea.lookup(".player-name-label")).setText("Opponent 2");
                }
                break;
                
            case 4: // 4 players: bottom, right (Opp1), top (Opp2), left (Opp3) - counterclockwise
                topPlayerArea.setVisible(true);
                leftPlayerArea.setVisible(true);
                rightPlayerArea.setVisible(true);
                
                if (aiPlayerNames.size() >= 3) {
                    // Player 1 at right, Player 2 at top, Player 3 at left (counterclockwise from player)
                    ((javafx.scene.control.Label)rightPlayerArea.lookup(".player-name-label")).setText(aiPlayerNames.get(0));
                    ((javafx.scene.control.Label)topPlayerArea.lookup(".player-name-label")).setText(aiPlayerNames.get(1));
                    ((javafx.scene.control.Label)leftPlayerArea.lookup(".player-name-label")).setText(aiPlayerNames.get(2));
                } else {
                    ((javafx.scene.control.Label)rightPlayerArea.lookup(".player-name-label")).setText("Opponent 1");
                    ((javafx.scene.control.Label)topPlayerArea.lookup(".player-name-label")).setText("Opponent 2");
                    ((javafx.scene.control.Label)leftPlayerArea.lookup(".player-name-label")).setText("Opponent 3");
                }
                break;
                
            default: // Default to 2 players if invalid count
                topPlayerArea.setVisible(true);
                leftPlayerArea.setVisible(false);
                rightPlayerArea.setVisible(false);
                
                ((javafx.scene.control.Label)topPlayerArea.lookup(".player-name-label")).setText("Opponent 1");
                break;
        }
        
        // For grid layout - must keep areas managed but transparent
        leftPlayerArea.setManaged(true);
        rightPlayerArea.setManaged(true);
        
        // Make invisible areas transparent instead of completely hiding them
        if (!leftPlayerArea.isVisible()) {
            leftPlayerArea.setOpacity(0);
            ((javafx.scene.control.Label)leftPlayerArea.lookup(".player-name-label")).setOpacity(0);
        } else {
            leftPlayerArea.setOpacity(1);
            ((javafx.scene.control.Label)leftPlayerArea.lookup(".player-name-label")).setOpacity(1);
        }
        
        if (!rightPlayerArea.isVisible()) {
            rightPlayerArea.setOpacity(0);
            ((javafx.scene.control.Label)rightPlayerArea.lookup(".player-name-label")).setOpacity(0);
        } else {
            rightPlayerArea.setOpacity(1);
            ((javafx.scene.control.Label)rightPlayerArea.lookup(".player-name-label")).setOpacity(1);
        }
        
        // Set initial game direction to counter-clockwise
        game.setDirection(metu.ceng.ceng453_20242_group3_frontend.features.game.model.Direction.COUNTER_CLOCKWISE);
    }
} 