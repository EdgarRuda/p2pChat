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
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import net.synedra.validatorfx.Validator;

import java.net.SocketException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;


public class LoginController implements Initializable {

    @FXML
    private Pane loginWindow;
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
    @FXML
    private Label registrationStatus;


    private ContactList contactList;
    private TcpConnection tcpConnection;
    private Validator directConnectionValidator;
    private Validator serverConnectionValidator;
    private Validator registrationValidator;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.contactList = new ContactList();

        this.directConnectionValidator = new Validator();
        this.serverConnectionValidator = new Validator();
        this.registrationValidator = new Validator();

        loginName.requestFocus();
        serverStatus.textProperty().bind(TcpConnection.statusProperty());
        registrationStatus.textProperty().bind(TcpConnection.registrationStatusProperty());
        loginButton.disableProperty().bind(TcpConnection.IsConnectedProperty().not());
        registerButton.disableProperty().bind(TcpConnection.IsConnectedProperty().not());
        goToRegButton.disableProperty().bind(TcpConnection.IsConnectedProperty().not());
        redoIcon.visibleProperty().bind(TcpConnection.IsConnectedProperty().not());

        directConnectionValidator.createCheck()
                .dependsOn("username", directUserName.textProperty())
                .withMethod(c -> {
                    String userName = c.get("username");
                    if (userName.isEmpty()) {
                        c.error("please enter user name");
                    }
                })
                .decorates(directUserName);


        directConnectionValidator.createCheck()
                .dependsOn("ip", ipField.textProperty())
                .withMethod(c -> {
                    String ip = c.get("ip");

                    if(!VerificationService.verifyIp(ip)){
                        c.error("wrong ip format");
                    }
                })
                .decorates(ipField);


        directConnectionValidator.createCheck()
                .dependsOn("port", portField.textProperty())
                .withMethod(c -> {
                    String port = c.get("port");

                    if(!VerificationService.verifyPort(port)){
                        c.error("wrong port number");
                    }
                })
                .decorates(portField);

        serverConnectionValidator.createCheck()
                .dependsOn("login", loginName.textProperty())
                .withMethod(c -> {
                    String login = c.get("login");

                    if(login.isEmpty()){
                        c.error("please enter login");
                    }

                    else if(login.length()<3){
                        c.error("login is too short");
                    }
                })
                .decorates(loginName);

        serverConnectionValidator.createCheck()
                .dependsOn("pass", loginPass.textProperty())
                .withMethod(c -> {
                    String pass = c.get("pass");

                    if(pass.isEmpty()){
                        c.error("please enter pass");
                    }

                    if(pass.length()<3){
                        c.error("wrong pass");
                    }

                })
                .decorates(loginPass);

        registrationValidator.createCheck()
                .dependsOn("login", loginRegistration.textProperty())
                .withMethod(c -> {
                    String login = c.get("login");

                    if(login.isEmpty()){
                        c.error("please enter name");
                    }

                    else if(login.length()<3){
                        c.error("name is too short");
                    }
                })
                .decorates(loginRegistration);

        registrationValidator.createCheck()
                .dependsOn("first",firstPassRegistration.textProperty())
                .dependsOn("second",secondPassRegistration.textProperty())
                .withMethod(c -> {
                    String first = c.get("first");
                    String second = c.get("second");

                    if(first.isEmpty() || second.isEmpty()){
                        c.error("please enter pass");
                    }

                    if(!first.equals(second)){
                        c.error("pass do not match");
                    }

                    if(first.length()<3 || second.length()<3){
                        c.error("pass dis too short");
                    }
                })
                .decorates(firstPassRegistration)
                .decorates(secondPassRegistration);

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
        if (TcpConnection.getIsConnected()) {
            serverConnectionValidator.validate();
            if (!serverConnectionValidator.containsErrors())
                tcpConnection.tryToLogin(loginName.getText(), loginPass.getText());
        }
    }

    @FXML
    private void registerUser() throws SocketException {
        if (TcpConnection.getIsConnected()){
            registrationValidator.validate();
            if(!registrationValidator.containsErrors())
                tcpConnection.registerUser(loginRegistration.getText(), firstPassRegistration.getText());

            }
    }

    @FXML
    private void directConnection() throws Exception {
        directConnectionValidator.validate();
        if(directConnectionValidator.containsErrors()) return;

            User me = new User(directUserName.getText());

            contactList.addUser(me);

            User contact = new User(ipField.getText(), Integer.parseInt(portField.getText()));
            contact.openUdpConnection(Integer.parseInt(portField.getText()));
            contact.setIsConfirmed();
            contact.setIsConnectionEstablished(false);
            contact.startCommunication();


            contactList.addUser(contact);
            openMainFrame(false);


//        if (!directUserName.getText().isEmpty())
//            if (!ipField.getText().isEmpty()) {
//
//                String ip = ipField.getText();
//                if (!VerificationService.verifyIp(ip))
//                    return;
//
//                String port = portField.getText();
//                if (port.length() > 5)
//                    return;
//                for (int i = 0; i < 5; i++)
//                    if (!Character.isDigit(port.charAt(i)))
//                        return;
//
//                User me = new User(directUserName.getText());
//
//                contactList.addUser(me);
//
//                User contact = new User(ip, Integer.parseInt(portField.getText()));
//                contact.openUdpConnection(Integer.parseInt(portField.getText()));
//                contact.setIsConfirmed();
//                contact.setIsConnectionEstablished(false);
//                contact.startCommunication();
//
//
//                contactList.addUser(contact);
//                openMainFrame(false);
//            }
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
        TranslateTransition slideReg = new TranslateTransition();
        slideReg.setDuration(Duration.seconds(0.3));
        slideReg.setNode(registrationWindow);

        TranslateTransition slideLog = new TranslateTransition();
        slideLog.setDuration(Duration.seconds(0.3));
        slideLog.setNode(loginWindow);

        slideLog.setToX(300);
        slideReg.setToX(299);
        slideReg.play();
        slideLog.play();
    }

    @FXML
    private void toDirect() {
        TranslateTransition slideDir = new TranslateTransition();
        slideDir.setDuration(Duration.seconds(0.3));
        slideDir.setNode(directWindow);

        TranslateTransition slideLog = new TranslateTransition();
        slideLog.setDuration(Duration.seconds(0.3));
        slideLog.setNode(loginWindow);

        slideDir.setToX(-299);
        slideLog.setToX(-300);
        slideDir.play();
        slideLog.play();
    }

    @FXML
    private void registerToLogin() {
        TranslateTransition slideLog = new TranslateTransition();
        slideLog.setDuration(Duration.seconds(0.3));
        slideLog.setNode(loginWindow);

        TranslateTransition slideReg = new TranslateTransition();
        slideReg.setDuration(Duration.seconds(0.3));
        slideReg.setNode(registrationWindow);

        slideReg.setToX(-300);
        slideLog.setToX(-0);

        slideReg.play();
        slideLog.play();
    }

    @FXML
    private void directToLogin() {
        TranslateTransition slideDir = new TranslateTransition();
        slideDir.setDuration(Duration.seconds(0.3));
        slideDir.setNode(directWindow);

        TranslateTransition slideLog = new TranslateTransition();
        slideLog.setDuration(Duration.seconds(0.3));
        slideLog.setNode(loginWindow);

        slideDir.setToX(300);
        slideLog.setToX(0);

        slideDir.play();
        slideLog.play();
    }


}
