package com.client.controller;


import com.client.Launcher;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.Random;


public class LoginController {


    @FXML
    public TextField userName;



    public void loadNewRoom() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getClassLoader().getResource("mainWindow-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        MainChatController mainChatController = fxmlLoader.getController();
        mainChatController.setUserName(userName.getText());
        mainChatController.setRoomNumber(new Random().nextInt(100));

        Stage stage = ((Stage)userName.getScene().getWindow());
        stage.setScene(scene);


    }
}
