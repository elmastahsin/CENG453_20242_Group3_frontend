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

    opens metu.ceng.ceng453_20242_group3_frontend to javafx.fxml;
    exports metu.ceng.ceng453_20242_group3_frontend;
}