package Utilities.Validation;

public interface CusValidation extends BaseValidation {
    String CUS_ID_VALID = "^[CCGgKk]\\d{4}$";

    String NAME_VALID = "^.{2,25}$";

    String PHONE_VALID = "^0\\d{9}$";
}
