package com.client;

import com.client.loginWindowController.LoginController;
import com.client.model.ContactList;
import com.client.service.TcpConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;


public class ChatApp extends Application {

    private static Stage mainStage;


    public static Stage getMainStage(){ return mainStage;}
    private void setMainStage(Stage stage) {mainStage = stage;}


    @Override
    public void start(Stage stage) throws IOException {
        setMainStage(stage);

        AnchorPane root;
        FXMLLoader loginLoader = new FXMLLoader(ChatApp.class.getResource("/login.fxml"));

        root = loginLoader.load();
        LoginController controller = loginLoader.getController();
        controller.initializeTcpConnection();


        Scene scene = new Scene(root);
        stage.setTitle("login");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/java.png"))));
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();


    }

    public static void main(String[] args) {
        launch(args);
    }
}