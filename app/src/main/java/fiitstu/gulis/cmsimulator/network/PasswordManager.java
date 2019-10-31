package fiitstu.gulis.cmsimulator.network;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordManager {
    /*

    HASHOVANIE SHA-256 PREVZATE ZO STRANKY
    https://www.geeksforgeeks.org/sha-256-hash-in-java/

     */

    public byte[] hashPassword(String password) throws NoSuchAlgorithmException
    {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        return messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));
    }

    public String toHexString(byte[] hash)
    {
        BigInteger number = new BigInteger(1, hash);

        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 32)
        {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    public String getAuthkey(String password)
    {
        byte[] hash;
        String hashedPassword = "";
        try {
            hash = hashPassword(password);
            hashedPassword = toHexString(hash);
        }

        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return hashedPassword;
    }
}
