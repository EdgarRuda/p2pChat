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

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;


public class LoginController implements Initializable {

    //Frames
    @FXML
    private Pane loginWindow;
    @FXML
    private Pane directWindow;
    @FXML
    private Pane registrationWindow;

    //Login frame
    @FXML
    private TextField loginName;
    @FXML
    private PasswordField loginPass;
    @FXML
    private Button loginButton;
    @FXML
    private Pane redoIcon;
    @FXML
    private Button goToRegButton;
    @FXML
    private Label serverStatus;


    //Direct connection frame
    @FXML
    private TextField directUserName;
    @FXML
    private TextField ipField;
    @FXML
    private TextField portField;


    //Registration
    @FXML
    private TextField loginRegistration;
    @FXML
    private PasswordField firstPassRegistration;
    @FXML
    private PasswordField secondPassRegistration;
    @FXML
    private Button registerButton;
    @FXML
    private Label registrationStatus;

    private Stage stage;
    private ContactList contactList;
    private TcpConnection tcpConnection;
    private Validator directConnectionValidator;
    private Validator serverConnectionValidator;
    private Validator registrationValidator;
    private static String userName;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        stage = ChatApp.getMainStage();
        stage.setOnHiding(event -> {
            tcpConnection.exit();
            contactList.clearAndClose();
        });

        loginName.requestFocus();
        bindControls();
        setValidation();
        setKeyListeners();
        initializeContactList();
    }

    private void bindControls(){
        serverStatus.textProperty().bind(TcpConnection.statusProperty());
        registrationStatus.textProperty().bind(TcpConnection.registrationStatusProperty());
        loginButton.disableProperty().bind(TcpConnection.IsConnectedProperty().not());
        registerButton.disableProperty().bind(TcpConnection.IsConnectedProperty().not());
        goToRegButton.disableProperty().bind(TcpConnection.IsConnectedProperty().not());
        redoIcon.visibleProperty().bind(TcpConnection.IsConnectedProperty().not());
    }

    private void setKeyListeners(){
        loginPass.setOnKeyPressed(event -> {
            if(event.getText().equals("\r"))
                loginToServer();
        });

        portField.setOnKeyPressed(event -> {
            if(event.getText().equals("\r")) {
                try {
                    directConnection();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        secondPassRegistration.setOnKeyPressed(event -> {
            if(event.getText().equals("\r"))
                registerUser();
        });
    }

    private void setValidation() {

        this.directConnectionValidator = new Validator();
        this.serverConnectionValidator = new Validator();
        this.registrationValidator = new Validator();

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
        tcpConnection.exit();
        initializeTcpConnection();
    }

    public void initializeContactList(){
        this.contactList = new ContactList();
    }

    public void initializeTcpConnection(TcpConnection tcpConnection){
        this.tcpConnection = tcpConnection;
        tcpConnection.setContactsModel(contactList);
    }

    public void initializeTcpConnection(){
        this.tcpConnection = new TcpConnection();
        tcpConnection.setContactsModel(contactList);
        initializeLoginListener();
    }

    public void initializeLoginListener(){
        tcpConnection.loggedInProperty().addListener(event -> {
            if (tcpConnection.getLoggedIn())
                try {

                    tcpConnection.setStatus("connection established");

                    this.openMainFrame(true);

                } catch (Exception e) {
                    e.printStackTrace();
                }

        });
    }

    @FXML
    private void loginToServer() {
        if (TcpConnection.getIsConnected()) {
            serverConnectionValidator.validate();
            if (!serverConnectionValidator.containsErrors()){
                tcpConnection.tryToLogin(loginName.getText(), loginPass.getText());
                userName = loginName.getText();
            }
        }
    }

    @FXML
    private void registerUser()  {
        if (TcpConnection.getIsConnected()){
            registrationValidator.validate();
            if(!registrationValidator.containsErrors()) {
                tcpConnection.registerUser(loginRegistration.getText(), firstPassRegistration.getText());
                userName = loginRegistration.getText();
            }
        }
    }

    @FXML
    private void directConnection() throws Exception {
        directConnectionValidator.validate();
        if(directConnectionValidator.containsErrors()) return;

            userName = directUserName.getText();

            User contact = new User(ipField.getText(), Integer.parseInt(portField.getText()));
            contact.openUdpConnection(Integer.parseInt(portField.getText()));
            contact.setIsConfirmed();
            contact.setIsConnectionEstablished(false);
            contact.startCommunication();


            contactList.addUser(contact);
            openMainFrame(false);

    }

    private void openMainFrame(boolean tcpEnabled) throws Exception {

        FXMLLoader loader = new FXMLLoader(ChatApp.class.getResource("/mainFrame.fxml"));
        Scene scene = new Scene(loader.load());

        MainFrameController mainFrameController = loader.getController();

        if (tcpEnabled)
            mainFrameController.setTcpConnection(tcpConnection);

        mainFrameController.initUserSearch();
        mainFrameController.setContactsModel(contactList);
        mainFrameController.setUserName(userName);

        stage.setResizable(true);
        stage.setTitle("chat");
        stage.setScene(scene);
        stage.show();

        if (!tcpEnabled)
            mainFrameController.loadContacts();

    }




    //ANIMATIONS
    @FXML
    private void slideToRegisterWindow() {
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
    private void slideToDirectWindow() {
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
    private void slideFromRegisterToLogin() {
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
    private void slideFromDirectToLogin() {
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
