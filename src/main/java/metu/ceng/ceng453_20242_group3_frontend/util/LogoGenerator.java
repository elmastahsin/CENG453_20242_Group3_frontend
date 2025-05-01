package metu.ceng.ceng453_20242_group3_frontend.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Utility class to generate a simple UNO logo image file.
 */
public class LogoGenerator {

    /**
     * Generates a simple UNO logo and saves it to the resources directory.
     */
    public static void generateLogo() {
        try {
            // Create directory if it doesn't exist
            Path imagesDir = Paths.get("src/main/resources/images");
            if (!Files.exists(imagesDir)) {
                Files.createDirectories(imagesDir);
            }
            
            // Check if logo already exists
            Path logoPath = imagesDir.resolve("CengUnoLogo.png");
            if (Files.exists(logoPath)) {
                System.out.println("Logo already exists at: " + logoPath.toAbsolutePath());
                return;
            }
            
            // Create a canvas to draw on
            Canvas canvas = new Canvas(400, 200);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            
            // Draw logo background (rounded rectangle)
            gc.setFill(Color.rgb(0, 122, 193)); // UNO blue
            gc.fillRoundRect(0, 0, 400, 200, 30, 30);
            
            // Draw a white border
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(8);
            gc.strokeRoundRect(10, 10, 380, 180, 20, 20);
            
            // Draw the text "CENG" in white
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 40));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("CENG", 200, 80);
            
            // Draw the text "UNO" in white with a special styling
            gc.setFont(Font.font("Impact", FontWeight.BOLD, 70));
            gc.setFill(Color.rgb(227, 35, 45)); // UNO red
            gc.fillText("UNO", 200, 150);
            
            // Convert the canvas to an image and save it
            BufferedImage image = SwingFXUtils.fromFXImage(canvas.snapshot(null, null), null);
            File file = logoPath.toFile();
            ImageIO.write(image, "png", file);
            
            System.out.println("Logo generated successfully at: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error generating logo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        generateLogo();
    }
} 