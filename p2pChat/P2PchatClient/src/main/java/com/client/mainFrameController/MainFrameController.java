package com.client.mainFrameController;

import com.client.ChatApp;
import com.client.model.ContactList;
import com.client.service.TcpConnection;
import com.client.model.User;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Objects;

public class MainFrameController{
    @FXML
    private AnchorPane mainView;
    @FXML
    private Label userName;
    @FXML
    private Label searchResultNotification;
    @FXML
    private BorderPane mainPane;

    @FXML
    private VBox contactListBox;
    @FXML
    private TextField searchBar;
    @FXML
    private VBox searchResultBox;


    private ContactList contactList;

    private TcpConnection tcpConnection;

    private boolean searchStarted;


    public void initUserSearch() {

        searchResultBox.setTranslateX(-200);
        searchResultNotification.setVisible(false);
        searchResultBox.getChildren().clear();
        if(this.tcpConnection != null)
            searchBar.setText("");
        else {
            searchBar.setText("search disabled");
            searchBar.setDisable(true);
            return;
        }
        searchBar.textProperty().addListener(event ->{
            //copy pasting doesnt work??
            if(searchBar.getText().length()>2) {
                try {
                    searchResultBox.getChildren().clear();
                    Platform.runLater(()->searchResultNotification.setText("searching.."));
                    tcpConnection.searchUser(searchBar.getText());
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
            else if(!searchBar.getText().isEmpty()) {
                if(!searchStarted) {
                    searchStarted=true;
                    contactListBox.setFocusTraversable(true);
                    TranslateTransition slide = new TranslateTransition();
                    slide.setDuration(Duration.seconds(0.2));
                    slide.setNode(searchResultBox);

                    slide.setToX(0);
                    slide.play();


                slide.setOnFinished((ActionEvent e) -> searchResultNotification.setVisible(true));
                }
                else searchResultNotification.setVisible(true);

                searchResultBox.getChildren().clear();
                Platform.runLater(() -> searchResultNotification.setText("start typing to see results.."));
            }
            else{
                System.out.println("end of search");
                searchStarted =false;
                contactListBox.setFocusTraversable(false);
                searchResultNotification.setVisible(false);
                TranslateTransition slide = new TranslateTransition();
                slide.setDuration(Duration.seconds(0.2));
                slide.setNode(searchResultBox);

                slide.setToX(-200);
                slide.play();

            }
        });

    }

    public void setContactsModel(ContactList contactList){

        this.contactList = contactList;
        contactList.setCurrentUser(contactList.getContacts().get(0));
        userName.setText(contactList.getContacts().get(0).getName());

    }

    public void setTcpConnection(TcpConnection tcpConnection) throws Exception {
        this.tcpConnection = tcpConnection;
        tcpConnection.setMainController(this);
        tcpConnection.requestContactData();
        tcpConnection.requestPendingFriends();
        tcpConnection.sendAlive();




    }


    @FXML
    public void logout() throws IOException{
        if (tcpConnection != null)
           tcpConnection.exit();

        for (User contact : contactList.getContacts())
            contact.closeUdpConnection();


        FXMLLoader loader = new FXMLLoader(ChatApp.class.getResource("/login.fxml"));
        Scene scene = new Scene(loader.load());

        ((Stage) mainPane.getScene().getWindow()).close();
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
    }

    public void displaySearchResult(ArrayList<String> searchResult) throws IOException {
        Pane pane;

        contactList.trimSearchResult(searchResult);
        if(searchResult.isEmpty()) {
            Platform.runLater(() -> searchResultNotification.setText("no users found"));
            searchResultNotification.setVisible(true);
            return;
        }
        searchResultNotification.setVisible(false);
        for (String contact : searchResult) {


                pane = FXMLLoader.load(Objects.requireNonNull(ChatApp.class.getClassLoader().getResource("profile.fxml")));

                for (Node child : pane.getChildren()) {
                    if (child instanceof Label && child.getId().equals("userInitials"))
                        ((Label) child).setText(contact.substring(0,1));
                    if (child instanceof Label && child.getId().equals("userName"))
                        ((Label) child).setText(contact);
                    if (child instanceof Circle && child.getId().equals("statusCircle"))
                        child.setVisible(false);
                    if (child instanceof Button && child.getId().equals("addUserToContacts")) {
                        child.setVisible(true);
                        Pane finalPane1 = pane;
                        ((Button) child).setOnAction(event -> {
                            try {
                                Platform.runLater(() -> {
                                    searchResultBox.getChildren().remove(finalPane1);
                                    if(searchResultBox.getChildren().isEmpty())
                                        searchBar.setText("");
                                });

                                User user = new User(contact);
                                user.setIsOutboundRequest(true);
                                contactList.addUser(user);
                                tcpConnection.friendRequest(contact);
                                loadSingleContact(user);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }

                Pane finalPane = pane;
                finalPane.setId("userProfiles");
                Platform.runLater(() -> searchResultBox.getChildren().add(finalPane));
            }
    }


    public void loadContacts() throws IOException {
        Pane pane;
        for (User contact : contactList.getContacts().subList(1, contactList.getContacts().size())) {
            pane = FXMLLoader.load(Objects.requireNonNull(ChatApp.class.getClassLoader().getResource("profile.fxml")));

            bindUserData(contact, pane);
            Pane finalPane = pane;

            Platform.runLater(() -> contactListBox.getChildren().add(finalPane));
        }
    }

    public void removeSingleUser(String userName){
        contactListBox.getChildren().removeIf(child -> child instanceof Label && child.getId().equals(userName));
    }

    public void loadSingleContact(User user) throws IOException {
        Pane pane;
        pane = FXMLLoader.load(Objects.requireNonNull(ChatApp.class.getClassLoader().getResource("profile.fxml")));

        bindUserData(user, pane);
        Pane finalPane = pane;

        Platform.runLater(() -> contactListBox.getChildren().add(finalPane));


    }
    private void bindUserData(User user, Pane pane){

        for (Node child : pane.getChildren()) {
            if (child instanceof Label && child.getId().equals("userInitials"))
                ((Label) child).setText(user.getName().substring(0,1));
            if (child instanceof Label && child.getId().equals("userName"))
                ((Label) child).textProperty().bind(user.nameProperty());
            if (child instanceof Label && child.getId().equals("userStatus"))
                ((Label) child).textProperty().bind(user.userStatusProperty());
            if (child instanceof Circle && child.getId().equals("statusCircle")){
                child.visibleProperty().bind(user.isConfirmedProperty());
                child.styleProperty().bind(user.profileStatusProperty());
            }
            if (child instanceof Button && child.getId().equals("declineContact")){
                child.visibleProperty().bind(user.isInboundRequestProperty());
                if (user.getIsInboundRequest()){
                    ((Button) child).setOnAction(event -> {
                        try {
                            tcpConnection.respondToFriendRequest(user.getName(), false);
                            contactList.removeUser(user);
                            Platform.runLater(()->contactListBox.getChildren().remove(pane));

                        } catch (SocketException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
            if (child instanceof Button && child.getId().equals("addUserToContacts")){
               child.visibleProperty().bind(user.isInboundRequestProperty());

                if (user.getIsInboundRequest()){

                    ((Button) child).setOnAction(event -> {
                        try {
                            user.setIsConfirmed();
                            tcpConnection.respondToFriendRequest(user.getName(), true);


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }

        pane.styleProperty().bind(user.profileStyleProperty());

        pane.setOnMouseClicked(mouseEvent -> {
            if(contactList.getCurrentUser()!= user) {

                mainPane.setCenter(user.getChatScene().getRoot());

                contactList.getCurrentUser().setProfileStyle("");
                contactList.setCurrentUser(user);
                contactList.getCurrentUser().setProfileStyle("-fx-background-color: rgb(102, 102, 153);");
            }

        });
    }


}
