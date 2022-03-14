module com.client{

    requires validatorfx;
    requires org.kordamp.ikonli.javafx;

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