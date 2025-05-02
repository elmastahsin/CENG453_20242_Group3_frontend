# Feature-Based Package Structure

The project has been reorganized to follow a feature-based package structure.

## Structure Overview

- `features/` - Root directory for all feature modules
  - `auth/` - Authentication-related features
    - `controller/` - Controllers for login, registration, password reset, etc.
    - `model/` - User model
    - `service/` - AuthService
  - `game/` - Game-related features
    - `controller/` - Controllers for game, game mode, main menu
    - `model/` - Game models including ComputerAIPlayer and cards
    - `util/` - Game-specific utilities
  - `leaderboard/` - Leaderboard features
    - `controller/` - Leaderboard controller
    - `model/` - LeaderboardEntry model
    - `service/` - LeaderboardService
  - `common/` - Shared utilities and components
    - `util/` - Common utilities like SessionManager, ApiClient, etc.