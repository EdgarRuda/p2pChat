package com.server.serverHandler;

import com.server.services.ContactService;
import com.server.services.UserService;
import com.server.models.UserModel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

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
