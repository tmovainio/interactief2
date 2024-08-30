package nl.utwente.di.interactief2.rest_api;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import nl.utwente.di.interactief2.JDBC.SecurityClasses.Sanitizer;
import nl.utwente.di.interactief2.JDBC.attribute_queries.PersonAttributes;
import nl.utwente.di.interactief2.JDBC.attribute_queries.SettingAttributes;
import nl.utwente.di.interactief2.JDBC.attribute_queries.SubmissionAttributes;
import nl.utwente.di.interactief2.rest_api.authentication.Authenticator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Set;

@Path("/leaderboard")
public class LeaderBoard {

    static Authenticator authenticator = new Authenticator();


    /**
     * Servlet to return the leaderboard
     * @param token The token to verify if the user is logged in
     * @return A proper HTTP response
     */
    @GET
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getLeaderboard(@HeaderParam("Authorization") String token) {
        // authenticate with the JWToken
        int sNumb;
        try {
            sNumb = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        //Get the setting and check if it is true or if it is an admin always print out the scoreboard.
        try {
            System.out.println(SettingAttributes.getSettingByName(SettingAttributes.SCORE_BOARD_TOGGLE));
            if (SettingAttributes.getSettingByName(SettingAttributes.SCORE_BOARD_TOGGLE) || PersonAttributes.checkIfAdmin(sNumb)) {

                //Return the scoreboard.
                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(new JSONArray(SubmissionAttributes.getScoreBoard()).toString()).build();
            } else {

                //The service has been disabled, and you are not an admin, so the response is GONE.
                return Response.status(Response.Status.GONE).type(MediaType.APPLICATION_JSON_TYPE).build();
            }

        } catch (SQLException e) {
            e.printStackTrace();

            //Something went horribly wrong...
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    /**
     * Servlet to return the leaderboard for a specific team by team name
     *
     * @param token    The token to verify if the user is logged in
     * @param teamName The name of the team
     * @return A proper HTTP response
     */
    @GET
    @Path("/{team_name}")
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getLeaderBoardByTeamName(@HeaderParam("Authorization") String token,
            @PathParam("team_name") String teamName) {
        // authenticate once more
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        // make sure it is only accessed by the right people
        if (Objects.equals(teamName, PersonAttributes.getTeamNameByParticipant(studentNumber)) && Sanitizer.teamNameCheck(teamName)) {

            // get the score to return
            JSONObject score = SubmissionAttributes.getTotalScoreOfTeam(teamName);
            if (!Objects.isNull(score)) {
                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE)
                        .entity(score.toString()).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE)
                        .build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    /**
     * Servlet to switch the availability of the leaderboard
     *
     * @param token The token to verify if the user is logged in
     * @return A proper HTTP response
     */
    @PATCH
    @Path("/availability")
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response switchLeaderboard(@HeaderParam("Authorization") String token) {
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        if (PersonAttributes.checkIfAdmin(studentNumber)) {
            try {
                boolean oldSetting = SettingAttributes.getSettingByName(SettingAttributes.SCORE_BOARD_TOGGLE);
                SettingAttributes.updateSetting(SettingAttributes.SCORE_BOARD_TOGGLE, !oldSetting);

                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).build();
            } catch (SQLException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE)
                        .build();
            }
        }
        return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    /**
     * Servlet to return the availability of the leaderboard
     *
     * @param token The token to verify if the user is logged in
     * @return A proper HTTP response
     */
    @GET
    @Path("/availability")
    @Produces("application/json")
    public Response getLeaderboardAvailability(@HeaderParam("Authorization") String token) {
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        if (PersonAttributes.checkIfAdmin(studentNumber)) {
            JSONObject json = new JSONObject();
            try {
                json.put("visible", SettingAttributes.getSettingByName(SettingAttributes.SCORE_BOARD_TOGGLE));
            } catch (SQLException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE)
                        .build();
            }
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(json.toString())
                    .build();
        }
        return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
