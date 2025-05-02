package metu.ceng.ceng453_20242_group3_frontend.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import javax.imageio.ImageIO;

/**
 * Utility class for generating game mode icon images.
 * This generates placeholder icons for singleplayer and multiplayer modes.
 */
public class GameModeIconGenerator {
    
    // Size of the icons
    private static final int ICON_SIZE = 240;
    
    // Directory where icons will be stored
    private static final String IMAGES_DIR = "src/main/resources/images";
    
    /**
     * Generates all game mode icons.
     * This should be called during application initialization.
     */
    public static void generateIcons() {
        try {
            // Create the images directory if it doesn't exist
            Path imagesPath = Paths.get(IMAGES_DIR);
            if (!Files.exists(imagesPath)) {
                Files.createDirectories(imagesPath);
            }
            
            // Generate the icons
            generateSinglePlayerIcon();
            generateMultiPlayerIcon();
            
            System.out.println("Game mode icons generated successfully");
        } catch (IOException e) {
            System.err.println("Error generating game mode icons: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generates the singleplayer icon.
     */
    private static void generateSinglePlayerIcon() throws IOException {
        // Create a canvas for drawing
        Canvas canvas = new Canvas(ICON_SIZE, ICON_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Draw a gradient background
        LinearGradient gradient = new LinearGradient(0, 0, 0, ICON_SIZE, 
                false, CycleMethod.NO_CYCLE, 
                new Stop(0, Color.web("#43b047")), 
                new Stop(1, Color.web("#2a7e2e")));
        
        gc.setFill(gradient);
        gc.fillOval(20, 20, ICON_SIZE - 40, ICON_SIZE - 40);
        
        // Add a nice shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setRadius(10);
        gc.setEffect(shadow);
        
        // Draw the person icon
        gc.setFill(Color.WHITE);
        
        // Head
        gc.fillOval(ICON_SIZE/2 - 25, 65, 50, 50);
        
        // Body
        gc.fillRect(ICON_SIZE/2 - 30, 120, 60, 70);
        
        // Remove the shadow for text
        gc.setEffect(null);
        
        // Add the text "Single Player"
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Poppins", FontWeight.BOLD, 24));
        gc.fillText("1", ICON_SIZE/2, 175);
        
        // Create the image file
        saveCanvasToFile(canvas, "singleplayer-icon.png");
    }
    
    /**
     * Generates the multiplayer icon.
     */
    private static void generateMultiPlayerIcon() throws IOException {
        // Create a canvas for drawing
        Canvas canvas = new Canvas(ICON_SIZE, ICON_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Draw a gradient background
        LinearGradient gradient = new LinearGradient(0, 0, 0, ICON_SIZE, 
                false, CycleMethod.NO_CYCLE, 
                new Stop(0, Color.web("#4dabf7")), 
                new Stop(1, Color.web("#1971c2")));
        
        gc.setFill(gradient);
        gc.fillOval(20, 20, ICON_SIZE - 40, ICON_SIZE - 40);
        
        // Add a nice shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setRadius(10);
        gc.setEffect(shadow);
        
        // Draw multiple person icons
        gc.setFill(Color.WHITE);
        
        // First person (left)
        gc.fillOval(70, 75, 40, 40); // Head
        gc.fillRect(65, 120, 50, 60); // Body
        
        // Second person (right)
        gc.fillOval(130, 75, 40, 40); // Head
        gc.fillRect(125, 120, 50, 60); // Body
        
        // Remove the shadow for text
        gc.setEffect(null);
        
        // Add text
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Poppins", FontWeight.BOLD, 24));
        gc.fillText("2+", ICON_SIZE/2, 175);
        
        // Create the image file
        saveCanvasToFile(canvas, "multiplayer-icon.png");
    }
    
    /**
     * Saves the canvas content to a PNG file.
     * 
     * @param canvas The canvas to save
     * @param fileName The file name to save as
     */
    private static void saveCanvasToFile(Canvas canvas, String fileName) throws IOException {
        // Create a WritableImage from the canvas
        WritableImage image = new WritableImage((int)canvas.getWidth(), (int)canvas.getHeight());
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        canvas.snapshot(params, image);
        
        // Save the image to a file
        File file = new File(IMAGES_DIR + "/" + fileName);
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
    }
} 