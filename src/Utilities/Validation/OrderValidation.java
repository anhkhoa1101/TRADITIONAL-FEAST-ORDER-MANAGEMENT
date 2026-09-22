package Utilities.Validation;

public interface OrderValidation extends BaseValidation {
    String PROVINCE_VALID = "^.{2,30}$";
    String NUM_TABLES_VALID = POSITIVE_INT_VALID;
    String DATE_VALID = "^(0[1-9]|[12]\\d|3[01])/(0[1-9]|1[0-2])/\\d{4} ([01]\\d|2[0-3]):[0-5]\\d$"; // dd/MM/yyyy HH:mm
}