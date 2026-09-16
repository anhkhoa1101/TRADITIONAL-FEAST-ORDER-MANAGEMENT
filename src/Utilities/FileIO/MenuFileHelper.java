package Utilities.FileIO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import Presentation.Menu;

import Core.Entities.SetMenu;
/**
 *
 * @author NguyenPhuc
 */
public class MenuFileHelper implements IFileIO<SetMenu> {

    private final String FILE_NAME = "src\\fileio\\FeastMenu.csv";

    @Override
    public List<SetMenu> readFromFile() throws Exception {
        List<SetMenu> list = new ArrayList<>();
        File f;
        FileInputStream file = null;
        BufferedReader myInput = null;// create Buffer
        try {
            f = new File(FILE_NAME);//open file
            String fullPath = f.getAbsolutePath(); //get Fullpath of file
            file = new FileInputStream(fullPath);
            myInput = new BufferedReader(new InputStreamReader(file));
            // read line until the end of the file
            String line = null;
            boolean first = true;
            while ((line = myInput.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (first) {
                    first = false;
                    continue;
                }
                SetMenu item = convertToMenu(line);
                list.add(item);
            }

        } catch (IOException ex) {
            throw ex;
        } finally {
            if (myInput != null) {
                myInput.close();
            }
            if (file != null) {
                file.close();
            }
        }
        return list;
    }

    @Override
    public boolean saveToFile(List<SetMenu> list) throws Exception {

        return true;
    }

    private SetMenu convertToMenu(String str) {
        SetMenu item = null;

        String[] p = str.split(",");
        String code = p[0].trim();
        String name = p[1].trim();
        double price = Double.parseDouble(p[2].trim());
        String ingredients = p[3].trim();
        item = new SetMenu(code, name, price, ingredients);
        return item;
    }
}
