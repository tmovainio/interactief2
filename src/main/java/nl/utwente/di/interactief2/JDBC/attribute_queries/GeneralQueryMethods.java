package nl.utwente.di.interactief2.JDBC.attribute_queries;

import org.json.JSONObject;
import org.json.XML;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GeneralQueryMethods {


    /*
    Note that there was a rework of the database around sprint 2 - 3,
    meaning that most of, if not all, query methods had to be redone.
    This means that the dynamic querying like the method update or insert are quite new,
    and not fully developed into all methods.
    */

    /**
     * Method to do an update to a table in the database.
     * Make sure that the condition names and values are in the same order.
     * Make sure that the table columns and values are in the same order.
     * 
     * @param tableName       The table for which an update needs to be executed.
     * @param conditionNames  The condition names on which row(s) you need to
     *                        update.
     * @param conditionValues The values on which the conditions need to be tested.
     * @param tableColumns    The columns effected by the update.
     * @param tableValues     The new values for the columns effected.
     * @param conn            The connection to the database.
     * @return True if everything runs, false if a problem arises.
     */
    protected static synchronized boolean update(String tableName, List<String> conditionNames,
            List<Object> conditionValues, List<String> tableColumns, List<Object> tableValues, Connection conn) {

        // Check if the lists are the same size to ensure correct input.
        if (tableColumns.size() == tableValues.size()) {
            StringBuilder setterString = new StringBuilder();

            // Insert the column names and a prepared statement variable input.
            for (int i = 0; i < tableColumns.size(); i++) {
                setterString.append(tableColumns.get(i)).append(" = ?");

                // If it is not the last element in the list, append comma to conform with sql
                // notation.
                if (i != (tableColumns.size() - 1)) {
                    setterString.append(", ");
                }
            }

            // Prepare the update sql statement.
            StringBuilder statement = new StringBuilder(
                    "UPDATE " + tableName + " SET " + setterString.toString() + " WHERE ");

            // Add conditions if needed.
            for (int i = 0; i < conditionNames.size(); i++) {
                statement.append(conditionNames.get(i)).append(" = ?");

                // If it is not the last element in the list, append comma to conform with sql
                // notation.
                if (i != (conditionNames.size() - 1)) {
                    statement.append(" AND ");
                }
            }

            // Initiate prepared statements
            try (PreparedStatement updateStatement = conn.prepareStatement(statement.toString())) {
                conn.setAutoCommit(false);

                // Add all the values in the value variables
                PreparedStatement updateStatementWithValues = addValues(updateStatement, tableValues, 0);
                if (Objects.isNull(updateStatementWithValues)) {
                    return false;
                }

                // Set all the conditional values.
                PreparedStatement updateStatementWithValuesAndConditions = addValues(updateStatementWithValues,
                        conditionValues, tableValues.size());
                if (Objects.isNull(updateStatementWithValuesAndConditions)) {
                    return false;
                }

                // Execute the query, commit and close.
                updateStatementWithValuesAndConditions.executeUpdate();
                conn.commit();
                conn.close();

                return true;
            } catch (SQLException e) {

                // Wrong list input.
                return false;
            }
        } else {

            // Wrong list sizes.
            return false;
        }
    }

    /**
     * Method to do an insert in a table in the database.
     * Make sure that the table columns and values are in the same order.
     * 
     * @param tableName    The table in which something has to be inserted.
     * @param tableColumns The table columns effected by the insert.
     * @param tableValues  The values to be inserted.
     * @param conn         The connection needed to insert into the database.
     * @return True if nothing goes wrong, false if something does go wrong (can
     *         literally be anything from malformed inout to connection drop).
     */
    protected static synchronized boolean insertInto(String tableName, List<String> tableColumns,
            List<Object> tableValues, Connection conn) {
        // Check if the lists are the same size to ensure correct input.
        if (tableColumns.size() == tableValues.size()) {
            StringBuilder columnString = new StringBuilder();

            // Append all the columns that need to be inserted into
            for (int i = 0; i < tableColumns.size(); i++) {
                columnString.append(tableColumns.get(i));

                // If it is not the last element in the list, append comma to conform with sql
                // notation.
                if (i != (tableColumns.size() - 1)) {
                    columnString.append(", ");
                }
            }

            // Append all the variable notations.
            StringBuilder ambiguousValuesString = new StringBuilder();
            for (int i = 0; i < tableValues.size(); i++) {
                ambiguousValuesString.append("?");

                // If it is not the last element in the list, append comma to conform with sql
                // notation.
                if (i != (tableValues.size() - 1)) {
                    ambiguousValuesString.append(", ");
                }
            }

            // Prepare the insert into statement.
            String statement = "INSERT INTO " + tableName + "(" + columnString.toString() +
                    ") VALUES (" + ambiguousValuesString.toString() + ");";

            // Initiate the prepared statement.
            try (PreparedStatement insertIntoStatement = conn.prepareStatement(statement)) {
                conn.setAutoCommit(false);

                // Insert all the values into the prepared statement.
                PreparedStatement insertIntoStatementWithValues = addValues(insertIntoStatement, tableValues, 0);
                // Check if the statement isn't problematic
                if (Objects.isNull(insertIntoStatementWithValues)) {
                    return false;
                }

                // Execute the update, commit and close connection
                insertIntoStatementWithValues.executeUpdate();
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

    /**
     * Method to get the highest ID + 1 existing in a given column of a given table.
     * 
     * @param tableName   The table name of which a new ID has to be given.
     * @param tableColumn The column name of which a new ID has to be given.
     * @param conn        The connection necessary for querying.
     * @return The new ID.
     */
    protected static int getNewID(String tableName, String tableColumn, Connection conn) {
        // Get arbitrary prefix for the table to query from (makes it look nice in the
        // query, that is all).
        String prefix = getPrefix(tableName);

        // Prepare the selector statement
        String getNewIDBuilder = "SELECT MAX(" + prefix + "." + tableColumn +
                ") FROM " + tableName + " " + prefix;

        // Initiate the prepared statement.
        try (PreparedStatement getNewIDStatement = conn.prepareStatement(getNewIDBuilder)) {
            conn.setAutoCommit(false);

            // Get execute the query. and commit (importantly not closing the connection!)
            ResultSet queryResult = getNewIDStatement.executeQuery();
            conn.commit();

            // Check if the query had any results and return it.
            if (queryResult.next()) {
                int result = queryResult.getInt(1);
                return result != 0 ? (result + 1) : Integer.MIN_VALUE;
            } else {
                throw new SQLException();
            }
        } catch (SQLException e) {
            return Integer.MIN_VALUE;
        }
    }

    /**
     * Method to do a select query for multiple objects with a given prepared
     * statement.
     * 
     * @param statement  The prepared statement which has to be executed.
     * @param objectName The column name that will result from the query.
     * @param conn       The connection needed for querying.
     * @return A list of JSON objects specified by the prepared statement (or an
     *         empty array list if there was nothing to query).
     * @throws SQLException Prepared statement is malformed.
     */
    protected static List<JSONObject> querySelectRefiner(PreparedStatement statement, String objectName,
            Connection conn) throws SQLException {
        // Execute query, commit and close the connection.
        ResultSet queryResult = statement.executeQuery();
        conn.commit();
        conn.close();

        // Add the result to an arraylist.
        List<JSONObject> result = new ArrayList<>();
        while (queryResult.next()) {
            JSONObject json = XML.toJSONObject(queryResult.getString(objectName));
            result.add(json);
        }
        return result;
    }

    /**
     * Method to return a json objects by a given statement with a single string
     * condition.
     * 
     * @param statement  The statement which will turn into the prepared statement.
     * @param filter     The filter string.
     * @param conn       The connection needed for querying.
     * @param columnName The column name resulting from the query.
     * @return A list of JSON objects specified by the statement (or an empty array
     *         list if there was nothing to query).
     */
    protected static List<JSONObject> getJsonObjectsByString(String statement, String filter, Connection conn,
            String columnName) {
        // Initiate Prepared statement
        try (PreparedStatement getTeamsStatement = conn.prepareStatement(statement)) {
            conn.setAutoCommit(false);

            // Check if there are filters.
            if (!Objects.isNull(filter) && !Objects.equals(filter, "")) {
                getTeamsStatement.setString(1, filter);
            }

            // Execute the query in the method that is specifically made to do this.
            return GeneralQueryMethods.querySelectRefiner(getTeamsStatement, columnName, conn);
        } catch (SQLException e) {

            // If something goes wrong, return empty list.
            return new ArrayList<>();
        }
    }

    /**
     * Method to return a json objects by a given statement with a single integer
     * condition.
     * 
     * @param statement  The statement which will turn into the prepared statement.
     * @param filter     The filter integer.
     * @param conn       The connection needed for querying.
     * @param columnName The column name resulting from the query.
     * @return A list of JSON objects specified by the statement (or an empty array
     *         list if there was nothing to query).
     */
    protected static List<JSONObject> getJsonObjectsByInt(String statement, int filter, Connection conn,
            String columnName) {
        // Initiate Prepared statement
        try (PreparedStatement getTeamsStatement = conn.prepareStatement(statement)) {
            conn.setAutoCommit(false);

            // Check if there are filters.
            if (filter != Integer.MIN_VALUE) {
                getTeamsStatement.setInt(1, filter);
            }

            // Execute the query in the method that is specifically made to do this.
            return GeneralQueryMethods.querySelectRefiner(getTeamsStatement, columnName, conn);
        } catch (SQLException e) {

            // If something goes wrong, return empty list.
            return new ArrayList<>();
        }
    }

    /**
     * Fancy prefix maker.
     * 
     * @param tableName The root of the prefix.
     * @return A prefix of length 2.
     */
    private static synchronized String getPrefix(String tableName) {
        return tableName.substring(0, 2);
    }

    /**
     * Method to dynamically set values in a given prepared statement.
     * 
     * @param statement The prepared statement in which values have to be set.
     * @param values    The list of values that need to be set into the prepared
     *                  statement.
     * @param indent    The indent that allows multiple calls to the same prepared
     *                  statement.
     * @return The refined prepared statement, or null if there were any problems.
     */
    private static synchronized PreparedStatement addValues(PreparedStatement statement, List<Object> values,
            int indent) {
        // Try adding values to a prepared statement
        try {

            // Loop through all the values specified
            for (Object value : values) {

                // Check what instance the value is of.
                // The value will be set in the prepared statement at place values.index(value),
                // which should correspond with the same index as the list specifying the name
                // of the value.
                // An indent is provided for certain queries having conditions.
                // The extra 1 is necessary because the indexes of arrays works differently than
                // indexing in prepared statements.
                if (value instanceof String) {

                    // Make sure the value is not null
                    if (!Objects.isNull(value)) {
                        statement.setString(values.indexOf(value) + indent + 1, ((String) value));
                    }
                } else if (value instanceof Integer) {

                    // Make sure the value is not null
                    if ((Integer) value != Integer.MIN_VALUE) {
                        statement.setInt(values.indexOf(value) + indent + 1, ((Integer) value));
                    }

                } else if (value instanceof Boolean) {
                    statement.setBoolean(values.indexOf(value) + indent + 1, ((boolean) value));
                } else {

                    // Something went wrong...
                    throw new SQLException();
                }
            }

            // Return the statement for further processing.
            return statement;
        } catch (SQLException e) {

            // Something went wrong so return a null.
            // This can then be checked by if statements further down the line.
            return null;
        }
    }

}
