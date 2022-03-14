package com.client.service;

import java.net.InetAddress;

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
