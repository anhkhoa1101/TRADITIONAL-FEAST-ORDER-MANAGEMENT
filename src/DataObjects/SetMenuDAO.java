package DataObjects;

import Core.Entities.SetMenu;
import Core.Interfaces.ISetMenuDAO;
import Utilities.FileIO.IFileIO;
import Utilities.FileIO.MenuFileHelper;
import java.util.ArrayList;
import java.util.List;

public class SetMenuDAO implements ISetMenuDAO {

    private static final String FILE_NAME = "src/DataObjects/Data/FeastMenu.csv";
    private final IFileIO<SetMenu> fileIO;
    private List<SetMenu> menuList;

    public SetMenuDAO() {
        fileIO = new MenuFileHelper(FILE_NAME);
        menuList = loadFromFile();              // Nạp file 1 lần lúc khởi động
    }

    // MỚI: đọc file -> RAM (chỉ dùng trong constructor)
    private List<SetMenu> loadFromFile() {
        try {
            return fileIO.readFromFile();
        } catch (Exception e) {
            System.out.println("Lỗi đọc file: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // SỬA: trả về list đang ở RAM, không đọc file
    @Override
    public List<SetMenu> readAll() {
        return new ArrayList<>(menuList);
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

    // MỚI: bắt buộc vì IBaseDAO có save() (Menu không gọi cái này)
    @Override
    public boolean save() {
        return writeAll(menuList);
    }

    @Override
    public boolean add(SetMenu s) {
        return menuList.add(s);                 // SỬA: chỉ RAM
    }

    @Override
    public boolean update(SetMenu s) {
        for (int i = 0; i < menuList.size(); i++) {
            if (menuList.get(i).getMenuID().equals(s.getMenuID())) {
                menuList.set(i, s);
                return true;                    // SỬA: chỉ RAM
            }
        }
        return false;
    }

    @Override
    public boolean delete(String menuID) {
        return menuList.removeIf(s -> s.getMenuID().equals(menuID));   // SỬA: chỉ RAM
    }

    @Override
    public SetMenu findByID(String menuID) {
        return menuList.stream().filter(s -> s.getMenuID().equals(menuID)).findFirst().orElse(null);
    }
}