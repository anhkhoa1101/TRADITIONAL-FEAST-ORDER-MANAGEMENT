package DataObjects;

import Core.Entities.SetMenu;
import Core.Interfaces.ISetMenuDAO;
import Utilities.FileIO.IFileIO;            // <- dùng interface (giảm coupling)
import Utilities.FileIO.MenuFileHelper;
import java.util.ArrayList;
import java.util.List;

public class SetMenuDAO implements ISetMenuDAO {

    private static final String FILE_NAME = "src/DataObjects/data/FeastMenu.csv";
    private final IFileIO<SetMenu> fileIO;    // <- has-a interface, đúng helper CSV
    private List<SetMenu> menuList;

    public SetMenuDAO() {
        fileIO = new MenuFileHelper(FILE_NAME);  // <- đúng: đọc/ghi CSV văn bản
        menuList = readAll();
    }

    @Override
    public List<SetMenu> readAll() {
        try {
            return fileIO.readFromFile();
        } catch (Exception e) {
            System.out.println("Lỗi đọc file: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean writeAll(List<SetMenu> list) {
        try {
            return fileIO.saveToFile(list);
        } catch (Exception e) {
            System.out.println("Lỗi ghi file: " + e.getMessage());
            return false;
        }
    }

    // add / update / delete / findByID GIỮ NGUYÊN (chỉ đổi kiểu field bên trên)
    @Override public boolean add(SetMenu s) { menuList.add(s); return writeAll(menuList); }

    @Override public boolean update(SetMenu s) {
        for (int i = 0; i < menuList.size(); i++) {
            if (menuList.get(i).getMenuID().equals(s.getMenuID())) {
                menuList.set(i, s); return writeAll(menuList);
            }
        }
        return false;
    }

    @Override public boolean delete(String menuID) {
        boolean removed = menuList.removeIf(s -> s.getMenuID().equals(menuID));
        return removed && writeAll(menuList);
    }

    @Override public SetMenu findByID(String menuID) {
        return menuList.stream().filter(s -> s.getMenuID().equals(menuID)).findFirst().orElse(null);
    }
}