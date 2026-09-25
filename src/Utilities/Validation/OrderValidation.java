package Utilities.Validation;

public interface OrderValidation extends BaseValidation {
    String PROVINCE_VALID = "^.{2,30}$";
    String NUM_TABLES_VALID = POSITIVE_INT_VALID;
    String DATE_VALID = "^(0[1-9]|[12]\\d|3[01])/(0[1-9]|1[0-2])/\\d{4}$"; // dd/MM/yyyy
}