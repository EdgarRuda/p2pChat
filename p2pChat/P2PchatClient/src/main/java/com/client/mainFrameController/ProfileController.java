package com.client.mainFrameController;

import com.client.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class ProfileController {
    @FXML
    private Pane colorPane;
    @FXML
    private Pane focusPane;
    @FXML
    private Button sendRequestButton;
    @FXML
    private Button approveRequestButton;
    @FXML
    private Pane contactProfile;
    @FXML
    private Label userName;
    @FXML
    private Label userStatus;
    @FXML
    private Circle userCircle;
    @FXML
    private Label userInitials;
    @FXML
    private Circle statusCircle;
    @FXML
    private Circle declineContactIcon;
    @FXML
    private Text declineContactText;
    @FXML
    private Circle messageUnreadStatus;

    private User user;

    public void setUser(User user){
        this.user = user;
        bindProperties();
    }

    public void bindProperties(){

        focusPane.setOnMouseEntered(event -> colorPane.setStyle("-fx-background-color: #666699;"));
        focusPane.setOnMouseExited(event -> colorPane.setStyle(""));

        userInitials.setText(user.getName().substring(0,1));
        userName.textProperty().bind(user.nameProperty());
        userStatus.textProperty().bind(user.userStatusProperty());
        userStatus.visibleProperty().bind(user.isSearchResultProperty().not());
        messageUnreadStatus.visibleProperty().bind(user.messageUnreadProperty());
        statusCircle.visibleProperty().bind(user.isConfirmedProperty());
        statusCircle.styleProperty().bind(user.profileStatusProperty());

        declineContactIcon.visibleProperty().bind(user.isInboundRequestProperty());
        declineContactText.visibleProperty().bind(user.isInboundRequestProperty());
        declineContactIcon.setOnMouseClicked(event -> user.declineUsersRequest());

        approveRequestButton.visibleProperty().bind(user.isInboundRequestProperty());
        approveRequestButton.setOnAction(event -> user.approveRequest());

        sendRequestButton.visibleProperty().bind(user.isSearchResultProperty());
        sendRequestButton.setOnAction(event -> user.sendRequest());

        contactProfile.styleProperty().bind(user.profileStyleProperty());
        contactProfile.setOnMouseClicked(mouseEvent -> {if(!user.getIsisSearchResult()) user.loadChat();});
    }
}
