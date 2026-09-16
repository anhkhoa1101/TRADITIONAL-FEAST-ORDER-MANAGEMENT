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
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public Menu() {
        in = new Inputter();
        customerList = new CustomerList();
        orderList = new OrderList();
        setMenuList = new SetMenuList();
    }

    public void run() {
        int choice;
        do {
            showMenu();
            choice = in.getInt("Chọn chức năng: ", "^\\d+$");
            switch (choice) {
                case 1 : registerCustomer();
                case 2 : updateCustomerInfo();
                case 3 : searchCustomerByName();
                case 4 : displayFeastMenus();
                case 5 : placeOrder();
                case 6 : updateOrderInfo();
                case 7 : saveData();
                case 8 : displayLists();
                case 0 : System.out.println("Tạm biệt!");
                default : System.out.println("Lựa chọn không hợp lệ!");
            }
        } while (choice != 0);
    }

    private void showMenu() {
        System.out.println(
                "===== TRADITIONAL FEAST ORDER MANAGEMENT =====\n" +
                        "1. Register customers.\n" +
                        "2. Update customer information.\n" +
                        "3. Search for customer information by name.\n" +
                        "4. Display feast menus.\n" +
                        "5. Place a feast order.\n" +
                        "6. Update order information.\n" +
                        "7. Save data to file.\n" +
                        "8. Display Customer or Order lists.\n" +
                        "0. Quit."
        );
    }

    // ----- Chức năng 1 -----
    private void registerCustomer() {
        String id = in.inputAndLoop("Nhập mã KH (VD: C0001): ", CusValidation.CUS_ID_VALID, true);
        String name = in.inputAndLoop("Nhập tên KH: ", CusValidation.NAME_VALID, true);
        String phone = in.inputAndLoop("Nhập SĐT (10 số): ", CusValidation.PHONE_VALID, true);
        String email = in.getString("Nhập email: ");

        boolean ok = customerList.addCustomer(new Customer(id, name, phone, email));
        System.out.println(ok ? "Đăng ký thành công!" : "Đăng ký thất bại!");
    }

    // ----- Chức năng 2 -----
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

    // ----- Chức năng 3 -----
    private void searchCustomerByName() {
        String name = in.getString("Nhập tên cần tìm: ");
        List<Customer> result = customerList.searchByName(name);
        if (result.isEmpty()) {
            System.out.println("Không tìm thấy khách hàng nào!");
        } else {
            result.forEach(System.out::println);
        }
    }

    // ----- Chức năng 4 -----
    private void displayFeastMenus() {
        List<SetMenu> menus = setMenuList.getAllSetMenus();
        if (menus.isEmpty()) {
            System.out.println("Chưa có set menu nào!");
        } else {
            menus.forEach(System.out::println);
        }
    }

    // ----- Chức năng 5 -----
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
        String dateStr = in.inputAndLoop("Nhập ngày tổ chức (dd/MM/yyyy): ", OrderValidation.DATE_VALID, true);
        try {
            eventDate = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            System.out.println("Ngày không hợp lệ!");
            return;
        }

        // orderCode để null/rỗng -> Order tự sinh mã bằng generateOrderCode()
        Order order = new Order(null, customer, province, menu, numOfTables, eventDate);

        boolean ok = orderList.addOrder(order);
        if (ok) {
            double total = menu.getPrice() * numOfTables;
            System.out.println("Đặt tiệc thành công! Mã đơn: " + order.getOrderCode() + " - Tổng tiền: " + total);
        } else {
            System.out.println("Đặt tiệc thất bại!");
        }
    }

    // ----- Chức năng 6 -----
    private void updateOrderInfo() {
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

    // ----- Chức năng 7 -----
    private void saveData() {
        System.out.println("Dữ liệu đã được lưu tự động sau mỗi thao tác add/update/delete.");
    }

    // ----- Chức năng 8 -----
    private void displayLists() {
        System.out.println("--- Danh sách khách hàng ---");
        List<Customer> customers = customerList.getAllCustomers();
        if (customers.isEmpty()) System.out.println("(Trống)");
        else customers.forEach(System.out::println);

        System.out.println("--- Danh sách đơn hàng ---");
        List<Order> orders = orderList.getAllOrders();
        if (orders.isEmpty()) System.out.println("(Trống)");
        else orders.forEach(System.out::println);
    }
}