package com.server.serverHandler;


import com.server.models.UserModel;
import com.server.services.ContactService;
import com.server.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;


/**
 * The ServerLaunch class opens the main socket to where the incoming connection will be processed.
 * ghostUserCleanup() is required so that in case the server crashes all data of the users is cleared up
 * because users Online status is determined by of the IP address or Port is present on the database.
 */
@Component
public class ServerLaunch {

    private final UserService userService;
    private final ContactService contactService;

    public ServerLaunch(UserService userService, ContactService contactService) {
        this.userService = userService;
        this.contactService = contactService;
    }

    /**
     * @throws IOException because of the open socket.
     */
    @Autowired
    public void LaunchServer() throws IOException {

        ghostUserCleanup();

        ServerSocket serverSocket = new ServerSocket(8001);
        Server server = new Server(serverSocket);

            server.startServer(userService, contactService);

    }

    /**
     * If server crashes unexpectedly, some user data is not cleaned up - this method clears up the table at launch
     */
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
