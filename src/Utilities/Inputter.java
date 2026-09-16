package Utilities;

import Utilities.Validation.BaseValidation;

import java.util.Scanner;

public class Inputter {
    private Scanner scanner;

    public Inputter(){
        this.scanner = new Scanner(System.in);
    }

    public String getString(String mess){
        System.out.println(mess);
        return scanner.nextLine();
    }

    public int getInt(String mess, String pattern){
        int result = 0;
        String temp = getString(mess);
        if(BaseValidation.isValid(temp, pattern)){
            result = Integer.parseInt(temp);
        }
        return result;
    }

    public double getDouble(String mess, String pattern) {
        double result = 0;
        String temp = getString(mess);

        if (BaseValidation.isValid(temp, pattern)) {
            result = Double.parseDouble(temp);
        }

        return result;
    }
// Ví dụ: inputAndLoop ("CustomerID: ", Acceptable.CUS_ID_VALID, loop)
    public String inputAndLoop(String mess, String pattern, boolean loop){
        String result = "";
        result = getString(mess);
        boolean more = true;
        do {
            more = !BaseValidation.isValid(result, pattern);
            if (more && (loop && result.length() > 0)) {
                System.out.println("Data is valid !. Re-enter");
            }
        } while (loop & more);
        return result.trim();
    }
}
