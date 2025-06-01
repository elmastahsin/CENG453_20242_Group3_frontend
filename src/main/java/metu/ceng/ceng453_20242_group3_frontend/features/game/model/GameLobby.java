package metu.ceng.ceng453_20242_group3_frontend.features.game.model;

import java.util.List;

/**
 * Model representing a game in the lobby waiting for players.
 */
public class GameLobby {
    private int playerCount;
    private List<String> players;
    private String gametype;
    private int gameId;
    
    public GameLobby() {
    }
    
    public GameLobby(int playerCount, List<String> players, String gametype, int gameId) {
        this.playerCount = playerCount;
        this.players = players;
        this.gametype = gametype;
        this.gameId = gameId;
    }
    
    // Getters and setters
    public int getPlayerCount() {
        return playerCount;
    }
    
    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }
    
    public List<String> getPlayers() {
        return players;
    }
    
    public void setPlayers(List<String> players) {
        this.players = players;
    }
    
    public String getGametype() {
        return gametype;
    }
    
    public void setGametype(String gametype) {
        this.gametype = gametype;
    }
    
    public int getGameId() {
        return gameId;
    }
    
    public void setGameId(int gameId) {
        this.gameId = gameId;
    }
    
    /**
     * Gets the maximum number of players for this game type.
     * 
     * @return Maximum player count
     */
    public int getMaxPlayers() {
        switch (gametype) {
            case "TWO_PLAYER":
                return 2;
            case "THREE_PLAYER":
                return 3;
            case "FOUR_PLAYER":
                return 4;
            default:
                return 2;
        }
    }
    
    /**
     * Checks if the game is full.
     * 
     * @return true if game is full, false otherwise
     */
    public boolean isFull() {
        return playerCount >= getMaxPlayers();
    }
    
    /**
     * Checks if the game is available for joining.
     * 
     * @return true if game can be joined, false otherwise
     */
    public boolean isJoinable() {
        return !isFull() && playerCount > 0;
    }
    
    /**
     * Gets a display-friendly version of the game type.
     * 
     * @return Formatted game type string
     */
    public String getDisplayGametype() {
        switch (gametype) {
            case "TWO_PLAYER":
                return "2 Players";
            case "THREE_PLAYER":
                return "3 Players";
            case "FOUR_PLAYER":
                return "4 Players";
            case "SINGLE_PLAYER":
                return "Single Player";
            default:
                return gametype;
        }
    }
    
    @Override
    public String toString() {
        return "GameLobby{" +
                "playerCount=" + playerCount +
                ", players=" + players +
                ", gametype='" + gametype + '\'' +
                ", gameId=" + gameId +
                '}';
    }
} 