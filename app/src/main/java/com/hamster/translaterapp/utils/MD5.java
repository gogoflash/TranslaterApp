package com.hamster.translaterapp.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ПК on 18.04.2017.
 */
public class MD5
{
    private static MessageDigest digest;

    public static String get(String string)
    {
        if(digest == null) {
            try
            {
                digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }
        }

        digest.reset();
        digest.update(string.getBytes());
        byte[] a = digest.digest();
        int len = a.length;
        StringBuilder sb = new StringBuilder(len << 1);
        for (int i = 0; i < len; i++) {
            sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
            sb.append(Character.forDigit(a[i] & 0x0f, 16));
        }
        return sb.toString();
    }
}
