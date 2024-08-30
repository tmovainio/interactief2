package nl.utwente.di.interactief2.rest_api;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import nl.utwente.di.interactief2.JDBC.SecurityClasses.Sanitizer;
import nl.utwente.di.interactief2.JDBC.attribute_queries.PersonAttributes;
import nl.utwente.di.interactief2.rest_api.dto.JWToken;
import nl.utwente.di.interactief2.rest_api.authentication.Authenticator;
import nl.utwente.di.interactief2.rest_api.dto.Credentials;
import org.json.JSONObject;

import java.util.Objects;

import static nl.utwente.di.interactief2.rest_api.LeaderBoard.authenticator;

@Path("/login")
public class Login {

    public Login() {
    }

    /**
     * Servlet to login and generate a JWToken
     * @param body The JSON body containing the credentials
     * @return A proper HTTP response
     */
    @POST
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response processCredentials(String body) {
        Credentials credentials;
        ObjectMapper mapper = new ObjectMapper();
        try {
            credentials = mapper.readValue(body, Credentials.class);
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(body).type(MediaType.APPLICATION_JSON_TYPE)
                    .build();
        }
        Authenticator authenticator = new Authenticator();
        String authenticationToken = authenticator.encodeJWToken(credentials.studentNumber);
        JWToken jwToken = new JWToken(authenticationToken);
        String formattedJsonAuthentication;

        try {
            formattedJsonAuthentication = mapper.writeValueAsString(jwToken);
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(body)
                    .type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        if (PersonAttributes.checkPassword(credentials.studentNumber, credentials.password)) {
            return Response.status(Response.Status.OK).entity(formattedJsonAuthentication)
                    .type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).entity(body).type(MediaType.APPLICATION_JSON_TYPE).build();
    }


    /**
     * Servlet that does reset the password sometimes
     * @param token The authorization token
     * @param sNumb The student number whose password is going to be reset
     * @param body the new password
     * @return A proper HTTP response
     */
    @POST
    @Path("/forgot_password/{student_number}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response requestNewPassword(@HeaderParam("Authorization") String token,
            @PathParam("student_number") int sNumb, String body) {

        // If you do not have a token, get a token through a sms or something (assuming
        // you come from outside the platform).
        int tokenNumber;
        try {
            tokenNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            JSONObject person = PersonAttributes.getPersonByID(sNumb);

            if (!Objects.isNull(person)) {
                String phoneNumb = person.getJSONObject(PersonAttributes.PERSON_OBJECT_NAME)
                        .getString(PersonAttributes.PHONE_NUMBER);

                /*
                 * So here you can do a code dump for a procedure to get a token through the
                 * phone number.
                 * This ensures that no-one will be able to just change passwords.
                 * This can only be done with a phone service, so this cannot be implemented by
                 * us.
                 * When a token is given, you can be redirected to the change password page to
                 * actually change the password.
                 * This token will then be necessary in the code after this try-catch block.
                 */

                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).build();
            } else {
                // Appropriate response to someone trying to change a password without a token
                return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON_TYPE).entity(
                        new JSONObject("Server says", "You thought you could change passwords without logging in?")
                                .toString())
                        .build();
            }
        }

        if (tokenNumber == sNumb) {

            // If you have a valid token, continue onwards and reset password and/or change
            // phone number.
            JSONObject json = new JSONObject(body);

            String newPassword = json.has("password") ? json.getString("password") : null;
            String newPhoneNumber = json.has("phone_number") ? json.getString("phone_number") : null;

            // Sanitize input
            if (Sanitizer.passwordCheck(newPassword) && Sanitizer.phoneNumberCheck(newPhoneNumber)) {

                // Check if you successfully change the person attributes.
                if (PersonAttributes.changePerson(tokenNumber, newPassword, newPhoneNumber)) {
                    return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE)
                            .build();
                }
            } else {
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
            }

        } else {

            // Appropriate response to someone thinking they are clever.
            return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new JSONObject("Server says", "You thought you could change a password of someone else?")
                            .toString())
                    .build();
        }
    }
}
