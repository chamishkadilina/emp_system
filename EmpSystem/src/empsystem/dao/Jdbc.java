package empsystem.dao;

import empsystem.db.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.TableModel;
import net.proteanit.sql.DbUtils;

/** Small helpers for running parameterized SQL. Each call uses its own connection. */
final class Jdbc {

    private Jdbc() {
    }

    /** Runs a query and copies the result into a table model for a JTable. */
    static TableModel queryTable(String sql, Object... params) throws SQLException {
        try (Connection con = Database.getConnection();
                PreparedStatement statement = prepare(con, sql, params);
                ResultSet rs = statement.executeQuery()) {
            return DbUtils.resultSetToTableModel(rs);
        }
    }

    /** Runs a query that returns a single number, such as a COUNT(*). */
    static int queryInt(String sql, Object... params) throws SQLException {
        try (Connection con = Database.getConnection();
                PreparedStatement statement = prepare(con, sql, params);
                ResultSet rs = statement.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Runs an INSERT, UPDATE or DELETE and returns the number of affected rows. */
    static int update(String sql, Object... params) throws SQLException {
        try (Connection con = Database.getConnection()) {
            return update(con, sql, params);
        }
    }

    static int update(Connection con, String sql, Object... params) throws SQLException {
        try (PreparedStatement statement = prepare(con, sql, params)) {
            return statement.executeUpdate();
        }
    }

    static PreparedStatement prepare(Connection con, String sql, Object... params) throws SQLException {
        PreparedStatement statement = con.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }
}
