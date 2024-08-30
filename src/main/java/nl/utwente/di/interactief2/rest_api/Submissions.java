package nl.utwente.di.interactief2.rest_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.activation.MimetypesFileTypeMap;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.Path;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import nl.utwente.di.interactief2.JDBC.attribute_queries.PersonAttributes;
import nl.utwente.di.interactief2.JDBC.attribute_queries.ProblemAttributes;
import nl.utwente.di.interactief2.JDBC.attribute_queries.SubmissionAttributes;
import nl.utwente.di.interactief2.JDBC.attribute_queries.TeamAttributes;
import nl.utwente.di.interactief2.rest_api.dto.Application;
import nl.utwente.di.interactief2.rest_api.authentication.Authenticator;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static nl.utwente.di.interactief2.rest_api.LeaderBoard.authenticator;

@Path("/submissions")
public class Submissions {

    private static final String TOKEN = "authorization";

    @Context
    private ServletContext context;

    Authenticator authenticator;

    public Submissions() {
        authenticator = new Authenticator();
    }

    /**
     * Servlet to add a submission
     * 
     * @param body  The submission information to be added
     * @param token The token to verify if the user is logged in
     * @return An appropriate HTTP response
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addSubmission(String body, @HeaderParam("Authorization") String token) {
        // use JWToken to authenticate
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        Application application;
        try {
            //map the included JSON so it fits in an object
            ObjectMapper mapper = new ObjectMapper();
            application = mapper.readValue(body, Application.class);

            if (SubmissionAttributes.submitSubmission(PersonAttributes.getTeamNameByParticipant(studentNumber),
                    application.problem_id, application.submission)) {
                //send it if everything is right
                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).build();
            } else {
                throw new Exception();
            }

        } catch (Exception e) {
            //don't if it's not
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

    }

    /**
     * Servlet to request a hint for a specific problem
     * 
     * @param token     The token to verify if the user is logged in
     * @param problemID The ID of the problem for which the hint is requested
     * @return An appropriate HTTP response
     */
    @POST
    @Path("/{problem_id}/hint")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response requestHint(@HeaderParam("Authorization") String token, @PathParam("problem_id") int problemID) {
        // use JWToken to authenticate
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        //gets the name of the team
        String teamName = PersonAttributes.getTeamNameByParticipant(studentNumber);
        if (!Objects.isNull(teamName) && !Objects.isNull(TeamAttributes.getTeamCaptainByTeamName(teamName))
                && SubmissionAttributes.askHint(teamName, problemID)) {
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    /**
     * Servlet to return all the hint requests of the submission table.
     * @param token The token to prove you are logged in as an administrator.
     * @return OK if you are an admin, UNAUTHORIZED if you are not logged in and FORBIDDEN if you are logged in but not an admin.
     */
    @GET
    @Path("/hints/requests")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getAllHintRequests(@HeaderParam("Authorization") String token) {
        //Check if the request comes from an authenticated user.
        int sNumb;
        try {
            sNumb = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            //Return UNAUTHORIZED response if the user is not authenticated.
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        //Check if the user is an admin.
        if(PersonAttributes.checkIfAdmin(sNumb)) {

            //Return OK with the promised data if the user is an admin.
            return Response.status(Response.Status.OK).entity(new JSONArray(SubmissionAttributes.getAllHintRequests()).toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
        } else {

            //Return FORBIDDEN if someone is authenticated but not an admin.
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    /**
     * Servlet to retrieve submissions based on certain criteria
     * 
     * @param token    The token to verify if the user is logged in
     * @param teamName The team name for filtering submissions
     * @param ungraded Flag indicating whether to include ungraded submissions
     *                 normally false
     * @param graded   Flag indicating whether to include graded submissions
     *                 normally false
     * @return An appropriate HTTP response
     */
    @GET
    @Produces("application/json")
    public Response getSubmissions(@HeaderParam("Authorization") String token, @QueryParam("team_name") String teamName,
            @DefaultValue("false") @QueryParam("ungraded") boolean ungraded,
            @DefaultValue("false") @QueryParam("graded") boolean graded) {
        // use JWToken to authenticate
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        //admin check
        if (PersonAttributes.checkIfAdmin(studentNumber)) {
            List<JSONObject> submissions = new ArrayList<>();
            if (graded) {
                submissions.addAll(SubmissionAttributes.getAllGradedSubmissions(null));
            }
            if (ungraded) {
                submissions.addAll(SubmissionAttributes.getAllUngradedSubmissions(null));
            }
            //concatenates both graded and ungraded for the full list of submissions
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new JSONArray(submissions).toString())
                    .build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    /**
     * Servlet to download an image for a submission by the team name and problem
     * ID, accessible by admin only
     * 
     * @param problemID The ID of the problem associated with the submission
     * @param teamName  The name of the team associated with the submission
     * @param token     The token to verify if the user is logged in as an admin
     * @return An appropriate HTTP response
     */
    @GET
    @Path("/admin/{team_name}/{problem_id}/file")
    @Produces({ "image/png", "image/jpg", "image/jpeg" })
    public Response downloadImageAdmin(@PathParam("problem_id") int problemID, @PathParam("team_name") String teamName,
            @HeaderParam("Authorization") String token) {
        // use JWToken to authenticate
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        if (PersonAttributes.checkIfAdmin(studentNumber)) {
            JSONObject submission = (SubmissionAttributes.getSubmissionByTeamNameAndProblemID(teamName, problemID));
            if (!Objects.isNull(submission)) {

                java.nio.file.Path path = java.nio.file.Path.of(context.getRealPath("files/"), submission.getJSONObject(SubmissionAttributes.SUBMISSION_COLUMN)
                        .getString(SubmissionAttributes.SUBMISSION_COLUMN));

                File file = new File(path.toString());

                if (!file.exists()) {
                    throw new WebApplicationException(500);
                }

                String mt = new MimetypesFileTypeMap().getContentType(file);
                return Response.ok(file, mt).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Path("/admin/{team_name}/{problem_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSubmissionAdmin(@PathParam("problem_id") int problemID, @PathParam("team_name") String teamName,
                                       @HeaderParam("Authorization") String token) {
        // use JWToken to authenticate
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        if (PersonAttributes.checkIfAdmin(studentNumber)) {
            JSONObject submission = (SubmissionAttributes.getSubmissionByTeamNameAndProblemID(teamName, problemID));
            if (!Objects.isNull(submission)) {
                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE)
                        .entity(submission.toString())
                        .build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).build();
            }
        }
        return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    /**
     * Servlet to upload an image file for a submission
     * 
     * @param uploadedInputStream The input stream of the uploaded image file
     * @param fileDetails         The details of the uploaded file
     * @param token               The token to verify if the user is logged in
     * @param problem_id          The ID of the problem associated with the
     *                            submission
     * @return An appropriate HTTP response
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadImage(
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetails,
            @HeaderParam("Authorization") String token,
            @FormDataParam("problem_id") int problem_id) {
        // use JWToken to authenticate
        int studentNumber = Integer.MIN_VALUE;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        System.out.println(fileDetails.getFileName());
        java.nio.file.Path filesPath = java.nio.file.Path.of(context.getRealPath("files/"));
        if (Files.notExists(filesPath)) {
            try {
                Files.createDirectory(filesPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Create a random file name
        String randomName = java.util.UUID.randomUUID().toString();
        java.nio.file.Path filePath = java.nio.file.Path.of(filesPath.toString(), randomName);
        // save it
        try {
            writeToFile(uploadedInputStream, filePath.toString());
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(" { \"fileID\": \"" + randomName + "\" } ").build();
        }
        SubmissionAttributes.submitSubmission(PersonAttributes.getTeamNameByParticipant(studentNumber), problem_id,
                randomName);
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(" { \"fileID\": \"" + randomName + "\" } ").build();
    }

    // save uploaded file to new location
    private void writeToFile(InputStream uploadedInputStream,
            String uploadedFileLocation) throws IOException {
        int read = 0;
        byte[] bytes = new byte[1024];

        OutputStream out = new FileOutputStream(uploadedFileLocation);
        while ((read = uploadedInputStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        out.flush();
        out.close();
    }

    @GET
    @Produces({ "image/jpg", "image/jpeg", "image/png" })
    @Path("/{problem_id}/file")
    public Response getImage(@HeaderParam("Authorization") String token, @PathParam("problem_id") int problem_id) {
        System.out.println("hi");
        // use JWToken to authenticate
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        String teamName = PersonAttributes.getTeamNameByParticipant(studentNumber);
        JSONObject submission = (SubmissionAttributes.getSubmissionByTeamNameAndProblemID(teamName, problem_id));
        java.nio.file.Path filePath = java.nio.file.Path.of(context.getRealPath("files/"), submission.getJSONObject(SubmissionAttributes.SUBMISSION_COLUMN)
                .getString(SubmissionAttributes.SUBMISSION_COLUMN));
        System.out.println("filePath = " + filePath);
        if (!Objects.isNull(submission)) {
            File file = new File(filePath.toString());

            if (!file.exists()) {
                throw new WebApplicationException(404);
            }

            String mt = new MimetypesFileTypeMap().getContentType(file);
            return Response.status(Response.Status.OK).type(mt).entity(file).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{problem_id}")
    public Response getSubmission(@HeaderParam("Authorization") String token, @PathParam("problem_id") int problem_id) {
        // use JWToken to authenticate
        int studentNumber = Integer.MIN_VALUE;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        String teamName = PersonAttributes.getTeamNameByParticipant(studentNumber);
        JSONObject submission = (SubmissionAttributes.getSubmissionByTeamNameAndProblemID(teamName, problem_id));
        if (!Objects.isNull(submission)) {
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(submission.toString()).build();
        }
        return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity("").build();
    }


    @PATCH
    @Path("/admin/{team_name}/{problem_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response gradeSubmission(@PathParam("problem_id") int problemID, @PathParam("team_name") String teamName, @HeaderParam("Authorization") String token, String body) {
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        if(PersonAttributes.checkIfAdmin(studentNumber)) {
            JSONObject json = new JSONObject(body);
            if(json.has(ProblemAttributes.SCORE) && json.has(SubmissionAttributes.GRADING_DESCRIPTION) && SubmissionAttributes.gradeSubmission(teamName, json.getString(SubmissionAttributes.GRADING_DESCRIPTION), problemID, json.getInt(ProblemAttributes.SCORE))) {
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

}
