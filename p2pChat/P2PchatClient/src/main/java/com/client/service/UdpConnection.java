package com.client.service;

import com.client.model.User;
import javafx.application.Platform;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class UdpConnection {


    private DatagramSocket socket;
    private int socketPort;

    private User user;
    private Timer timer = new Timer();

    private boolean socketOpen;
    private boolean timerRunning;
    private final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

    private final String PNG = "PNG";
    private final String MSG = "MSG";

    public void setPort(int port) {this.socketPort = port;}

    public void setUser(User user) {
        this.user = user;
    }

    public int getPort() {return socket.getLocalPort();}

    public boolean getSocketOpen() {return this.socketOpen;}
    public void setSocketOpen(boolean status) {this.socketOpen = status;}


    //for login connection
    public UdpConnection() {

        try {
            socket = new DatagramSocket();
            socketPort = socket.getLocalPort();
            socketOpen = true;

            timeStamp();
            System.out.println("UDP OPEN: " + socket.getLocalPort());

        } catch (Exception e) {
            closeSocket();
        }
    }

    //for direct connection
    public UdpConnection(int socketPort)  {

        try {
            socket = new DatagramSocket(socketPort);
            socketOpen = true;
            this.socketPort = socketPort;

            timeStamp();
            System.out.println("UDP OPEN: " + socketPort);

        } catch (Exception e) {
            System.out.println("PORT NUM " + socketPort + " IS BUSY, RUN AGAIN WITH ANOTHER PORT");
            closeSocket();
        }
    }

    //
    public void timeStamp() {
        System.out.print("[" + format.format(new Date()) + "] ");
    }


    public void sendMessage(String message) {
        byte[] data;

        if(!socket.isClosed()) {
            try {
                data = (MSG + "_" + user.getName() + "_" + message).getBytes();

                timeStamp();
                System.out.println("outbound msg: " + InetAddress.getByName(user.getIp()) + "_" + socketPort);

                socket.send(new DatagramPacket(data, data.length, InetAddress.getByName(user.getIp()), socketPort));

            } catch (Exception e) {
                timeStamp();
                System.out.println("SENDER - ERROR ON SENDING PLS RESTART");
                closeSocket();
            }
        }
    }

    public void pingContact() {

        new Thread(() -> {
            byte[] data = (PNG + "_" + user.getName()).getBytes();

            while (!socket.isClosed()) {
                try {

                    timeStamp();
                    System.out.print("outbound ping: ");
                    System.out.println(user.getName() + "_" + InetAddress.getByName(user.getIp()) + "_" + socketPort);

                    socket.send(new DatagramPacket(data, data.length,
                            InetAddress.getByName(user.getIp()), socketPort));

                    Thread.sleep(10000);

                } catch (Exception ignored) {
                    timeStamp();
                    System.out.println("UDP - PING STOPPED");
                    closeSocket();
                    break;
                }
            }
            timeStamp();
            System.out.println("UDP - PING STOPPED");
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
            while (!socket.isClosed()) {
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
                        case PNG: {
                            if (!user.getIsConnectionEstablished()) {
                                user.setIsConnectionEstablished(true);
                                startTimer(30);

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

                            restartTimer();
                            startTimer(30);
                            break;
                        }
                        case MSG: {
                            timeStamp();
                            System.out.println("inbound msg: " + user.getName() + ":" + messageArr[2]);
                            user.displayMessage(messageArr[2]);
                            break;
                        }

                        case "EXT":
                            return;

                        default:
                            break;

                    }
                } catch (Exception e) {
                    timeStamp();
                    System.out.println("UDP - STOPPED LISTENING");
                    closeSocket();
                    break;
                }
            }
        }).start();
    }


    private void startTimer(int time) {
        timerRunning=true;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                user.setIsConnectionEstablished(false);

                closeSocket();

                timeStamp();
                System.out.println("USER: " + user.getName() + " has disconnected");
                user.closeUdpConnection();
                timer.cancel();
            }
        };
        timer = new Timer();
        timer.schedule(task, time * 1000L);
    }

    private void restartTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
            }
        };
        timer.cancel();
        timer = new Timer();
        timer.schedule(task, 0);
    }


    public void closeSocket() {
        if(timerRunning){
            restartTimer();
            startTimer(1);
        }
        if (!socket.isClosed()) {
            socketOpen = false;
            timeStamp();
            System.out.println("UDP CLOSE: " + socket.getLocalPort());
            socket.close();

        }
    }


}
