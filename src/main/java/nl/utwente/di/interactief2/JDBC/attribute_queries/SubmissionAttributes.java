package nl.utwente.di.interactief2.JDBC.attribute_queries;

import nl.utwente.di.interactief2.JDBC.connection.DataBaseConnection;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SubmissionAttributes {

    public static final String SUBMISSION_COLUMN = "submission";
    public static final String USED_HINT = "used_hint";
    public static final String SUBMISSION_SCORE = "score";
    public static final String RANK_OF_LEADERBOARD = "rank";
    public static final String SCORE_OF_LEADERBOARD = "total_score";
    public static final String GRADING_DESCRIPTION = "grading_description";


    private static final String WHERE_CLAUSE = " WHERE";
    private static final String AND_CLAUSE = " AND";
    private static final String BY_TEAM_NAME = " s.team_name = ?";
    private static final String NOT_BY_SCORE = " s.score != ?";
    private static final String BY_SCORE = " s.score = ?";

    private static final List<String> SUBMISSION_ATTRIBUTES_LIST = List.of(SUBMISSION_COLUMN, GRADING_DESCRIPTION, ProblemAttributes.SCORE, USED_HINT);

    private static final String INSERT_SUBMISSION = "INSERT INTO " + SUBMISSION_COLUMN + " (team_name, problem_id, submission) VALUES (?, ?, ?)";
    private static final String HINT_SUBMISSION = "INSERT INTO " + SUBMISSION_COLUMN + " (team_name, problem_id, used_hint) VALUES (?, ?, ?)";
    private static final String DELETE_SUBMISSION = "DELETE FROM " + SUBMISSION_COLUMN + " WHERE problem_id = ?";
    private static final String GRADE_SUBMISSION = "UPDATE " + SUBMISSION_COLUMN + " SET score = ?, grading_description = ? WHERE team_name = ? AND problem_id = ?;";

    private static final String GET_ALL_SUBMISSIONS = "SELECT XMLELEMENT(NAME " + SUBMISSION_COLUMN
            + ", XMLFOREST(s.team_name, s.problem_id, s.submission, s.grading_description, s.score, s.used_hint)) AS " + SUBMISSION_COLUMN + " FROM submission s";

    public static final String GET_ALL_SCORES_OF_TEAMS = "SELECT XMLELEMENT(NAME " + SUBMISSION_SCORE
            + ", XMLFOREST(scores.team_name, scores.total_score)) AS " + SUBMISSION_SCORE
            + " FROM (SELECT sub.team_name, SUM(sub.score) AS total_score FROM submission sub WHERE sub.score != 0 OR sub.used_hint = TRUE GROUP BY sub.team_name) AS scores ORDER BY scores.total_score DESC;";


    //String to get all submission objects by score equal to a constant.
    private static final String GET_UNGRADED_SUBMISSIONS = GET_ALL_SUBMISSIONS + WHERE_CLAUSE + " s.grading_description IS NULL OR" + BY_SCORE;

    //String to get all submission objects by score not equal to a constant.
    private static final String GET_GRADED_SUBMISSIONS = GET_ALL_SUBMISSIONS + WHERE_CLAUSE + NOT_BY_SCORE;

    private static final String GET_ALL_HINT_ASKING_SUBMISSION = GET_ALL_SUBMISSIONS + WHERE_CLAUSE + BY_SCORE + AND_CLAUSE + " used_hint = true";

    /**
     * Inserts a submission into the database.
     * 
     * @param teamName   The team that submitted the submission.
     * @param problemID  The problem that the submission was submitted for.
     * @param submission The path to the submission file (photo or video).
     * @return <code>true</code> if the query was successful, <code>false</code>
     *         otherwise.
     */
    public static synchronized boolean submitSubmission(String teamName, int problemID, String submission) {
        Connection conn = DataBaseConnection.getConnection();
        try (PreparedStatement submitSubmissionStatement = conn.prepareStatement(INSERT_SUBMISSION)) {
            conn.setAutoCommit(false);

            submitSubmissionStatement.setString(1, teamName);
            submitSubmissionStatement.setInt(2, problemID);
            submitSubmissionStatement.setString(3, submission);

            submitSubmissionStatement.executeUpdate();
            conn.commit();
            conn.close();

            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static synchronized List<JSONObject> getAllHintRequests() {
        Connection conn = DataBaseConnection.getConnection();
        return getSubmissionsByStatement(GET_ALL_HINT_ASKING_SUBMISSION, null, Integer.MIN_VALUE, 0, conn);
    }

    /**
     * This method insert the teamName and problemID into the submission table and
     * sets the used_hint boolean to true.
     * 
     * @param teamName  The team that used the hint.
     * @param problemId The problem that the hint was used on.
     * @return <code>true</code> if the query was successful, <code>false</code>
     *         otherwise.
     */
    public static synchronized boolean askHint(String teamName, int problemId) {
        Connection conn = DataBaseConnection.getConnection();
        try (PreparedStatement askHintStatement = conn.prepareStatement(HINT_SUBMISSION)) {
            conn.setAutoCommit(false);

            askHintStatement.setString(1, teamName);
            askHintStatement.setInt(2, problemId);
            askHintStatement.setBoolean(3, true);

            askHintStatement.executeUpdate();
            conn.commit();
            conn.close();

            return true;
        } catch (SQLException e) {
            JSONObject submissionObject = getSubmissionByTeamNameAndProblemID(teamName, problemId);
            if(!Objects.isNull(submissionObject)) {
                JSONObject submissionWithoutWrapper = submissionObject.getJSONObject(SUBMISSION_COLUMN);
                String submission = submissionWithoutWrapper.has(SUBMISSION_COLUMN) ? submissionWithoutWrapper.getString(SUBMISSION_COLUMN) : "";
                String gradingDescription = submissionWithoutWrapper.has(GRADING_DESCRIPTION) ? submissionWithoutWrapper.getString(GRADING_DESCRIPTION) : "";
                int score = submissionWithoutWrapper.has(SUBMISSION_SCORE) ? submissionWithoutWrapper.getInt(SUBMISSION_SCORE) : 0;

                return alterSubmission(teamName, problemId, submission, gradingDescription, score, true);
            } else {
                return false;
            }
        }
    }

    /**
     * Delete a submission
     * 
     * @param teamName  the name of the team whose submission should be deleted
     * @param problemID the problemID of the puzzle
     * @return <code>true</code> if the query was successful, <code>false</code>
     *         otherwise.
     */
    public static synchronized boolean deleteSubmission(int problemID, String teamName) {
        Connection conn = DataBaseConnection.getConnection();
        boolean check = !Objects.isNull(teamName);
        try (PreparedStatement deleteSubmissionStatement = conn.prepareStatement(DELETE_SUBMISSION + (check ? " AND team_name = ?" : ""))) {
            conn.setAutoCommit(false);

            deleteSubmissionStatement.setInt(1, problemID);
            if(check) {
                deleteSubmissionStatement.setString(2, teamName);
            }

            deleteSubmissionStatement.executeUpdate();
            conn.commit();
            conn.close();

            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Update the submission with the new score
     * 
     * @param teamName  the name of the team whose submission should be deleted
     * @param problemID the problemID of the puzzle
     * @param score     the score the team scored
     * @return <code>true</code> if the query was successful, <code>false</code>
     *         otherwise.
     */
    public static synchronized boolean gradeSubmission(String teamName, String gradingDescription, int problemID, int score) {
        Connection conn = DataBaseConnection.getConnection();
        try (PreparedStatement gradeSubmissionStatement = conn.prepareStatement(GRADE_SUBMISSION)) {
            conn.setAutoCommit(false);

            gradeSubmissionStatement.setInt(1, score);
            gradeSubmissionStatement.setString(2, gradingDescription);
            gradeSubmissionStatement.setString(3, teamName);
            gradeSubmissionStatement.setInt(4, problemID);

            gradeSubmissionStatement.executeUpdate();
            conn.commit();
            conn.close();

            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Method to retrieve a specific submission.
     * @param teamName The team name for which there has to be queried, not nullable.
     * @param problemID The problemID for which there has to be queried, not Integer.MIN_VALUE.
     * @return The submission in json or (when there is incorrect input) null.
     */
    public static synchronized JSONObject getSubmissionByTeamNameAndProblemID(String teamName, int problemID) {
        if(Objects.isNull(teamName) || problemID < 0) {
            return null;
        }

        Connection conn = DataBaseConnection.getConnection();

        String statement = GET_ALL_SUBMISSIONS + " WHERE s.team_name = ? AND s.problem_id = ?";

        List<JSONObject> result = getSubmissionsByStatement(statement, teamName, problemID, Integer.MIN_VALUE, conn);
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Method that returns all ungraded submissions.
     * 
     * @param teamName The team you want the ungraded submissions of, otherwise
     *                 leave null.
     * @return A list of all ungraded submissions of a specific team or just all
     *         depending on if teamName was null.
     */
    public static synchronized List<JSONObject> getAllUngradedSubmissions(String teamName) {
        Connection conn = DataBaseConnection.getConnection();
        String statement = GET_UNGRADED_SUBMISSIONS + (nameCheck(teamName) ? AND_CLAUSE + BY_TEAM_NAME : "");

        return getSubmissionsByStatement(statement, teamName, Integer.MIN_VALUE, 0, conn);
    }

    /**
     * Method that returns all graded submissions.
     * @param teamName The team you want the graded submissions of, otherwise leave null.
     * @return A list of all graded submissions of a specific team or just all depending on if teamName was null.
     */
    public static synchronized List<JSONObject> getAllGradedSubmissions(String teamName) {
        Connection conn = DataBaseConnection.getConnection();
        String statement = GET_GRADED_SUBMISSIONS + (nameCheck(teamName) ? AND_CLAUSE + BY_TEAM_NAME : "");

        return getSubmissionsByStatement(statement, teamName, Integer.MIN_VALUE, 0, conn);
    }

    protected static synchronized List<JSONObject> getAllGradedSubmissionsByScore(String teamName, int score) {
        Connection conn = DataBaseConnection.getConnection();
        String statement = GET_ALL_SUBMISSIONS + WHERE_CLAUSE
                + (nameCheck(teamName) ? BY_TEAM_NAME + AND_CLAUSE : "") + NOT_BY_SCORE + ";";

        return getSubmissionsByStatement(statement, teamName, Integer.MIN_VALUE, score, conn);
    }

    private static synchronized boolean nameCheck(String teamName) {
        return !Objects.isNull(teamName) && !Objects.equals(teamName, "");
    }

    private static synchronized List<JSONObject> getSubmissionsByStatement(String statement, String teamName, int problemID, int filter, Connection conn) {
        try (PreparedStatement getSubmissionStatement = conn.prepareStatement(statement)) {
            conn.setAutoCommit(false);

            int indexParameter = 1;
            if (nameCheck(teamName)) {
                getSubmissionStatement.setString(indexParameter, teamName);
                indexParameter++;
            }
            if (problemID != Integer.MIN_VALUE) {
                getSubmissionStatement.setInt(indexParameter, problemID);
                indexParameter++;
            }
            if (filter != Integer.MIN_VALUE) {
                getSubmissionStatement.setInt(indexParameter, filter);
            }

            return GeneralQueryMethods.querySelectRefiner(getSubmissionStatement, SUBMISSION_COLUMN, conn);

        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    private static synchronized List<JSONObject> getRefinedScoreBoard(Connection conn) {
        List<JSONObject> queryResult;
        try (PreparedStatement getScoreBoardStatement = conn.prepareStatement(SubmissionAttributes.GET_ALL_SCORES_OF_TEAMS)) {
            conn.setAutoCommit(false);

            queryResult = GeneralQueryMethods.querySelectRefiner(getScoreBoardStatement, SUBMISSION_SCORE, conn);
            int rank = 0;
            int previousScore = Integer.MAX_VALUE;
            int thisScore;

            for (JSONObject jsonObject : queryResult) {
                int hintCount = getHintCount(jsonObject.getJSONObject(SUBMISSION_SCORE).getString(TeamAttributes.TEAM_NAME));
                if(hintCount != Integer.MIN_VALUE) {
                    int scoreOld = jsonObject.getJSONObject(SUBMISSION_SCORE).getInt(SCORE_OF_LEADERBOARD);
                    jsonObject.getJSONObject(SUBMISSION_SCORE).remove(SCORE_OF_LEADERBOARD);

                    jsonObject.getJSONObject(SUBMISSION_SCORE).put(SCORE_OF_LEADERBOARD, scoreOld - hintCount);
                }

                thisScore = jsonObject.getJSONObject(SUBMISSION_SCORE).getInt(SCORE_OF_LEADERBOARD);

                if (thisScore < previousScore) {
                    rank++;
                }
                jsonObject.getJSONObject(SUBMISSION_SCORE).put(RANK_OF_LEADERBOARD, rank);
                previousScore = thisScore;
            }

            return queryResult;
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    public static synchronized boolean alterSubmission(String teamName, int problemID, String submission, String gradingDescription, int score, boolean usedHint) {
        Connection conn = DataBaseConnection.getConnection();

        List<Object> values = List.of(submission, gradingDescription, score, usedHint);
        return GeneralQueryMethods.update(SUBMISSION_COLUMN, List.of(TeamAttributes.TEAM_NAME, ProblemAttributes.PROBLEM_ID), List.of(teamName, problemID), SUBMISSION_ATTRIBUTES_LIST, values, conn);
    }

    public static synchronized JSONObject getTotalScoreOfTeam(String teamName) {
        Connection conn = DataBaseConnection.getConnection();
        List<JSONObject> result = getRefinedScoreBoard(conn);
        JSONObject teamScore = new JSONObject();
        JSONObject defaultScore = new JSONObject();
        defaultScore.put(SCORE_OF_LEADERBOARD, 0);
        defaultScore.put(RANK_OF_LEADERBOARD, Integer.MIN_VALUE);
        defaultScore.put(TeamAttributes.TEAM_NAME, teamName);
        teamScore.put(SUBMISSION_SCORE, defaultScore);

        for(JSONObject score : result) {
            if(Objects.equals(score.getJSONObject(SUBMISSION_SCORE).get(TeamAttributes.TEAM_NAME), teamName)) {
                teamScore = score;
            }
        }

        return teamScore;
    }

    /**
     * Method to return the amount of used hints by a specific team.
     * @param teamName The team to get the total used_hint count from.
     * @return The amount of hints used or Integer.MIN_VALUE if something went wrong (team name doesn't exist).
     */
    public static synchronized int getHintCount(String teamName) {
        Connection conn = DataBaseConnection.getConnection();

        //Initiate prepared statement.
        try(PreparedStatement getHintCountStatement = conn.prepareStatement("SELECT COUNT(s.used_hint) AS hint FROM submission s WHERE used_hint = true AND s.team_name = ?;")) {
            conn.setAutoCommit(false);

            //Set the team name into the prepared statement.
            getHintCountStatement.setString(1, teamName);

            //Execute the query, commit and close the connection.
            ResultSet queryResult = getHintCountStatement.executeQuery();
            conn.commit();
            conn.close();

            //If there is something returned, return it, otherwise throw an error.
            if(queryResult.next()) {
                return queryResult.getInt("hint");
            } else {
                throw new SQLException();
            }
        } catch (SQLException e) {
            return Integer.MIN_VALUE;
        }
    }

    public static synchronized List<JSONObject> getScoreBoard() {
        Connection conn = DataBaseConnection.getConnection();

        return getRefinedScoreBoard(conn);
    }

}
