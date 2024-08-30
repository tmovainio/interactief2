package nl.utwente.di.interactief2.JDBC.attribute_queries;

import nl.utwente.di.interactief2.JDBC.connection.DataBaseConnection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProblemAttributes {

    public static final int MAX_SCORE = 5;
    private static final String PROBLEM_COLUMN = "problem";
    public static final String PUZZLE_COLUMN = "puzzle";
    public static final String CHALLENGE_COLUMN = "challenge";
    public static final String PROBLEM_NAME = "problem_name";
    public static final String DESCRIPTION = "description";
    public static final String FILE_PATH = "image";
    public static final String PROBLEM_ID = "problem_id";
    public static final String CHALLENGES_IN_LOCATION_COLUMN = CHALLENGE_COLUMN + "s";
    public static final String CRAZY88_COLUMN = "crazy88";
    public static final String SCORE = "score";

    //List of all the editable attributes in the database for a problem.
    private static final List<String> PROBLEM_ATTRIBUTE_UPDATE_LIST =  List.of(PROBLEM_NAME, SCORE);

    //List of all the editable attributes in the database for a crazy88 problem.
    private static final List<String> CRAZY88_ATTRIBUTE_UPDATE_LIST = List.of(DESCRIPTION);

    //List of all the editable attributes in the database for a challenge.
    private static final List<String> CHALLENGE_ATTRIBUTE_UPDATE_LIST = List.of(LocationAttributes.LOCATION_ID, DESCRIPTION);

    //List of all the editable attributes in the database for a puzzle.
    private static final List<String> PUZZLE_ATTRIBUTE_UPDATE_LIST = List.of(FILE_PATH, LocationAttributes.LOCATION_ID);

    private static final String AND_CLAUSE = " AND";

    private static final String BY_LOCATION_ID_OF_CHALLENGES = " ch.location_id = ?";
    private static final String GET_SUBMITTED_BY_TEAM_NAME = " AND s.team_name = ?";


    //private static final String ;
    private static final String GET_ALL_CRAZY88 = "SELECT XMLELEMENT(NAME crazy88, XMLFOREST(pr.problem_name, cr.problem_id, pr.score, cr.description)) AS crazy88 FROM problem pr, crazy88 cr WHERE pr.problem_id = cr.problem_id";
    private static final String GET_ALL_NON_SUBMITTED_CRAZY88 = "SELECT XMLELEMENT(NAME crazy88, XMLFOREST(pr.problem_name, cr.problem_id, pr.score, cr.description)) AS crazy88 FROM problem pr, crazy88 cr, submission s WHERE pr.problem_id = cr.problem_id AND s.problem_id = cr.problem_id AND (s.score = 0 OR s.grading_description IS NULL)";
    private static final String GET_ALL_SUBMITTED_CRAZY88 = "SELECT XMLELEMENT(NAME crazy88, XMLFOREST(pr.problem_name, cr.problem_id, pr.score, cr.description)) AS crazy88 FROM problem pr, crazy88 cr, submission s WHERE pr.problem_id = cr.problem_id AND s.problem_id = cr.problem_id AND s.score != 0";
    private static final String INSERT_CRAZY88 = "INSERT INTO crazy88 (problem_id, description) VALUES (?, ?)";
    private static final String DELETE_CRAZY88 = "DELETE FROM crazy88 WHERE problem_id = ?";

    protected static final String GET_ALL_CHALLENGES = "SELECT XMLELEMENT(NAME challenge, XMLFOREST(pr.problem_name, ch.problem_id, pr.score, ch.description, XMLFOREST(ch.location_id, l.location_name) AS location)) AS challenge FROM problem pr, challenges ch, location l WHERE pr.problem_id = ch.problem_id AND ch.location_id = l.location_id";

    private static final String GET_ALL_NON_SUBMITTED_CHALLENGES = "SELECT XMLELEMENT(NAME challenge, XMLFOREST(pr.problem_name, ch.problem_id, pr.score, ch.description, XMLFOREST(ch.location_id, l.location_name) AS location)) AS challenge FROM problem pr, challenges ch, location l, submission s WHERE pr.problem_id = ch.problem_id AND ch.location_id = l.location_id AND s.problem_id = ch.problem_id  AND (s.score = 0 OR s.grading_description IS NULL);";
    private static final String GET_ALL_SUBMITTED_CHALLENGES = "SELECT XMLELEMENT(NAME challenge, XMLFOREST(pr.problem_name, ch.problem_id, pr.score, ch.description, XMLFOREST(ch.location_id, l.location_name) AS location)) AS challenge FROM problem pr, challenges ch, location l, submission s WHERE pr.problem_id = ch.problem_id AND ch.location_id = l.location_id AND s.problem_id = ch.problem_id AND s.score != 0";
    private static final String INSERT_CHALLENGE = "INSERT INTO challenges (problem_id, location_id, description) VALUES (?, ?, ?);";
    private static final String DELETE_CHALLENGE = "DELETE FROM challenges WHERE problem_id = ?;";
    private static final String GET_CHALLENGE_BY_ID = "SELECT XMLELEMENT(NAME challenge, XMLFOREST(pr.problem_name, ch.problem_id, pr.score, ch.description, XMLFOREST(ch.location_id, l.location_name) AS location)) AS " + CHALLENGE_COLUMN + " FROM problem pr, challenges ch, location l WHERE pr.problem_id = ch.problem_id AND ch.location_id = l.location_id AND ch.problem_id = ?;";

    protected static final String JOIN_PUZZLES = " AND pr.problem_id = pu.problem_id";
    protected static final String GET_ALL_PUZZLES = "SELECT XMLELEMENT(NAME puzzle, XMLFOREST(pr.problem_name, pu.problem_id, pr.score, pu.image, pu.location_id)) AS puzzle FROM problem pr, puzzles pu WHERE pr.problem_id = pu.problem_id";
    private static final String GET_ALL_NON_SUBMITTED_PUZZLES = "SELECT XMLELEMENT(NAME puzzle, XMLFOREST(pr.problem_name, pu.problem_id, pr.score, pu.image, pu.location_id)) AS puzzle FROM problem pr, puzzles pu, submission s WHERE pr.problem_id = pu.problem_id AND s.problem_id = pu.problem_id AND (s.score = 0 OR s.grading_description IS NULL)";
    private static final String GET_ALL_SUBMITTED_PUZZLES = "SELECT XMLELEMENT(NAME puzzle, XMLFOREST(pr.problem_name, pu.problem_id, pr.score, pu.image, pu.location_id)) AS puzzle FROM problem pr, puzzles pu, submission s WHERE pr.problem_id = pu.problem_id AND s.problem_id = pu.problem_id AND s.score != 0";

    private static final String INSERT_PUZZLES = "INSERT INTO puzzles (problem_id, image, location_id) VALUES (?, ?, ?);";
    private static final String DELETE_PUZZLES = "DELETE FROM puzzles WHERE problem_id = ?;";

    private static final String DELETE_PROBLEM = "DELETE FROM problem WHERE problem_id = ?;";
    private static final String INSERT_PROBLEM = "INSERT INTO problem (problem_id, problem_name, score) VALUES (?, ?, ?);";

    private static final String GET_SCORE_OF_PROBLEM = "SELECT XMLELEMENT(NAME " + SCORE + ",pr.score) FROM problem pr WHERE pr.problem_id = ?;";


    public ProblemAttributes() {

    }


    private static synchronized boolean updateProblem(int problemID, String problemName, int score) {
        Connection conn = DataBaseConnection.getConnection();
        //get list of objets, then update
        List<Object> values = List.of(problemName, score);
        return GeneralQueryMethods.update(PROBLEM_COLUMN, List.of(PROBLEM_ID), List.of(problemID), PROBLEM_ATTRIBUTE_UPDATE_LIST, values, conn);
    }

    public static synchronized JSONObject getChallengeByIDWithUnlockCheck(int problemID, String teamName) {
        JSONObject challenge = getChallengeByID(problemID);
        //checks if the object is indeed valid
        if(!Objects.isNull(challenge)) {
            int locationID = challenge.getJSONObject(CHALLENGE_COLUMN).getInt(LocationAttributes.LOCATION_ID);
            //get a specific location among unlocked locations
            List<JSONObject> challengeList = challengeSelector(LocationAttributes.getUnlockedLocationsByTeam(teamName), locationID);
            //for each unlocked option, send it if it passes the check
            for(JSONObject challengeOfList : challengeList) {
                if(challengeOfList.getJSONObject(CHALLENGE_COLUMN).getJSONObject(LocationAttributes.LOCATION_COLUMN).getInt(LocationAttributes.LOCATION_ID) == locationID) {
                    return challengeOfList;
                }
            }
        }

        return null;
    }

    /**
     * Method to return a challenge specified by an ID.
     * @param problemID The id to be queried for.
     * @return
     */
    public static synchronized JSONObject getChallengeByID(int problemID) {
        Connection conn = DataBaseConnection.getConnection();
        //set up a prepered statement with the challenge id
        try(PreparedStatement getChallengeByIDStatement = conn.prepareStatement(GET_CHALLENGE_BY_ID)) {
            conn.setAutoCommit(false);
            //use a connection to get the challenge
            getChallengeByIDStatement.setInt(1, problemID);

            ResultSet queryResult = getChallengeByIDStatement.executeQuery();
            conn.commit();
            conn.close();
            //then return it
            if(queryResult.next()) {
                return XML.toJSONObject(queryResult.getString(CHALLENGE_COLUMN));
            }

            return null;
        } catch (SQLException e) {
            return null;
        }
    }

    public static void printOut(int problemID, String name, int score, String description) {

        /*
        Connection conn = DataBaseConnection.getConnection();


        if(Objects.isNull(name) && Objects.equals(score, Integer.MIN_VALUE) && Objects.isNull(description)) {
            throw new BadRequestException();
        }

        JSONObject crazy88 = ProblemAttributes.getCrazy88ByID(problemID);

        if (Objects.isNull(crazy88)) {
            throw new BadRequestException();
        }

        String oldName = crazy88.getJSONObject(ProblemAttributes.CRAZY88_COLUMN).getString(ProblemAttributes.PROBLEM_NAME);
        int oldScore = crazy88.getJSONObject(ProblemAttributes.CRAZY88_COLUMN).getInt(ProblemAttributes.SCORE);
        String oldDescr = crazy88.getJSONObject(ProblemAttributes.CRAZY88_COLUMN).getString(ProblemAttributes.DESCRIPTION);

        return ProblemAttributes.updateCrazy88(problemID, (Objects.equals(name, oldName) ? oldName : name), (Objects.equals(score, oldScore) ? oldScore : score),  (Objects.equals(description, oldDescr) ? oldDescr : description));

         */
        System.out.println(challengeSelector(LocationAttributes.getUnlockedLocationsByTeam("driemteam"), 1));
    }

    private static synchronized List<JSONObject> challengeSelector(List<JSONObject> unlockedLocations, int locationID) {
        // loop through all unlocked locations
        for(JSONObject location : unlockedLocations) {
            // to check if they are in fact valid, and then add them to the list
            int unlockedLocationID = location.getJSONObject(LocationAttributes.LOCATION_COLUMN).getInt(LocationAttributes.LOCATION_ID);
            if(unlockedLocationID == locationID) {
                List<JSONObject> result = new ArrayList<>();
                JSONArray jsonArray = location.getJSONObject(LocationAttributes.LOCATION_COLUMN).getJSONArray(CHALLENGES_IN_LOCATION_COLUMN);
                for(int i = 0; i < jsonArray.length(); i++) {
                    result.add((JSONObject) jsonArray.get(i));
                }
                //then return that array
                return result;
            }
        }
        return new ArrayList<>();
    }

    /**
     * Method to return all the crazy88 problems which have not been submitted yet for a certain team.
     * @param teamName The team name of the to be queried crazy88 problems.
     * @return A list of JSONObject crazy88 problems that have no submissions yet in the submission page.
     */
    public static synchronized List<JSONObject> getAllNonSubmittedCrazy88(String teamName) {
        //starts a connection
        Connection conn = DataBaseConnection.getConnection();
        String statement = GET_ALL_NON_SUBMITTED_CRAZY88;
        //checks if the team name exists
        if(!Objects.isNull(teamName)) {
            statement += GET_SUBMITTED_BY_TEAM_NAME;
        }
        return getProblemsByStatementAndString(statement, teamName, CRAZY88_COLUMN, conn);
    }

    /**
     * Method to return all crazy88 problems which have been submitted for a certain team.
     * @param teamName The team name of the to be queried crazy88 problems.
     * @return A list of JSONObject crazy88 problems that have submissions in the submission page.
     */
    public static synchronized List<JSONObject> getAllSubmittedCrazy88(String teamName) {
        //start a connection
        Connection conn = DataBaseConnection.getConnection();
        String statement = GET_ALL_SUBMITTED_CRAZY88;
        //check if the team name exists
        if(!Objects.isNull(teamName)) {
            statement += GET_SUBMITTED_BY_TEAM_NAME;
        }
        //return the list of JSONObject crazy88 problems
        return getProblemsByStatementAndString(statement, teamName, CRAZY88_COLUMN, conn);
    }

    /**
     * Method to return all crazy88 problems.
     * @return A list of all JSONObject crazy88 problems.
     */
    public static synchronized List<JSONObject> getAllCrazy88() {
        Connection conn = DataBaseConnection.getConnection();
        // returns a list of all JSONObject crazy88 problems
        return getProblemsByStatementAndString(GET_ALL_CRAZY88, null, CRAZY88_COLUMN, conn);
    }

    /**
     * Method to return the crazy88 problem specified by an id.
     * @param problemID The problemID for which the crazy88 problem has to be returned
     * @return The crazy88 problem if the problemID specified exists in the database, null otherwise.
     */
    public static synchronized JSONObject getCrazy88ByID(int problemID) {
        Connection conn = DataBaseConnection.getConnection();
        //retrieve the problem, or null if it does not exist
        List<JSONObject> result = getProblemsByStatementAndInt(GET_ALL_CRAZY88 + " AND pr.problem_id = ?", problemID, CRAZY88_COLUMN, conn);
        // returns the crazy88 problem if the problemID exists, null otherwise
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Method to add a crazy88 problem into the database.
     * @param problemName The name of the to be inserted crazy88 problem.
     * @param description The description of the to be inserted crazy88 problem.
     * @param score The maximum score obtainable for the to be inserted crazy88 problem.
     * @return True if successfully inserted the new crazy88 problem, false otherwise.
     */
    public static synchronized boolean insertCrazy88(String problemName, String description, int score) {
        //establish a conenction
        Connection conn = DataBaseConnection.getConnection();
        int problemID = GeneralQueryMethods.getNewID(PROBLEM_COLUMN, PROBLEM_ID, conn);
        //check if the problem can be inserted with the proper data
        if(insertProblem(problemID, problemName, score)) {
            //if yes, prepare the rest
            try(PreparedStatement insertCrazy88Statement = conn.prepareStatement(INSERT_CRAZY88)) {
                conn.setAutoCommit(false);

                insertCrazy88Statement.setInt(1, problemID);
                insertCrazy88Statement.setString(2, description);

                insertCrazy88Statement.executeUpdate();
                conn.commit();
                conn.close();

                return true;
            } catch (SQLException e) {
                //if not, undo it, failed
                deleteProblem(problemID);
                return false;
            }
        } else {
            return false;
        }
    }

    public static synchronized boolean updateCrazy88(int problemID, String problemName, int score, String description) {
        // try to update the problem
        if(updateProblem(problemID, problemName, score)) {
            //on success, open a connection, take the values, then update
            Connection conn = DataBaseConnection.getConnection();

            List<Object> values = List.of(description);

            return GeneralQueryMethods.update(CRAZY88_COLUMN, List.of(PROBLEM_ID), List.of(problemID), CRAZY88_ATTRIBUTE_UPDATE_LIST, values, conn);
        } else {
            //on a failure, return false
            return false;
        }
    }

    /**
     * Method to delete a crazy88 problem.
     * @param problemID The problem id of the to be deleted crazy88 problem.
     * @return True if deleted, false if any problems arises (for example there are still submissions).
     */
    public static synchronized boolean deleteCrazy88(int problemID) {
        //open a connection
        Connection conn = DataBaseConnection.getConnection();
        //delete the crazy88
        try(PreparedStatement deleteCrazy88Statement = conn.prepareStatement(DELETE_CRAZY88)) {
            conn.setAutoCommit(false);

            deleteCrazy88Statement.setInt(1, problemID);

            deleteCrazy88Statement.executeUpdate();
            conn.commit();
            conn.close();

            deleteProblem(problemID);
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Method to returns all puzzles.
     * @return A list of JSONObjects of all the puzzles.
     */
    public static synchronized List<JSONObject> getAllPuzzles() {
        //open a connection
        Connection conn = DataBaseConnection.getConnection();
        //use it to request a list of all puzzles
        return getProblemsByStatementAndString(GET_ALL_PUZZLES + JOIN_PUZZLES + ";", null, PUZZLE_COLUMN, conn);
    }

    /**
     * Method to return all the puzzles with a certain locationID.
     * @param locationID The locationID.
     * @return A list of all the puzzles that have that locationID.
     */
    public static synchronized List<JSONObject> getAllPuzzlesByLocationID(int locationID) {
        //open a connection
        Connection conn = DataBaseConnection.getConnection();
        //use it to query for all puzzles by location_id
        return getProblemsByStatementAndInt(GET_ALL_PUZZLES + AND_CLAUSE + " pu.location_id = ?",locationID, PUZZLE_COLUMN, conn);
    }

    /**
     * Method to return the crazy88 problem specified by an id.
     * @param problemID The problemID for which the crazy88 problem has to be returned
     * @return The crazy88 problem if the problemID specified exists in the database, null otherwise.
     */
    public static synchronized JSONObject getPuzzleByID(int problemID) {
        //start a connection
        Connection conn = DataBaseConnection.getConnection();
        //queries for a specific problem that maches problemID
        List<JSONObject> result = getProblemsByStatementAndInt(GET_ALL_PUZZLES + " AND pr.problem_id = ?", problemID, PUZZLE_COLUMN, conn);
        //returning it if possible, otherwise returns null
        return result.isEmpty() ? null : result.get(0);
    }


    /**
     * Method to return all puzzles which have not been submitted yet for a certain team.
     * @param teamName The team name for the to be queried puzzles.
     * @return A list of JSONObject puzzles that have no submissions yet in the submission page.
     */
    public static synchronized List<JSONObject> getAllNonSubmittedPuzzles(String teamName) {
        //open a connection
        Connection conn = DataBaseConnection.getConnection();
        //prepare a statement
        String statement = GET_ALL_NON_SUBMITTED_PUZZLES;
        if(!Objects.isNull(teamName)) {
            statement += GET_SUBMITTED_BY_TEAM_NAME;
        }
        //use the statement to get a list of JSONObject puzzles
        return getProblemsByStatementAndString(statement, teamName, PUZZLE_COLUMN, conn);
    }

    /**
     * Method to return all puzzles which have been submitted for a certain team.
     * @param teamName The team name for the to be queried puzzles.
     * @return A list of JSONObject puzzles that have submissions in the submission page.
     */
    public static synchronized List<JSONObject> getAllSubmittedPuzzles(String teamName) {
        //open a connection
        Connection conn = DataBaseConnection.getConnection();
        String statement = GET_ALL_SUBMITTED_PUZZLES;
        if(!Objects.isNull(teamName)) {
            statement += GET_SUBMITTED_BY_TEAM_NAME;
        }
        //use it to get a list of puzzles that have submissions in the submission page
        return getProblemsByStatementAndString(statement, teamName, PUZZLE_COLUMN, conn);
    }

    /**
     * Method to add a puzzle into the database.
     * @param problemName The name of the puzzle.
     * @param image The path for the image of the puzzle.
     * @param locationID The location ID it unlocks.
     * @return True if the puzzle was successfully inserted, false otherwise.
     */
    public static synchronized boolean insertPuzzle(String problemName, String image, int locationID) {
        //open a connection
        Connection conn = DataBaseConnection.getConnection();
        int problemID = GeneralQueryMethods.getNewID(PROBLEM_COLUMN, PROBLEM_ID, conn);
        if(insertProblem(problemID, problemName, 1)) {
            try (PreparedStatement insertPuzzleStatement = conn.prepareStatement(INSERT_PUZZLES)) {
                conn.setAutoCommit(false);
                //insert statements for the puzzle
                insertPuzzleStatement.setInt(1, problemID);
                insertPuzzleStatement.setString(2, image);
                insertPuzzleStatement.setInt(3, locationID);
                //execute then commit and close the connection
                insertPuzzleStatement.executeUpdate();
                conn.commit();
                conn.close();

                return true;
            } catch (SQLException e) {
                deleteProblem(problemID);
                return false;
            }
        } else {
            return false;
        }
    }

    public static synchronized boolean updatePuzzle(int problemID, String problemName, String filePath, int location_id) {
        if(updateProblem(problemID, problemName, 1)) {
            Connection conn = DataBaseConnection.getConnection();

            List<Object> values = List.of(filePath, location_id);
            return GeneralQueryMethods.update(PUZZLE_COLUMN + "s", List.of(PROBLEM_ID), List.of(problemID), PUZZLE_ATTRIBUTE_UPDATE_LIST, values, conn);
        } else {
            return false;
        }
    }

    /**
     * Method to delete a puzzle.
     * @param problemID The problem id of the to be deleted puzzle.
     * @return True if deleted, false if any problems arises (for example there are still submissions).
     */
    public static synchronized boolean deletePuzzle(int problemID) {
        Connection conn = DataBaseConnection.getConnection();
        try(PreparedStatement deletePuzzleStatement = conn.prepareStatement(DELETE_PUZZLES)) {
            conn.setAutoCommit(false);

            deletePuzzleStatement.setInt(1, problemID);

            deletePuzzleStatement.executeUpdate();
            conn.commit();
            conn.close();

            deleteProblem(problemID);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Method to return all challenges bound to a specific location.
     * @param locationID The location of which all challenges need to be returned for.
     * @return A list of all the challenge as JSONObjects.
     */
    public static synchronized List<JSONObject> getAllChallengesByLocation(int locationID) {
        Connection conn = DataBaseConnection.getConnection();
        return getProblemsByStatementAndInt( GET_ALL_CHALLENGES + AND_CLAUSE + BY_LOCATION_ID_OF_CHALLENGES + ";", locationID, CHALLENGE_COLUMN, conn);
    }

    /**
     * Method to return all challenges.
     * @return A list of all the challenges as JSONObjects.
     */
    public static synchronized List<JSONObject> getAllChallenges() {
        Connection conn = DataBaseConnection.getConnection();
        return getProblemsByStatementAndString(GET_ALL_CHALLENGES, null, CHALLENGE_COLUMN, conn);
    }


    /**
     * Method to return all challenges which have been submitted for a certain team.
     * @param teamName The team name for the to be queried challenges.
     * @return A list of JSONObject challenges that have submissions in the submission page.
     */
    public static synchronized List<JSONObject> getAllNonSubmittedChallenges(String teamName) {
        Connection conn = DataBaseConnection.getConnection();
        String statement = GET_ALL_NON_SUBMITTED_CHALLENGES;
        if(!Objects.isNull(teamName)) {
            statement += GET_SUBMITTED_BY_TEAM_NAME;
        }

        return getProblemsByStatementAndString(statement, teamName, CHALLENGE_COLUMN, conn);
    }

    /**
     * Method to return all challenges which have not been submitted yet for a certain team.
     * @param teamName The team name for the to be queried challenges.
     * @return A list of JSONObject challenges that have no submissions yet in the submission page.
     */
    public static synchronized List<JSONObject> getAllSubmittedChallenges(String teamName) {
        Connection conn = DataBaseConnection.getConnection();
        String statement = GET_ALL_SUBMITTED_CHALLENGES;
        if(!Objects.isNull(teamName)) {
            statement += GET_SUBMITTED_BY_TEAM_NAME;
        }

        return getProblemsByStatementAndString(statement, teamName, CHALLENGE_COLUMN, conn);
    }

    /**
     * Method to delete a challenge.
     * @param problemID The problem id of the to be deleted challenge.
     * @return True if deleted, false if any problems arises (for example there are still submissions).
     */
    public static synchronized boolean deleteChallenge(int problemID) {
        Connection conn = DataBaseConnection.getConnection();
        try(PreparedStatement deleteChallengeStatement = conn.prepareStatement(DELETE_CHALLENGE)) {
            conn.setAutoCommit(false);

            deleteChallengeStatement.setInt(1, problemID);

            deleteChallengeStatement.executeUpdate();
            conn.commit();
            conn.close();

            deleteProblem(problemID);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Method to insert a challenge into the database.
     * @param problemName The name of the to be inserted challenge.
     * @param locationID The location ID ti which the challenge is tied to.
     * @param description The description of the challenge.
     * @return True if successfully inserted, false otherwise.
     */
    public static synchronized boolean insertChallenge(String problemName, int locationID, String description, int score) {
        Connection conn = DataBaseConnection.getConnection();
        int problemID = GeneralQueryMethods.getNewID(PROBLEM_COLUMN, PROBLEM_ID, conn);
        if(insertProblem(problemID, problemName, score)) {
            try(PreparedStatement insertChallengeStatement = conn.prepareStatement(INSERT_CHALLENGE)) {
                conn.setAutoCommit(false);

                insertChallengeStatement.setInt(1, problemID);
                insertChallengeStatement.setInt(2, locationID);
                insertChallengeStatement.setString(3, description);

                insertChallengeStatement.executeUpdate();
                conn.commit();
                conn.close();

                return true;
            } catch (SQLException e) {
                deleteProblem(problemID);
                return false;
            }
        } else {
            return false;
        }
    }

    public static synchronized boolean updateChallenge(int problemID, String problemName, int score, int locationID, String description) {
        if(updateProblem(problemID, problemName, score)) {
            Connection conn = DataBaseConnection.getConnection();

            List<Object> values = List.of(locationID, description);
            return GeneralQueryMethods.update(CHALLENGE_COLUMN + "s", List.of(PROBLEM_ID), List.of(problemID), CHALLENGE_ATTRIBUTE_UPDATE_LIST, values, conn);
        } else {
            return false;
        }
    }

    protected static synchronized List<JSONObject> getProblemsByStatementAndString(String statement, String filter, String columnName, Connection conn) {
        return GeneralQueryMethods.getJsonObjectsByString(statement, filter, conn, columnName);
    }

    private static synchronized List<JSONObject> getProblemsByStatementAndInt(String statement, int filter, String columnName, Connection conn) {
        return GeneralQueryMethods.getJsonObjectsByInt(statement, filter, conn, columnName);
    }

    private static synchronized boolean deleteProblem(int problemID) {
        if(SubmissionAttributes.deleteSubmission(problemID, null)) {

            Connection conn = DataBaseConnection.getConnection();
            try (PreparedStatement deleteProblemStatement = conn.prepareStatement(DELETE_PROBLEM)) {
                conn.setAutoCommit(false);

                deleteProblemStatement.setInt(1, problemID);

                deleteProblemStatement.executeUpdate();
                conn.commit();
                conn.close();

                return true;
            } catch (SQLException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    private static synchronized boolean insertProblem(int problemID, String problemName, int score) {
        Connection conn = DataBaseConnection.getConnection();
        try(PreparedStatement insertProblemStatement = conn.prepareStatement(INSERT_PROBLEM)) {
            conn.setAutoCommit(false);

            insertProblemStatement.setInt(1, problemID);
            insertProblemStatement.setString(2, problemName);
            insertProblemStatement.setInt(3, score);

            insertProblemStatement.executeUpdate();
            conn.commit();
            conn.close();

            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Method to fetch a score of a specific problem.
     * @param problemID The problem for which the score has to be returned.
     * @return The score of the problem specified, Integer.MIN_VALUE otherwise.
     */
    public static synchronized int getScoreOfProblem(int problemID) {
        Connection conn = DataBaseConnection.getConnection();
        List<JSONObject> result = GeneralQueryMethods.getJsonObjectsByInt(GET_SCORE_OF_PROBLEM, problemID, conn, SCORE);
        return !result.isEmpty() ? result.get(0).getInt(SCORE) : Integer.MIN_VALUE;
    }

}
