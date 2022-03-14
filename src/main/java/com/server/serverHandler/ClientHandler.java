package com.server.serverHandler;

import com.server.VerificationService;
import com.server.models.ContactModel;
import com.server.services.ContactService;
import com.server.services.UserService;
import com.server.models.UserModel;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * The main functioning class of the application
 * There are two main storage solutions:
 * 1. One is the userList, that stores the online users. This is required for message sending between logged in clients
 * 2. Main storage where the userModel, that is essentially userData is stored and another table that has linked contacts
 *
 * The class has two main operating blocks:
 * 1. The first is the ClientHandler constructor, that takes the logged-in users information and stores it into userList
 * 2. The second is the run() method, that acts as a switch statement that takes in messages from the server interprets
 * them and sends another one back
 *
 * The application of the switch statement can be seen in the final String section with LOG_USER, REG_USER, etc.
 *
 * The Client handler operates on a Thread therefore the socket bufferedReader and bufferedWriter need to be maintained
 * via the close everything method.
 *
 * closeEverything also clears up the ipAddresses of users that were logged in essence securing their data.
 */
public class ClientHandler implements Runnable {

    private final Logger logger = Logger.getLogger(this.getClass());

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
                String clientMessage = bufferedReader.readLine();
                logger.info("Message received form Client "+ currentUser.getUserName() +": " + clientMessage);

                String[] loginMessage = new String[3];   //Make the index free
                try {
                    loginMessage = clientMessage.split("_");

                } catch (Exception e) {
                    logger.error("Message String not parsed Correctly");
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
            logger.error("LOG_USER or REG_USER Message Received Incorrectly.");
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
                logger.info("Message received form Client "+ currentUser.getUserName() +": " + clientMessage);
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
                        break;

                    case SEARCH:
                        searchForUsers(message);
                        break;

                    case FRIENDS:
                        sendContactsToClient();
                        break;

                    case PORT:
                        sendMessageToOtherOnlineUser("PRT_" + currentUser.getUserName() + "_" + message[2] ,message[1]);
                        break;

                    case ADD_CONTACT:


                        addContact(message);
                        break;

                    case EXIT:

                        exitCleanup();
                        break;

                    default:

                        logger.error("Something is wrong with the Message_types");
                        sendToUser("Something is wrong with the Message_types");
                        break;

                }

            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }


    }


    /**
     * @param name specifies the username to which the message is sent to
     * @param status specifies the status that must be placed: pending, accepted, blocked
     */
    public void sendContactToOtherUsersContactList(String name, String status) {

        String nameToAdd = currentUser.getUserName();

        UserModel userToSendTo = userServiceGLBL.findUserModelByName(name);

        ContactModel contactModel = new ContactModel(null, nameToAdd, status, userToSendTo);

        contactServiceGLBL.addContact(contactModel, userToSendTo);

        logger.info("Client: " + currentUser.getUserName() + " was added with status: " + status + " to user: " + userToSendTo);

    }


    /**
     * @param userService specifies that the user service will be called in this method
     * @param message is the input from client that is processed. Index 1 is: the client name Index 2 is: the password
     * @throws IOException because of the use of bufferedWriter to send the response to client.
     */
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
                logger.info("User " + message[1] + " logged in.");
            } else {
                sendToUser("LOG_FALSE");
            }
        } catch (Exception e) {
            sendToUser("LOG_FALSE");
            logger.error("Exception while logging in a User: " + message[1]);
        }


    }


    /**
     * @param userService specifies that the user service will be called in this method
     * @param message is the input from client that is processed. Index 1 is: the client name Index 2 is: the password
     * @throws IOException because of the use of bufferedWriter to send the response to client.
     */
    public void registerUser(UserService userService, String[] message) throws IOException {
        try {
            //Sets the user to be with the unhashed password
            currentUser = new UserModel(null, message[1], null, userIP, message[2]);
            //Adds the user and hashes the password
            userService.addUser(currentUser);
            //Receives the user userModel with the hashed password version
            currentUser = userService.findUserModelByName(message[1]);
            this.clientUsername = currentUser.getUserName();
            userList.add(this);
            isLoggedIn=true;
            sendToUser("REG_TRUE");
            logger.info("User " + message[1] + " registered.");
        } catch (Exception e) {
            sendToUser("REG_FALSE");
            logger.error("Exception while registering a new User " + message[1]);
        }
    }


    /**
     * @param message is the input from client that is processed. Index 1 is: the client name
     * @throws IOException because of the use of bufferedWriter to send the response to client.
     */
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
        logger.info("Sending REQUEST_USER response: " + response);

    }


    /**
     * @param message is the input from client that is processed. Index 1 is: part of the username that the client is
     *              searching for
     * @throws IOException because of the use of bufferedWriter to send the response to client.
     */
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
            logger.info("SEARCH: User List sent to user " + currentUser.getUserName());
        } catch (Exception e) {
            logger.error("SRH failed: No user by the name: " + message[1]);
            sendToUser("SRH_FALSE");
        }
    }


    /**
     * @param message specifies the message that has to be sent to client.
     * @throws IOException because of the use of bufferedWriter to send the response to client.
     */
    public void sendToUser(String message) throws IOException {
        try {
            bufferedWriter.flush();
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException ignored) {

        }
    }

    /**
     * @param message is the input from client that is processed. Index 1 is: the username that has to be added Index 2
     *                is: an identifier either TRUE if the user should be placed as a contact or FALSE if the user
     *                should be blocked.
     */
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
            logger.error("Something wrong with the addContact() method");
        }


    }

    /**
     * Clears up user data form the DB so that the user will not be viewed as active on other clients.
     */
    public void exitCleanup() {

        if (currentUser != null) {
        userServiceGLBL.updateUserPortAndIpAddress(currentUser, null, null, currentUser.getId());
        logger.error("User " + currentUser.getUserName() + " had his port and IP removed form DB" ); }

    }

    /**
     * This method sends all contacts to client - those that he/she himself/herself issued or those that we sent from
     * other users.
     * @throws IOException because of the use of bufferedWriter to send the response to client.
     */
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


    /**
     * @param messageToSend is the message that will be sent to another user.
     * @param userName is the user that will receive the sent message.
     */
    public void sendMessageToOtherOnlineUser(String messageToSend, String userName) {

        for (ClientHandler clientHandler : userList) {

            try {
                if (clientHandler.clientUsername != null && !clientHandler.clientUsername.isEmpty()) {

                if (clientHandler.clientUsername.equals(userName) && !clientHandler.clientUsername.equals(currentUser.getUserName())) {
                    clientHandler.bufferedWriter.flush();
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                } }

            } catch (IOException ignored) {
            }
        }
    }

    /**
     * @param socket receives the currently used socket in order to close it down
     * @param bufferedReader receives the currently used reader in order to close it down
     * @param bufferedWriter receives the currently used writer in order to close it down
     */
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
