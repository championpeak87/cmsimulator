package fiitstu.gulis.cmsimulator.network.users;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordManager {
    private static final int SALT_SIZE = 32;
    private static final int CHAR_SIZE = 256;
    /*

    HASHOVANIE SHA-256 PREVZATE ZO STRANKY
    https://www.geeksforgeeks.org/sha-256-hash-in-java/

     */

    public byte[] hashPassword(String password) throws NoSuchAlgorithmException
    {

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        return messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));
    }

    public String saltPassword()
    {
        // COMPLETED: Create random salt generator
        SecureRandom secureRandom = new SecureRandom();
        byte[] saltLetter = secureRandom.generateSeed(SALT_SIZE);
        String salt = toHexString(saltLetter);

        return salt;
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

    public String getAuthkey(String password, String salt)
    {
        byte[] hash;
        String saltedPassword = password + salt;
        String hashedPassword = "";

        try {
            hash = hashPassword(saltedPassword);
            hashedPassword = toHexString(hash);
        }

        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return hashedPassword;
    }
}
