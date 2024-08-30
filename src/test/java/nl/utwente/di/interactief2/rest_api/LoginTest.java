package nl.utwente.di.interactief2.rest_api;

import jakarta.ws.rs.core.Response;
import nl.utwente.di.interactief2.JDBC.attribute_queries.PersonAttributes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.mockito.Mockito;

public class LoginTest {

    /*
    @Test
    public void testLoginUnauthorized() {
        String requestBody = """
                {
                    "student_number":123456,
                    "password":"ShortPassUn235"
                }
                """;
        int expected = Response.Status.UNAUTHORIZED.getStatusCode();
        PersonAttributes personAttributesMock = Mockito.mock(PersonAttributes.class);
        Mockito.when(personAttributesMock.checkPassword(123456, "ShortPassUn235")).thenReturn(false);
        Login login = new Login(personAttributesMock);
        int got = login.processCredentials(requestBody).getStatus();
        assertEquals(got, expected);
        Mockito.verify(personAttributesMock, Mockito.times(1)).checkPassword(123456, "ShortPassUn235");
    }

    @Test
    public void testLoginAuthorized() {
        String requestBody = """
                {
                    "student_number":123456,
                    "password":"ShortPassUn235"
                }
                """;
        int expected = Response.Status.UNAUTHORIZED.getStatusCode();
        PersonAttributes personAttributesMock = Mockito.mock(PersonAttributes.class);
        Mockito.when(personAttributesMock.checkPassword(123456, "ShortPassUn235")).thenReturn(false);
        //Login login = new Login(personAttributesMock);
        int got = login.processCredentials(requestBody).getStatus();
        assertEquals(got, expected);
        Mockito.verify(personAttributesMock, Mockito.times(1)).checkPassword(123456, "ShortPassUn235");
    }

    @Test
    public void testLoginBadRequest() {
        String requestBody = """
                {
                    ThisSHOULDnotBeVALIDjson
                    "student_number":123456,
                    "password":"ShortPassUn235"
                }
                """;
        int expected = Response.Status.BAD_REQUEST.getStatusCode();
        PersonAttributes personAttributesMock = Mockito.mock(PersonAttributes.class);
        Mockito.when(personAttributesMock.checkPassword(123456, "ShortPassUn235")).thenReturn(true);
        //Login login = new Login(personAttributesMock);
        int got = login.processCredentials(requestBody).getStatus();
        assertEquals(got, expected);
        Mockito.verify(personAttributesMock, Mockito.times(0));
    }
    */

}
