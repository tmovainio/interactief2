package nl.utwente.di.interactief2.rest_api.authentication;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthenticatorTest {

        @Test
        public void testTokenEncodeDecode() throws InterruptedException {
            int studentNumber = 123456;
            Authenticator authenticator = new Authenticator();
            String token = authenticator.encodeJWToken(studentNumber);
            Thread.sleep(350);
            int got = authenticator.decodeJWToken(token);
            int expected = studentNumber;
            assertEquals(got, expected);
        }

}
