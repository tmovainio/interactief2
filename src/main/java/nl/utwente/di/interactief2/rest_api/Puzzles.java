package nl.utwente.di.interactief2.rest_api;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.activation.MimetypesFileTypeMap;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import nl.utwente.di.interactief2.JDBC.SecurityClasses.Sanitizer;
import nl.utwente.di.interactief2.JDBC.attribute_queries.LocationAttributes;
import nl.utwente.di.interactief2.JDBC.attribute_queries.PersonAttributes;
import nl.utwente.di.interactief2.JDBC.attribute_queries.ProblemAttributes;
import nl.utwente.di.interactief2.rest_api.authentication.Authenticator;
import nl.utwente.di.interactief2.rest_api.dto.PuzzleMap;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

@Path("/puzzles")
public class Puzzles {

    Authenticator authenticator = new Authenticator();
    @Context
    private ServletContext context;

    /**
     * Servlet to insert a puzzle into the database
     * 
     * @param body  The input for the puzzle
     * @param token The token to verify authentication of an admin
     * @return An appropriate HTTP response
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postPuzzle(
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetails,
            @HeaderParam("Authorization") String token,
            @FormDataParam("body") String body) {
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        //checks if admin
        if (PersonAttributes.checkIfAdmin(studentNumber)) {
            ObjectMapper mapper = new ObjectMapper();
            PuzzleMap puzzleMap;
            try {
                //sets up a puzzleMap to include all relevant attributes
                puzzleMap = mapper.readValue(body, PuzzleMap.class);
            } catch (JsonProcessingException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity(body).type(MediaType.APPLICATION_JSON_TYPE)
                        .build();
            }
            //does make the file path
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
            boolean check = ProblemAttributes.insertPuzzle(puzzleMap.problemName, filePath.toString(),
                    puzzleMap.locationID);
            return Response.status(check ? Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    /**
     * Servlet to delete a puzzle from the database
     * 
     * @param token     The token used for authentication
     * @param problemID The ID of the puzzle to be deleted
     * @return An appropriate HTTP response
     */
    @DELETE
    @Path("/{problem_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deletePuzzle(@HeaderParam("Authorization") String token, @PathParam("problem_id") int problemID) {
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        if (PersonAttributes.checkIfAdmin(studentNumber)) {
            JSONObject puzzle = ProblemAttributes.getPuzzleByID(problemID);
            //delete a puzzle
            if (ProblemAttributes.deletePuzzle(problemID)) {
                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
            }

        } else {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    /**
     * Servlet to return all puzzles in the database.
     * 
     * @param token The token to verify if you are logged in.
     * @return An appropriate http response and/or the puzzles in json.
     */
    @GET
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getPuzzles(@HeaderParam("Authorization") String token) {
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new JSONArray(getFilesInPuzzles(ProblemAttributes.getAllPuzzles())).toString()).build();
    }

    /**
     * Servlet to return all graded puzzles (depending on logged in user).
     * 
     * @param token Token to verify authentication.
     * @return All graded puzzles or the graded puzzles specifically for a team.
     */
    @GET
    @Path("/graded")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getAllGradedPuzzles(@HeaderParam("Authorization") String token) {
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        //get the team
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
        //return all graded ones
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new JSONArray(getFilesInPuzzles(ProblemAttributes.getAllSubmittedPuzzles(teamName))).toString())
                .build();
    }

    /**
     * Servlet to return all ungraded puzzles (depending on logged in user).
     * 
     * @param token Token to verify authentication.
     * @return All graded puzzles or the ungraded puzzles specifically for a team.
     */
    @GET
    @Path("/ungraded")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getAllUnGradedPuzzles(@HeaderParam("Authorization") String token) {
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
        //return all ungraded ones
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new JSONArray(getFilesInPuzzles(ProblemAttributes.getAllNonSubmittedPuzzles(teamName)))
                        .toString())
                .build();
    }

    /**
     * Servlet to alter a puzzles in the database.
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
    public Response alterPuzzles(JSONObject body, @HeaderParam("Authorization") String token,
            @PathParam("problem_id") int problemId) {
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);

            if (PersonAttributes.checkIfAdmin(studentNumber)) {

                String newName = body.has(ProblemAttributes.PROBLEM_NAME)
                        ? body.getString(ProblemAttributes.PROBLEM_NAME)
                        : null;
                // TODO: check if file is the same or smth I dunno, then maybe change up the
                // file path. Good luck with this one.
                String newFilePath = body.has(ProblemAttributes.FILE_PATH) ? body.getString(ProblemAttributes.FILE_PATH)
                        : null;
                int newLocation = body.has(LocationAttributes.LOCATION_ID) ? body.getInt(LocationAttributes.LOCATION_ID)
                        : Integer.MIN_VALUE;

                if (Objects.isNull(newName) && Objects.isNull(newFilePath) && Objects.equals(newLocation, Integer.MIN_VALUE)) {
                    throw new BadRequestException();
                }

                JSONObject puzzle = ProblemAttributes.getPuzzleByID(problemId);

                if (Objects.isNull(puzzle)) {
                    throw new BadRequestException();
                }

                String oldName = puzzle.getJSONObject(ProblemAttributes.PUZZLE_COLUMN)
                        .getString(ProblemAttributes.PROBLEM_NAME);
                String oldFilePath = puzzle.getJSONObject(ProblemAttributes.PUZZLE_COLUMN)
                        .getString(ProblemAttributes.FILE_PATH);
                int oldLocation = puzzle.getJSONObject(ProblemAttributes.CHALLENGE_COLUMN)
                        .getInt(LocationAttributes.LOCATION_ID);

                if (ProblemAttributes.updatePuzzle(problemId, (Objects.equals(newName, oldName) ? oldName : newName),
                        (Objects.equals(newFilePath, oldFilePath) ? oldFilePath : newFilePath),
                        (Objects.equals(newLocation, oldLocation) ? oldLocation : newLocation))) {
                    return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE)
                            .entity(ProblemAttributes.getPuzzleByID(problemId)).build();
                } else {
                    throw new BadRequestException();
                }

            } else {
                return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON_TYPE).build();
            }
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    private List<JSONObject> getFilesInPuzzles(List<JSONObject> puzzles) {

        for (JSONObject puzzle : puzzles) {
            String filePath = puzzle.getJSONObject(ProblemAttributes.PUZZLE_COLUMN)
                    .getString(ProblemAttributes.FILE_PATH);
            if (!Objects.isNull(filePath) && !Objects.equals(filePath, "")) {
                // TODO: Replace filePath with the actual file.

                // puzzle.getJSONObject(ProblemAttributes.PUZZLE_COLUMN).remove(ProblemAttributes.FILE_PATH);
                // puzzle.getJSONObject(ProblemAttributes.PUZZLE_COLUMN).append("[HOW YOU WANT
                // TO NAME IT]", "[YOUR FILE THING HERE]");
            }
        }

        return puzzles;
    }

    /**
     * Servlet to download an image
     * 
     * @param problemID The ID of the puzzle
     * @param token     The token used for authentication
     * @return A response containing the image file to be downloaded
     * @throws WebApplicationException to send back a 404 sometimes
     */
    @GET
    @Path("/{problem_id}/file")
    @Produces({ "image/png", "image/jpg", "image/gif" })
    public Response downloadImage(@PathParam("problem_id") int problemID, @HeaderParam("Authorization") String token) {
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        JSONObject puzzle = (ProblemAttributes.getPuzzleByID(problemID));
        if (!Objects.isNull(puzzle)) {
            File file = new File(
                    puzzle.getJSONObject(ProblemAttributes.PUZZLE_COLUMN).getString(ProblemAttributes.FILE_PATH));

            if (!file.exists()) {
                throw new WebApplicationException(404);
            }

            String mt = new MimetypesFileTypeMap().getContentType(file);
            return Response.status(Response.Status.OK).type(mt).entity(file).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Path("/{problem_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getObject(@PathParam("problem_id") int problemID, @HeaderParam("Authorization") String token) {
        int studentNumber;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        JSONObject puzzle = (ProblemAttributes.getPuzzleByID(problemID));
        if (!Objects.isNull(puzzle)) {
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(puzzle.toString()).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

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

}
