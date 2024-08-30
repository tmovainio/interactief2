package nl.utwente.di.interactief2.JDBC.connection;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

import java.sql.*;
import java.util.Objects;

public class DataBaseConnection {

    public static Connection getConnection() {
        Dotenv dotenv;
        Connection conn;
            String dbUserName;
            String dbPassword;
            try {
                dotenv = Dotenv.configure().load();
                dbUserName = dotenv.get("db_user");
                dbPassword = dotenv.get("db_pass");
            } catch (DotenvException e) {
                dbUserName = System.getenv("db_user");
                dbPassword = System.getenv("db_pass");
            }
            if(Objects.isNull(dbUserName) || Objects.isNull(dbPassword)) {
                throw new RuntimeException("it went wrong");
            }
            try {
                Class.forName("org.postgresql.Driver");
                conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbUserName, dbUserName,
                        dbPassword);
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        return conn;
    }

    public static boolean closeConnection(Connection conn) {
        try {
            conn.commit();
            conn.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

}
