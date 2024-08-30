package nl.utwente.di.interactief2.rest_api;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import nl.utwente.di.interactief2.JDBC.attribute_queries.PersonAttributes;
import nl.utwente.di.interactief2.JDBC.attribute_queries.TeamAttributes;
import nl.utwente.di.interactief2.rest_api.authentication.Authenticator;
import nl.utwente.di.interactief2.rest_api.dto.Person;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("/teams")
public class Teams {
    Authenticator authenticator;

    public Teams() {
        authenticator = new Authenticator();
    }

    /**
     * Servlet to create a new team
     * @param token The token to verify if the user is logged in
     * @param body The request body containing the team information
     * @return An appropriate HTTP response containing the result of the team creation
     */
    @POST
    @Produces("application/json")
    @Consumes("application/json")
    public Response createTeam(@HeaderParam("Authorization") String token, String body) {
        //checks if the user is logged in
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        //retrieve steam name
        JSONObject json = new JSONObject(body);
        String teamName = json.getString("teamName");
        if (!TeamAttributes.createTeam(teamName, studentNumber)) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).build();
        } else {
            JSONObject team = TeamAttributes.getTeamByName(teamName, (PersonAttributes.checkIfAdmin(studentNumber) || TeamAttributes.getTeamCaptainByTeamName(teamName).getJSONObject("participant").getInt("s_numb") == studentNumber));
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(team.toString())
                    .build();
        }

    }

    /**
     * Servlet to retrieve all teams
     * @param token The token to verify if the user is logged in
     * @return An appropriate HTTP response
     */
    @GET
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getTeams(@HeaderParam("Authorization") String token, @QueryParam("status") @DefaultValue("") String status) {
        //checks if the user is logged in
        try {
            authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        if (status.equals("")) {
            return Response.status(Response.Status.OK)
                    .entity(new JSONArray(TeamAttributes.getAllTeams()).toString())
                    .type(MediaType.APPLICATION_JSON_TYPE).build();
        } else if (status.equals("AWAITING_APPROVAL")) {
            return Response.status(Response.Status.OK).entity(new JSONArray(TeamAttributes.getAllUnapprovedTeams()).toString()).type(MediaType.APPLICATION_JSON).build();
        } else if (status.equals("APPROVED")) {
            return Response.status(Response.Status.OK).entity(new JSONArray(TeamAttributes.getAllApprovedTeams(new PersonAttributes())).toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
        } else {
            return Response.status(Response.Status.OK)
                    .entity(new JSONArray(TeamAttributes.getAllTeams()).toString())
                    .type(MediaType.APPLICATION_JSON_TYPE).build();
        }

    }

    /**
     * Servlet to retrieve a specific team by its name
     * @param token The token to verify if the user is logged in
     * @param teamName The name of the team to retrieve
     * @return An appropriate HTTP response
     */
    @GET
    @Path("/{team_name}")
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getSpecificTeam(@HeaderParam("Authorization") String token, @PathParam("team_name") String teamName) {
        //checks if the user is logged in
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        JSONObject team = TeamAttributes.getTeamByName(teamName, (PersonAttributes.checkIfAdmin(studentNumber) || TeamAttributes.getTeamCaptainByTeamName(teamName).getInt("s_numb") == studentNumber));
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(team.toString())
                .build();
    }

    /**
     * Servlet to reset the invitation link
     * @param token The token to verify if the user is logged in
     * @param teamName The name of the team whose invitation link to reset
     * @return An appropriate HTTP response
     */
    @GET
    @Path("/join/reset_invite/{team_name}")
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response resetInvite(@HeaderParam("Authorization") String token, @PathParam("team_name") String teamName) {
        //checks if the user is logged in
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        // checks if admin or if it is a team captain
        if (PersonAttributes.checkIfAdmin(studentNumber) || PersonAttributes.getTeamNameByParticipant(studentNumber) == teamName) {
            if (TeamAttributes.setInviteLink(teamName)) {
                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    /**
     * Servlet to approve or reject a team's registration
     * @param token The token to verify if the user is logged in
     * @param teamName The name of the team to approve or reject
     * @param body The request body containing the approval status
     * @return An appropriate HTTP response
     */
    @PATCH
    @Path("/{team_name}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response approveTeam(@HeaderParam("Authorization") String token, @PathParam("team_name") String teamName,
                                String body) {
        JSONObject json = new JSONObject(body);
        //checks if the user is logged in
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        if (studentNumber != -1 && PersonAttributes.checkIfAdmin(studentNumber)) {
            if (TeamAttributes.approveTeam(teamName, json.getBoolean("approve"))) {
                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).build();
            }
        }
        return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    /**
     * Servlet to join a team using an invite code
     * @param token The token to verify if the user is logged in
     * @param invite_code The invite code used to join the team
     * @return An appropriate HTTP response
     */
    @POST
    @Path("/join/{invite_code}")
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response joinTeam(@HeaderParam("Authorization") String token, @PathParam("invite_code") String invite_code) {
        //checks if the user is logged in
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        String teamName;
        //joins a team
        try {
            teamName = TeamAttributes.joinTeamByInviteLink(studentNumber, invite_code);
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        if (teamName == null) {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        JSONObject team = TeamAttributes.getTeamByName(teamName, (PersonAttributes.checkIfAdmin(studentNumber) || TeamAttributes.getTeamCaptainByTeamName(teamName).getJSONObject("participant").getInt("s_numb") == studentNumber));
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(team.toString())
                .build();
    }

    /**
     *  Servlet to delete a team
     * @param token The token to verify if the user is logged in
     * @param team_name The name of the team to be deleted
     * @return An appropriate HTTP response
     */
    @DELETE
    @Path("/{team_name}/delete")
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteTeam(@HeaderParam("Authorization") String token, @PathParam("team_name") String team_name) {
        //checks if the user is logged in
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        try {
            //checks if it is either the team captain or an admin
            if (PersonAttributes.checkIfAdmin(studentNumber) || TeamAttributes.getTeamCaptainByTeamName(team_name).getJSONObject("participant").getInt("s_numb") == studentNumber) {
                //whereupon deletion takes place
                if (TeamAttributes.deleteTeamByTeamName(team_name)) {
                    return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).build();
                }
            } else {
                return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON_TYPE).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

    }

}
