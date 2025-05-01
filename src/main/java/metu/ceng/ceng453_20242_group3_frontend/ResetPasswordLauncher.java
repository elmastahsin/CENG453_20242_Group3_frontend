package metu.ceng.ceng453_20242_group3_frontend;

/**
 * Simple launcher class to handle reset password tokens.
 * This class is used to launch the application with a reset token.
 */
public class ResetPasswordLauncher {
    
    /**
     * Main method to launch the application with a reset token.
     * 
     * @param args Command line arguments, expected to contain the reset token
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -cp path/to/app.jar metu.ceng.ceng453_20242_group3_frontend.ResetPasswordLauncher token");
            return;
        }
        
        String token = args[0];
        System.out.println("Launching with reset token: " + token);
        
        // Launch the main application with the reset token
        UnoApplication.main(new String[]{"reset-password:" + token});
    }
} 