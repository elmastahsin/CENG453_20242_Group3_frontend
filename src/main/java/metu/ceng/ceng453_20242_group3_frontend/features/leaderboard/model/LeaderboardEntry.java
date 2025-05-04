package metu.ceng.ceng453_20242_group3_frontend.features.leaderboard.model;

/**
 * Model class representing an entry in the leaderboard.
 */
public class LeaderboardEntry {
    private int rank;
    private String username;
    private int score;
    
    public LeaderboardEntry(int rank, String username, int score) {
        this.rank = rank;
        this.username = username;
        this.score = score;
    }
    
    public int getRank() {
        return rank;
    }
    
    public void setRank(int rank) {
        this.rank = rank;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    @Override
    public String toString() {
        return "LeaderboardEntry [rank=" + rank + ", username=" + username + ", score=" + score + "]";
    }
} 