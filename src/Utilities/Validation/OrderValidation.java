package Utilities.Validation;

public interface OrderValidation extends BaseValidation {
    String PROVINCE_VALID = "^.{2,30}$";
    String NUM_TABLES_VALID = POSITIVE_INT_VALID;
    String DATE_VALID = "^\\d{2}/\\d{2}/\\d{4}$"; // dd/MM/yyyy
}