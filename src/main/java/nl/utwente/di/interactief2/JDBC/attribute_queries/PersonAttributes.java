package nl.utwente.di.interactief2.JDBC.attribute_queries;
import jakarta.ws.rs.GET;
import nl.utwente.di.interactief2.JDBC.SecurityClasses.Hasher;
import nl.utwente.di.interactief2.JDBC.SecurityClasses.Salter;
import nl.utwente.di.interactief2.JDBC.connection.DataBaseConnection;
import org.json.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PersonAttributes {

    //Column and object names
    private static final String CHECK_COLUMN_NAME = "exist";
    public static final String PERSON_OBJECT_NAME = "person";
    public static final String PARTICIPANT_OBJECT_NAME = "participant";
    public static final String STUDENT_NUMBER = "s_numb";
    public static final String NAME = "name";
    public static final String PHONE_NUMBER = "phone_numb";
    public static final String TEAM_NAME = "team_name";
    private static final String PASSWORD = "password";
    private static final String SALT = "salt";

    private static final List<String> PERSON_ATTRIBUTE_LIST = List.of(STUDENT_NUMBER, NAME, PHONE_NUMBER, PASSWORD, SALT);

    //Standard query WHERE, AND clauses
    protected static final String WHERE_CLAUSE = " WHERE";
    protected static final String AND_CLAUSE = " AND";
    private static final String BY_ID_OF_PERSON = " pe.s_numb = ?";
    private static final String BY_ID_OF_PARTICIPANT = " pa.s_numb = ?";
    private static final String BY_TEAM_NAME = " pa.team_name = ?";


    //Checks queries for password and admin
    private static final String CHECK_SELECT = "SELECT COUNT(1) AS ";
    private static final String CHECK_PASSWORD = CHECK_SELECT + CHECK_COLUMN_NAME + " FROM person pe WHERE pe.s_numb = ? AND pe.password = ?";
    private static final String CHECK_IF_ADMIN = CHECK_SELECT + CHECK_COLUMN_NAME + " FROM administrator a WHERE a.s_numb = ?;";
    private static final String UPDATE_PERSON = "UPDATE person SET";
    private static final String UPDATE_PASSWORD = " password = ?, salt = ?";
    private static final String UPDATE_PHONE_NUMBER = " phone_numb = ?";
    private static final String UPDATE_PERSON_WHERE = " WHERE s_numb = ?;";



    //get-team-name-by-sNumb statement

    private static final String GET_PERSON_BY_ID = "SELECT XMLELEMENT(NAME person, XMLFOREST(pe.s_numb, pe.name, pe.phone_numb)) AS person FROM person pe WHERE pe.s_numb = ?;";

    //delete person by id statement
    private static final String DELETE_PERSON = "DELETE FROM person pe WHERE" + BY_ID_OF_PERSON + ";";

    //get-participant(-by) statement constructors
    private static final String GET_TEAM_NAME_BY_PARTICIPANT = "SELECT pa.team_name FROM participant pa WHERE pa.s_numb = ?";

    protected static final String GET_PARTICIPANT = "SELECT XMLELEMENT(NAME " + PARTICIPANT_OBJECT_NAME + ", XMLFOREST(pe.s_numb, pe.name, pe.phone_numb, pa.team_name)) AS " + PARTICIPANT_OBJECT_NAME + " FROM person pe, participant pa WHERE pe.s_numb = pa.s_numb";

    //Delete statement for participant
    private static final String LEAVE_TEAM = "DELETE FROM participant pa WHERE" + BY_ID_OF_PARTICIPANT;

    //Insert statement for participant
    private static final String JOIN_TEAM = "INSERT INTO participant (s_numb, team_name) VALUES (?, ?);";

    //Insert statement for person
    private static final String INSERT_PERSON = "INSERT INTO person (s_numb, name, phone_numb, password, salt) VALUES (?, ?, ?, ?, ?);";
    private static final String GET_SALT = "SELECT salt FROM person WHERE s_numb = ?;";


    public PersonAttributes() {

    }



    //Join team statement
    protected static synchronized boolean joinTeam(int sNumb, String teamName) {
        //Check if the person exists and also if the team exists.
        boolean check = !Objects.isNull(getPersonByID(sNumb)) && TeamAttributes.checkIfTeamExistsByName(teamName);
        if(check) {
            Connection conn = DataBaseConnection.getConnection();

            //Initiate prepared statement
            try(PreparedStatement joinTeamStatement = conn.prepareStatement(JOIN_TEAM)) {
                conn.setAutoCommit(false);

                //Set the variables in the prepared statement
                joinTeamStatement.setInt(1, sNumb);
                joinTeamStatement.setString(2, teamName);

                //Execute the update, commit and close the connection.
                joinTeamStatement.executeUpdate();
                conn.commit();
                conn.close();

                return true;
            } catch (SQLException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Method to add a person to the database.
     * @param sNumb The student number of the to be added person.
     * @param name The name of the to be added person.
     * @param phoneNumber The phone number of the to be added person.
     * @param password The password of the to be added person.
     * @return A boolean for if a duplicate student number has been detected.
     */
    public static synchronized boolean insertPerson(int sNumb, String name, String phoneNumber, String password) {
        //Make sure that person is not already in the database.
        if (Objects.isNull(getPersonByID(sNumb))) {
            Connection conn = DataBaseConnection.getConnection();

            //Gather all the values.
            String salt = Salter.getSalt();
            List<Object> values;
            try {
                values = List.of(sNumb, name, phoneNumber, Hasher.hash(password, salt), salt);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                return  false;
            }
            //Dynamic insert query.
            return GeneralQueryMethods.insertInto(PERSON_OBJECT_NAME, PERSON_ATTRIBUTE_LIST, values, conn);
        } else {
            return false;
        }
    }

    /**
     * Method to update personal information.
     * @param sNumb THe person who needs an update.
     * @param password The new password (set null if not desired to be updated).
     * @param phoneNumb The new phone number (set null if not desired to be updated).
     * @return True if no issues arose, false otherwise.
     */
    public static synchronized boolean changePerson(int sNumb, String password, String phoneNumb) {
        //Make a string builder, so we can make a statement appropriate to the input.
        StringBuilder changePersonStatementString = new StringBuilder(UPDATE_PERSON);

        //General purpose checks to determine the input.
        boolean passwordCheck = !Objects.isNull(password) && !Objects.equals(password, "");
        boolean phoneNumbCheck = !Objects.isNull(phoneNumb) && !Objects.equals(phoneNumb, "");
        boolean bothTrue = phoneNumbCheck && passwordCheck;

        //Prepare the statement.
        changePersonStatementString.append(passwordCheck ? UPDATE_PASSWORD : "").append(bothTrue ? "," : "").append(phoneNumbCheck ? UPDATE_PHONE_NUMBER : "").append(UPDATE_PERSON_WHERE);

        //Get the connection and initiate the prepared statement.
        Connection conn = DataBaseConnection.getConnection();
        try(PreparedStatement changePersonStatement = conn.prepareStatement(changePersonStatementString.toString())) {
            conn.setAutoCommit(false);

            //Check if there is a password as input, otherwise continue.
            if(passwordCheck) {
                String salt = Salter.getSalt();
                changePersonStatement.setString(1, Hasher.hash(password, salt));
                changePersonStatement.setString(2, salt);
            }
            //Check if there is a phone number s input, otherwise continue.
            if(phoneNumbCheck) {
                changePersonStatement.setString((bothTrue ? 3 : 1), phoneNumb);
            }

            //Set the student number.
            changePersonStatement.setInt((bothTrue ? 4 : (passwordCheck ? 3 : 2)), sNumb);

            //Execute the update, commit and close.
            changePersonStatement.executeUpdate();
            conn.commit();
            conn.close();

            return true;
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            return false;
        }
    }

    /**
     * Method to add an administrator to the database.
     * @param personID The person that is granted the administrator privileges.
     * @return True if successful, false otherwise.
     */
    public static synchronized boolean insertAdmin(int personID) {
        Connection conn = DataBaseConnection.getConnection();

        //Get all the attributes of an admin.
        List<String> adminAttributes = List.of(STUDENT_NUMBER, "privilege");
        return GeneralQueryMethods.insertInto("administrator", adminAttributes, List.of(personID, "committee member"), conn);
    }

    /**
     * Method to remove an administrator to the database.
     * @param personID The person that has its administrator privileges revoked.
     * @return True if successful, false otherwise.
     */
    public static synchronized boolean removeAdmin(int personID) {
        Connection conn = DataBaseConnection.getConnection();

        //Initiate the prepared statement.
        try(PreparedStatement removeAdminStatement = conn.prepareStatement("DELETE FROM administrator WHERE s_numb = ?")) {
            conn.setAutoCommit(false);

            //Set the student number of the administrator.
            removeAdminStatement.setInt(1, personID);

            removeAdminStatement.executeUpdate();
            conn.commit();
            conn.close();

            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * To delete an account.
     * @param sNumb The student number of the person OR participant.
     */
    public static synchronized void deletePerson(int sNumb) throws SQLException {
        leaveTeam(sNumb);
        deleteStatement(DELETE_PERSON, sNumb);
    }

    /**
     * To make a participant into a person by leaving the team.
     * @param sNumb The student number of the person that is leaving.
     */
    public static synchronized void leaveTeam(int sNumb) throws SQLException {
        String teamName = getTeamNameByParticipant(sNumb);

        //Leave team.
        deleteStatement(LEAVE_TEAM, sNumb);

        //Update the fullness of the team.
        if(!Objects.isNull(teamName) && !Objects.equals(teamName, "")) {
            TeamAttributes.updateTeamFullness(teamName);
        }
    }


    //General delete statement.
    private static synchronized void deleteStatement(String statement, int filter) throws SQLException{
        Connection conn = DataBaseConnection.getConnection();

        //Initiate prepared statement.
        try(PreparedStatement preparedStatement = conn.prepareStatement(statement)) {
            conn.setAutoCommit(false);

            //Set the prepared statement variables.
            preparedStatement.setInt(1, filter);

            //Execute the update, commit and close the connection.
            preparedStatement.executeUpdate();
            conn.commit();
            conn.close();

        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }


    /**
     * Method to return team name by participant id.
     * @param sNumb The student number of the participant for which the team name has to be queried.
     * @return A team name in string form if the student number belonged to a participant, empty if it was not found and null if something went wrong.
     */
    public static synchronized String getTeamNameByParticipant(int sNumb) {
        Connection conn = DataBaseConnection.getConnection();

        //Initiate prepared statement.
        try(PreparedStatement getTeamNameByParticipant = conn.prepareStatement(GET_TEAM_NAME_BY_PARTICIPANT)) {
            conn.setAutoCommit(false);

            //Set the student number into the query.
            getTeamNameByParticipant.setInt(1, sNumb);

            //Execute the query, commit and close the connection.
            ResultSet queryResult = getTeamNameByParticipant.executeQuery();
            conn.commit();
            conn.close();

            //Check if the query returned something, and if it did, return it, otherwise return null.
            return queryResult.next() ? queryResult.getString(TEAM_NAME) : null;
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Get a JSON person object by providing an ID.
     * @param sNumb The student number of the person that needs to be fetched.
     * @return A JSON object that contains the information of the corresponding person to the student number OR null if no person was found.
     */
    public static synchronized JSONObject getPersonByID(int sNumb) {
        Connection connection = DataBaseConnection.getConnection();

        //Gather all the persons
        List<JSONObject> result = getPersons(GET_PERSON_BY_ID, sNumb, connection);
        //Check if the query returned something, and if it did, return it, otherwise return null.
        return !result.isEmpty() ? result.get(0) : null;
    }

    //General method to return person objects from the person table.
    private static synchronized List<JSONObject> getPersons(String statement, int filter, Connection conn) {

        //Initiate prepared statement.
        try (PreparedStatement getPersonStatement = conn.prepareStatement(statement)) {
            conn.setAutoCommit(false);

            //If there is a filter, input it in the prepared statement.
            if(filter != Integer.MIN_VALUE) {
                getPersonStatement.setInt(1, filter);
            }

            //Return the values given by the general query executor.
            return GeneralQueryMethods.querySelectRefiner(getPersonStatement, PERSON_OBJECT_NAME, conn);
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Get a JSON participant object by providing an ID.
     * @param sNumb The student number of the participant that needs to be fetched.
     * @return The participant or null if no-one was found.
     */
    public static synchronized JSONObject getParticipantByID(int sNumb) {
        Connection conn = DataBaseConnection.getConnection();

        //Get all the participants
        List<JSONObject> result = getParticipantsByPreparedStatement(GET_PARTICIPANT + AND_CLAUSE + BY_ID_OF_PERSON + ";", Integer.toString(sNumb), conn);

        //Check if the query returned something, and if it di, return it, otherwise return null.
        return result.size() == 1 ? result.get(0) : null;
    }

    /**
     * Get all team members from a team (including captain without telling who is the captain).
     * @param teamName The team name that needs all members returned.
     * @return A list with participant JSON objects that all have the same team name.
     */
    public static synchronized List<JSONObject> getTeamMembers(String teamName) {
        Connection conn = DataBaseConnection.getConnection();

        //Return the general query method output with a statement that gets all the team members.
        return getParticipantsByPreparedStatement(GET_PARTICIPANT + AND_CLAUSE + BY_TEAM_NAME + ";", teamName, conn);
    }

    /**
     * A method to return ALL participants.
     * For testing purposes.
     * @return A list with participant JSON objects.
     */
    public static synchronized List<JSONObject> getAllParticipants() {
        Connection conn = DataBaseConnection.getConnection();
        return getParticipantsByPreparedStatement(GET_PARTICIPANT + ";", null, conn);
    }

    //General method to query the participant table.
    private static synchronized List<JSONObject> getParticipantsByPreparedStatement(String statement, String filter, Connection conn) {
        //Initiate prepared statement.
        try (PreparedStatement getParticipantStatement = conn.prepareStatement(statement)) {
            conn.setAutoCommit(false);

            //Check if the filter is an integer or string and input set it in the prepared statement.
            if(parseToInt(filter) != Integer.MIN_VALUE) {
                getParticipantStatement.setInt(1, parseToInt(filter));
            } else if(!Objects.isNull(filter)) {
                getParticipantStatement.setString(1, filter);
            }

            //Return output from the general query method.
            return GeneralQueryMethods.querySelectRefiner(getParticipantStatement, PARTICIPANT_OBJECT_NAME, conn);
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    //Method to check if a string is able to be parsed to an integer.
    private static synchronized int parseToInt(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }

    /**
     * To check if a person is an administrator.
     * @param sNumb The person to be checked.
     * @return True if the person is an administrator, false otherwise.
     */
    public static synchronized boolean checkIfAdmin(int sNumb) {
        Connection conn = DataBaseConnection.getConnection();

        //Initiate the prepared statement.
        try(PreparedStatement checkIfAdminStatement = conn.prepareStatement(CHECK_IF_ADMIN)) {
            conn.setAutoCommit(false);

            //Set the student number in the prepared statement.
            checkIfAdminStatement.setInt(1, sNumb);

            //Execute the query, commit and close the connection.
            ResultSet result = checkIfAdminStatement.executeQuery();
            conn.commit();
            conn.close();

            //Check if the query returned something (because it makes use of COUNT()).
            return result.next() && result.getInt(CHECK_COLUMN_NAME) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    //Method to get the salt of a certain user.
    private static synchronized String getSalt(int sNumb) {
        Connection conn = DataBaseConnection.getConnection();

        //Initiate prepared statement.
        try(PreparedStatement getSaltStatement = conn.prepareStatement(GET_SALT)) {
            conn.setAutoCommit(false);

            //Set the student number in the prepared statement.
            getSaltStatement.setInt(1, sNumb);

            //Execute the query, commit and close the connection.
            ResultSet result = getSaltStatement.executeQuery();
            conn.commit();
            conn.close();

            //Check if the query returned something, and if it did, return it, otherwise return null.
            return result.next() ? result.getString("salt") : null;
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * A method to (securely) do a password check.
     * @param sNumb The student number to check to a corresponding person.
     * @param password The password that needs to be matched.
     * @return True if it is the right password, false otherwise.
     */
    //@ requires sNumb > 0 && password != null;
    public static synchronized boolean checkPassword (int sNumb, String password) {
        // Get the salt from the database
        String salt = getSalt(sNumb);

        //Make sure the salt is not empty.
        if(Objects.isNull(salt)) {
            return false;
        }
        Connection conn = DataBaseConnection.getConnection();

        //Initiate the prepared statement.
        try (PreparedStatement checkPasswordStatement = conn.prepareStatement(CHECK_PASSWORD)){
            conn.setAutoCommit(false);

            //Set the student number in the prepared statement and also the hashed and salted password.
            checkPasswordStatement.setInt(1, sNumb);
            checkPasswordStatement.setString(2, Hasher.hash(password, salt));

            //Execute the query, commit and close the connection.
            ResultSet result = checkPasswordStatement.executeQuery();
            conn.commit();
            conn.close();

            //Check if the query returned something (because it makes use of COUNT()).
            return result.next() && result.getInt(CHECK_COLUMN_NAME) > 0;
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            return false;
        }
    }
}
