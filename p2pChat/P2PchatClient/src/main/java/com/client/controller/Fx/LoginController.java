package com.client.controller.Fx;


import com.client.Launcher;
import com.client.Model.ServerData;
import com.client.controller.Server.ServerController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


import java.io.IOException;


public class LoginController {


    @FXML
    public TextField userName;

    private ServerController serverController;
    private static boolean loginStatus = false;


    public void tryToLogin() throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                 connectToServer();
            }
        }).start();

        openMainChat();
    }

    private void coonectToServer(){

    }

    private void connectToServer(){
        serverController =  new ServerController(ServerData.IP, ServerData.PORT, userName.getText());
    }

    private void openMainChat() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getClassLoader().getResource("mainWindow-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        MainChatController mainChatController = fxmlLoader.getController();
        mainChatController.setUserName(userName.getText());
        mainChatController.setServerController(serverController);

        Stage stage = ((Stage)userName.getScene().getWindow());
        stage.setScene(scene);
    }
}
