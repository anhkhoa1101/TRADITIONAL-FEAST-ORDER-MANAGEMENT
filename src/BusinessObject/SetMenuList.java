package BusinessObject;

import Core.Entities.SetMenu;
import Core.Interfaces.ISetMenuDAO;
import DataObjects.SetMenuDAO;

import java.util.List;

public class SetMenuList {

    private final ISetMenuDAO setMenuDAO;

    public SetMenuList() {
        setMenuDAO = new SetMenuDAO();
    }

    public boolean addSetMenu(SetMenu s) {
        if (s == null || isNullOrEmpty(s.getMenuID()) || isNullOrEmpty(s.getMenuName())) {
            System.out.println("Thông tin set menu không hợp lệ!");
            return false;
        }
        if (s.getPrice() < 0) {
            System.out.println("Giá không hợp lệ!");
            return false;
        }
        if (isExist(s.getMenuID())) {
            System.out.println("Mã set menu đã tồn tại: " + s.getMenuID());
            return false;
        }
        return setMenuDAO.add(s);
    }

    public boolean updateSetMenu(SetMenu s) {
        if (!isExist(s.getMenuID())) {
            System.out.println("Không tìm thấy set menu: " + s.getMenuID());
            return false;
        }
        return setMenuDAO.update(s);
    }

    public boolean deleteSetMenu(String menuID) {
        if (!isExist(menuID)) {
            System.out.println("Không tìm thấy set menu để xóa: " + menuID);
            return false;
        }
        return setMenuDAO.delete(menuID);
    }

    public List<SetMenu> getAllSetMenus() {
        return setMenuDAO.readAll();
    }

    public SetMenu findByID(String menuID) {
        return setMenuDAO.findByID(menuID);
    }

    public boolean isExist(String menuID) {
        return setMenuDAO.findByID(menuID) != null;
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}