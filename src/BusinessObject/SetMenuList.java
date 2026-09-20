package BusinessObject;

import Core.Entities.SetMenu;
import Core.Interfaces.ISetMenuDAO;

import java.util.List;

public class SetMenuList {

    private final ISetMenuDAO setMenuDAO;

    public SetMenuList(ISetMenuDAO setMenuDAO) {
        this.setMenuDAO = setMenuDAO;
    }


    public List<SetMenu> getAllSetMenus() {
        return setMenuDAO.readAll();
    }

    public SetMenu findByID(String menuID) {
        return setMenuDAO.findByID(menuID);
    }

//    public boolean isExist(String menuID) {
//        return setMenuDAO.findByID(menuID) != null;
//    }
//
//    private boolean isNullOrEmpty(String s) {
//        return s == null || s.trim().isEmpty();
//    }
}