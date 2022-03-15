package com.client.loginWindowController;


import com.client.ChatApp;
import com.client.mainFrameController.MainFrameController;
import com.client.model.ContactList;
import com.client.model.User;
import com.client.service.TcpConnection;
import com.client.service.VerificationService;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.SocketException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;


public class LoginController implements Initializable {


    @FXML
    private Pane directWindow;
    @FXML
    private Pane registrationWindow;
    @FXML
    private Pane redoIcon;
    @FXML
    private Button goToRegButton;
    @FXML
    private Label serverStatus;
    @FXML
    private Button registerButton;
    @FXML
    private TextField loginName;
    @FXML
    private PasswordField loginPass;
    @FXML
    private TextField loginRegistration;
    @FXML
    private PasswordField firstPassRegistration;
    @FXML
    private PasswordField secondPassRegistration;
    @FXML
    private Button loginButton;
    @FXML
    private TextField portField;
    @FXML
    private TextField directUserName;
    @FXML
    private TextField ipField;


    private ContactList contactList;
    private TcpConnection tcpConnection;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.contactList = new ContactList();

        loginName.requestFocus();
        serverStatus.textProperty().bind(TcpConnection.statusProperty());
        loginButton.disableProperty().bind(TcpConnection.IsConnectedProperty().not());
        registerButton.disableProperty().bind(TcpConnection.IsConnectedProperty().not());
        goToRegButton.disableProperty().bind(TcpConnection.IsConnectedProperty().not());
        redoIcon.visibleProperty().bind(TcpConnection.IsConnectedProperty().not());


    }
    @FXML
    private void resetConnection(){
        tcpConnection = new TcpConnection();
        initTcpConnection(tcpConnection);
    }

    public void initTcpConnection(TcpConnection connect){
        this.tcpConnection = Objects.requireNonNullElseGet(connect, TcpConnection::new);

        tcpConnection.loggedInProperty().addListener(event -> {
            if (tcpConnection.getLoggedIn()) {
                try {
                    String name = !loginName.getText().isEmpty() ?
                            loginName.getText() : loginRegistration.getText();

                    User me = new User(name);

                    contactList.addUser(me);
                    tcpConnection.setContactsModel(contactList);

                    openMainFrame(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
    }

    @FXML
    private void loginToServer() throws Exception {
        if (TcpConnection.getIsConnected())
            if (!loginName.getText().isEmpty() && !loginPass.getText().isEmpty())
                tcpConnection.tryToLogin(loginName.getText(), loginPass.getText());

    }

    @FXML
    private void registerUser() throws SocketException {
        if (TcpConnection.getIsConnected())
            if (!loginRegistration.getText().isEmpty()){
                if (!firstPassRegistration.getText().isEmpty() && !secondPassRegistration.getText().isEmpty())
                    if (firstPassRegistration.getText().equals(secondPassRegistration.getText()))
                        tcpConnection.registerUser(loginRegistration.getText(), firstPassRegistration.getText());

            }
    }

    @FXML
    private void directConnection() throws Exception {

        if (!directUserName.getText().isEmpty())
            if (!ipField.getText().isEmpty()) {

                String ip = ipField.getText();
                if (!VerificationService.verifyIp(ip))
                    return;

                String port = portField.getText();
                if (port.length() > 5)
                    return;
                for (int i = 0; i < 5; i++)
                    if (!Character.isDigit(port.charAt(i)))
                        return;

                User me = new User(directUserName.getText());

                contactList.addUser(me);

                User contact = new User(ip, Integer.parseInt(portField.getText()));
                contact.openUdpConnection(Integer.parseInt(portField.getText()));
                contact.setIsConfirmed();
                contact.setIsConnectionEstablished(false);
                contact.startCommunication();


                contactList.addUser(contact);
                openMainFrame(false);
            }
    }

    private void openMainFrame(boolean tcpEnabled) throws Exception {

        FXMLLoader loader = new FXMLLoader(ChatApp.class.getResource("/mainFrame.fxml"));
        Scene scene = new Scene(loader.load());

        MainFrameController mainFrameController = loader.getController();

        if (tcpEnabled)
            mainFrameController.setTcpConnection(tcpConnection);
        mainFrameController.initUserSearch();
        mainFrameController.setContactsModel(contactList);


        User user;

        if(loginName.getText().equals("test1")) {
            user = new User("test2");
            user.setIsConfirmed();
            user.setIsConnectionEstablished(false);
            contactList.addUser(user);

            user = new User("test3");
            user.setIsInboundRequest(true);
            user.setIsConnectionEstablished(false);
            contactList.addUser(user);

            user = new User("test4");
            user.setIsOutboundRequest(true);
            user.setIsConnectionEstablished(false);
            contactList.addUser(user);

        }

        if(loginName.getText().equals("test2")) {
            user = new User("test1");
            user.setIsConfirmed();
            user.setIsConnectionEstablished(false);
            contactList.addUser(user);

            user = new User("test3");
            user.setIsConfirmed();
            user.setIsConnectionEstablished(false);
            contactList.addUser(user);

        }

        if(loginName.getText().equals("test3")) {
            user = new User("test1");
            user.setIsConfirmed();
            user.setIsConnectionEstablished(false);
            contactList.addUser(user);

            user = new User("test2");
            user.setIsConfirmed();
            user.setIsConnectionEstablished(false);
            contactList.addUser(user);

        }

//        if(loginName.getText().equals("ed1")) {
//            user = new User("Tadas");
//            user.setIsConfirmed();
//            user.setIsConnectionEstablished(false);
//            contactList.addUser(user);
//
//            user = new User("Laura");
//            user.setIsConfirmed();
//            user.setIsConnectionEstablished(false);
//            contactList.addUser(user);
//        }
//
//        if(loginName.getText().equals("Tadas")) {
//            user = new User("Laura");
//            user.setIsConfirmed();
//            user.setIsConnectionEstablished(false);
//
//            contactList.addUser(user);
//
//            user = new User("ed1");
//            user.setIsConfirmed();
//            user.setIsConnectionEstablished(false);
//            contactList.addUser(user);
//
//        }
//        if(loginName.getText().equals("Laura")) {
//            user = new User("Tadas");
//            user.setIsConfirmed();
//            user.setIsConnectionEstablished(false);
//
//            contactList.addUser(user);
//
//            user = new User("ed1");
//            user.setIsConfirmed();
//            user.setIsConnectionEstablished(false);
//            contactList.addUser(user);
//        }



        mainFrameController.loadContacts();

        Stage stage = ChatApp.getMainStage();
        stage.setResizable(true);

        stage.setOnHiding(event -> tcpConnection.exit());
        stage.setScene(scene);
        stage.show();
    }

    //ANIMATIONS
    @FXML
    private void toRegister() {
        TranslateTransition slide = new TranslateTransition();
        slide.setDuration(Duration.seconds(0.3));
        slide.setNode(registrationWindow);

        slide.setToX(300);
        slide.play();
    }

    @FXML
    private void toDirect() {
        TranslateTransition slide = new TranslateTransition();
        slide.setDuration(Duration.seconds(0.3));
        slide.setNode(directWindow);

        slide.setToX(-300);
        slide.play();
    }

    @FXML
    private void registerToLogin() {
        TranslateTransition slide = new TranslateTransition();
        slide.setDuration(Duration.seconds(0.3));
        slide.setNode(registrationWindow);

        slide.setToX(-300);
        slide.play();
    }

    @FXML
    private void directToLogin() {
        TranslateTransition slide = new TranslateTransition();
        slide.setDuration(Duration.seconds(0.3));
        slide.setNode(directWindow);

        slide.setToX(300);
        slide.play();
    }


}
