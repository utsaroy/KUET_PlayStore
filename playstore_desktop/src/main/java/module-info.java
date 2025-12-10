module com.utsa.kpstore.playstore_desktop {
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

    opens com.utsa.kpstore.playstore_desktop to javafx.fxml;
    opens com.utsa.kpstore.playstore_desktop.controllers to javafx.fxml;
    exports com.utsa.kpstore.playstore_desktop;
    exports com.utsa.kpstore.playstore_desktop.controllers;
}