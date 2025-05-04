# Project Refactoring Summary

## Original Structure (Layer-Based)
The original project structure was organized by technical layer:
- `controller/` - All controllers regardless of feature
- `model/` - All models
- `service/` - All services
- `util/` - All utilities
- `config/` - Configuration

## New Structure (Feature-Based)
The refactored project structure is organized by functional features:

```
src/main/java/metu/ceng/ceng453_20242_group3_frontend/
├── app/
│   └── UnoApplication.java
├── config/
│   └── AppConfig.java
├── features/
│   ├── auth/
│   │   ├── controller/
│   │   │   ├── ForgotPasswordController.java
│   │   │   ├── LoginController.java
│   │   │   ├── RegisterController.java
│   │   │   └── ResetPasswordController.java
│   │   ├── model/
│   │   │   └── User.java
│   │   ├── service/
│   │   │   └── AuthService.java
│   │   └── ResetPasswordLauncher.java
│   ├── common/
│   │   └── util/
│   │       ├── ApiClient.java
│   │       ├── LogoGenerator.java
│   │       ├── ProtocolHandler.java
│   │       └── SessionManager.java
│   ├── game/
│   │   ├── controller/
│   │   │   ├── GameController.java
│   │   │   ├── GameModeController.java
│   │   │   └── MainMenuController.java
│   │   ├── model/
│   │   │   ├── card/
│   │   │   │   ├── Card.java
│   │   │   │   └── NumberCard.java
│   │   │   └── ComputerAIPlayer.java
│   │   └── util/
│   │       ├── GameModeIconGenerator.java
│   │       └── IconGenerator.java
│   ├── leaderboard/
│   │   ├── controller/
│   │   │   └── LeaderboardController.java
│   │   ├── model/
│   │   │   └── LeaderboardEntry.java
│   │   └── service/
│   │       └── LeaderboardService.java
│   └── README.md
```