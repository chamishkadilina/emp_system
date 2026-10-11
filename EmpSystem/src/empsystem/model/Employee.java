package empsystem.model;

import java.time.LocalDate;

/** One row of the {@code employee} table. {@code id} is null for an employee not saved yet. */
public record Employee(
        String id,
        String firstName,
        String lastName,
        String gender,
        String contact,
        LocalDate dateOfBirth,
        String position,
        String department,
        byte[] photo) {

    public String fullName() {
        return firstName + " " + lastName;
    }
}
