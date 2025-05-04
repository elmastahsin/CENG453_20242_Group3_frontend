package metu.ceng.ceng453_20242_group3_frontend.config;

/**
 * Configuration class for the UNO game application.
 * Contains constants and configuration properties.
 */
public class AppConfig {
    // API endpoints
    public static final String BASE_API_URL = "https://ceng-453-group-3.onrender.com/api";
    
    // Authentication endpoints
    public static final String LOGIN_ENDPOINT = BASE_API_URL + "/auth/login";
    public static final String REGISTER_ENDPOINT = BASE_API_URL + "/auth/register";
    public static final String FORGOT_PASSWORD_ENDPOINT = BASE_API_URL + "/auth/forgot-password";
    public static final String SET_NEW_PASSWORD_ENDPOINT = BASE_API_URL + "/auth/set-new-password";
    public static final String VALIDATE_RESET_TOKEN_ENDPOINT = BASE_API_URL + "/auth/validate-reset-token";
    public static final String REFRESH_TOKEN_ENDPOINT = BASE_API_URL + "/auth/refresh";
    
    // Leaderboard endpoints
    public static final String WEEKLY_LEADERBOARD_ENDPOINT = BASE_API_URL + "/leaderboard/weekly";
    public static final String MONTHLY_LEADERBOARD_ENDPOINT = BASE_API_URL + "/leaderboard/monthly";
    public static final String ALL_TIME_LEADERBOARD_ENDPOINT = BASE_API_URL + "/leaderboard/all-time";
    
    // Game constants
    public static final int INITIAL_CARDS_PER_PLAYER = 7;
    public static final int DEFAULT_NUMBER_OF_PLAYERS = 4;
    public static final int MAX_NUMBER_OF_PLAYERS = 4;
    public static final int MIN_NUMBER_OF_PLAYERS = 2;
    public static final int UNO_DECK_SIZE = 120;
    
    // Card-related constants
    public static final int CARD_NUMBER_MIN = 0;
    public static final int CARD_NUMBER_MAX = 9;
    public static final int CARD_WILD_COUNT = 4; // Number of each wild card type
    public static final int CARD_ACTION_COUNT = 2; // Number of each action card per color
    
    // UI constants
    public static final int WINDOW_WIDTH = 1024;
    public static final int WINDOW_HEIGHT = 768;
    public static final String GAME_TITLE = "UNO Game";
    
    // Backward compatibility aliases
    public static final String RESET_PASSWORD_ENDPOINT = FORGOT_PASSWORD_ENDPOINT;
    public static final String COMPLETE_PASSWORD_RESET_ENDPOINT = SET_NEW_PASSWORD_ENDPOINT;
} 