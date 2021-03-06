package com.client.service;

import com.client.domain.Server;
import com.client.mainFrameController.MainFrameController;
import com.client.model.ContactList;
import com.client.model.User;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class TcpConnection {


    private Socket socket;
    public BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private ContactList contactList;
    private ArrayList<String> searchResult;

    private final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    private MainFrameController mainFrameController;

    private static final StringProperty status = new SimpleStringProperty();
    public static StringProperty statusProperty() {
        return status;
    }
    public final void setStatus(String status) {
        Platform.runLater(() -> statusProperty().set(status));
        timeStamp();
        System.out.println(status);
    }

    private static final StringProperty registrationStatus = new SimpleStringProperty();
    public static StringProperty registrationStatusProperty() {
        return registrationStatus;
    }
    public final void setRegistrationStatus(String status) {Platform.runLater(() -> registrationStatusProperty().set(status));}

    private static final BooleanProperty isConnected = new SimpleBooleanProperty();
    public static BooleanProperty IsConnectedProperty() {
        return isConnected;
    }
    public static Boolean getIsConnected() {
        return isConnected.get();
    }
    public final void setIsConnected(boolean status) {
        IsConnectedProperty().set(status);
        timeStamp();
        System.out.println("IS CONNECTED: " + status);
    }

    private final BooleanProperty loggedIn = new SimpleBooleanProperty();
    public BooleanProperty loggedInProperty() {
        return loggedIn;
    }
    public Boolean getLoggedIn() {
        return loggedIn.get();
    }
    public final void setLoggedIn(boolean status) {
        Platform.runLater(() -> loggedInProperty().set(status));
        timeStamp();
        System.out.println("IS LOGGED IN: " + status);
    }

    private static final BooleanProperty connectionStarted = new SimpleBooleanProperty();
    public static BooleanProperty connectionStartedProperty() {
        return connectionStarted;
    }
    public Boolean getConnectionStarted() {
        return connectionStarted.get();
    }
    public final void setConnectionStarted(boolean status) {
        Platform.runLater(() -> connectionStartedProperty().set(status));
        if(status){
        timeStamp();
        System.out.println("CONNECTION STARTED");
        }
    }


    final String LOG_USER = "LOG";
    final String REG_USER = "REG";
    final String USR_REQUEST = "USR";
    final String EXIT = "EXT";
    final String SEARCH = "SRH";
    final String ADD = "ADD";
    final String PRT = "PRT";
    final String FRN = "FRN";


    public TcpConnection() {
        setStatus("connecting to server..");
        setIsConnected(false);
        setConnectionStarted(true);
        try {

            this.socket = new Socket(Server.IP, Server.TCP_PORT);
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            setIsConnected(true);
            setStatus("connection established");
            setRegistrationStatus("");
            Thread listener = new Thread(this::listenFromServer);
            listener.start();
            setConnectionStarted(false);


        } catch (IOException e) {
            closeEverything();
        }
    }

    //Getters & Setters
    public void setContactsModel(ContactList contactList) {
        this.contactList = contactList;
    }

    public void setMainController(MainFrameController mainFrameController) {
        this.mainFrameController = mainFrameController;
    }
    //

    public void timeStamp() {
        System.out.print("[" + format.format(new Date()) + "] ");
    }

    public void tryToLogin(String userName, String password) {
        timeStamp();
        System.out.println(LOG_USER + "_" + userName + "_" + password);
        sendToServer(LOG_USER + "_" + userName + "_" + password);
    }

    public void registerUser(String userName, String pass){
        timeStamp();
        System.out.println(REG_USER + "_" + userName + "_" + pass);
        sendToServer(REG_USER + "_" + userName + "_" + pass);
    }

    public void searchUser(String userName) throws SocketException {
        timeStamp();
        System.out.println(SEARCH + "_" + userName);
        searchResult.clear();
        sendToServer(SEARCH + "_" + userName);
    }

    public void friendRequest(String userName) {
        timeStamp();
        System.out.println(ADD + "_" + userName);
        sendToServer(ADD + "_" + userName);
    }

    public void respondToFriendRequest(String userName, boolean accepted){
        timeStamp();
        String message = ADD + "_" + userName + "_" + String.valueOf(accepted).toUpperCase();
        System.out.println(message);
        sendToServer(message);
    }

    public void requestPendingFriends() {
        timeStamp();
        System.out.println(FRN);
        sendToServer(FRN);
    }

    private void initializeCommunication(String name, String ip) {
        contactList.getUser(name).initializeCommunication(ip);
        sendPortData(name, contactList.getUser(name).getPort());
    }

    public void exit() {
        timeStamp();
        System.out.println(EXIT);
        sendToServer(EXIT);
        closeEverything();
    }

    public void logout() {

        setLoggedIn(false);

        timeStamp();
        System.out.println(EXIT);
        sendToServer(EXIT);

    }


    public void sendPortData(String name, int port) {
        new Thread(() -> {
            int sendLastTime = 5;
            try {
                while (contactList.getUser(name).getConnectionPending() || sendLastTime != 0) {

                    timeStamp();
                    System.out.println(PRT + "_" + name + "_" + port);
                    sendToServer(PRT + "_" + name + "_" + port);
                    if (!contactList.getUser(name).getConnectionPending()) {
                        sendLastTime--;
                    }
                    Thread.sleep(5000);
                }
            } catch (Exception ignored) {
            }
        }).start();


    }

    public void sendAlive() {
        new Thread(() -> {
            try {
                while (getIsConnected()) {
                    sendToServer("PNG");
                    Thread.sleep(10000);
                }
            } catch (Exception ignored) {
            }
        }).start();

    }

    public void requestContactData() {
        new Thread(() -> {
            try {
                while (getIsConnected() && getLoggedIn()) {
                    for (User contact : contactList.getContacts())
                        if (contact.getIsConfirmed()) {
                            timeStamp();
                            System.out.println(USR_REQUEST + "_" + contact.getName());
                            sendToServer(USR_REQUEST + "_" + contact.getName());
                        }
                    Thread.sleep(10000);
                }
            } catch (Exception ignored) {
            }
        }).start();

    }


    private void sendToServer(String message) {
        try {
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (Exception e) {
            closeEverything();
        }
    }


    public void listenFromServer() {
        searchResult = new ArrayList<>();
        timeStamp();
        System.out.println("listening..");

        String messageFromServer;
        String[] messageArray;
        boolean hasNull;

        while (getIsConnected()) {
            try {
                hasNull = false;
                messageFromServer = bufferedReader.readLine();

                if (messageFromServer != null && !messageFromServer.isEmpty()) {
                    messageArray = messageFromServer.split("_");
                } else continue;


                for (String s : messageArray)
                    if (s == null) {
                        hasNull = true;
                        break;
                    }

                if (hasNull) continue;

                timeStamp();
                System.out.println(Arrays.toString(messageArray));

                switch (messageArray[0]) {
                    case LOG_USER: {
                        if (messageArray[1].equals("TRUE"))
                            if(!getLoggedIn()){
                                setLoggedIn(true);
                             }

                        if (messageArray[1].equals("FALSE"))
                            setStatus("login refused");

                        break;
                    }

                    case REG_USER: {
                        if (messageArray[1].equals("TRUE")){
                            setRegistrationStatus("logging in..");
                            setLoggedIn(true);
                        }
                        if (messageArray[1].equals("FALSE"))
                            setRegistrationStatus("registration refused");
                        break;
                    }

                    case USR_REQUEST: {
                        if (messageArray[2].equals("FALSE")) {
                            if (contactList.getUser(messageArray[1]).getIsOnline())
                                contactList.getUser(messageArray[1]).setOnline(false);
                                contactList.getUser(messageArray[1]).setConnectionPending(false);
                            break;
                        } else {
                            if (VerificationService.verifyIp(messageArray[3]))
                                if (!contactList.getUser(messageArray[1]).getIsOnline())
                                    initializeCommunication(messageArray[1], messageArray[3]);
                        }
                        break;

                    }

                    case SEARCH: {
                        if (!messageArray[1].equals("FALSE")) {
                            searchResult.addAll(Arrays.asList(messageArray));
                        }
                        mainFrameController.displaySearchResult(searchResult);
                        break;
                    }

                    case ADD: {
                        if (messageArray.length == 2) {
                            User user = new User(messageArray[1]);
                            user.setIsInboundRequest(true);
                            contactList.addUser(user);
                            mainFrameController.loadSingleContact(user);
                            break;
                        }
                        if (messageArray[2].equals("TRUE")) {
                            contactList.getUser(messageArray[1]).setIsConfirmed();
                            break;
                        }
                        if (messageArray[2].equals("FALSE")) {
                            mainFrameController.removeProfileFromContactList(contactList.getUser(messageArray[1]));
                            contactList.removeUser(messageArray[1]);
                            break;
                        }
                    }

                    case PRT: {
                        if (contactList.getUser(messageArray[1]).getIsConnectionEstablished())
                            continue;
                        if (contactList.getUser(messageArray[1]).getConnectionPending())
                            contactList.getUser(messageArray[1]).startCommunication(Integer.parseInt(messageArray[2]));
                        break;
                    }

                    case FRN: {
                        User user = new User(messageArray[1]);
                        switch (messageArray[2]) {
                            case "approved":
                                user.setIsConfirmed();
                                user.setIsConnectionEstablished(false);
                                break;
                            case "IN":
                                user.setIsInboundRequest(true);
                                user.setIsConnectionEstablished(false);
                                break;
                            case "OUT":
                                user.setIsOutboundRequest(true);
                                user.setIsConnectionEstablished(false);
                                break;
                            default:
                                break;
                        }
                        contactList.getContacts().add(user);
                        mainFrameController.loadSingleContact(user);
                        break;

                    }
                    default:
                        break;
                }


            } catch (Exception e) {
                closeEverything();
                break;
            }
        }
        timeStamp();
        System.out.println("TCP LISTENER WAS CLOSED");
        closeEverything();

    }


    public void closeEverything() {
        if (getIsConnected()) {
            timeStamp();
            System.out.println("SOCKET CLOSED");
            setStatus("disconnected");

            if (getIsConnected()) setIsConnected(false);
            if (getLoggedIn()) setLoggedIn(false);
            if (getConnectionStarted()) setConnectionStarted(false);

            try {
                if (this.socket != null) {
                    socket.close();
                }
                if (this.bufferedReader != null) {
                    bufferedReader.close();
                }
                if (this.bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
