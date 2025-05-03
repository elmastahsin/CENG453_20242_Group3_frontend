package metu.ceng.ceng453_20242_group3_frontend.features.game.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import metu.ceng.ceng453_20242_group3_frontend.features.game.model.CardColor;

import java.util.function.Consumer;

/**
 * Dialog for selecting a color after playing a wild card.
 */
public class ColorSelectionDialog {
    
    private final Stage dialogStage;
    private CardColor selectedColor;
    
    /**
     * Creates a new color selection dialog.
     * 
     * @param parentStage The parent stage for the dialog
     * @param onColorSelected Callback for when a color is selected
     */
    public ColorSelectionDialog(Stage parentStage, Consumer<CardColor> onColorSelected) {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.initStyle(StageStyle.UNDECORATED);
        dialogStage.setTitle("Select Color");
        
        // Create UI components
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9); -fx-background-radius: 15;");
        
        Label titleLabel = new Label("Select a Color");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        // Create color buttons
        HBox colorButtons = new HBox(15);
        colorButtons.setAlignment(Pos.CENTER);
        
        // Red button
        Button redButton = createColorButton(Color.RED, CardColor.RED, onColorSelected);
        
        // Blue button
        Button blueButton = createColorButton(Color.BLUE, CardColor.BLUE, onColorSelected);
        
        // Green button
        Button greenButton = createColorButton(Color.GREEN, CardColor.GREEN, onColorSelected);
        
        // Yellow button
        Button yellowButton = createColorButton(Color.YELLOW, CardColor.YELLOW, onColorSelected);
        
        colorButtons.getChildren().addAll(redButton, blueButton, greenButton, yellowButton);
        
        root.getChildren().addAll(titleLabel, colorButtons);
        
        Scene dialogScene = new Scene(root, 400, 200);
        dialogScene.setFill(Color.TRANSPARENT);
        
        dialogStage.setScene(dialogScene);
    }
    
    /**
     * Creates a button for a specific color.
     * 
     * @param color The JavaFX color
     * @param cardColor The UNO card color enum value
     * @param onColorSelected Callback for when a color is selected
     * @return A styled button for the color
     */
    private Button createColorButton(Color color, CardColor cardColor, Consumer<CardColor> onColorSelected) {
        Button button = new Button();
        button.setPrefSize(70, 70);
        button.setStyle("-fx-background-color: transparent;");
        
        Rectangle colorRect = new Rectangle(60, 60);
        colorRect.setFill(color);
        colorRect.setArcWidth(10);
        colorRect.setArcHeight(10);
        colorRect.setStroke(Color.WHITE);
        colorRect.setStrokeWidth(2);
        
        button.setGraphic(colorRect);
        
        // Add hover effect
        colorRect.setOnMouseEntered(e -> {
            colorRect.setEffect(new javafx.scene.effect.DropShadow(15, Color.WHITE));
            colorRect.setScaleX(1.1);
            colorRect.setScaleY(1.1);
        });
        
        colorRect.setOnMouseExited(e -> {
            colorRect.setEffect(null);
            colorRect.setScaleX(1.0);
            colorRect.setScaleY(1.0);
        });
        
        // Set action
        button.setOnAction(e -> {
            selectedColor = cardColor;
            onColorSelected.accept(cardColor);
            dialogStage.close();
        });
        
        return button;
    }
    
    /**
     * Shows the dialog.
     */
    public void show() {
        dialogStage.showAndWait();
    }
    
    /**
     * Gets the selected color.
     * 
     * @return The selected color or null if no color was selected
     */
    public CardColor getSelectedColor() {
        return selectedColor;
    }
} 