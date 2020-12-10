package udp;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.spec.KeySpec;
import java.util.Base64;

public class AES {

    private static String salt = "PRLab2";
    // for using Cipher
    private static Cipher cipher = null;

    public static String encryptFile(String plainText, Key secretKey) {
        byte[] plainTextByte = plainText.getBytes();

        String encryptedText = "";

        try {
            try {
                cipher = Cipher.getInstance("AES");
            } catch(Exception e) {
                System.err.println("Error while getting AES algorithm: " + e);
            }
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedByte = cipher.doFinal(plainTextByte);

            Base64.Encoder encoder = Base64.getEncoder();

            encryptedText = encoder.encodeToString(encryptedByte);

        } catch(Exception e) {
            System.err.println("Error while initializing Cipher while encrypting text: " + e);
        }

        return encryptedText;
    }

    // decryptFile method takes encrypted text and decrypts it with secretKey, returns decrypted string
    public static String decryptFile(String encryptedText, Key secretKey) {
        Base64.Decoder decoder = Base64.getDecoder();

        byte[] encryptedTextByte = decoder.decode(encryptedText);

        String decryptedText = "";

        try {
            try {
                cipher = Cipher.getInstance("AES");
            } catch(Exception e) {
                System.err.println("Error while getting AES algorithm: " + e);
            }
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decryptedByte = cipher.doFinal(encryptedTextByte);

            decryptedText = new String(decryptedByte);
        } catch(Exception e) {
            System.err.println("Error while initializing Cipher while decrypting text: " + e);
        }

        return decryptedText;
    }
}
