package com.unify.assignment.unifyfacerecognitionapp;

import android.util.Log;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
    private String iv = "fedcba9876543210";
    private IvParameterSpec mIvParameterSpec;
    private SecretKeySpec mSecretKeySpec;
    private Cipher mCipher;
    private String SecretKey = "0123456789abcdef";

    public Encryption(){

        mIvParameterSpec = new IvParameterSpec(iv.getBytes());
        mSecretKeySpec = new SecretKeySpec(SecretKey.getBytes(), "AES");

        try{
            mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        }catch (NoSuchPaddingException e){
            Log.e("ENCRYPTION", "NO SUCH PADDING");
            e.printStackTrace();
        }catch (NoSuchAlgorithmException e){
            Log.e("ENCRYPTION", "NO SUCH ALGORITHM");
            e.printStackTrace();
        }

    }

    public byte[] Encrypt(String data) throws Exception{
        if (data == null || data.length() == 0)
            throw new Exception("Empty string");

        byte[] encrypted = null;

        try{
            mCipher.init(Cipher.ENCRYPT_MODE, mSecretKeySpec, mIvParameterSpec);

            encrypted = mCipher.doFinal(padString(data).getBytes());
        }catch (Exception e ){
            e.printStackTrace();
        }



        return encrypted;
    }

    private static String padString(String source) {
        char paddingChar = 0;
        int size = 16;
        int x = source.length() % size;
        int padLength = size - x;
        for (int i = 0; i < padLength; i++) {
            source += paddingChar;
        }
        return source;
    }
}
