package com.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class GetInetInfo{
    public static void main(String[] args){
        
        InetAddress address = null;
        try {
            address = InetAddress.getByName("117.194.200.75");
        } catch(UnknownHostException e) {
            e.printStackTrace();
            // System.exit(2);
            return;
        }
        System.out.println("Host name: " + address.getHostName());
        System.out.println("Host address: " + address.getHostAddress());
        // System.exit(0);
        return;
    }
} // GetInetInfo