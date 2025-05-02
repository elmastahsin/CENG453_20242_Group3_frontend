package metu.ceng.ceng453_20242_group3_frontend.features.game.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

/**
 * Utility class to generate the application icon.
 */
public class IconGenerator {

    /**
     * Generates a UNO icon and saves it to the resources directory.
     */
    public static void generateIcon() {
        try {
            // Create directory if it doesn't exist
            Path imagesDir = Paths.get("src/main/resources/images");
            if (!Files.exists(imagesDir)) {
                Files.createDirectories(imagesDir);
            }
            
            // Check if icon already exists
            Path iconPath = imagesDir.resolve("app_icon.png");
            if (Files.exists(iconPath)) {
                System.out.println("Icon already exists at: " + iconPath.toAbsolutePath());
                return;
            }
            
            // Create the icon using Java AWT instead of JavaFX
            BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            
            // Enable anti-aliasing for smoother rendering
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw circular background (UNO blue)
            g2d.setColor(new Color(0, 122, 193));
            g2d.fillOval(0, 0, 512, 512);
            
            // Draw a white border
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(20));
            g2d.drawOval(25, 25, 462, 462);
            
            // Draw the text "UNO" in UNO red
            g2d.setColor(new Color(227, 35, 45));
            
            // Try to load Impact font, fallback to a bold sans-serif if not available
            Font font;
            try {
                font = new Font("Impact", Font.BOLD, 200);
            } catch (Exception e) {
                font = new Font(Font.SANS_SERIF, Font.BOLD, 170);
            }
            
            g2d.setFont(font);
            
            // Center the text
            FontMetrics metrics = g2d.getFontMetrics(font);
            String text = "UNO";
            int x = (512 - metrics.stringWidth(text)) / 2;
            int y = ((512 - metrics.getHeight()) / 2) + metrics.getAscent();
            
            g2d.drawString(text, x, y);
            
            // Dispose of graphics context
            g2d.dispose();
            
            // Save the image
            File file = iconPath.toFile();
            ImageIO.write(image, "png", file);
            
            System.out.println("Icon generated successfully at: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error generating icon: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        generateIcon();
    }
} 