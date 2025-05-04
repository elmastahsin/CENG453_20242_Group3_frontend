package metu.ceng.ceng453_20242_group3_frontend.features.leaderboard.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import metu.ceng.ceng453_20242_group3_frontend.features.common.util.ApiClient;
import metu.ceng.ceng453_20242_group3_frontend.features.leaderboard.model.LeaderboardEntry;

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
                    List<LeaderboardEntry> entries = parseLeaderboardResponse(response);
                    onSuccess.accept(entries);
                } catch (Exception e) {
                    onError.accept("Failed to parse weekly leaderboard data: " + e.getMessage());
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
                    List<LeaderboardEntry> entries = parseLeaderboardResponse(response);
                    onSuccess.accept(entries);
                } catch (Exception e) {
                    onError.accept("Failed to parse monthly leaderboard data: " + e.getMessage());
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
                    List<LeaderboardEntry> entries = parseLeaderboardResponse(response);
                    onSuccess.accept(entries);
                } catch (Exception e) {
                    onError.accept("Failed to parse all-time leaderboard data: " + e.getMessage());
                }
            },
            error -> onError.accept("Failed to get all-time leaderboard: " + error)
        );
    }
    
    /**
     * Parse the JSON response from the leaderboard API and convert to a list of LeaderboardEntry objects.
     * The API response has the format: {"status":{"code":"OK","description":"Success"},"data":[...]}
     * 
     * @param response JSON response from the API
     * @return List of LeaderboardEntry objects
     * @throws JsonProcessingException if parsing fails
     */
    private List<LeaderboardEntry> parseLeaderboardResponse(String response) throws JsonProcessingException {
        // Parse the root JSON object
        JsonNode rootNode = objectMapper.readTree(response);
        
        // Extract the data array
        JsonNode dataNode = rootNode.get("data");
        if (dataNode == null) {
            throw new JsonProcessingException("Response does not contain 'data' field") {};
        }
        
        // Convert the data entries to LeaderboardEntry objects
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (JsonNode entryNode : dataNode) {
            int id = entryNode.get("id").asInt();
            String username = entryNode.get("username").asText();
            int score = entryNode.get("score").asInt();
            
            entries.add(new LeaderboardEntry(id, username, score));
        }
        
        return entries;
    }
} 