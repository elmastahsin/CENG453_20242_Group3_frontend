module metu.ceng.ceng453_20242_group3_frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

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

    opens metu.ceng.ceng453_20242_group3_frontend to javafx.fxml;
    exports metu.ceng.ceng453_20242_group3_frontend;
    
    opens metu.ceng.ceng453_20242_group3_frontend.controller to javafx.fxml;
    exports metu.ceng.ceng453_20242_group3_frontend.controller;
    
    exports metu.ceng.ceng453_20242_group3_frontend.model;
    exports metu.ceng.ceng453_20242_group3_frontend.model.card;
    exports metu.ceng.ceng453_20242_group3_frontend.service;
    exports metu.ceng.ceng453_20242_group3_frontend.util;
    exports metu.ceng.ceng453_20242_group3_frontend.config;
}