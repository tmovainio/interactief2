package nl.utwente.di.interactief2.rest_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserInformationTest {

    // assumes the Login returns a functional token
    // @Test
    // public void testUserInformation() {
    // HttpHeaders headers = Mockito.mock(HttpHeaders.class);
    // PersonAttributes personAttributesMock = Mockito.mock(PersonAttributes.class);
    //
    // Authenticator authenticator = new Authenticator();
    // String tokenJWT = authenticator.encodeJWToken(123456);
    //
    // Mockito.when(headers.firstValue("authorization")).thenReturn(tokenJWT.describeConstable());
    // Person returnPerson = new Person(123456, "name", "phoneNumber", "teamName");
    // Mockito.when(personAttributesMock.getPersonBy_sNumb(123456)).thenReturn(returnPerson);
    //
    // UserInformation userInformation = new UserInformation(personAttributesMock);
    // Response got = userInformation.processCredentials(headers);
    // assertEquals(Response.Status.OK.getStatusCode(), got.getStatus());
    // assertEquals("{\"name\":\"name\",\"student_number\":123456,\"phone_number\":\"phoneNumber\",\"team_name\":\"teamName\"}",
    // got.getEntity());
    // }
}
