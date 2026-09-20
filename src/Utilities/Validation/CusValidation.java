package Utilities.Validation;

public interface CusValidation extends BaseValidation {
    String CUS_ID_VALID = "^[CCGgKk]\\d{4}$";

    String NAME_VALID = "^.{2,25}$";


    String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    String VIETTEL      = "03[2-9]|086|09[678]";
    String VINAPHONE    = "08[1-5]|088|09[14]";
    String MOBIFONE     = "07[06-9]|089|09[03]";

    String PHONE_VALID = "^(" + VIETTEL + "|" + VINAPHONE + "|" + MOBIFONE + ")\\d{7}$";
}
