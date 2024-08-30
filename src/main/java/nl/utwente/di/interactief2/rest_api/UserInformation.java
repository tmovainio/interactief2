package nl.utwente.di.interactief2.rest_api;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import nl.utwente.di.interactief2.JDBC.attribute_queries.PersonAttributes;
import nl.utwente.di.interactief2.JDBC.attribute_queries.TeamAttributes;
import nl.utwente.di.interactief2.rest_api.authentication.Authenticator;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

@Path("/user")
public class UserInformation {

    Authenticator authenticator = new Authenticator();


    /**
     * Servlet to process user credentials
     * @param token The token to verify if the user is logged in
     * @return An appropriate HTTP response
     */
    @GET
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response processCredentials(@HeaderParam("Authorization") String token) {
        //check if logged in
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        JSONObject json = PersonAttributes.getParticipantByID(studentNumber);
        if (Objects.isNull(json)) {
            json = PersonAttributes.getPersonByID(studentNumber);
        }
        //check if it exists
        if (!Objects.isNull(json)) {
            try {
                json.getJSONObject("person").put("admin", PersonAttributes.checkIfAdmin(studentNumber));
            } catch (JSONException e) {
                json.getJSONObject("participant").put("admin", PersonAttributes.checkIfAdmin(studentNumber));
            }
            //return it
            return Response.status(Response.Status.OK).entity(json.toString()).type(MediaType.APPLICATION_JSON_TYPE)
                    .build();
        } else {
            return Response.status(Response.Status.OK).entity("{\"boolean\": \"true\"}").type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    /**
     * Retrieves the team information for the authenticated user
     * @param token The token to verify if the user is logged in
     * @return An appropriate HTTP response
     */
    @GET
    @Path("/team")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeam(@HeaderParam("Authorization") String token) {
        //check if logged in
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        //get team name
        String teamName = PersonAttributes.getTeamNameByParticipant(studentNumber);
        if (Objects.isNull(teamName)) {
            return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(" { \"message\": \"no team found\" } ").build();
        }

        //get team
        JSONObject json = TeamAttributes.getTeamByName(teamName, (PersonAttributes.checkIfAdmin(studentNumber) || TeamAttributes.getTeamCaptainByTeamName(teamName).getJSONObject("participant").getInt("s_numb") == studentNumber));
        if(Objects.isNull(json)){
            return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(" { \"message\": \"no team found\" } ").build();
        } //send it
        return Response.status(Response.Status.OK).entity(json.toString()).type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

}
