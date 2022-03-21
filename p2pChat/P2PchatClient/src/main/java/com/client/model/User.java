package com.client.model;

import com.client.ChatApp;
import com.client.mainFrameController.ChatController;
import com.client.mainFrameController.MainFrameController;
import com.client.mainFrameController.ProfileController;
import com.client.service.UdpConnection;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;


public class User  {


    private final StringProperty name = new SimpleStringProperty();
    public final StringProperty nameProperty() { return this.name;}
    public final String getName() {return this.name.get();}
    public final void setName(String name){
      nameProperty().set(name);
    }

    private final IntegerProperty port = new SimpleIntegerProperty();
    public final IntegerProperty portProperty() { return this.port;}
    public final Integer getPort() {return this.port.get();}
    public final void setPort(Integer port){this.portProperty().set(port);}

    private final StringProperty userStatus = new SimpleStringProperty();
    public final StringProperty userStatusProperty() { return this.userStatus;}
    public final void setUserStatus(String status){
        Platform.runLater(() -> userStatusProperty().set(status));
    }

    private final StringProperty profileStatus = new SimpleStringProperty();
    public final StringProperty profileStatusProperty() { return this.profileStatus;}
    public final void setProfileStatus(String status){
        Platform.runLater(() -> profileStatusProperty().set(status));
    }

    private final StringProperty ip = new SimpleStringProperty();
    public final StringProperty ipProperty() { return this.ip;}
    public final String getIp() {return this.ip.get();}
    public final void setIp(String ip){this.ipProperty().set(ip);}

    private final StringProperty profileStyle = new SimpleStringProperty();
    public final StringProperty profileStyleProperty() { return this.profileStyle;}
    public final void setProfileStyle(String ip){this.profileStyleProperty().set(ip);}

    public final BooleanProperty isConnectionEstablished = new SimpleBooleanProperty();
    public final BooleanProperty isConnectionEstablishedProperty(){ return this.isConnectionEstablished;}
    public final Boolean getIsConnectionEstablished() { return this.isConnectionEstablished.get();}
    public final void setIsConnectionEstablished(Boolean status) {
        this.isConnectionEstablishedProperty().set(status);
        if(status){
            this.setUserStatus("online");
            this.setProfileStatus("-fx-fill: #48bc4e; -fx-stroke: black;");
        }
        else{
            if(!getIsConfirmed())
                this.setUserStatus("pending");
            else
                this.setUserStatus("offline");
            this.setProfileStatus("-fx-fill: #c62222; -fx-stroke: black;");
        }
    }

    private final BooleanProperty isOutboundRequest = new SimpleBooleanProperty();
    public final BooleanProperty isOutboundRequestProperty() { return this.isOutboundRequest;}
    public boolean getIsOutboundRequest() {return this.isOutboundRequest.get();}
    public void setIsOutboundRequest(boolean status) {this.isOutboundRequestProperty().set(status);}


    private final BooleanProperty isInboundRequest = new SimpleBooleanProperty();
    public final BooleanProperty isInboundRequestProperty() { return this.isInboundRequest;}
    public boolean getIsInboundRequest() {return this.isInboundRequest.get();}
    public void setIsInboundRequest(boolean status) {this.isInboundRequestProperty().set(status);}

    private final BooleanProperty isSearchResult = new SimpleBooleanProperty();
    public final BooleanProperty isSearchResultProperty() { return this.isSearchResult;}
    public boolean getIsisSearchResult() {return this.isSearchResult.get();}
    public void setIsSearchResult(boolean status) {this.isSearchResultProperty().set(status);}

    public void setOnline(boolean status) {this.isOnline = status;}
    public boolean getIsOnline() {return this.isOnline;}

    public boolean getConnectionPending() {return this.connectionPending;}
    public void setConnectionPending(boolean status) {this.connectionPending = status;}

    private final BooleanProperty isConfirmed = new SimpleBooleanProperty();
    public final BooleanProperty isConfirmedProperty() { return this.isConfirmed;}
    public boolean getIsConfirmed() {return this.isConfirmed.get();}
    public void setIsConfirmed() {
        this.isConfirmedProperty().set(true);
        Platform.runLater(()->this.setIsOutboundRequest(false));
        Platform.runLater(()->this.setIsInboundRequest(false));
        this.setIsConnectionEstablished(false);
    }

    private final BooleanProperty messageUnread = new SimpleBooleanProperty();
    public final BooleanProperty messageUnreadProperty(){return this.messageUnread;}
    public boolean getMessageUnread(){return this.messageUnread.get();}
    public void setMessageUnread(boolean status) {this.messageUnread.set(status);}



    //chat controller

    public final Pane getChatPane() {return chatPane;}

    private void initializeChatController() throws IOException {
        FXMLLoader loader = new FXMLLoader(ChatApp.class.getResource("/chat.fxml"));
        chatPane = loader.load();
        chatController = loader.getController();
        chatController.setUser(this);
    }

    public void loadChat(){
        mainFrameController.loadChat(this);
    }

    public void displayMessage(String message){
        chatController.displayMessage(message);
    }

    public void sendUdpMessage(String message){udpConnection.sendMessage(message);}



    //profile controller
    public final Pane getProfilePane() {return profilePane;}

    private void initializeProfileController() throws IOException {
        FXMLLoader loader = new FXMLLoader(ChatApp.class.getResource("/profile.fxml"));
        profilePane = loader.load();
        profileController = loader.getController();
        profileController.setUser(this);
    }

    public void declineUsersRequest(){
        mainFrameController.declineRequest(this);
    }

    public void approveRequest(){
        setIsConfirmed();
        setIsConnectionEstablished(false);
        mainFrameController.approveRequest(this);
    }

    public void sendRequest() {
        setIsSearchResult(false);
        setIsOutboundRequest(true);
        mainFrameController.sendRequest(this);
    }

    public void setMainFrameController(MainFrameController mainFrameController){
        this.mainFrameController = mainFrameController;
    }

    public boolean getIsFocused(){return this.isFocused;}
    public void setIsFocused(boolean status) {
        this.isFocused = status;
        setMessageUnread(false);
    }



    //UDP service
    //for direct connection

    public void openUdpConnection(int port)  {
        udpConnection = new UdpConnection(port);
        udpConnection.setUser(this);
        setPort(port);
    }

    public void startCommunication(){

        udpConnection.listenForMessage();
        udpConnection.pingContact();
    }


    //for server connection
    public void openUdpConnection() {
        udpConnection = new UdpConnection();
        udpConnection.setUser(this);
        setPort(udpConnection.getPort());
    }

    public void initializeCommunication(String ip){
        setOnline(true);

        setIp(ip);
        openUdpConnection();
        this.setConnectionPending(true);
    }

    public void startCommunication(int port){
        try {
            udpConnection.setPort(port);
            udpConnection.setSocketOpen(true);
            this.setConnectionPending(false);
            udpConnection.listenForMessage();
            udpConnection.pingContact();

        }catch (Exception ignored){}
    }

    public void closeUdpConnection(){
        if(udpConnection != null){
            udpConnection.closeSocket();
            udpConnection=null;
        }
    }

    //Constructors

    public User(String ip, int port) throws IOException {
        setName(ip);
        setIp(ip);
        setPort(port);
        initializeChatController();
        initializeProfileController();
        setIsConnectionEstablished(false);
    }

    public User(String name) throws IOException {
        setName(name);
        setIp("");
        initializeChatController();
        initializeProfileController();
        setIsConnectionEstablished(false);
    }


    private UdpConnection udpConnection;

    private boolean isOnline;
    private boolean connectionPending;
    public boolean isFocused;

    private Pane chatPane;
    private ChatController chatController;

    private Pane profilePane;
    private ProfileController profileController;

    private MainFrameController mainFrameController;



}
