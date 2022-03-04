package com.server.model;

public class User {


    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public boolean isOnline() {return isOnline;}

    public void setOnline(boolean online) {isOnline = online;}

    private int port;
    private String ip;
    private String name;
    private int userID;
    private boolean isOnline;

    public User(){}
    public User(String name, int userID){
        this.name = name;
        this.userID = userID;
    }

}
