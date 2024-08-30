package nl.utwente.di.interactief2.rest_api;

import nl.utwente.di.interactief2.exceptions.UnSanitizedInputException;
import jakarta.ws.rs.Path;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import nl.utwente.di.interactief2.JDBC.SecurityClasses.Sanitizer;
import nl.utwente.di.interactief2.JDBC.attribute_queries.*;
import nl.utwente.di.interactief2.rest_api.dto.ChallengeMap;
import nl.utwente.di.interactief2.rest_api.authentication.Authenticator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Path("/challenges")
public class Challenges {

    Authenticator authenticator;

    public Challenges() {
        authenticator = new Authenticator();
    }

    /**
     * Servlet to insert a challenge into the database.
     *
     * @param body  The challenge information to be inserted.
     * @param token The token to verify if you are actually authenticated as an
     *              admin.
     * @return An appropriate http response.
     */
    @POST
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postChallenge(String body, @HeaderParam("Authorization") String token) throws Exception {
        //checks if the caller is logged in
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        //checks if the caller is an admin
        if (PersonAttributes.checkIfAdmin(studentNumber)) {
            ObjectMapper mapper = new ObjectMapper();
            ChallengeMap problem;
            // attempt to turn the JSON into a class with usable vars
            try {
                problem = mapper.readValue(body, ChallengeMap.class);
                //sanitizes input
                if (!Sanitizer.generalStringCheck(problem.description)
                        || !Sanitizer.generalStringCheck(problem.problemName)) {
                    throw new Exception();
                }
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
            }
            //create the challenge, then return if it was successful
            boolean check = ProblemAttributes.insertChallenge(problem.problemName, problem.locationID,
                    problem.description, problem.score);
            return Response.status(check ? Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    /**
     * Servlet to delete a challenge from the database.
     *
     * @param token     Token to verify if you are logged in.
     * @param problemID The ID of the challenge to be deleted.
     * @return An appropriate HTTP response indicating the result of the deletion
     * operation.
     */
    @DELETE
    @Path("/{problem_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteChallenge(@HeaderParam("Authorization") String token,
                                    @PathParam("problem_id") int problemID) {
        // authenticate the token
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        //check if the token holder is an admin
        if (PersonAttributes.checkIfAdmin(studentNumber)) {
            //delete hte challenge
            if (ProblemAttributes.deleteChallenge(problemID)) {
                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
            }

        } else {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    /**
     * Servlet to return all challenges in the database.
     *
     * @param token The token to verify if you are logged in.
     * @return An appropriate http response and/or the challenges in json.
     */
    @GET
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getChallenges(@HeaderParam("Authorization") String token) {
        //extracts the student number from the JWToken
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        //using the student number, checks if they are an admin
        if (PersonAttributes.checkIfAdmin(studentNumber)) {
            //if so, they are allowed to have all the challenges
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new JSONArray(ProblemAttributes.getAllChallenges()).toString()).build();
        } else {
            //if not, they can only get the challenges that their team has access to
            String teamName = PersonAttributes.getTeamNameByParticipant(studentNumber);

            if (!Objects.isNull(teamName)) {
                List<JSONObject> result = new ArrayList<>();
                List<JSONObject> unlockedLocations = LocationAttributes.getUnlockedLocationsByTeam(teamName);
                for (JSONObject location : unlockedLocations) {
                    result.addAll(ProblemAttributes.getAllChallengesByLocation(location
                            .getJSONObject(LocationAttributes.LOCATION_COLUMN).getInt(LocationAttributes.LOCATION_ID)));
                }
                //return all results that fit the criteria
                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE)
                        .entity(new JSONArray(result).toString()).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
            }
        }
    }

    /**
     * Servlet to return a specific challenge (if you have it unlocked).
     *
     * @param token     The token to verify if you are logged in.
     * @param problemId The challenge to query for.
     * @return An appropriate http response and/or the challenge object in json.
     */
    @GET
    @Path("/{problem_id}")
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getChallengeByTeam(@HeaderParam("Authorization") String token,
                                       @PathParam("problem_id") int problemId) {
        // using the authenticator, a studentnumber is produced
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        //a challenge with the right id, submitted by the team with the person with the above student number in it
        JSONObject challenge;
        if (PersonAttributes.checkIfAdmin(studentNumber)) {
            challenge = ProblemAttributes.getChallengeByID(problemId);
        } else {
            challenge = ProblemAttributes.getChallengeByIDWithUnlockCheck(problemId,
                    PersonAttributes.getTeamNameByParticipant(studentNumber));
        }


        //if the challenge is found, return it
        if (!Objects.isNull(challenge)) {
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(challenge.toString()).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    /**
     * Servlet to return all graded challenges (depending on logged in user).
     *
     * @param token Token to verify authentication.
     * @return All graded challenges or the graded challenges specifically for a
     * team.
     */
    @GET
    @Path("/graded")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getAllGradedChallenges(@HeaderParam("Authorization") String token) {
        //decrypts the JWTtoken using the authenticator, throwing an UNAUTHORIZED if the token is invalid
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        //using the studentnumber from the JWToken, it is checked if the user is an admin
        String teamName;
        if (PersonAttributes.checkIfAdmin(studentNumber)) {
            teamName = null;
        } else {
            //Whereupon they get their team name, if applicable
            teamName = PersonAttributes.getTeamNameByParticipant(studentNumber);

            // You are not a participant, you are a person
            if (Objects.isNull(teamName)) {
                return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
            }
        }
        //if the tests pass, the submitted challenges of either the team or all teams are sent, dependant on admin/not admin
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new JSONArray(ProblemAttributes.getAllSubmittedChallenges(teamName)).toString()).build();
    }

    /**
     * Servlet to return all ungraded challenges (depending on logged in user).
     *
     * @param token Token to verify authentication.
     * @return All ungraded challenges or the ungraded challenges specifically for a
     * team.
     */
    @GET
    @Path("/ungraded")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getAllUnGradedChallenges(@HeaderParam("Authorization") String token) {
        //once more token + authenticator to student number, however this time teamName is directly determined
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        //using the studentnumber from the JWToken, it is checked if the user is an admin
        String teamName;
        if (PersonAttributes.checkIfAdmin(studentNumber)) {
            teamName = null;
        } else {
            //Whereupon they get their team name, if applicable
            teamName = PersonAttributes.getTeamNameByParticipant(studentNumber);

            // You are not a participant, you are a person
            if (Objects.isNull(teamName)) {
                return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
            }
        }

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new JSONArray(ProblemAttributes.getAllNonSubmittedChallenges(teamName)).toString()).build();
    }

    /**
     * Servlet to alter a challenge in the database.
     *
     * @param body      The input for the altering.
     * @param token     The token to verify the user is authenticated as an admin.
     * @param problemId The id of the problem that needs to be altered.
     * @return A response appropriate for the input.
     */
    @PATCH
    @Path("/{problem_id}")
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response alterChallenge(JSONObject body, @HeaderParam("Authorization") String token,
                                   @PathParam("problem_id") int problemId) {
        // derives the student number from the token while checking security
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);

            if (PersonAttributes.checkIfAdmin(studentNumber)) {

                String newName = body.has(ProblemAttributes.PROBLEM_NAME)
                        ? body.getString(ProblemAttributes.PROBLEM_NAME)
                        : null;
                int newLocation = body.has(LocationAttributes.LOCATION_ID) ? body.getInt(LocationAttributes.LOCATION_ID)
                        : Integer.MIN_VALUE;
                int newScore = body.has(ProblemAttributes.SCORE) ? body.getInt(ProblemAttributes.SCORE)
                        : Integer.MIN_VALUE;
                String newDescr = body.has(ProblemAttributes.DESCRIPTION)
                        ? body.getString(ProblemAttributes.DESCRIPTION)
                        : null;
                //checks if the object is null or is equal to a MIN_VALUE, wich we use as a placeholder for invalid
                if (Objects.isNull(newName) && Objects.equals(newLocation, Integer.MIN_VALUE)
                        && Objects.equals(newScore, Integer.MIN_VALUE) && Objects.isNull(newDescr)) {
                    throw new BadRequestException();
                } else if (!Sanitizer.generalStringCheck(newName) || !Sanitizer.generalStringCheck(newDescr)) {
                    throw new UnSanitizedInputException();
                }
                //get the challenge
                JSONObject challenge = ProblemAttributes.getChallengeByID(problemId);

                if (Objects.isNull(challenge)) {
                    throw new BadRequestException();
                }
                // store all the old values of the challenge
                String oldName = challenge.getJSONObject(ProblemAttributes.CHALLENGE_COLUMN)
                        .getString(ProblemAttributes.PROBLEM_NAME);
                int oldLocation = challenge.getJSONObject(ProblemAttributes.CHALLENGE_COLUMN)
                        .getInt(LocationAttributes.LOCATION_ID);
                int oldScore = challenge.getJSONObject(ProblemAttributes.CHALLENGE_COLUMN)
                        .getInt(ProblemAttributes.SCORE);
                String oldDescr = challenge.getJSONObject(ProblemAttributes.CHALLENGE_COLUMN)
                        .getString(ProblemAttributes.DESCRIPTION);
                //set the new values
                if (ProblemAttributes.updateChallenge(problemId, (Objects.equals(newName, oldName) ? oldName : newName),
                        (Objects.equals(newScore, oldScore) ? oldScore : newScore),
                        (Objects.equals(newLocation, oldLocation) ? oldLocation : newLocation),
                        (Objects.equals(newDescr, oldDescr) ? oldDescr : newDescr))) {
                    //return that it was a success
                    return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).build();
                } else {
                    throw new BadRequestException();
                }

            } else {
                return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON_TYPE).build();
            }
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (UnSanitizedInputException | BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

}
