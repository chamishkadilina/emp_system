package empsystem.dao;

import java.sql.SQLException;
import javax.swing.table.TableModel;

/** Check-in and check-out times (tables {@code atten_in} and {@code atten_out}). */
public class AttendanceDao {

    /** How many of the latest records to show. The order matches the range combo box. */
    public enum Range {
        LAST_WEEK(7),
        LAST_MONTH(31),
        ALL(0);

        private final int limit;

        Range(int limit) {
            this.limit = limit;
        }
    }

    public TableModel checkIns(String empId, Range range) throws SQLException {
        return latest("atten_in", "attendance_in", empId, range);
    }

    public TableModel checkOuts(String empId, Range range) throws SQLException {
        return latest("atten_out", "attendance_out", empId, range);
    }

    /** {@code date} may be a full date (2024-10-07) or just its start (2024-10). */
    public TableModel checkInsOn(String empId, String date) throws SQLException {
        return on("atten_in", "attendance_in", empId, date);
    }

    public TableModel checkOutsOn(String empId, String date) throws SQLException {
        return on("atten_out", "attendance_out", empId, date);
    }

    // Table and column names below are constants from this class, never user input.

    private static TableModel latest(String table, String column, String empId, Range range)
            throws SQLException {
        String limit = range.limit > 0 ? " LIMIT " + range.limit : "";
        return Jdbc.queryTable("SELECT * FROM (SELECT " + column + " FROM " + table
                + " WHERE empid = ? ORDER BY " + column + " DESC" + limit + ") AS latest"
                + " ORDER BY " + column + " ASC", empId);
    }

    private static TableModel on(String table, String column, String empId, String date)
            throws SQLException {
        return Jdbc.queryTable("SELECT " + column + " FROM " + table
                + " WHERE empid = ? AND CAST(" + column + " AS CHAR) LIKE ?", empId, date + "%");
    }
}
