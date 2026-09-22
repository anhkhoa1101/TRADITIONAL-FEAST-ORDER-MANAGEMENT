package BusinessObject;

import Core.Entities.SetMenu;
import Core.Interfaces.ISetMenuDAO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SetMenuList {

    private final ISetMenuDAO setMenuDAO;

    public SetMenuList(ISetMenuDAO setMenuDAO) {
        this.setMenuDAO = setMenuDAO;
    }

    /**
     * Trả về danh sách set menu, sắp xếp tăng dần theo giá (đúng yêu cầu Function 4).
     */
    public List<SetMenu> getAllSetMenus() {
        return setMenuDAO.readAll().stream()
                .sorted(Comparator.comparingDouble(SetMenu::getPrice))
                .collect(Collectors.toList());
    }

    public SetMenu findByID(String menuID) {
        return setMenuDAO.findByID(menuID);
    }
}