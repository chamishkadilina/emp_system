package empsystem.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Opens connections to the MySQL database. */
public final class Database {

    private static final int PORT = 3306;
    private static final String NAME = "empmanage";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + ServerSettings.getHost() + ":" + PORT + "/" + NAME;
        return DriverManager.getConnection(url, USER, PASSWORD);
    }
}
