package com.tieba;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Test {
    public static void main(String[] args) {
    
    }
    
    private static String generateSign(String string) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            
            String input = string + "tiebaclient!!!";
            md.update(input.getBytes());
            
            byte[] digest = md.digest();
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : digest) {
                String hex = String.format("%02x", b);
                hexString.append(hex);
            }
            
            return hexString.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
