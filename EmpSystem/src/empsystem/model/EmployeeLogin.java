package empsystem.model;

/** An employee's login for the employee app (table {@code emppass}). */
public record EmployeeLogin(String userName, String password) {
}
