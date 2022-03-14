package com.client.service;

import com.client.model.User;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class UdpConnection {


    private DatagramSocket socket;
    private int socketPort;

    private User user;

    private Timer timer = new Timer();

    private final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

    public boolean socketOpen;

    public void setPort(int port){
        this.socketPort = port;
    }

    private final String PNG = "PNG";
    private final String MSG = "MSG";

    private static final BooleanProperty isConnected = new SimpleBooleanProperty();

    public static BooleanProperty IsConnectedProperty() {
        return isConnected;
    }

    public static Boolean getIsConnected() {
        return isConnected.get();
    }

    public final void setIsConnected(boolean status) {
        IsConnectedProperty().set(status);
        timeStamp();
        System.out.println(status);
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getPort(){
        return socket.getLocalPort();
    }


    //for login connection
    public UdpConnection() {

        try {
            socket = new DatagramSocket();
            socketPort = socket.getLocalPort();
            socketOpen=true;

            timeStamp();
            System.out.println("UDP OPEN: " + socket.getLocalPort());

        } catch (Exception e) {
            closeSocket();
        }
    }

    //for direct connection
    public UdpConnection(int socketPort) throws Exception {

        try {
            socket = new DatagramSocket(socketPort);
            socketOpen=true;
            this.socketPort = socketPort;

            timeStamp();
            System.out.println("UDP OPEN: " +socketPort);

        } catch (Exception e) {
            System.out.println("PORT NUM " + socketPort + " IS BUSY, RUN AGAIN WITH ANOTHER PORT");
            throw new Exception();
        }
    }
    //
    public void timeStamp() {
        System.out.print("[" + format.format(new Date()) + "] ");
    }


    public void sendMessage(String message) {
        byte[] data;

        try {
            data = (MSG +";"+ user.getName() + ";" + message).getBytes();

            timeStamp();
            System.out.println("outbound msg: " + InetAddress.getByName(user.getIp()) + "_" + socketPort);

            socket.send(new DatagramPacket(data, data.length, InetAddress.getByName(user.getIp()), socketPort));

        } catch (Exception e) {
            timeStamp();
            System.out.println("SENDER - ERROR ON SENDING PLS RESTART");
            closeSocket();
        }

    }

    public void pingContact() {

        new Thread(() -> {
            byte[] data = (PNG +";" + user.getName()).getBytes();

            while (socketOpen) {
                try {

                    timeStamp();
                    System.out.print("outbound ping: ");
                    System.out.println(user.getName() + "_" + InetAddress.getByName(user.getIp()) + "_" + socketPort);

                    socket.send(new DatagramPacket(data, data.length,
                            InetAddress.getByName(user.getIp()), socketPort));

                    Thread.sleep(13000);

                } catch (Exception ignored) {
                    timeStamp();
                    System.out.println("PING - ERROR ON SENDING PLS RESTART");
                    closeSocket();
                    break;
                }
            }
        }).start();

    }

    public void listenForMessage() {
        new Thread(() -> {
            DatagramPacket inbound;
            String inboundMessage;
            String[] messageArr;
            boolean hasNull;
            timeStamp();
            System.out.println("UDP listening..");
            while (socketOpen) {
                try {
                    hasNull = false;
                    inbound = new DatagramPacket(new byte[1024], 1024);
                    socket.receive(inbound);
                    inboundMessage = new String(inbound.getData(), 0, inbound.getLength());

                    if (!inboundMessage.isEmpty()) {
                        messageArr = inboundMessage.split("_");

                    } else continue;


                    for (String s : messageArr)
                        if (s == null) {
                            hasNull = true;
                            break;
                        }

                    if (hasNull) continue;

                    switch (messageArr[0]) {
                        case "PNG": {
                            if (!user.getIsConnectionEstablished()) {
                                user.setIsConnectionEstablished(true);
                                startTimer(user);

                                if (user.getName().equals(user.getIp())) {
                                    final String name = messageArr[1];
                                    Platform.runLater(() ->
                                            user.setName(name));

                                }
                                timeStamp();
                                System.out.println(user.getName() + " has connected");
                            }
                            timeStamp();
                            System.out.println("inbound  ping: " + messageArr[1] + "_" + inbound.getAddress() + "_" + inbound.getPort());

                            killTimer();
                            startTimer(user);
                            break;
                        }
                        case "MSG": {
                            timeStamp();
                            System.out.println("inbound msg: " + user.getName() + ":" + messageArr[2]);
                            user.displayMessage(messageArr[2]);
                            break;
                        }
                        case "PRT":{
                            timeStamp();
                            System.out.println("inbound msg: " + Arrays.toString(messageArr));
                        }
                        case "USR":{
                            timeStamp();
                            System.out.println("inbound msg: " + Arrays.toString(messageArr));
                        }

                        case "EXT": return;

                        default: break;

                    }
                } catch (Exception e) {
                    System.out.println("UDP SOCKET STATUS: " + socketOpen);
                    System.out.println("LISTENER - ERROR ON RECEIVING PLS RESTART");
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }



    private void startTimer(User user) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                user.setIsConnectionEstablished(false);

                closeSocket();

                timeStamp();
                System.out.println("USER: " + user.getName() + " has disconnected");
            }
        };
        timer = new Timer();
        timer.schedule(task, 30 * 1000L);
    }

    private void killTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
            }
        };
        timer.cancel();
        timer = new Timer();
        timer.schedule(task, 15 * 1000L);

    }

    public void closeSocket(){
        if(socket!=null){
        socketOpen=false;
        timeStamp();
        System.out.println("UDP CLOSE: " + socket.getLocalPort());
        try{
        socket.send(new DatagramPacket("EXT".getBytes(), 3, InetAddress.getByName("localhost"), socket.getLocalPort()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.close();
        }
    }


}
