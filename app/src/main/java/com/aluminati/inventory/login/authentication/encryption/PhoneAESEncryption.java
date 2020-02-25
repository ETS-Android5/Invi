package com.aluminati.inventory.login.authentication.encryption;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;


import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class PhoneAESEncryption extends AsyncTask<String,String,String> {

    private static final int itr = 10;
    private static final int keySize = 128;
    private static final String cypInst = "AES/CBC/PKCS5Padding";
    private static final String secKeyInt = "PBKDF2withHmacSHA1";
    private static final String salt = "testSalt";
    private static final String initVec = "7894323219012135";
    private static final String TAG = "EncryptPhoneNumber";
    private String result;
    private String phoneNumber;

    private Consumer consumer;

    public interface Consumer {
        void accept(String internet);
    }

    public PhoneAESEncryption(String phoneNumber, Consumer consumer){
        Log.i(TAG, phoneNumber);
        this.consumer = consumer;
        this.phoneNumber = phoneNumber;
        execute();
    }

    public PhoneAESEncryption(){

    }

    private static String encrypt(String phoneNumberToEncrypt) throws Exception{

        SecretKeySpec secretKeySpec = new SecretKeySpec(getRaw(phoneNumberToEncrypt, salt), "AES");
        Cipher cipher = Cipher.getInstance(cypInst);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(initVec.getBytes()));
        byte[] encrypted = cipher.doFinal(phoneNumberToEncrypt.getBytes());
        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    public static String decrypt(String phoneNumberToDecrypt) throws Exception{
        byte[] encrypted_keys = Base64.decode(phoneNumberToDecrypt, Base64.DEFAULT);
        SecretKeySpec secretKeySpec = new SecretKeySpec(getRaw(phoneNumberToDecrypt,salt), "AES");
        Cipher cipher = Cipher.getInstance(cypInst);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(initVec.getBytes()));
        byte[] decrypted = cipher.doFinal(encrypted_keys);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    private static byte[] getRaw(String phoneNumberToEncrypt, String salt){
        try{
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(secKeyInt);
            KeySpec keySpec = new PBEKeySpec(phoneNumberToEncrypt.toCharArray(), salt.getBytes(), itr, keySize);
            return secretKeyFactory.generateSecret(keySpec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e){
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    protected String doInBackground(String... strings) {
        try{
            Log.i(TAG, phoneNumber);
            result = encrypt(phoneNumber);
        }catch (Exception e){
            Log.w(TAG, "Error during conversion", e);
        }
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        consumer.accept(s);
    }


}
