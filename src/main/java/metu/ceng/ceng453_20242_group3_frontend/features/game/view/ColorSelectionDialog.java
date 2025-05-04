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
        // Create button with color fill
        Button button = new Button();
        button.setPrefSize(70, 70);
        button.setStyle("-fx-background-color: " + toRGBCode(color) + "; -fx-background-radius: 10; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 10;");
        
        // Add hover effect
        button.setOnMouseEntered(e -> button.setEffect(new javafx.scene.effect.DropShadow(10, color.brighter())));
        button.setOnMouseExited(e -> button.setEffect(null));
        
        // Add click action
        button.setOnAction(e -> {
            this.selectedColor = cardColor;
            System.out.println("Wild card color selected: " + cardColor);
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
    
    /**
     * Converts a JavaFX Color to a CSS RGB code string.
     * 
     * @param color The JavaFX Color to convert
     * @return A CSS-compatible RGB color string
     */
    private String toRGBCode(Color color) {
        return String.format("rgb(%d, %d, %d)", 
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }
} 