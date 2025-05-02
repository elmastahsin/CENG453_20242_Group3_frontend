package metu.ceng.ceng453_20242_group3_frontend.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import metu.ceng.ceng453_20242_group3_frontend.model.LeaderboardEntry;
import metu.ceng.ceng453_20242_group3_frontend.util.ApiClient;

/**
 * Service class for leaderboard-related operations.
 */
public class LeaderboardService {
    
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;
    
    public LeaderboardService() {
        apiClient = new ApiClient();
        objectMapper = new ObjectMapper();
    }
    
    /**
     * Gets the weekly leaderboard.
     * 
     * @param onSuccess Callback for successful API call
     * @param onError Callback for API error
     */
    public void getWeeklyLeaderboard(Consumer<List<LeaderboardEntry>> onSuccess, Consumer<String> onError) {
        apiClient.get("/leaderboard/weekly", 
            response -> {
                try {
                    List<LeaderboardEntry> entries = parseLeaderboardData(response);
                    onSuccess.accept(entries);
                } catch (Exception e) {
                    onError.accept("Failed to parse weekly leaderboard: " + e.getMessage());
                }
            },
            error -> onError.accept("Failed to get weekly leaderboard: " + error)
        );
    }
    
    /**
     * Gets the monthly leaderboard.
     * 
     * @param onSuccess Callback for successful API call
     * @param onError Callback for API error
     */
    public void getMonthlyLeaderboard(Consumer<List<LeaderboardEntry>> onSuccess, Consumer<String> onError) {
        apiClient.get("/leaderboard/monthly", 
            response -> {
                try {
                    List<LeaderboardEntry> entries = parseLeaderboardData(response);
                    onSuccess.accept(entries);
                } catch (Exception e) {
                    onError.accept("Failed to parse monthly leaderboard: " + e.getMessage());
                }
            },
            error -> onError.accept("Failed to get monthly leaderboard: " + error)
        );
    }
    
    /**
     * Gets the all-time leaderboard.
     * 
     * @param onSuccess Callback for successful API call
     * @param onError Callback for API error
     */
    public void getAllTimeLeaderboard(Consumer<List<LeaderboardEntry>> onSuccess, Consumer<String> onError) {
        apiClient.get("/leaderboard/all-time", 
            response -> {
                try {
                    List<LeaderboardEntry> entries = parseLeaderboardData(response);
                    onSuccess.accept(entries);
                } catch (Exception e) {
                    onError.accept("Failed to parse all-time leaderboard: " + e.getMessage());
                }
            },
            error -> onError.accept("Failed to get all-time leaderboard: " + error)
        );
    }
    
    /**
     * Parses the API response into a list of LeaderboardEntry objects.
     * 
     * @param response JSON response from the API
     * @return List of LeaderboardEntry objects
     * @throws IOException If parsing fails
     */
    private List<LeaderboardEntry> parseLeaderboardData(String response) throws IOException {
        List<LeaderboardEntry> entries = new ArrayList<>();
        JsonNode root = objectMapper.readTree(response);
        
        // Process API response based on actual structure
        // This is a typical structure, adjust based on actual API response
        JsonNode leaderboardData = root.path("data");
        
        if (leaderboardData.isArray()) {
            int rank = 1;
            for (JsonNode entry : leaderboardData) {
                String username = entry.path("username").asText();
                int score = entry.path("score").asInt();
                entries.add(new LeaderboardEntry(rank++, username, score));
            }
        }
        
        return entries;
    }
} 