package nl.utwente.di.interactief2.JDBC.SecurityClasses;

import java.security.SecureRandom;

import static nl.utwente.di.interactief2.JDBC.SecurityClasses.Hasher.toHex;

public class Salter {

    /**
     * Generates a random salt
     * @return a random salt
     */
    public static String getSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return toHex(salt);
    }

}
