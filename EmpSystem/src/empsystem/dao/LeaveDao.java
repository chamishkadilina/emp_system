package empsystem.dao;

import empsystem.db.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javax.swing.table.TableModel;

/**
 * Leave requests. Pending requests are in {@code leaveform}; the employee app reads the decision
 * from {@code leavehistory}. A request is identified by employee ID, start date and end date.
 */
public class LeaveDao {

    public TableModel listPending() throws SQLException {
        return Jdbc.queryTable("SELECT empid, reason, start_date, end_date FROM leaveform");
    }

    public Optional<String> findComment(String empId, String startDate, String endDate)
            throws SQLException {
        try (Connection con = Database.getConnection();
                PreparedStatement statement = Jdbc.prepare(con, "SELECT Comment FROM leaveform"
                        + " WHERE empid = ? AND start_date = ? AND end_date = ?",
                        empId, startDate, endDate);
                ResultSet rs = statement.executeQuery()) {
            return rs.next() ? Optional.ofNullable(rs.getString(1)) : Optional.empty();
        }
    }

    /** Records the decision for the employee and removes the request from the pending list. */
    public void decide(String empId, String startDate, String endDate, boolean accepted)
            throws SQLException {
        String status = accepted ? "Your Leave is Accepted" : "Your Leave is Rejected";
        try (Connection con = Database.getConnection()) {
            Jdbc.update(con, "UPDATE leavehistory SET status = ?"
                    + " WHERE empid = ? AND staet_date = ? AND end_date = ?",
                    status, empId, startDate, endDate);
            Jdbc.update(con, "DELETE FROM leaveform"
                    + " WHERE empid = ? AND start_date = ? AND end_date = ?",
                    empId, startDate, endDate);
        }
    }
}
