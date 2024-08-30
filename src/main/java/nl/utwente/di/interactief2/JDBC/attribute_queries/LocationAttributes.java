package nl.utwente.di.interactief2.JDBC.attribute_queries;

import nl.utwente.di.interactief2.JDBC.connection.DataBaseConnection;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LocationAttributes {

    public static final String LOCATION_COLUMN = "location";
    public static final String LOCATION_ID = "location_id";
    public static final String LOCATION_NAME = "location_name";
    private static final List<String> LOCATION_ATTRIBUTE_LIST = List.of(LOCATION_ID, LOCATION_NAME);

    private static final String GET_ALL_LOCATIONS = "SELECT XMLELEMENT(NAME " + LOCATION_COLUMN + ", XMLFOREST("
            + LOCATION_ID + ", location_name)) AS " + LOCATION_COLUMN + " FROM location l";

    private static final String DELETE_LOCATION = "DELETE FROM location WHERE location_id = ?";

    /**
     * Method to update a location in the database.
     * 
     * @param locationName The new location name (leave null if nto desired to
     *                     update)
     * @param locationID   The problemID for which there has to be an update.
     * @return True if there were no problems, false otherwise.
     */
    public static synchronized boolean updateLocation(String locationName, int locationID) {
        Connection conn = DataBaseConnection.getConnection();

        // Get a list of all the values.
        List<Object> tableValues = List.of(locationName);
        return GeneralQueryMethods.update(LOCATION_COLUMN, List.of(LOCATION_ID), List.of(locationID),
                List.of(LOCATION_NAME), tableValues, conn);
    }

    /**
     * Method to get a location JSONObject by the ID of the location.
     * 
     * @param locationID The ID of the location to be fetched.
     * @return The JSONObject containing the location attributes if found, null
     *         otherwise.
     */
    public static synchronized JSONObject getLocationByID(int locationID) {
        Connection conn = DataBaseConnection.getConnection();

        // Get the location specified by the id.
        List<JSONObject> jsonResult = GeneralQueryMethods
                .getJsonObjectsByInt(GET_ALL_LOCATIONS + " WHERE location_id = ?", locationID, conn, LOCATION_COLUMN);
        JSONObject result = jsonResult.isEmpty() ? null : jsonResult.get(0);

        // Add the challenges to the JSON object.
        if (!Objects.isNull(result)) {
            result.getJSONObject(LOCATION_COLUMN).put(ProblemAttributes.CHALLENGES_IN_LOCATION_COLUMN,
                    new JSONArray(ProblemAttributes.getAllChallengesByLocation(locationID)));
        }

        return result;
    }

    /**
     * Method to return all locations.
     * 
     * @return A list of all locations as JSONObjects.
     */
    public static synchronized List<JSONObject> getAllLocations() {
        Connection conn = DataBaseConnection.getConnection();

        // Get all the locations.
        List<JSONObject> result = GeneralQueryMethods.getJsonObjectsByString(GET_ALL_LOCATIONS + ";", null, conn,
                LOCATION_COLUMN);
        int locationID;

        // Add all the challenge objects to the locations.
        for (JSONObject location : result) {
            locationID = location.getJSONObject(LOCATION_COLUMN).getInt(LOCATION_ID);
            location.getJSONObject(LOCATION_COLUMN).put(ProblemAttributes.CHALLENGES_IN_LOCATION_COLUMN,
                    new JSONArray(ProblemAttributes.getAllChallengesByLocation(locationID)));
        }

        return result;
    }

    /**
     * Method to get all unlocked locations by a given team name.
     * 
     * @param teamName The team name of which all the unlocked locations have to be
     *                 returned.
     * @return A list of JSONObjects that are unlocked by the specified team name.
     */
    public static synchronized List<JSONObject> getUnlockedLocationsByTeam(String teamName) {

        // Get all the graded submissions with a score greater than 0.
        List<JSONObject> submissionJsonList = SubmissionAttributes.getAllGradedSubmissionsByScore(teamName, 0);
        ArrayList<Integer> idPuzzleList = new ArrayList<>();
        // Get all the puzzles belonging to the submissions.
        for (JSONObject submission : submissionJsonList) {
            idPuzzleList.add(submission.getJSONObject(SubmissionAttributes.SUBMISSION_COLUMN)
                    .getInt(ProblemAttributes.PROBLEM_ID));
        }

        // Make the statement for the challenge query.
        String statement = ProblemAttributes.GET_ALL_PUZZLES
                + ((idPuzzleList.isEmpty() ? (ProblemAttributes.JOIN_PUZZLES + "" ) : "") + " AND (");
        StringBuilder builder = new StringBuilder(statement);
        for (Integer puzzleID : idPuzzleList) {
            builder.append("(pr.problem_id = ").append(puzzleID).append(" AND pu.problem_id = ")
                    .append(puzzleID).append(")");
            if(idPuzzleList.indexOf(puzzleID) < idPuzzleList.size() - 1) {
                builder.append(" OR ");
            }
        }
        builder.append(")");

        // Query all the challenges belonging to the unlocked locations.
        Connection conn = DataBaseConnection.getConnection();
        List<JSONObject> jsonPuzzleList = ProblemAttributes.getProblemsByStatementAndString(builder.toString(), null,
                ProblemAttributes.PUZZLE_COLUMN, conn);
        List<JSONObject> result = new ArrayList<>();

        // Loop through all the locations and add all the challenges to it.
        for (JSONObject challenge : jsonPuzzleList) {
            result.add(getLocationByID(
                    challenge.getJSONObject(ProblemAttributes.PUZZLE_COLUMN).getInt(LOCATION_ID)));
        }

        // Return the list.
        return result;
    }

    /**
     * Method to add a location
     * 
     * @param locationName The name of the to be added location.
     * @return True if it successfully inserted, false otherwise.
     */
    public static synchronized boolean addLocation(String locationName) {
        Connection conn = DataBaseConnection.getConnection();

        // Make sure that the new is not problematic.
        int newID = GeneralQueryMethods.getNewID(LOCATION_COLUMN, LOCATION_ID, conn);
        if (newID == Integer.MIN_VALUE) {
            return false;
        }

        List<Object> values = List.of(newID, locationName);
        return GeneralQueryMethods.insertInto(LOCATION_COLUMN, LOCATION_ATTRIBUTE_LIST, values, conn);
    }

    /**
     * Method to delete a location by a specific ID.
     * @param locationID The ID of the to be deleted location.
     */
    public static synchronized boolean deleteLocation(int locationID) {
        if (locationID != 0) {
            Connection conn = DataBaseConnection.getConnection();

            JSONObject location = getLocationByID(locationID);
            if (!Objects.isNull(location)) {
                JSONArray challengeJsonArray = location.getJSONObject(LOCATION_COLUMN).getJSONArray(ProblemAttributes.CHALLENGES_IN_LOCATION_COLUMN);
                List<JSONObject> challengeList = new ArrayList<>();
                for (Object challenge : challengeJsonArray) {
                    challengeList.add(((JSONObject) challenge).getJSONObject(ProblemAttributes.CHALLENGE_COLUMN));
                }

                for (JSONObject challenge : challengeList) {
                    int problemID = challenge.getInt(ProblemAttributes.PROBLEM_ID);
                    String problemName = challenge.getString(ProblemAttributes.PROBLEM_NAME);
                    int score = challenge.getInt(ProblemAttributes.SCORE);
                    String description = challenge.getString(ProblemAttributes.DESCRIPTION);
                    if(!ProblemAttributes.updateChallenge(problemID, problemName, score, 0, description)) {
                        return false;
                    }
                }

                List<JSONObject> puzzleList = ProblemAttributes.getAllPuzzlesByLocationID(locationID);
                for(JSONObject puzzle : puzzleList) {
                    int problemID = puzzle.getJSONObject(ProblemAttributes.PUZZLE_COLUMN).getInt(ProblemAttributes.PROBLEM_ID);
                    String problemName = puzzle.getJSONObject(ProblemAttributes.PUZZLE_COLUMN).getString(ProblemAttributes.PROBLEM_NAME);
                    String filePath = puzzle.getJSONObject(ProblemAttributes.PUZZLE_COLUMN).getString(ProblemAttributes.FILE_PATH);

                    if(!ProblemAttributes.updatePuzzle(problemID, problemName, filePath, 0)) {
                        return false;
                    }
                }

                // Initiate prepared statement.
                try (PreparedStatement deleteLocationStatement = conn.prepareStatement(DELETE_LOCATION)) {
                    conn.setAutoCommit(false);

                    // Set the variable in the prepared statement.
                    deleteLocationStatement.setInt(1, locationID);

                    // Execute the update, commit and close the connection.
                    deleteLocationStatement.executeUpdate();
                    conn.commit();
                    conn.close();

                    return true;
                } catch (SQLException e) {

                    // Something went wrong, most likely thr locationID does not exist.
                    return false;
                }
            }
        }

        return false;
    }
}
