package empsystem.dao;

import empsystem.db.Database;
import empsystem.model.Employee;
import empsystem.model.EmployeeLogin;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javax.swing.table.TableModel;

/**
 * Employees (table {@code employee}). Lists use the view {@code emp1}, which leaves out the photo.
 */
public class EmployeeDao {

    /** Department filter value that means "every department". */
    public static final String ALL_DEPARTMENTS = "ALL";

    public TableModel list(String department) throws SQLException {
        if (ALL_DEPARTMENTS.equals(department)) {
            return Jdbc.queryTable("SELECT * FROM emp1");
        }
        return Jdbc.queryTable("SELECT * FROM emp1 WHERE department = ?", department);
    }

    public int count(String department) throws SQLException {
        if (ALL_DEPARTMENTS.equals(department)) {
            return Jdbc.queryInt("SELECT COUNT(*) FROM employee");
        }
        return Jdbc.queryInt("SELECT COUNT(*) FROM employee WHERE department = ?", department);
    }

    /** Matches the exact employee ID, or part of the name, gender or position. */
    public TableModel search(String text) throws SQLException {
        String like = "%" + text + "%";
        return Jdbc.queryTable("SELECT * FROM emp1 WHERE EmpID = ? OR First_Name LIKE ?"
                + " OR Last_Name LIKE ? OR Gender LIKE ? OR position LIKE ?",
                text, like, like, like, like);
    }

    public Optional<Employee> findById(String id) throws SQLException {
        try (Connection con = Database.getConnection();
                PreparedStatement statement = Jdbc.prepare(con,
                        "SELECT * FROM employee WHERE EmpID = ?", id);
                ResultSet rs = statement.executeQuery()) {
            if (!rs.next()) {
                return Optional.empty();
            }
            Date dob = rs.getDate("DOB");
            return Optional.of(new Employee(
                    rs.getString("EmpID"),
                    rs.getString("First_Name"),
                    rs.getString("Last_Name"),
                    rs.getString("Gender"),
                    rs.getString("Contact_N0"),
                    dob == null ? null : dob.toLocalDate(),
                    rs.getString("position"),
                    rs.getString("department"),
                    rs.getBytes("Image")));
        }
    }

    public Optional<EmployeeLogin> findLogin(String id) throws SQLException {
        try (Connection con = Database.getConnection();
                PreparedStatement statement = Jdbc.prepare(con,
                        "SELECT name, password FROM emppass WHERE Empid = ?", id);
                ResultSet rs = statement.executeQuery()) {
            if (!rs.next()) {
                return Optional.empty();
            }
            return Optional.of(new EmployeeLogin(rs.getString("name"), rs.getString("password")));
        }
    }

    /** Saves a new employee and creates their employee-app login with a random 4-digit password. */
    public void add(Employee employee) throws SQLException {
        try (Connection con = Database.getConnection()) {
            Jdbc.update(con, "INSERT INTO emppass (name, password)"
                    + " VALUES (?, LPAD(FLOOR(RAND() * 10000), 4, '0'))",
                    employee.firstName());
            Jdbc.update(con, "INSERT INTO employee (First_Name, Last_Name, Gender, Contact_N0, DOB,"
                    + " position, department, Image) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    employee.firstName(),
                    employee.lastName(),
                    employee.gender(),
                    employee.contact(),
                    employee.dateOfBirth(),
                    employee.position(),
                    employee.department(),
                    employee.photo());
        }
    }

    /**
     * Updates the employee's details, but not the photo. The date of birth is left unchanged when it
     * is null. Returns false when no employee has that ID.
     */
    public boolean update(Employee employee) throws SQLException {
        String sql = "UPDATE employee SET First_Name = ?, Last_Name = ?, Gender = ?, Contact_N0 = ?,"
                + " position = ?, department = ?";
        if (employee.dateOfBirth() == null) {
            return Jdbc.update(sql + " WHERE EmpID = ?",
                    employee.firstName(), employee.lastName(), employee.gender(),
                    employee.contact(), employee.position(), employee.department(),
                    employee.id()) > 0;
        }
        return Jdbc.update(sql + ", DOB = ? WHERE EmpID = ?",
                employee.firstName(), employee.lastName(), employee.gender(),
                employee.contact(), employee.position(), employee.department(),
                employee.dateOfBirth(), employee.id()) > 0;
    }

    public void updatePhoto(String id, byte[] photo) throws SQLException {
        Jdbc.update("UPDATE employee SET Image = ? WHERE EmpID = ?", photo, id);
    }

    /**
     * Deletes the employee together with their login, attendance and leave records. Returns false
     * when no employee has that ID.
     */
    public boolean delete(String id) throws SQLException {
        try (Connection con = Database.getConnection()) {
            int deleted = Jdbc.update(con, "DELETE FROM employee WHERE EmpID = ?", id);
            Jdbc.update(con, "DELETE FROM emppass WHERE Empid = ?", id);
            Jdbc.update(con, "DELETE FROM atten_in WHERE empid = ?", id);
            Jdbc.update(con, "DELETE FROM atten_out WHERE empid = ?", id);
            Jdbc.update(con, "DELETE FROM leaveform WHERE empid = ?", id);
            Jdbc.update(con, "DELETE FROM leavehistory WHERE empid = ?", id);
            return deleted > 0;
        }
    }
}
