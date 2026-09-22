package Presentation;

import BusinessObject.CustomerList;
import BusinessObject.OrderList;
import BusinessObject.SetMenuList;

import Core.Entities.Customer;
import Core.Entities.Order;
import Core.Entities.SetMenu;

import Utilities.Inputter;
import Utilities.Validation.CusValidation;
import Utilities.Validation.OrderValidation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Menu {

    private final Inputter in;
    private final CustomerList customerList;
    private final OrderList orderList;
    private final SetMenuList setMenuList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public Menu(Inputter in, CustomerList customerList, OrderList orderList, SetMenuList setMenuList) {
        this.in = in;
        this.customerList = customerList;
        this.orderList = orderList;
        this.setMenuList = setMenuList;
    }

    public void run() {
        int choice;
        do {
            showMenu();
            choice = in.getInt("Chọn chức năng: ", "^\\d+$");
            switch (choice) {
                case 1 : customerManagement(); break;
                case 2 : displayFeastMenus(); break;
                case 3 : placeOrder(); break;
                case 4 : updateOrderInfo(); break;
                case 5 : saveData(); break;
                case 6 : displayOrderList(); break;
                case 7 : displayInvoices(); break;
                case 0 : System.out.println("Tạm biệt!"); break;
                default : System.out.println("Lựa chọn không hợp lệ!");
            }
        } while (choice != 0);
    }

    private void showMenu() {
        System.out.println(
                "===== TRADITIONAL FEAST ORDER MANAGEMENT =====\n" +
                        "1. Customer Management.\n" +
                        "2. Display feast menus.\n" +
                        "3. Place a feast order.\n" +
                        "4. Update order information.\n" +
                        "5. Save data to file.\n" +
                        "6. Display order list.\n" +
                        "7. Display invoice list.\n" +
                        "0. Quit."
        );
    }

    // ===================== HELPER DÙNG CHUNG =====================

    /**
     * In danh sách bất kỳ ra console theo khuôn chung: tiêu đề -> nội dung / thông báo rỗng.
     * Gộp lại để tránh lặp code giữa displayCusList / displayFeastMenus / displayOrderList.
     */
    private <T> void printList(String header, List<T> list, String emptyMessage) {
        System.out.println(header);
        if (list.isEmpty()) {
            System.out.println(emptyMessage);
        } else {
            list.forEach(System.out::println);
        }
    }

    /**
     * Đọc lựa chọn số nguyên và điều hướng theo bảng case do caller cung cấp.
     * Dùng chung cho run() và customerManagement() để tránh lặp cấu trúc do-while + switch.
     */
    private int readChoice() {
        return in.getInt("Chọn chức năng: ", "^\\d+$");
    }

    // ===================== CUSTOMER MANAGEMENT =====================

    private void customerManagement() {
        int choice;
        do {
            System.out.println(
                    "----- CUSTOMER MANAGEMENT -----\n" +
                            "1. Register customer.\n" +
                            "2. Update customer information.\n" +
                            "3. Search customer by name.\n" +
                            "4. Delete customer.\n" +
                            "5. Display customer list.\n" +
                            "0. Back to main menu."
            );
            choice = readChoice();
            switch (choice) {
                case 1 : registerCustomer(); break;
                case 2 : updateCustomerInfo(); break;
                case 3 : searchCustomerByName(); break;
                case 4 : deleteCustomer(); break;
                case 5 : displayCusList(); break;
                case 0 : break;
                default : System.out.println("Lựa chọn không hợp lệ!");
            }
        } while (choice != 0);
    }

    private void registerCustomer() {
        String id = in.inputAndLoop("Nhập mã KH (VD: C0001): ", CusValidation.CUS_ID_VALID, true);
        String name = in.inputAndLoop("Nhập tên KH: ", CusValidation.NAME_VALID, true);
        String phone = in.inputAndLoop("Nhập SĐT (10 số): ", CusValidation.PHONE_VALID, true);
        String email = in.inputAndLoop("Nhập email: ", CusValidation.EMAIL_PATTERN, true);

        boolean ok = customerList.addCustomer(new Customer(id, name, phone, email));
        System.out.println(ok ? "Đăng ký thành công!" : "Đăng ký thất bại!");
    }

    private void updateCustomerInfo() {
        String id = in.inputAndLoop("Nhập mã KH cần cập nhật: ", CusValidation.CUS_ID_VALID, true);
        Customer c = customerList.findCustomer(id);
        if (c == null) {
            System.out.println("Không tìm thấy khách hàng!");
            return;
        }
        c.setName(in.inputAndLoop("Tên mới: ", CusValidation.NAME_VALID, true));
        c.setPhone(in.inputAndLoop("SĐT mới: ", CusValidation.PHONE_VALID, true));
        c.setEmail(in.getString("Email mới: "));

        boolean ok = customerList.updateCustomer(c);
        System.out.println(ok ? "Cập nhật thành công!" : "Cập nhật thất bại!");
    }

    private void searchCustomerByName() {
        String name = in.getString("Nhập tên cần tìm: ");
        printList("--- Kết quả tìm kiếm ---",
                customerList.searchByName(name),
                "Không tìm thấy khách hàng nào!");
    }

    private void deleteCustomer() {
        String id = in.inputAndLoop("Nhập mã KH cần xóa: ", CusValidation.CUS_ID_VALID, true);
        boolean ok = customerList.deleteCustomer(id);
        System.out.println(ok ? "Xóa thành công!" : "Xóa thất bại!");
    }

    private void displayCusList() {
        printList("--- Danh sách khách hàng ---", customerList.getAllCustomers(), "(Trống)");
    }

    // ===================== FEAST MENU =====================

    private void displayFeastMenus() {
        printList("--- Danh sách set menu ---", setMenuList.getAllSetMenus(), "Chưa có set menu nào!");
    }

    // ===================== ORDER =====================

    private void placeOrder() {
        String customerID = in.inputAndLoop("Nhập mã KH: ", CusValidation.CUS_ID_VALID, true);
        Customer customer = customerList.findCustomer(customerID);
        if (customer == null) {
            System.out.println("Khách hàng không tồn tại!");
            return;
        }

        displayFeastMenus();
        String menuID = in.getString("Nhập mã set menu muốn đặt: ");
        SetMenu menu = setMenuList.findByID(menuID);
        if (menu == null) {
            System.out.println("Set menu không tồn tại!");
            return;
        }

        String province = in.inputAndLoop("Nhập tỉnh/thành tổ chức: ", OrderValidation.PROVINCE_VALID, true);
        int numOfTables = in.getInt("Nhập số bàn: ", OrderValidation.NUM_TABLES_VALID);

        Date eventDate;
        String dateStr = in.inputAndLoop("Nhập ngày giờ tổ chức (dd/MM/yyyy HH:mm): ", OrderValidation.DATE_VALID, true);
        try {
            eventDate = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            System.out.println("Ngày không hợp lệ!");
            return;
        }

        Order order = new Order(customer, province, menu, numOfTables, eventDate);

        boolean ok = orderList.addOrder(order);
        if (ok) {
            double total = menu.getPrice() * numOfTables;
            System.out.printf("Đặt tiệc thành công! Mã đơn: %s - Tổng tiền: %,.0f VNĐ%n", order.getOrderCode(), total);
        } else {
            System.out.println("Đặt tiệc thất bại!");
        }
    }

    private void updateOrderInfo() {
        displayOrderList();
        String orderCode = in.getString("Nhập mã đơn cần cập nhật: ");
        Order o = orderList.findOrder(orderCode);
        if (o == null) {
            System.out.println("Không tìm thấy đơn hàng!");
            return;
        }

        String province = in.inputAndLoop("Tỉnh/thành mới: ", OrderValidation.PROVINCE_VALID, true);
        int numOfTables = in.getInt("Số bàn mới: ", OrderValidation.NUM_TABLES_VALID);

        String changeMenu = in.getString("Đổi set menu? (y/n): ");
        if (changeMenu.equalsIgnoreCase("y")) {
            displayFeastMenus();
            String menuID = in.getString("Nhập mã set menu mới: ");
            SetMenu menu = setMenuList.findByID(menuID);
            if (menu == null) {
                System.out.println("Set menu không tồn tại!");
                return;
            }
            o.setMenuID(menu);
        }

        o.setProvince(province);
        o.setNumOfTables(numOfTables);

        boolean ok = orderList.updateOrder(o);
        System.out.println(ok ? "Cập nhật thành công!" : "Cập nhật thất bại!");
    }

    private void displayOrderList() {
        printList("--- Danh sách đơn hàng ---", orderList.getAllOrders(), "(Trống)");
    }

    private void displayInvoices() {
        List<Order> orders = orderList.getAllOrders();
        if (orders.isEmpty()) {
            System.out.println("(Chưa có đơn hàng nào)");
            return;
        }

        System.out.println("--- Danh sách hóa đơn ---");
        System.out.printf("%-15s| %-6s| %-25s| %-6s| %-6s| %-16s| %15s%n",
                "Mã đơn", "Mã KH", "Tên KH", "Menu", "Số bàn", "Ngày", "Thành tiền");

        for (Order o : orders) {
            System.out.printf("%-15s| %-6s| %-25s| %-6s| %-6d| %-11s| %,15.0f%n",
                    o.getOrderCode(),
                    o.getCustomerID().getId(),
                    o.getCustomerID().getName(),
                    o.getMenuID().getMenuID(),
                    o.getNumOfTables(),
                    dateFormat.format(o.getEventDate()),
                    orderList.calcOrderTotal(o));
        }

        System.out.printf("%nTổng doanh thu (%d đơn): %,.0f VNĐ%n",
                orders.size(), orderList.getTotalRevenue());
    }

    // ===================== SAVE =====================

    private void saveData() {
        boolean customerOk = customerList.saveToFile();
        boolean orderOk = orderList.saveToFile();
        System.out.println(customerOk && orderOk
                ? "Lưu dữ liệu thành công!"
                : "Lưu dữ liệu thất bại!");
    }
}