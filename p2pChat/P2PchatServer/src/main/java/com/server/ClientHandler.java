package com.server;



import com.server.model.User;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


public class ClientHandler implements Runnable {


    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>(); //might be a database

    private User user;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            user = new User();

            user.setName(bufferedReader.readLine());
            user.setPort(Integer.parseInt(bufferedReader.readLine()));
            user.setIp(String.valueOf(socket.getRemoteSocketAddress())); //?????

            System.out.println(user.getName());
            System.out.println(user.getPort());

            clientHandlers.add(this);


        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);

        }
    }

    @Override
    public void run() {

        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();

                //message types
                //CONTACTS user - send back data of everyone online
                //CONNECT + user name - send back data of another users port and ip
                if(messageFromClient.contains("CONNECT")){

                }
                System.out.println(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }

    }


    public void removeClientHandler() {
        clientHandlers.remove(this);
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {

        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
