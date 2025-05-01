package metu.ceng.ceng453_20242_group3_frontend.util;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility class for handling custom URI protocols like 'uno-reset://' for password reset links.
 */
public class ProtocolHandler {
    
    private static final String PROTOCOL_NAME = "uno-reset";
    
    /**
     * Registers the custom URI protocol with the operating system.
     * This should be called during application installation or first run.
     * 
     * Note: This is a simplified implementation. In a production application,
     * you would need to handle registry settings on Windows, or appropriate
     * configuration on macOS/Linux.
     */
    public static void registerProtocol() {
        // This is a platform-dependent operation
        String os = System.getProperty("os.name").toLowerCase();
        
        try {
            if (os.contains("win")) {
                // Windows implementation
                registerWindows();
            } else if (os.contains("mac")) {
                // macOS implementation
                registerMacOS();
            } else if (os.contains("nix") || os.contains("nux")) {
                // Linux implementation
                registerLinux();
            }
        } catch (Exception e) {
            System.err.println("Failed to register protocol handler: " + e.getMessage());
        }
    }
    
    /**
     * Handles a URI with our custom protocol.
     * 
     * @param uri The URI to handle
     * @return The token from the URI
     */
    public static String handleUri(URI uri) {
        if (uri.getScheme().equals(PROTOCOL_NAME)) {
            // Extract token from URI
            String path = uri.getSchemeSpecificPart();
            if (path.startsWith("//")) {
                path = path.substring(2);
            }
            
            // Remove any additional path components
            if (path.contains("/")) {
                path = path.substring(0, path.indexOf("/"));
            }
            
            return path;
        }
        
        return null;
    }
    
    /**
     * Registers the protocol on Windows.
     */
    private static void registerWindows() throws IOException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java.exe";
        
        String currentJar = new File(ProtocolHandler.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath()).getPath();
        
        // Create registry settings
        String command = "reg add HKEY_CLASSES_ROOT\\" + PROTOCOL_NAME + " /ve /d \"URL:UNO Reset Protocol\" /f";
        Runtime.getRuntime().exec(command);
        
        command = "reg add HKEY_CLASSES_ROOT\\" + PROTOCOL_NAME + " /v \"URL Protocol\" /d \"\" /f";
        Runtime.getRuntime().exec(command);
        
        command = "reg add HKEY_CLASSES_ROOT\\" + PROTOCOL_NAME + "\\shell\\open\\command /ve" + 
                  " /d \"\\\"" + javaBin + "\\\" -jar \\\"" + currentJar + "\\\" %1\" /f";
        Runtime.getRuntime().exec(command);
    }
    
    /**
     * Registers the protocol on macOS.
     * Note: For a complete implementation, this requires admin privileges
     * and should be part of the application installer.
     */
    private static void registerMacOS() {
        try {
            String homeDir = System.getProperty("user.home");
            String plistPath = homeDir + "/Library/Preferences/uno-reset-handler.plist";
            File plistFile = new File(plistPath);
            
            // Create plist content for the protocol handler
            StringBuilder plist = new StringBuilder();
            plist.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            plist.append("<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n");
            plist.append("<plist version=\"1.0\">\n");
            plist.append("<dict>\n");
            plist.append("    <key>CFBundleIdentifier</key>\n");
            plist.append("    <string>com.uno.reset</string>\n");
            plist.append("    <key>CFBundleURLTypes</key>\n");
            plist.append("    <array>\n");
            plist.append("        <dict>\n");
            plist.append("            <key>CFBundleURLSchemes</key>\n");
            plist.append("            <array>\n");
            plist.append("                <string>").append(PROTOCOL_NAME).append("</string>\n");
            plist.append("            </array>\n");
            plist.append("        </dict>\n");
            plist.append("    </array>\n");
            plist.append("</dict>\n");
            plist.append("</plist>\n");
            
            // Write the plist file
            try (FileWriter writer = new FileWriter(plistFile)) {
                writer.write(plist.toString());
            }
            
            System.out.println("Created protocol handler plist at: " + plistPath);
            System.out.println("Note: On macOS, you will need to manually associate this application with the protocol.");
            System.out.println("Use 'lsregister -v -f " + plistPath + "' to register it.");
            
        } catch (Exception e) {
            System.err.println("macOS protocol registration error: " + e.getMessage());
        }
    }
    
    /**
     * Registers the protocol on Linux.
     */
    private static void registerLinux() {
        // Linux protocol registration typically involves creating a .desktop file
        System.err.println("Linux protocol registration not fully implemented");
    }
    
    /**
     * Tests the protocol handler by opening a test URI.
     */
    public static void testProtocolHandler() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(PROTOCOL_NAME + "://testtoken"));
            } catch (IOException | URISyntaxException e) {
                System.err.println("Failed to test protocol handler: " + e.getMessage());
            }
        }
    }
} 