package metu.ceng.ceng453_20242_group3_frontend.features.game.view;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import metu.ceng.ceng453_20242_group3_frontend.features.game.model.CardColor;

/**
 * A notification component that displays the color selected by an AI player
 * after playing a wild card.
 */
public class ColorNotification {
    
    private final StackPane notificationPane;
    private final SequentialTransition animation;
    
    /**
     * Creates a new color notification.
     * 
     * @param playerName The name of the player who selected the color
     * @param selectedColor The selected color
     */
    public ColorNotification(String playerName, CardColor selectedColor) {
        // Create the notification container
        notificationPane = new StackPane();
        notificationPane.setMaxWidth(300);
        notificationPane.setMaxHeight(80);
        notificationPane.setOpacity(0);
        
        // Create the notification background
        Rectangle background = new Rectangle(300, 80);
        background.setArcWidth(15);
        background.setArcHeight(15);
        background.setFill(Color.rgb(0, 0, 0, 0.8));
        background.setStroke(Color.WHITE);
        background.setStrokeWidth(1);
        
        // Create the notification content
        HBox content = new HBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(10));
        
        // Color square
        Rectangle colorSquare = new Rectangle(40, 40);
        colorSquare.setArcWidth(8);
        colorSquare.setArcHeight(8);
        
        // Set the color based on the selected color
        switch (selectedColor) {
            case RED:
                colorSquare.setFill(Color.RED);
                break;
            case BLUE:
                colorSquare.setFill(Color.BLUE);
                break;
            case GREEN:
                colorSquare.setFill(Color.GREEN);
                break;
            case YELLOW:
                colorSquare.setFill(Color.YELLOW);
                break;
            default:
                colorSquare.setFill(Color.GRAY);
                break;
        }
        
        // Create the notification text
        Label textLabel = new Label(playerName + " has chosen " + selectedColor + " color");
        textLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        textLabel.setWrapText(true);
        
        content.getChildren().addAll(colorSquare, textLabel);
        
        // Add components to the notification pane
        notificationPane.getChildren().addAll(background, content);
        
        // Create the appearance animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notificationPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        // Create a pause to show the notification
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        
        // Create the disappearance animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), notificationPane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        
        // Create the sequential animation
        animation = new SequentialTransition(fadeIn, pause, fadeOut);
        animation.setOnFinished(e -> {
            // Remove the notification from its parent when animation is complete
            if (notificationPane.getParent() != null) {
                ((javafx.scene.layout.Pane) notificationPane.getParent()).getChildren().remove(notificationPane);
            }
        });
    }
    
    /**
     * Gets the notification pane.
     * 
     * @return The notification pane
     */
    public StackPane getNotificationPane() {
        return notificationPane;
    }
    
    /**
     * Shows the notification.
     */
    public void show() {
        animation.play();
    }
} 