package com.server.serverHandler;


import com.server.models.UserModel;
import com.server.services.ContactService;
import com.server.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;


@Component
public class ServerLaunch {

    private final UserService userService;
    private final ContactService contactService;

    public ServerLaunch(UserService userService, ContactService contactService) {
        this.userService = userService;
        this.contactService = contactService;
    }
    //Our Main Method
    @Autowired
    public void LaunchServer() throws IOException {

        ghostUserCleanup();

        ServerSocket serverSocket = new ServerSocket(8001);
        Server server = new Server(serverSocket);

            server.startServer(userService, contactService);

    }


    // if server crashes unexpectedly, some user data is not cleaned up - this method clears up the table at launch
    public void ghostUserCleanup() {

        List<UserModel> onlineUsers = userService.findAllOnlineUsers();
        try {

            for (UserModel onlineUser : onlineUsers) {
                userService.updateUserPortAndIpAddress(onlineUser, null, null, onlineUser.getId());
                        System.err.println("User " + onlineUser.getUserName() + " had his port and IP removed form DB");


                }
            } catch (Exception e) {
            System.err.println("Exception occurred on onlineUserCleanup()");
        }

    }

}
