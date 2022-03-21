package com.client.mainFrameController;

import com.client.ChatApp;
import com.client.loginWindowController.LoginController;
import com.client.model.ContactList;
import com.client.model.User;
import com.client.service.TcpConnection;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainFrameController implements Initializable {
    @FXML
    private ImageView clearTextButton;
    @FXML
    private ImageView logoutIcon;
    @FXML
    private Pane backgroundPane;
    @FXML
    private ScrollPane scrollableLeftPane;
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

    private Stage stage;
    private ContactList contactList;
    private TcpConnection tcpConnection;
    private boolean searchStarted;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        stage = ChatApp.getMainStage();
        stage.setOnHiding(event -> {
            if(tcpConnection!=null)
                tcpConnection.exit();
            contactList.clearAndClose();
        });
        initLogoutIcon();
    }

    public void initUserSearch() {

        scrollableLeftPane.heightProperty().addListener(event ->
                backgroundPane.setPrefHeight(scrollableLeftPane.getHeight()));

        searchResultBox.setTranslateX(-200);
        searchResultNotification.setVisible(false);
        searchResultBox.getChildren().clear();

        if(this.tcpConnection != null){
            searchBar.setText("");
        }
        else {
            searchBar.setText("search disabled");
            searchBar.setDisable(true);
            return;
        }

        clearTextButton.setOnMouseClicked(event-> searchBar.setText(""));


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

    private void initLogoutIcon(){
        logoutIcon.setOnMouseClicked(event-> {
            try {
                logout();
            } catch (IOException ignored) {}
        });
    }

    public void setContactsModel(ContactList contactList){
        this.contactList = contactList;
    }

    public void setUserName(String userName){
        this.userName.setText(userName);
    }

    public void setTcpConnection(TcpConnection tcpConnection) {
        this.tcpConnection = tcpConnection;
        tcpConnection.setMainController(this);
        tcpConnection.requestPendingFriends();
        tcpConnection.requestContactData();
        tcpConnection.sendAlive();
    }


    @FXML
    public void logout() throws IOException {

        contactList.clearAndClose();

        FXMLLoader loader = new FXMLLoader(ChatApp.class.getResource("/login.fxml"));
        Scene scene = new Scene(loader.load());

        LoginController loginController = loader.getController();
        if (tcpConnection != null) {
            tcpConnection.logout();
            loginController.initializeTcpConnection(this.tcpConnection);
        } else
            loginController.initializeTcpConnection();


        stage.setResizable(false);
        stage.setTitle("login");
        stage.setScene(scene);
        stage.show();
    }


    public void displaySearchResult(ArrayList<String> searchResult) throws IOException {
        contactList.trimSearchResult(searchResult, userName.getText());
        if(searchResult.isEmpty()) {
            Platform.runLater(() -> searchResultNotification.setText("no users found"));
            searchResultNotification.setVisible(true);
            return;
        }
        searchResultNotification.setVisible(false);
        User user;
        for (String name : searchResult) {
                user = new User(name);
                user.setIsSearchResult(true);
                user.setMainFrameController(this);

                addProfileToSearchList(user);
            }
    }


    public void loadContacts() {
        for (User user : contactList.getContacts())
            loadSingleContact(user);

    }

    public void loadSingleContact(User user) {
        user.setMainFrameController(this);
        addProfileToContactList(user);

    }

    public void addProfileToContactList(User user){
        Platform.runLater(() -> contactListBox.getChildren().add(user.getProfilePane()));
    }

    public void removeProfileFromContactList(User user){
        Platform.runLater(()->contactListBox.getChildren().remove(user.getProfilePane()));

    }

    public void addProfileToSearchList(User user){
        Platform.runLater(() -> searchResultBox.getChildren().add(user.getProfilePane()));
    }

    public void removeProfileFromSearchList(User user){
        searchResultBox.getChildren().remove(user.getProfilePane());
    }

    public void declineRequest(User user){
        tcpConnection.respondToFriendRequest(user.getName(), false);
        contactList.removeUser(user);
        removeProfileFromContactList(user);
    }

    public void approveRequest(User user){
        tcpConnection.respondToFriendRequest(user.getName(), true);
    }

    public void sendRequest(User user){

        removeProfileFromSearchList(user);

        if(searchResultBox.getChildren().isEmpty())
            searchBar.setText("");

        contactList.addUser(user);
        tcpConnection.friendRequest(user.getName());
        loadSingleContact(user);
    }

    public void loadChat(User user){
        if(contactList.getCurrentUser()!= user) {

            mainPane.setCenter(user.getChatPane());
            if(contactList.getCurrentUser()!=null){
                contactList.getCurrentUser().setIsFocused(false);
                contactList.getCurrentUser().setProfileStyle("");
            }
            contactList.setCurrentUser(user);
            user.setIsFocused(true);
            user.setProfileStyle("-fx-background-color: #666699;");
        }
    }

}
