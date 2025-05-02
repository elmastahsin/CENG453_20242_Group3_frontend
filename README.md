# CENG453 Group 3 - UNO Game Frontend

## Overview
This repository contains the frontend implementation of the UNO card game created for the CENG453 course.

## Features
- User authentication (login, registration, password reset)
- Game play interface
- Leaderboard
- Game settings

## How to Run
1. Clone the repository
2. Build the project using Maven:
   ```
   mvn clean package
   ```
3. Run the application:
   ```
   java -jar target/CENG453_20242_Group3_frontend-1.0-SNAPSHOT.jar
   ```

## Password Reset
When you receive a password reset email with a reset token, you can use one of the following methods to reset your password:

### Method 1: Using the provided scripts
#### On macOS/Linux:
```
./reset-password.sh YOUR_RESET_TOKEN
```

Example:
```
./reset-password.sh bdf045f7-fdb4-49be-b2ec-70f50ffbd0de
```

#### On Windows:
```
reset-password.bat YOUR_RESET_TOKEN
```

Example:
```
reset-password.bat bdf045f7-fdb4-49be-b2ec-70f50ffbd0de
```

### Method 2: Direct Java execution
You can also directly use the ResetPasswordLauncher class:

```
java -cp target/classes:target/dependency/* metu.ceng.ceng453_20242_group3_frontend.ResetPasswordLauncher YOUR_RESET_TOKEN
```

For Windows:
```
java -cp target/classes;target/dependency/* metu.ceng.ceng453_20242_group3_frontend.ResetPasswordLauncher YOUR_RESET_TOKEN
```

## Notes for macOS Users
On macOS, the custom URL protocol (uno-reset://) might not work directly. Instead, use the reset-password.sh script provided above.

## Troubleshooting
If you encounter any issues:
1. Make sure you have Java 17 or later installed
2. Check that all dependencies are properly installed
3. Ensure you have proper permissions to execute the scripts
4. For password reset issues, use the scripts provided instead of clicking links directly 