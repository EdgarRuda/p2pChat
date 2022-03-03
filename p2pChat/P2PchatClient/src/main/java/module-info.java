module com.client.p2pchatclient {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.client.p2pchatclient to javafx.fxml;
    exports com.client.p2pchatclient;
}