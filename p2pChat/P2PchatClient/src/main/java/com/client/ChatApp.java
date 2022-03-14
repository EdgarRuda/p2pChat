package com.client;

import com.client.loginWindowController.LoginController;
import com.client.service.TcpConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatApp extends Application {




    @Override
    public void start(Stage stage) throws IOException {

        AnchorPane root;
        TcpConnection tcpConnection = new TcpConnection();
        FXMLLoader loginLoader = new FXMLLoader(ChatApp.class.getResource("/login.fxml"));

        root = loginLoader.load();

        LoginController loginController = loginLoader.getController();
        loginController.initTcpConnection(tcpConnection);

        Scene scene = new Scene(root);
        stage.setTitle("login");
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}