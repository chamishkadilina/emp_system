package empsystem.dao;

import empsystem.db.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

/** Admin accounts that can log in to this application (table {@code admin}). */
public class AdminDao {

    /** Returns the admin's display name when the user name and password match. */
    public Optional<String> login(String userName, String password) throws SQLException {
        try (Connection con = Database.getConnection();
                PreparedStatement statement = Jdbc.prepare(con,
                        "SELECT name, password FROM admin WHERE user_name = ?", userName);
                ResultSet rs = statement.executeQuery()) {
            // The password is compared here because the column's collation ignores case.
            if (rs.next() && password.equals(rs.getString("password"))) {
                return Optional.of(Objects.toString(rs.getString("name"), ""));
            }
            return Optional.empty();
        }
    }

    public int count() throws SQLException {
        return Jdbc.queryInt("SELECT COUNT(*) FROM admin");
    }

    /** Throws {@link java.sql.SQLIntegrityConstraintViolationException} if the user name exists. */
    public void add(String name, String userName, String password) throws SQLException {
        Jdbc.update("INSERT INTO admin (name, user_name, password) VALUES (?, ?, ?)",
                name, userName, password);
    }

    /** Returns false when no admin has that user name. */
    public boolean delete(String userName) throws SQLException {
        return Jdbc.update("DELETE FROM admin WHERE user_name = ?", userName) > 0;
    }
}
