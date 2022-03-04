package com.client.controller.Fx;


import com.client.Model.User;
import com.client.controller.Server.ServerController;
import com.client.controller.Server.UdpController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;


public class MainChatController implements Initializable {

    @FXML
    public  VBox messagesBox;
    @FXML
    public Button sendButton;
    @FXML
    public TextField messageField;
    @FXML
    public Label roomNumberLabel;
    @FXML
    public VBox usersBox;
    @FXML
    public Label userNameLabel;
    @FXML
    public ScrollPane one;

    private ServerController serverController;
    private UdpController udpController;
    private User userToChat;
    private String userName;


    public void setUserName(String userName) {
        this.userName = userName;
        userNameLabel.setText(userName);
    }

    public void setServerController(ServerController serverController){
        this.serverController = serverController;
    }
    public void setUdpController(UdpController udpController) { this.udpController = udpController;}

    public void sendMessageOnEnter(KeyEvent keyEvent) throws IOException {
        if(keyEvent.getText().equals("\r"))
            sendMessage();
    }

    public void sendMessage() throws IOException {
        String messageToSend = messageField.getText();
        if (!messageToSend.isEmpty()) {
            Text text = new Text(messageToSend);
            TextFlow textFlow = new TextFlow(text);
            textFlow.setId("userTextFlow");

            HBox hbox = new HBox();
            hbox.setId("hbox");
            hbox.setAlignment(Pos.CENTER_RIGHT);
            hbox.getChildren().add(textFlow);

            messagesBox.getChildren().add(hbox);


            messageField.clear();
            serverController.sendToServer(messageToSend);
            //udpController.sendData(userToChat, messageToSend);

        }
    }

    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                try {
                    DatagramPacket receivePackage = new DatagramPacket(new byte[1024],
                            1024, InetAddress.getByName(userToChat.getIp()), userToChat.getPort());
                    udpController.getSocket().receive(receivePackage);
                    displayMessage(new String(receivePackage.getData(), 0, receivePackage.getLength()), messagesBox);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                }
            }
        }).start();
    }
    private  void displayMessage(String message, VBox messagesBox) {

        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setId("clientTextFlow");
        HBox hbox = new HBox();
        hbox.setId("hbox");

        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getChildren().add(textFlow);



        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                messagesBox.getChildren().add(hbox);
            }
        });

    }

    private void askForConnection() throws IOException {
        serverController.sendToServer("CONNECT " + "user2");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        userToChat = new User("second user", 111);

//        try {
//            udpController.getUdpData(userToChat);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        listenForMessage();


    }

    public void testMessage(ActionEvent event) {
        displayMessage("test", messagesBox);
    }



}
