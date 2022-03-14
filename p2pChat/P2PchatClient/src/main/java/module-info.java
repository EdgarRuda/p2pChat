module com.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    exports com.client.model;
    exports com.client;
    opens com.client to javafx.fxml;
    opens com.client.model to javafx.fxml;
    exports com.client.mainFrameController;
    opens com.client.mainFrameController to javafx.fxml;
    exports com.client.loginWindowController;
    opens com.client.loginWindowController to javafx.fxml;
    exports com.client.service;
    opens com.client.service to javafx.fxml;
}