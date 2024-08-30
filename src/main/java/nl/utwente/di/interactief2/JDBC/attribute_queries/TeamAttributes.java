package nl.utwente.di.interactief2.JDBC.attribute_queries;

import nl.utwente.di.interactief2.JDBC.connection.DataBaseConnection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class TeamAttributes {

    //Standards and column names
    public static final int TEAM_SIZE = 4;

    public static final String INVITE_LINK = "content";
    private static final String CHECK_COLUMN_NAME = "exist";

    //Standard WHERE, AND clauses.
    private static final String WHERE_CLAUSE = " WHERE";
    private static final String AND_CLAUSE = " AND";
    private static final String UNAPPROVED = " t.approved IS NULL";
    private static final String APPROVED = " t.approved = true";
    public static final String TEAM_COLUMN = "team";
    public static final String TEAM_NAME = "team_name";
    public static final String TEAM_CAPTAIN = "captain";
    public static final String TEAM_MEMBERS = "team_members";
    public static final String IS_FULL_BOOLEAN = "is_full";
    public static final String APPROVED_BOOLEAN = "approved";
    private static final String BY_NAME = " t." + TEAM_NAME + " = ?";
    private static final String BY_INVITE_LINK = " t.invite_link = ?";


    private static final String GET_TEAM_BY_NAME = "SELECT COUNT(1) AS " + CHECK_COLUMN_NAME + " FROM teams t WHERE t.team_name = ?";

    private static final String CREATE_TEAM = "INSERT INTO teams (team_name, captain, is_full) VALUES (?, ?, ?);";

    private static final String GET_TEAM_BY_INVITE_LINK = "SELECT XMLFOREST(t.team_name, t.is_full) AS " + TEAM_COLUMN + " FROM teams t WHERE t.invite_link = ?";

    private static final String APPROVE_TEAM = "UPDATE teams SET approved = ?, invite_link = ? WHERE team_name = ?";
    private static final String SET_INVITE_LINK = "UPDATE teams SET invite_link = ? WHERE team_name = ?";

    private static final String UPDATE_TEAM_FULLNESS = "UPDATE teams SET is_full = ? WHERE team_name = ?";

    private static final String GET_TEAM_CAPTAIN = "SELECT t.captain FROM teams t WHERE t.team_name = ?";

    private static final String DELETE_TEAM = "DELETE FROM teams WHERE team_name = ?;";
    private static final String GET_TEAMS = "SELECT XMLELEMENT(NAME " + TEAM_COLUMN + ", XMLFOREST(t.team_name, t. is_full, t.approved, (" + PersonAttributes.GET_PARTICIPANT + " AND t.captain = pe.s_numb) AS " + TEAM_CAPTAIN + "), t.invite_link) AS " + TEAM_COLUMN + " FROM teams t";

    public TeamAttributes() {

    }

    /**
     * Method that deletes a team and also removes all the participants.
     * @param teamName The team name of the team that needs to be deleted.
     * @return True if there were no issues, false otherwise.
     */
    public static synchronized boolean deleteTeamByTeamName(String teamName) {
        Connection conn = DataBaseConnection.getConnection();
        List<JSONObject> teamMemberList = PersonAttributes.getTeamMembers(teamName);

        try(PreparedStatement deleteTeamByTeamNameStatement = conn.prepareStatement(DELETE_TEAM)) {
            for(JSONObject member : teamMemberList) {
                int sNumb = member.getJSONObject(PersonAttributes.PARTICIPANT_OBJECT_NAME).getInt(PersonAttributes.STUDENT_NUMBER);
                PersonAttributes.leaveTeam(sNumb);
            }

            conn.setAutoCommit(false);

            deleteTeamByTeamNameStatement.setString(1, teamName);

            deleteTeamByTeamNameStatement.executeUpdate();
            conn.commit();
            conn.close();

            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Method to approve a team.
     * @param teamName The team name of the team that needs to be approved.
     * @return True if there were no issues, false otherwise.
     */
    public static synchronized boolean approveTeam(String teamName, boolean approved) {
        Connection conn = DataBaseConnection.getConnection();
        try(PreparedStatement approveTeamStatement = conn.prepareStatement(APPROVE_TEAM)) {
            conn.setAutoCommit(false);

            approveTeamStatement.setBoolean(1, approved);
            if (approved) {
                approveTeamStatement.setString(2, inviteLinkGenerator());
            } else {
                approveTeamStatement.setNull(2, Types.NULL);
            }
            approveTeamStatement.setString(3, teamName);

            approveTeamStatement.executeUpdate();
            conn.commit();
            conn.close();

            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Method to manually request a new invite link.
     * @param teamName The team name of the team that needs a new invite link.
     * @return True if there were no issues, false otherwise.
     */
    public static synchronized boolean setInviteLink(String teamName) {
        Connection conn = DataBaseConnection.getConnection();
        try(PreparedStatement setInviteLinkStatement = conn.prepareStatement(SET_INVITE_LINK)) {
            conn.setAutoCommit(false);

            setInviteLinkStatement.setString(1, inviteLinkGenerator());
            setInviteLinkStatement.setString(2, teamName);

            setInviteLinkStatement.executeUpdate();
            conn.commit();
            conn.close();

            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static synchronized String inviteLinkGenerator() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();

    }


    private static synchronized boolean isTeamFull(String teamName) {
        JSONObject team = getTeamByName(teamName, false);

        return team.getJSONObject(TEAM_COLUMN).getBoolean(IS_FULL_BOOLEAN);
    }

    protected static synchronized void updateTeamFullness(String teamName) {
        int teamMembersSize = PersonAttributes.getTeamMembers(teamName).size();
        boolean isFull = isTeamFull(teamName);
        boolean check = (isFull && teamMembersSize != TEAM_SIZE) || (!isFull && teamMembersSize == TEAM_SIZE);

        if(check) {
            Connection conn = DataBaseConnection.getConnection();
            try(PreparedStatement updateTeamFullnessStatement = conn.prepareStatement(UPDATE_TEAM_FULLNESS)) {
                conn.setAutoCommit(false);

                updateTeamFullnessStatement.setBoolean(1,!isFull);
                updateTeamFullnessStatement.setString(2, teamName);

                updateTeamFullnessStatement.executeUpdate();
                conn.commit();
                conn.close();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static synchronized List<JSONObject> addParticipantsToListOfTeams(List<JSONObject> jsonResult) {
        if(!Objects.isNull(jsonResult)) {
            for (JSONObject team : jsonResult) {
                String teamName = team.getJSONObject(TEAM_COLUMN).getString(TEAM_NAME);
                List<JSONObject> teamMembersList = PersonAttributes.getTeamMembers(teamName);
                int teamIndex = jsonResult.indexOf(team);

                JSONArray teamMembersJSON = new JSONArray(teamMembersList);

                jsonResult.get(teamIndex).getJSONObject(TEAM_COLUMN).put(TEAM_MEMBERS, teamMembersJSON);
            }
        }
        return jsonResult;
    }


    /**
     * Method to get all existing teams regardless of approved.
     * @return A list of all team objects in JSON that exist in the database.
     */
    public static synchronized List<JSONObject> getAllTeams() {
        Connection conn = DataBaseConnection.getConnection();
        return addParticipantsToListOfTeams(getTeamsByPreparedStatement(GET_TEAMS + ";", "", conn));
    }

    /**
     * Method to get all unapproved teams.
     * @return A list of unapproved teams as JSON objects.
     */
    public static synchronized List<JSONObject> getAllUnapprovedTeams() {
        Connection conn = DataBaseConnection.getConnection();
        return getTeamsByPreparedStatement(GET_TEAMS + WHERE_CLAUSE + UNAPPROVED + ";", "", conn);
    }

    /**
     * Method to get all approved teams.
     * @param personAttributes The PersonAttributes object necessary for querying.
     * @return A list of all approved teams as JSON objects.
     */
    public static synchronized List<JSONObject> getAllApprovedTeams(PersonAttributes personAttributes) {
        Connection conn = DataBaseConnection.getConnection();
        return addParticipantsToListOfTeams(getTeamsByPreparedStatement(GET_TEAMS + WHERE_CLAUSE + APPROVED + ";", "", conn));
    }


    /**
     * Method to get a team object by team name.
     * @param teamName The team name as a String that needs to be fetched.
     * @return A JSON object corresponding to the team name, otherwise null.
     */
    public static synchronized JSONObject getTeamByName(String teamName, boolean isAuthorized) {
        Connection conn = DataBaseConnection.getConnection();
        List<JSONObject> queryResult = getTeamsByPreparedStatement(GET_TEAMS + WHERE_CLAUSE + BY_NAME + ";", teamName, conn);

        List<JSONObject> result = addParticipantsToListOfTeams(queryResult);
        JSONObject team = !Objects.isNull(result) && !result.isEmpty() ? result.get(0) : null;
        if(!Objects.isNull(team) && !isAuthorized) {
            team.getJSONObject(TEAM_COLUMN).remove(INVITE_LINK);
        }
        return team;
    }

    protected static synchronized List<JSONObject> getTeamsByPreparedStatement(String statement, String filter, Connection conn) {
        return GeneralQueryMethods.getJsonObjectsByString(statement, filter, conn, TEAM_COLUMN);
    }

    protected static synchronized boolean checkIfTeamExistsByName(String teamName) {
        Connection conn = DataBaseConnection.getConnection();
        try(PreparedStatement getTeamByNameStatement = conn.prepareStatement(GET_TEAM_BY_NAME)) {
            conn.setAutoCommit(false);

            getTeamByNameStatement.setString(1, teamName);

            ResultSet queryResult = getTeamByNameStatement.executeQuery();
            conn.commit();
            conn.close();

            return queryResult.next() && queryResult.getInt(CHECK_COLUMN_NAME) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * To make a person a participant, it needs to join a team.
     * This method allows you to make a person join a team, provided the invite link is valid.
     * @param sNumb The person who needs to become a participant.
     * @param inviteLink The link of the team the person wants to join.
     * @return The team name if the link was correct and team is not full, otherwise null.
     */
    public static synchronized String joinTeamByInviteLink(int sNumb, String inviteLink) {
        JSONObject jsonObject = inviteLinkCheck(inviteLink);
        if (!Objects.isNull(jsonObject) && !jsonObject.getBoolean(IS_FULL_BOOLEAN)) {
            String teamName = jsonObject.getString(TEAM_NAME);
            boolean check = PersonAttributes.joinTeam(sNumb, teamName) && !isTeamFull(teamName);
            if(check) {
                updateTeamFullness(teamName);
            }
            return check ? teamName : null;
        } else {
            return null;
        }
    }

    private static synchronized JSONObject inviteLinkCheck(String inviteLink) {
        Connection conn = DataBaseConnection.getConnection();
        try(PreparedStatement inviteLinkCheckStatement = conn.prepareStatement(GET_TEAM_BY_INVITE_LINK)) {
            conn.setAutoCommit(false);

            inviteLinkCheckStatement.setString(1, inviteLink);

            ResultSet queryResult = inviteLinkCheckStatement.executeQuery();
            conn.commit();
            conn.close();

            if(queryResult.next()) {
                return XML.toJSONObject(queryResult.getString(TEAM_COLUMN));
            } else {
                return null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Creates a team in the database.
     * On default, it does not have an invite-link nor is it approved.
     * @param teamName The name of the to be created team.
     * @param captain The student number of the person who wants to create a team.
     * @return True if the team was successfully created, false if duplicate names or sql failure.
     */
    public static synchronized boolean createTeam(String teamName, int captain) {
        boolean check = !checkIfTeamExistsByName(teamName) && !Objects.isNull(PersonAttributes.getPersonByID(captain)) && Objects.isNull(PersonAttributes.getParticipantByID(captain));
        if(check) {
            Connection conn = DataBaseConnection.getConnection();
            try(PreparedStatement createTeamStatement = conn.prepareStatement(CREATE_TEAM)) {
                conn.setAutoCommit(false);

                createTeamStatement.setString(1, teamName);
                createTeamStatement.setInt(2, captain);
                createTeamStatement.setBoolean(3, false);

                createTeamStatement.executeUpdate();
                conn.commit();
                conn.close();

                PersonAttributes.joinTeam(captain, teamName);

                return true;
            } catch (SQLException e) {
                return false;
            }

        } else {
            return false;
        }
    }

    /**
     * Get the team captain of a given team.
     * @param teamName The team which needs to be queried.
     * @return Returns a participant JSON object defined by my other work.
     */
    public static synchronized JSONObject getTeamCaptainByTeamName(String teamName){
        if(checkIfTeamExistsByName(teamName)) {
            Connection conn = DataBaseConnection.getConnection();
            try(PreparedStatement getTeamCaptainByTeamNameStatement = conn.prepareStatement(GET_TEAM_CAPTAIN)) {
                conn.setAutoCommit(false);

                getTeamCaptainByTeamNameStatement.setString(1, teamName);

                ResultSet queryResult = getTeamCaptainByTeamNameStatement.executeQuery();
                conn.commit();
                conn.close();

                if(queryResult.next()) {
                    return PersonAttributes.getParticipantByID(queryResult.getInt(TEAM_CAPTAIN));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

}
