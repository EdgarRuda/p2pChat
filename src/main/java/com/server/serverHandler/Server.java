package com.server.serverHandler;

import com.server.services.ContactService;
import com.server.services.UserService;
import com.server.models.UserModel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class creates a new server Socket upon connection to the server.
 * It works as a passive listener for incoming connections.
 * With each created socket a new Thread is made and the heavy-lifting is passed to the ClientHandler class.
 * This method allows the server to have a singular port instead of multiple separate ports for each connection.
 *
 * Thread exception handler is used to clear up all bug/error related  when user data is not removed from the database.
 */
public class Server {

    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * @param userService is required to pass down to the ClientHandler
     * @param contactService is required to pass down to the ClientHandler
     */
    public void startServer(UserService userService, ContactService contactService) {
        System.out.println("Server Launched. CHECK 1.");
        try {

            while(!serverSocket.isClosed()) {

                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected!");
                socket.getPort();
                socket.getRemoteSocketAddress();

                ClientHandler clientHandler = new ClientHandler(socket, userService, contactService);


                Thread thread = new Thread(clientHandler);
                thread.start();



                Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        System.out.println("Caught " + e);

                        UserModel currentUser1 =clientHandler.currentUser;
                        clientHandler.userServiceGLBL.updateUserPortAndIpAddress(currentUser1, null, null, currentUser1.getId());
                        System.out.println("User " + currentUser1.getUserName() + " logged out with an error. Clearing Port and IP " );

                    }
                });


            }

        } catch (IOException e) {

        }

    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
