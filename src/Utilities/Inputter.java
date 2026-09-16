package Utilities;

import Utilities.Validation.BaseValidation;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.util.Scanner;

public class Inputter {
    private Scanner scanner;

    public Inputter(){
        this.scanner = new Scanner(new InputStreamReader(System.in, StandardCharsets.UTF_8));
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
public String inputAndLoop(String mess, String pattern, boolean loop) {
    String result;

    do {
        result = getString(mess).trim();

        if (!BaseValidation.isValid(result, pattern)) {
            if (loop) {
                System.out.println("Data is invalid! Re-enter.");
            } else {
                return result;
            }
        } else {
            return result;
        }

    } while (loop);

    return result;
}
}
