package metu.ceng.ceng453_20242_group3_frontend.features.leaderboard.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import metu.ceng.ceng453_20242_group3_frontend.features.leaderboard.model.LeaderboardEntry;
import metu.ceng.ceng453_20242_group3_frontend.features.common.util.ApiClient;
import metu.ceng.ceng453_20242_group3_frontend.features.common.util.SessionManager;

/**
 * Service class for leaderboard-related operations.
 */
public class LeaderboardService {
    
    private final ApiClient apiClient;
    
    public LeaderboardService() {
        apiClient = new ApiClient();
    }
    
    /**
     * Gets the weekly leaderboard.
     * 
     * @param onSuccess Callback for successful API call
     * @param onError Callback for API error
     */
    public void getWeeklyLeaderboard(Consumer<List<LeaderboardEntry>> onSuccess, Consumer<String> onError) {
        // In a real implementation, this would make an API call
        // For now, we'll just return sample data
        try {
            // Sample API call (commented out for now)
            /*
            apiClient.get("/leaderboard/weekly", 
                response -> {
                    // Parse JSON response and create LeaderboardEntry objects
                    List<LeaderboardEntry> entries = new ArrayList<>();
                    // ... parsing logic
                    onSuccess.accept(entries);
                },
                error -> onError.accept(error)
            );
            */
            
            // Just return sample data for now
            List<LeaderboardEntry> sampleData = new ArrayList<>();
            sampleData.add(new LeaderboardEntry(1, "player1", 1500));
            sampleData.add(new LeaderboardEntry(2, "player2", 1200));
            sampleData.add(new LeaderboardEntry(3, "player3", 1000));
            onSuccess.accept(sampleData);
        } catch (Exception e) {
            onError.accept("Failed to get weekly leaderboard: " + e.getMessage());
        }
    }
    
    /**
     * Gets the monthly leaderboard.
     * 
     * @param onSuccess Callback for successful API call
     * @param onError Callback for API error
     */
    public void getMonthlyLeaderboard(Consumer<List<LeaderboardEntry>> onSuccess, Consumer<String> onError) {
        // Similar to weekly leaderboard, for now we'll just return sample data
        try {
            List<LeaderboardEntry> sampleData = new ArrayList<>();
            sampleData.add(new LeaderboardEntry(1, "player1", 5000));
            sampleData.add(new LeaderboardEntry(2, "player2", 4500));
            sampleData.add(new LeaderboardEntry(3, "player3", 4000));
            onSuccess.accept(sampleData);
        } catch (Exception e) {
            onError.accept("Failed to get monthly leaderboard: " + e.getMessage());
        }
    }
    
    /**
     * Gets the all-time leaderboard.
     * 
     * @param onSuccess Callback for successful API call
     * @param onError Callback for API error
     */
    public void getAllTimeLeaderboard(Consumer<List<LeaderboardEntry>> onSuccess, Consumer<String> onError) {
        // Similar to other leaderboards, for now we'll just return sample data
        try {
            List<LeaderboardEntry> sampleData = new ArrayList<>();
            sampleData.add(new LeaderboardEntry(1, "champion1", 20000));
            sampleData.add(new LeaderboardEntry(2, "champion2", 18000));
            sampleData.add(new LeaderboardEntry(3, "champion3", 16000));
            onSuccess.accept(sampleData);
        } catch (Exception e) {
            onError.accept("Failed to get all-time leaderboard: " + e.getMessage());
        }
    }
} 