package com.client.controller.Fx;


import com.client.Launcher;
import com.client.Model.Server;
import com.client.controller.Server.ServerController;
import com.client.controller.Server.UdpController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public class LoginController {


    @FXML
    public TextField userName;

    private ServerController serverController;
    private  UdpController udpController;
    private static boolean loginStatus = false;


    public void tryToLogin() throws Exception {
        connectToServer();
        openMainChat();
    }




    private void connectToServer() throws IOException {

        serverController =  new ServerController(Server.IP, Server.PORT, userName.getText());
        udpController =  new UdpController();

        serverController.sendToServer(String.valueOf(udpController.openUdpSocket()));
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
