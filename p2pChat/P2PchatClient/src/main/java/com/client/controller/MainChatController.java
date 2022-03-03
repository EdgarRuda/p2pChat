package com.client.controller;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


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


    private String userName;
    private int roomNumber;


    public void setUserName(String userName) {
        this.userName = userName;
        userNameLabel.setText(userName);
    }

    public void setRoomNumber(int roomNumber) {
        roomNumberLabel.setText(String.valueOf(roomNumber));
        this.roomNumber = roomNumber;
    }


    public void sendMessage(ActionEvent event) {
        String messageToSend = messageField.getText();

        if (!messageToSend.isEmpty()) {
            Text text = new Text(messageToSend);
            TextFlow textFlow = new TextFlow(text);
            textFlow.setStyle("-fx-color: rgb(239,242,255); -fx-background-color: rgb(15,125,242);");

            HBox hbox = new HBox();
            hbox.setAlignment(Pos.CENTER_RIGHT);
            hbox.getChildren().add(textFlow);

            messagesBox.getChildren().add(hbox);

            messageField.clear();


        }
    }


    private  void displayMessage(String message, VBox messagesBox) {

        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-color: rgb(239,242,255); -fx-background-color: rgb(155,125,242);");

        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getChildren().add(textFlow);



        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                messagesBox.getChildren().add(hbox);
            }
        });

    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void testMessage(ActionEvent event) {
        displayMessage("test", messagesBox);
    }
}
