package Utilities.Validation;

public interface BaseValidation {

    String INTEGER_VALID = "^\\d+$";

    String POSITIVE_INT_VALID = "^[1-9]\\d*$";

    String DOUBLE_VALID = "^\\d+(\\.\\d+)?$";

    String POSITIVE_DOUBLE_VALID =
            "^[1-9]\\d*(\\.\\d+)?$";

    static boolean isValid(String value, String pattern) {
        if (value == null || pattern == null) {
            return false;
        }

        return value.matches(pattern);
    }
}