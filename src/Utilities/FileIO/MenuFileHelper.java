package Utilities.FileIO;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import Core.Entities.SetMenu;

public class MenuFileHelper implements IFileIO<SetMenu> {

    private final String filePath;

    // Constructor nhận đường dẫn -> DAO giữ quyền định nghĩa path (single source of truth)
    public MenuFileHelper(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<SetMenu> readFromFile() throws Exception {
        List<SetMenu> list = new ArrayList<>();
        File f = new File(filePath);
        if (!f.exists()) {
            return list;                       // chưa có file -> rỗng, không ném lỗi
        }
        try (FileInputStream file = new FileInputStream(f);
             BufferedReader myInput = new BufferedReader(
                     new InputStreamReader(file, StandardCharsets.UTF_8))) {

            String line;
            boolean first = true;
            while ((line = myInput.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (first) {                   // bỏ dòng header "Code,Name,Price,Ingredients"
                    first = false;
                    continue;
                }
                if (line.startsWith("\uFEFF")) {        // strip BOM nếu có
                    line = line.substring(1);
                }
                list.add(convertToMenu(line));
            }
        }
        return list;
    }

    @Override
    public boolean saveToFile(List<SetMenu> list) throws Exception {
        File f = new File(filePath);
        try (FileOutputStream fos = new FileOutputStream(f);
             BufferedWriter bw = new BufferedWriter(
                     new OutputStreamWriter(fos, StandardCharsets.UTF_8))) {
            bw.write("Code,Name,Price,Ingredients");
            bw.newLine();
            for (SetMenu s : list) {
                bw.write(s.getMenuID() + "," + s.getMenuName() + ","
                        + s.getPrice() + "," + s.getIngredients());
                bw.newLine();
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private SetMenu convertToMenu(String str) {
        String[] p = str.split(",", -1);
        String code = p[0].trim();
        String name = p[1].trim();
        double price = Double.parseDouble(p[2].trim());
        String ingredients = p.length > 3 ? p[3].trim() : "";
        return new SetMenu(code, name, price, ingredients);
    }
}