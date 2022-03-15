package com.client.service;

import java.net.InetAddress;

public class VerificationService {

    public static boolean verifyIp(String ip) {

        if(ip == null || ip.isEmpty()) return false;

        if(ip.equals("localhost")) return true;

        if(ip.matches("[a-zA-Z]+")) return false;

        else{
            try{
                InetAddress.getByName(ip);
            }catch (Exception e){
                return false;
            }
        }
        return true;
    }

    public static boolean verifyPort(String port){
        if(port.isEmpty() || port.length() > 5) return false;

        if(port.equals("0")) return false;
        
        for (int i = 0; i < port.length(); i++)
            if (!Character.isDigit(port.charAt(i)))
                return false;
        return true;
    }
}
