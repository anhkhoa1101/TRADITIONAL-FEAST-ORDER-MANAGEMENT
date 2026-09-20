package BusinessObject;

import Core.Entities.Customer;
import Core.Entities.Order;
import Core.Entities.SetMenu;
import Core.Interfaces.IOrderDAO;
import DataObjects.OrderDAO;

import java.util.List;

public class OrderList {

    private final IOrderDAO orderDAO;
    private final CustomerList customerList;
    private final SetMenuList setMenuList;

    public OrderList(IOrderDAO orderDAO, CustomerList customerList, SetMenuList setMenuList) {
        this.orderDAO = orderDAO;
        this.customerList = customerList;
        this.setMenuList = setMenuList;
    }

    /**
     * Thêm đơn hàng — kiểm tra Customer và SetMenu (theo object nhúng trong Order) có thực sự tồn tại trong hệ thống không.
     */
    public boolean addOrder(Order o) {
        if (o == null || o.getCustomerID() == null || o.getMenuID() == null) {
            System.out.println("Thông tin đơn hàng không đầy đủ!");
            return false;
        }
        if (isExist(o.getOrderCode())) {
            System.out.println("Mã đơn hàng đã tồn tại: " + o.getOrderCode());
            return false;
        }

        // Kiểm tra khách hàng thực sự tồn tại trong CustomerList (so theo ID)
        Customer realCustomer = customerList.findCustomer(o.getCustomerID().getId());
        if (realCustomer == null) {
            System.out.println("Khách hàng không tồn tại: " + o.getCustomerID().getId());
            return false;
        }

        // Kiểm tra set menu thực sự tồn tại
        SetMenu realMenu = setMenuList.findByID(o.getMenuID().getMenuID());
        if (realMenu == null) {
            System.out.println("Set menu không tồn tại: " + o.getMenuID().getMenuID());
            return false;
        }

        if (o.getNumOfTables() <= 0) {
            System.out.println("Số bàn phải lớn hơn 0!");
            return false;
        }

        // Gán lại object thật từ DB (tránh trường hợp Order được truyền vào chứa Customer/SetMenu "rỗng", chỉ có ID)
        o.setCustomerID(realCustomer);
        o.setMenuID(realMenu);

        return orderDAO.add(o);
    }

    public boolean updateOrder(Order o) {
        if (!isExist(o.getOrderCode())) {
            System.out.println("Không tìm thấy đơn hàng: " + o.getOrderCode());
            return false;
        }
        return orderDAO.update(o);
    }

    public boolean deleteOrder(String orderCode) {
        if (!isExist(orderCode)) {
            System.out.println("Không tìm thấy đơn hàng để xóa: " + orderCode);
            return false;
        }
        return orderDAO.delete(orderCode);
    }

    public List<Order> getAllOrders() {
        return orderDAO.readAll();
    }

    public Order findOrder(String orderCode) {
        return orderDAO.findByID(orderCode);
    }

    public double calcOrderTotal(Order o) {
        if (o == null || o.getMenuID() == null) return 0;
        return o.getMenuID().getPrice() * o.getNumOfTables();
    }

    public boolean isExist(String orderCode) {
        return orderDAO.findByID(orderCode) != null;
    }

    public List<Order> getOrdersByCustomer(String customerID) {
        return orderDAO.findByCustomerID(customerID);
    }

    /**
     * Tính tổng doanh thu = giá SetMenu * số bàn, cộng dồn tất cả đơn hàng.
     */
    public double getTotalRevenue() {
        return orderDAO.readAll().stream()
                .filter(o -> o.getMenuID() != null)
                .mapToDouble(o -> o.getMenuID().getPrice() * o.getNumOfTables())
                .sum();
    }

    public boolean saveToFile() {
        return orderDAO.save();
    }
}