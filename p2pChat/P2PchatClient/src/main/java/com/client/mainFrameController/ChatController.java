package com.client.mainFrameController;

import com.client.service.UdpConnection;
import com.client.model.User;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;


public class ChatController implements Initializable {
    @FXML
    private ScrollPane chatScroll;
    @FXML
    private VBox messageBox;
    @FXML
    private TextField messageField;

    private User chatUser;
    DateFormat format = new SimpleDateFormat("HH:mm");

    public void setupUser(User user){
        this.chatUser = user;}

    @FXML
    public void checkForEnter(KeyEvent keyEvent) {
        if(keyEvent.getText().equals("\r"))
            sendMessage();
    }
    @FXML
    public void sendMessage() {
        String messageToSend = messageField.getText();
        if (!messageToSend.isEmpty()) {


            TextFlow textFlow = new TextFlow(new Text(messageToSend));
            textFlow.setId("userTextFlow");

            HBox hbox = new HBox();
            hbox.setId("hbox");
            hbox.setAlignment(Pos.CENTER_RIGHT);
            hbox.getChildren().add(textFlow);

            TextFlow time = new TextFlow(new Text(format.format(new Date())));
            HBox timeHBox = new HBox();
            timeHBox.setId("hbox");
            timeHBox.getChildren().add(time);
            timeHBox.setAlignment(Pos.CENTER_RIGHT);

            messageBox.getChildren().addAll(timeHBox,hbox);
            messageField.clear();

            chatUser.sendMessage(messageToSend);
        }
    }


    public void displayMessage(String message) {

        TextFlow textFlow = new TextFlow(new Text(message));
        textFlow.setId("clientTextFlow");

        HBox hbox = new HBox();
        hbox.setId("hbox");
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getChildren().add(textFlow);

        TextFlow time = new TextFlow(new Text(format.format(new Date())));
        HBox timeHBox = new HBox();
        timeHBox.setId("hbox");
        timeHBox.getChildren().add(time);
        timeHBox.setAlignment(Pos.CENTER_LEFT);


        Platform.runLater(() -> {
            messageBox.getChildren().addAll(timeHBox, hbox);

        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chatScroll.widthProperty().addListener(event ->{
            messageBox.setPrefWidth(chatScroll.getWidth());
        });

        messageBox.heightProperty().addListener(new ChangeListener() {

            @Override
            public void changed(ObservableValue observable, Object oldvalue, Object newValue) {

                chatScroll.setVvalue((Double)newValue );
            }
        });

    }


}
