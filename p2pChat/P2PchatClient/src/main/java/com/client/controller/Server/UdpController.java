package com.client.controller.Server;

import com.client.Model.Server;
import com.client.Model.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class UdpController {

    private DatagramSocket userSocket;
    private int udpPort = 8001;
    private int holePunchPort;
    public DatagramSocket getSocket(){
        return userSocket;
    }



    public int openUdpSocket() throws SocketException {
        userSocket =  new DatagramSocket();
        return userSocket.getLocalPort();
    }

    private void spamUdpSocket() throws Exception{
        DatagramPacket temp = new DatagramPacket(new byte[1],1, InetAddress.getByName("0.0.0.0"), udpPort );
    }

    public void sendData(User user, String message) throws Exception {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        DatagramPacket sendPacket = new DatagramPacket(data,
                data.length, InetAddress.getByName(user.getIp()), user.getPort());

        userSocket.send(sendPacket);
    }

    public String listenForData(User user) throws Exception{
        DatagramPacket receivePackage = new DatagramPacket(new byte[1024],
                1024, InetAddress.getByName(user.getIp()), user.getPort());
        userSocket.receive(receivePackage);
        return new String(receivePackage.getData(), 0, receivePackage.getLength());
    }


    public void getUdpData(User user) throws Exception {

        DatagramSocket clientSocket = new DatagramSocket();
        ;
        byte[] sendData = BigInteger.valueOf(user.getUserID()).toByteArray();

        DatagramPacket sendPacket = new DatagramPacket(sendData,
                sendData.length, InetAddress.getByName(Server.IP), udpPort);
        clientSocket.send(sendPacket);

        DatagramPacket receivePacket = new DatagramPacket(new byte[1024], 1024);
        clientSocket.receive(receivePacket);


        String response = new String(receivePacket.getData());
        String[] splitResponse = response.split("-");
        InetAddress ip = InetAddress.getByName(splitResponse[0].substring(1));
        int port = Integer.parseInt(splitResponse[1]);

        user.setIp(splitResponse[0].substring(1));
        user.setPort(port);

        holePunchPort = clientSocket.getLocalPort();
        clientSocket.close();

        userSocket = new DatagramSocket(holePunchPort);

    }


    }
