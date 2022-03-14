package com.server.serverHandler;

import com.server.VerificationService;
import com.server.models.ContactModel;
import com.server.services.ContactService;
import com.server.services.UserService;
import com.server.models.UserModel;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> userList = new ArrayList<>();
    private String clientUsername;

    private boolean isLoggedIn;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    public UserModel currentUser;
    public UserModel prevUser;
    public String userIP;
    UserService userServiceGLBL;
    ContactService contactServiceGLBL;

    final String LOG_USER = "LOG";
    //login request:
    //client: LOG + " " + userName + " " + password
    //server: LOG + " " + TRUE/FALSE

    final String REG_USER = "REG";
    //register request:
    //client: REG + " " + newUserName + " " + password
    //server: REG + " " + TRUE/FALSE

    final String USR_REQUEST = "USR";
    //requesting user data:
    //client: USR + " " + userName
    //server: USR + " " + userName + " " + IP + " " + userIp + " " + PORT + " " + userPort
    //or
    //server: USR + " " + userName + " " + OFF

    final String EXIT = "EXT";
    //logging off
    //client: EXT + " " + GG

    final String SEARCH = "SRH";
    //user search
    //client: SRH + " " + userName;
    //server: SRH + " " + userName + " " + TRUE/FALSE

    final String ADD_CONTACT = "ADD";
    //confirmation to add to contact list
    //client: ADD + " " + userName
    //server: ADD + " " + userName

    final String PORT = "PRT";
    //send port info
    //client: PRT + " " + portNum;

    final String FRIENDS = "FRN";
    //client: FRN
    //server: ADD + " " + userName

    final String PING = "PNG";
    //client: PNG GG





    public ClientHandler(Socket socket, UserService userService, ContactService contactService) {

        userServiceGLBL = userService;
        contactServiceGLBL = contactService;

        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String userIPNotParsed = String.valueOf(socket.getInetAddress());
            userIP = userIPNotParsed.substring(1);
            while(!isLoggedIn) {
                String messageLOGorREG = bufferedReader.readLine();


                System.out.println("Message received from Client: " + messageLOGorREG);

                String[] loginMessage = new String[3];   //Make the index free
                try {
                    loginMessage = messageLOGorREG.split("_");

                } catch (Exception e) {
                    System.err.println("Message String not parsed Correctly");
                }


                switch (loginMessage[0]) {

                    case LOG_USER:
                        logInUser(userServiceGLBL, loginMessage);
                        break;

                    case REG_USER:

                        registerUser(userServiceGLBL, loginMessage);
                        break;

                    default:

                        sendToUser("LOG_USER or REG_USER Message Received Incorrectly.");
                        break;
                }
            }

        } catch (IOException e) {
            System.err.println("ERROR: LOG_USER or REG_USER Message Received Incorrectly.");
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        listenTCP();
    }

    public void listenTCP () {

        while (socket.isConnected()) {
            try {

                //reads incoming messages via TCP from Client
                String clientMessage = bufferedReader.readLine();

                //Prints User requests from the server
                System.out.println("Message received form Client "+ currentUser.getUserName() +": " + clientMessage);

                //splits the messages to keywords NAME name PORT port IP ipAddress
                String[] message = clientMessage.split("_");


                switch(message[0]) {

                    case LOG_USER:

                        logInUser(userServiceGLBL, message);
                        break;

                    case REG_USER:

                        registerUser(userServiceGLBL, message);
                        break;

                    case USR_REQUEST:

                        requestUser(message);
                        break;

                    case PING:

                        System.out.println(currentUser.getUserName() + " Pinged");
                        break;

                    case SEARCH:

                        searchForUsers(message);
                        break;

                    case FRIENDS:

                        sendContactsToClient();
                        break;

                    case PORT:

                        //userServiceGLBL.updateUserPortAndIpAddress(currentUser, Integer.valueOf(message[1]), userIP, currentUser.getId());
                        sendMessageToOtherOnlineUser("PRT_" + currentUser.getUserName() + "_" + message[2] ,message[1]);
                        break;

                    case ADD_CONTACT:


                        addContact(message);
                        break;

                    case EXIT:

                        exitCleanup();
                        break;

                    default:

                        System.err.println("Something is wrong with the Message_types");
                        sendToUser("Something is wrong with the Message_types");
                        break;

                }

            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }


    }


    public void addContact(UserModel userModel, String name, String status) {

        ContactModel contactModel = new ContactModel(null, name, status, userModel);

        contactServiceGLBL.addContact(contactModel, userModel);

    }

    public void sendContactToOtherUsersContactList(String name, String status) {

        String nameToAdd = currentUser.getUserName();

        UserModel userToSendTo = userServiceGLBL.findUserModelByName(name);

        ContactModel contactModel = new ContactModel(null, nameToAdd, status, userToSendTo);

        contactServiceGLBL.addContact(contactModel, userToSendTo);

        System.out.println("Client: " + currentUser.getUserName() + " was added with status: " + status + " to user: " + userToSendTo);

    }


    public void logInUser(UserService userService, String[] message) throws IOException {

        try {
            prevUser =userService.findUserModelByName(message[1]);
            if (userService.checkPassword(prevUser, message[2])) {  //password logic
                currentUser = new UserModel(null, message[1], null, userIP, prevUser.getPassword());
                userService.updateUserPortAndIpAddress(currentUser, null, userIP, prevUser.getId());
                this.clientUsername = currentUser.getUserName();
                userList.add(this);
                sendToUser("LOG_TRUE");
                isLoggedIn=true;
            } else {
                sendToUser("LOG_FALSE");
            }
        } catch (Exception e) {
            sendToUser("LOG_FALSE");
            System.err.println("Exception while logging in a User: " + message[1]);
        }


    }


    public void registerUser(UserService userService, String[] message) throws IOException {
        try {
            //Sets the user to be with the unhashed password
            currentUser = new UserModel(null, message[1], null, userIP, message[2]);
            System.out.println("1. REG PWD: " + currentUser.getPassword());
            //Adds the user and hashes the password
            userService.addUser(currentUser);
            //Receives the user userModel with the hashed password version
            currentUser = userService.findUserModelByName(message[1]);
            System.out.println("2. REG PWD: " + currentUser.getPassword());
            this.clientUsername = currentUser.getUserName();
            userList.add(this);
            isLoggedIn=true;
            sendToUser("REG_TRUE");
        } catch (Exception e) {
            sendToUser("REG_FALSE");
            System.err.println("Exception while registering a new User " + message[1]);
        }
    }


    public void requestUser(String[] message) throws IOException {

        //Finds the user
        String response;
        try {
            UserModel contactInfo = userServiceGLBL.findUserModelByName(message[1]);

            if (VerificationService.verifyIp(contactInfo.getIpAddress())) {
                response = "USR_" + contactInfo.getUserName() + "_TRUE_" + contactInfo.getIpAddress();
            } else {
                response = "USR_" + contactInfo.getUserName() + "_FALSE";
            }

        } catch (Exception e) {
            response = "USR_" + message[1] + "_FAILED";
        }
        sendToUser(response);
        System.out.println("Sending USR response: " + response);

    }

    public void searchForUsers(String[] message) throws IOException {
        try {
            String userStringToSend = "SRH";
            List<UserModel> usersFound = userServiceGLBL.findUsersWithPartOfName(message[1]);
            if (usersFound.isEmpty()) {
                sendToUser("SRH_FALSE");
            } else {
                for (UserModel userFound : usersFound) {
                    userStringToSend = userStringToSend.concat("_" + userFound.getUserName());
                }
                sendToUser(userStringToSend);
            }
            System.out.println("SEARCH: User List sent to user " + currentUser.getUserName());
        } catch (Exception e) {
            System.err.println("SRH failed: No user by the name: " + message[1]);
            sendToUser("SRH_FALSE");
        }
    }


    public void sendToUser(String message) throws IOException {
        try {
            bufferedWriter.flush();
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {

        }
    }

    public void addContact(String[] message) {

        if (message.length == 2) {

            sendContactToOtherUsersContactList(message[1], "pending");
            sendMessageToOtherOnlineUser("ADD_" + currentUser.getUserName(), message[1]);

        } else if (message.length == 3 && message[2].equals("TRUE")) {

            ContactModel changeContactStatus = contactServiceGLBL.getContactModelByUserModelIdAndUserNameAndStatus(currentUser.getId(), message[1], "pending");

            contactServiceGLBL.updateContactStatus(changeContactStatus, "approved");
            sendContactToOtherUsersContactList(message[1], "approved");

            sendMessageToOtherOnlineUser("ADD_" + currentUser.getUserName() + "_TRUE", message[1]);

        } else if (message.length == 3 && message[2].equals("FALSE")) {

            ContactModel changeContactStatus = contactServiceGLBL.getContactModelByUserModelIdAndUserNameAndStatus(currentUser.getId(), message[1], "pending");

            contactServiceGLBL.updateContactStatus(changeContactStatus, "blocked");
            sendContactToOtherUsersContactList(message[1], "blocked");

            sendMessageToOtherOnlineUser("ADD_" + currentUser.getUserName() + "_FALSE", message[1]);

        } else {
            System.err.println("Something wrong with the addContact() method");
        }


    }

    public void exitCleanup() {

        if (currentUser != null) {
        userServiceGLBL.updateUserPortAndIpAddress(currentUser, null, null, currentUser.getId());
        System.err.println("User " + currentUser.getUserName() + " had his port and IP removed form DB" ); }

    }

    public void sendContactsToClient() throws IOException {
        List<ContactModel> contacts = contactServiceGLBL.getAllContactsByUserModelId(currentUser.getId());

        for (ContactModel contact : contacts) {
            if (contact.getStatus().equals("pending")) {
                sendToUser("FRN_" + contact.getContactName() + "_IN");
            } else {
                sendToUser("FRN_" + contact.getContactName() + "_" + contact.getStatus());
            }
        }

        List<ContactModel> outBoundContacts = contactServiceGLBL.getContactModelByUserModelIdAndStatus(currentUser.getUserName(), "pending");

        for (ContactModel outBoundContact : outBoundContacts) {

            UserModel temp = outBoundContact.getUserModel();

            sendToUser("FRN_" + temp.getUserName() + "_OUT");
        }
    }


    public void sendMessageToOtherOnlineUser(String messageToSend, String userName) {

        for (ClientHandler clientHandler : userList) {

            try {
                if (clientHandler.clientUsername == null || clientHandler.clientUsername.isEmpty()) {

                } else {

                if (clientHandler.clientUsername.equals(userName) && !clientHandler.clientUsername.equals(currentUser.getUserName())) {
                    clientHandler.bufferedWriter.flush();
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                } }

            } catch (IOException e) {
                System.out.println("PROBLEM SENDING THE MESSAGE TO OTHER USER");
                //closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {

        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
                exitCleanup();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
