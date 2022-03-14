package com.server;

import java.net.InetAddress;

/**
 * Is used to verify if the ip address is a valid one
 */
public class VerificationService {

    public static boolean verifyIp(String ip) {
        if(ip == null || ip.isEmpty())
            return false;
        else{
            try{
                InetAddress.getByName(ip);
            }catch (Exception e){
                return false;
            }
        }
        return true;
    }
}
