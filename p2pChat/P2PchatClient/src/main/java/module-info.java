module com.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.client.controller.Fx to javafx.fxml;

    exports com.client;
}