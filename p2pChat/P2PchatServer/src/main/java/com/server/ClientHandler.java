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

            this.user.setName(bufferedReader.readLine());
            this.user.setPort(Integer.parseInt(bufferedReader.readLine()));

            System.out.println(user.getName());
            System.out.println(user.getPort());

            clientHandlers.add(this);
            //broadcastMessage("Server: " + clientUsername + " has entered the chat.");
            iterateUsers();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);

        }
    }

    private void requestConnectionBetweenUsers(String recepient){
        for (ClientHandler clientHandler : clientHandlers) {

        }
    }

    private void iterateUsers(){
       if(clientHandlers.size()>1){

       }
    }
    @Override
    public void run() {

        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                if(messageFromClient.contains("CONNECT")){
                    System.out.println(messageFromClient.substring(7));
                    requestConnectionBetweenUsers(messageFromClient.substring(7));
                }
                System.out.println(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }

    }

//    public void broadcastMessage(String messageToSend) {
//        for (ClientHandler clientHandler : clientHandlers) {
//            try {
//                if (!clientHandler.clientUsername.equals(clientUsername)) {
//                    clientHandler.bufferedWriter.write(messageToSend);
//                    clientHandler.bufferedWriter.newLine();
//                    clientHandler.bufferedWriter.flush();
//                }
//            } catch (IOException e) {
//                closeEverything(socket, bufferedReader, bufferedWriter);
//            }
//        }
//    }

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
