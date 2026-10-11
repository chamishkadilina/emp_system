package empsystem.util;

import java.util.regex.Pattern;

/** Input rules for the forms. Empty input passes; required fields are checked separately. */
public final class Validation {

    private static final Pattern NAME = Pattern.compile("[a-zA-Z ]{0,30}");
    private static final Pattern CONTACT = Pattern.compile("[0-9]{0,10}");
    private static final Pattern EMPLOYEE_ID = Pattern.compile("[0-9]{0,5}");

    private Validation() {
    }

    /** Letters and spaces, at most 30 characters. Used for names and positions. */
    public static boolean isName(String text) {
        return NAME.matcher(text).matches();
    }

    public static boolean isContact(String text) {
        return CONTACT.matcher(text).matches();
    }

    public static boolean isEmployeeId(String text) {
        return EMPLOYEE_ID.matcher(text).matches();
    }
}
