package nl.utwente.di.interactief2.rest_api;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.utwente.di.interactief2.exceptions.UnSanitizedInputException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import nl.utwente.di.interactief2.JDBC.SecurityClasses.Sanitizer;
import nl.utwente.di.interactief2.JDBC.attribute_queries.PersonAttributes;
import nl.utwente.di.interactief2.JDBC.attribute_queries.ProblemAttributes;
import nl.utwente.di.interactief2.rest_api.authentication.Authenticator;
import nl.utwente.di.interactief2.rest_api.dto.Crazy88Map;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

@Path("/crazy88")
public class Crazy88 {

    Authenticator authenticator = new Authenticator();

    /**
     * Servlet to insert a crazy88 problem into the database.
     * 
     * @param body  The input for the crazy88 problem.
     * @param token The token to verify authentication of an admin.
     * @return A response appropriate to the input.
     */
    @POST
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postCrazy88(String body, @HeaderParam("Authorization") String token) {
        //use the token to check if the request is valid and the student number of the requested
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        if (PersonAttributes.checkIfAdmin(studentNumber)) {
            ObjectMapper mapper = new ObjectMapper();
            Crazy88Map crazy88Map;
            try {
                crazy88Map = mapper.readValue(body, Crazy88Map.class);

                if (!Sanitizer.generalStringCheck(crazy88Map.description)
                        || !Sanitizer.generalStringCheck(crazy88Map.problemName)) {
                    throw new Exception();
                }
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
            }

            boolean check = ProblemAttributes.insertCrazy88(crazy88Map.problemName, crazy88Map.description,
                    crazy88Map.score);
            return Response.status(check ? Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    /**
     * Servlet to delete a Crazy88 challenge from the database.
     * 
     * @param token     The token to verify if the user is logged in.
     * @param problemID The ID of the Crazy88 challenge to be deleted.
     * @return An appropriate response.
     */
    @DELETE
    @Path("/{problem_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteCrazy88(@HeaderParam("Authorization") String token, @PathParam("problem_id") int problemID) {
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        if (PersonAttributes.checkIfAdmin(studentNumber)) {

            if (ProblemAttributes.deleteCrazy88(problemID)) {
                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
            }

        } else {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    /**
     * Servlet to return all crazy88 problems in the database.
     * 
     * @param token The token to verify if you are logged in.
     * @return An appropriate http response and/or the crazy88 problems in json.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCrazy88(@HeaderParam("Authorization") String token) {
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new JSONArray(ProblemAttributes.getAllCrazy88()).toString()).build();
    }

    /**
     * Servlet to return all graded crazy88 problems (depending on logged in user).
     * 
     * @param token Token to verify authentication.
     * @return All graded crazy88 problems or the graded crazy88 problems
     *         specifically for a team.
     */
    @GET
    @Path("/graded")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getAllGradedCrazy88(@HeaderParam("Authorization") String token) {
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        String teamName;
        if (PersonAttributes.checkIfAdmin(studentNumber)) {
            teamName = null;
        } else {
            teamName = PersonAttributes.getTeamNameByParticipant(studentNumber);

            // You are not a participant, you are a person
            if (Objects.isNull(teamName)) {
                return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
            }
        }

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new JSONArray(ProblemAttributes.getAllSubmittedCrazy88(teamName))).build();
    }

    /**
     * Servlet to return all ungraded crazy88 problems (depending on logged in
     * user).
     * 
     * @param token Token to verify authentication.
     * @return All graded crazy88 problems or the ungraded crazy88 problems
     *         specifically for a team.
     */
    @GET
    @Path("/ungraded")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getAllUnGradedCrazy88(@HeaderParam("Authorization") String token) {
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        String teamName;
        if (PersonAttributes.checkIfAdmin(studentNumber)) {
            teamName = null;
        } else {
            teamName = PersonAttributes.getTeamNameByParticipant(studentNumber);

            // You are not a participant, you are a person
            if (Objects.isNull(teamName)) {
                return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
            }
        }

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new JSONArray(ProblemAttributes.getAllNonSubmittedCrazy88(teamName))).build();
    }

    /**
     * Servlet to retrieve a specific crazy88 problem in json.
     * @param token The token necessary to check if you are authenticated.
     * @param problemID The problemID to retrieve.
     * @return A response appropriate to the input.
     */
    @GET
    @Path("/{problem_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getSpecificCrazy88(@HeaderParam("Authorization") String token, @PathParam("problem_id") int problemID) {
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        JSONObject crazy88 = ProblemAttributes.getCrazy88ByID(problemID);

        if(!Objects.isNull(crazy88)) {
            return Response.status(Response.Status.OK).entity(crazy88.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }


    /**
     * Servlet to alter a crazy88 problem in the database.
     * 
     * @param body      The input for the altering.
     * @param token     The token to verify the user is authenticated as an admin.
     * @param problemId The id of the problem that needs to be altered.
     * @return A response appropriate for the input.
     */
    @PATCH
    @Path("/{problem_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response alterCrazy88(String body, @HeaderParam("Authorization") String token,
            @PathParam("problem_id") int problemId) {
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);

            if (PersonAttributes.checkIfAdmin(studentNumber)) {
                JSONObject json = new JSONObject(body);

                String newName = json.has(ProblemAttributes.PROBLEM_NAME)
                        ? json.getString(ProblemAttributes.PROBLEM_NAME)
                        : null;
                int newScore = json.has(ProblemAttributes.SCORE) ? json.getInt(ProblemAttributes.SCORE)
                        : Integer.MIN_VALUE;
                String newDescr = json.has(ProblemAttributes.DESCRIPTION)
                        ? json.getString(ProblemAttributes.DESCRIPTION)
                        : null;

                if (Objects.isNull(newName) && Objects.equals(newScore, Integer.MIN_VALUE)
                        && Objects.isNull(newDescr)) {
                    throw new BadRequestException();
                } else if (!Sanitizer.generalStringCheck(newName) || !Sanitizer.generalStringCheck(newDescr)) {
                    throw new UnSanitizedInputException();
                }

                JSONObject crazy88 = ProblemAttributes.getCrazy88ByID(problemId);

                if (Objects.isNull(crazy88)) {
                    throw new BadRequestException();
                }

                String oldName = crazy88.getJSONObject(ProblemAttributes.CRAZY88_COLUMN)
                        .getString(ProblemAttributes.PROBLEM_NAME);
                int oldScore = crazy88.getJSONObject(ProblemAttributes.CRAZY88_COLUMN).getInt(ProblemAttributes.SCORE);
                String oldDescr = crazy88.getJSONObject(ProblemAttributes.CRAZY88_COLUMN)
                        .getString(ProblemAttributes.DESCRIPTION);

                if (ProblemAttributes.updateCrazy88(problemId, (Objects.equals(newName, oldName) ? oldName : newName),
                        (Objects.equals(newScore, oldScore) ? oldScore : newScore),
                        (Objects.equals(newDescr, oldDescr) ? oldDescr : newDescr))) {
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
