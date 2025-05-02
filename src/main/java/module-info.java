module metu.ceng.ceng453_20242_group3_frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.desktop;
    requires javafx.swing;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.net.http;
    requires com.google.gson;
    requires com.fasterxml.jackson.databind;

    // App package
    exports metu.ceng.ceng453_20242_group3_frontend.app;
    
    // Config package
    exports metu.ceng.ceng453_20242_group3_frontend.config;
    
    // Auth feature
    exports metu.ceng.ceng453_20242_group3_frontend.features.auth.controller to javafx.fxml;
    opens metu.ceng.ceng453_20242_group3_frontend.features.auth.controller to javafx.fxml;
    exports metu.ceng.ceng453_20242_group3_frontend.features.auth.model;
    exports metu.ceng.ceng453_20242_group3_frontend.features.auth.service;
    
    // Game feature
    exports metu.ceng.ceng453_20242_group3_frontend.features.game.controller to javafx.fxml;
    opens metu.ceng.ceng453_20242_group3_frontend.features.game.controller to javafx.fxml;
    exports metu.ceng.ceng453_20242_group3_frontend.features.game.model;
    exports metu.ceng.ceng453_20242_group3_frontend.features.game.util;
    
    // Leaderboard feature
    exports metu.ceng.ceng453_20242_group3_frontend.features.leaderboard.controller to javafx.fxml;
    opens metu.ceng.ceng453_20242_group3_frontend.features.leaderboard.controller to javafx.fxml;
    exports metu.ceng.ceng453_20242_group3_frontend.features.leaderboard.model;
    exports metu.ceng.ceng453_20242_group3_frontend.features.leaderboard.service;
    
    // Common utilities
    exports metu.ceng.ceng453_20242_group3_frontend.features.common.util;
}