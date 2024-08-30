package nl.utwente.di.interactief2.JDBC.attribute_queries;

import jakarta.ws.rs.core.Response;
import nl.utwente.di.interactief2.JDBC.connection.DataBaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SettingAttributes {

    public static final String SETTING_COLUMN = "setting";
    public static final String SCORE_BOARD_TOGGLE = "score_board_toggle";

    public static final String NAME = "name";

    public static final String GET_SETTING_BY_NAME = "SELECT s.setting FROM settings s WHERE s.name = ?";
    public static final List<String> SETTING_ATTRIBUTES = List.of(SETTING_COLUMN);

    public static synchronized boolean getSettingByName(String name) throws SQLException {
        Connection conn = DataBaseConnection.getConnection();
        try(PreparedStatement getSettingByNameStatement = conn.prepareStatement(GET_SETTING_BY_NAME)) {
            conn.setAutoCommit(false);

            getSettingByNameStatement.setString(1, name);

            ResultSet queryResult = getSettingByNameStatement.executeQuery();
            conn.commit();
            conn.close();

            if(queryResult.next()) {
                return queryResult.getBoolean(SETTING_COLUMN);
            } else {
                throw new SQLException();
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    public static synchronized boolean updateSetting(String name, boolean value) {
        Connection conn = DataBaseConnection.getConnection();

        List<Object> values = List.of(value);
        return GeneralQueryMethods.update(SETTING_COLUMN + "s", List.of(NAME), List.of(name), SETTING_ATTRIBUTES, values, conn);
    }

}
