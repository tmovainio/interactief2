package nl.utwente.di.interactief2.JDBC.SecurityClasses;

import nl.utwente.di.interactief2.JDBC.attribute_queries.PersonAttributes;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Hasher {

    public Hasher() {
    }

    public static String hash(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        char[] chars = password.toCharArray();
        int i = 1000;

        PBEKeySpec spec = new PBEKeySpec(chars, salt.getBytes(), i, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");

        byte[] hash = skf.generateSecret(spec).getEncoded();
        return toHex(hash);
    }

    public static String toHex(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(Integer.toHexString((b & 0xFF) | 0x100), 1, 3);
        }
        return sb.toString();
    }

    public static boolean verifyPassword(int sid, String password, PersonAttributes personAttributes) {
        return personAttributes.checkPassword(sid, password);
    }

}
