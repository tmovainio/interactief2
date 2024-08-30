package nl.utwente.di.interactief2.JDBC.SecurityClasses;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Character.isUpperCase;

public class Sanitizer {

    private static final int MINIMUM_STUDENT_NUMBER = 0;
    private static final int MAXIMUM_STUDENT_NUMBER = 9999999;
    private static final int MAXIMUM_FILE_NAME_LENGTH = 100;
    private static final int MAXIMUM_USER_NAME_LENGTH = 18;
    private static final int MINIMUM_USER_NAME_LENGTH = 3;
    private static final int MINIMUM_PASSWORD_LENGTH = 12;
    private static final int MINIMUM_TEAM_NAME_LENGTH = 1;
    private static final String PHONE_NUMBER_REGEX = "^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$"
            + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?){2}\\d{3}$"
            + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?)(\\d{2}[ ]?){2}\\d{2}$";

    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile(PHONE_NUMBER_REGEX);

    private static boolean checkForUTF8(String thing) {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        try {
            decoder.decode(ByteBuffer.wrap(thing.getBytes()));
        } catch (CharacterCodingException ex) {
            return false;
        }
        return true;
    }

    public static boolean checkStudentName(String name) {
        return checkForUTF8(name) && name.length() >= MINIMUM_USER_NAME_LENGTH && name.length() <= MAXIMUM_FILE_NAME_LENGTH;
    }

    public static boolean phoneNumberCheck(String phoneNumber) {
        Matcher matcher = PHONE_NUMBER_PATTERN.matcher(phoneNumber);
        return matcher.matches();
    }

    public static boolean studentNumberFormat(int sNumb) {
        return sNumb > MINIMUM_STUDENT_NUMBER && sNumb <= MAXIMUM_STUDENT_NUMBER;
    }

    public static boolean teamNameCheck(String teamName) {
        return checkForUTF8(teamName) && teamName.length() > MINIMUM_TEAM_NAME_LENGTH;
    }

    public static boolean passwordCheck(String password) {
        return checkForUTF8(password) && password.length() > MINIMUM_PASSWORD_LENGTH && passwordContainsCharacters(password);
    }

    private static boolean passwordContainsCharacters(String password) {
        char[] passwordArray = password.toCharArray();

        boolean hasUpperCase = false;
        for(char thing : passwordArray) {
            if(isUpperCase(thing)) {
                hasUpperCase = true;
            }
        }

        return  hasUpperCase;
    }

    public static boolean fileNameCheck(String name) {
        return checkForUTF8(name) && name.length() <= MAXIMUM_FILE_NAME_LENGTH;
    }

    public static boolean generalStringCheck(String string) {
        return checkForUTF8(string);
    }

}
