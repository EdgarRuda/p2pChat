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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
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
            tcpConnection.exit();
            contactList.clearAndClose();
        });
        initLogoutIcon();
    }

    public void initUserSearch() {

        scrollableLeftPane.requestFocus();
        scrollableLeftPane.heightProperty().addListener(event ->
                backgroundPane.setPrefHeight(scrollableLeftPane.getHeight()));

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
                    if (child instanceof Circle && child.getId().equals("messageUnreadStatus"))
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
        for (User contact : contactList.getContacts()) {
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
            if (child.getId().equals("messageUnreadStatus"))
                child.visibleProperty().bind(user.messageUnreadProperty());
            if (child.getId().equals("statusCircle")){
                child.visibleProperty().bind(user.isConfirmedProperty());
                child.styleProperty().bind(user.profileStatusProperty());
            }
//            if (child instanceof Pane && child.getId().equals("focusPane")) {
//                child.setOnMouseEntered(event -> child.setStyle("-fx-background-color: #666699;"));
//                child.setOnMouseExited(event -> child.setStyle(""));
//            }
            if (child.getId().equals("declineContact")){
                child.visibleProperty().bind(user.isInboundRequestProperty());
                if (user.getIsInboundRequest()){
                    child.setOnMouseClicked(event -> {

                            tcpConnection.respondToFriendRequest(user.getName(), false);
                            contactList.removeUser(user);
                            Platform.runLater(()->contactListBox.getChildren().remove(pane));
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
                if(contactList.getCurrentUser()!=null){
                    contactList.getCurrentUser().setIsFocused(false);
                    contactList.getCurrentUser().setProfileStyle("");
                }
                contactList.setCurrentUser(user);
                user.setIsFocused(true);
                user.setProfileStyle("-fx-background-color: #666699;");
            }

        });
    }


}
