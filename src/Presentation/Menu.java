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

/**
 * Presentation.Menu — tầng giao diện console (entry point tương tác với người dùng).
 * Chỉ nhận input, gọi xuống BusinessObject (CustomerList/OrderList/SetMenuList) và in kết quả ra màn hình.
 * Không tự xử lý logic nghiệp vụ (validate, tính toán) và không tự đọc/ghi file (đã ủy quyền cho DAO ở tầng dưới).
 */
public class Menu {

    private final Inputter in;
    private final CustomerList customerList;
    private final OrderList orderList;
    private final SetMenuList setMenuList;
    // Format ngày dùng chung cho nhập/hiển thị eventDate của Order
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public Menu(Inputter in, CustomerList customerList, OrderList orderList, SetMenuList setMenuList) {
        this.in = in;
        this.customerList = customerList;
        this.orderList = orderList;
        this.setMenuList = setMenuList;
    }

    /**
     * Vòng lặp chính của chương trình — hiển thị menu, đọc lựa chọn, điều hướng tới chức năng tương ứng.
     * Thoát khi người dùng chọn 0.
     */
    public void run() {
        int choice;
        do {
            showMenu();
            choice = in.getInt("Chọn chức năng: ", "^\\d+$");
            switch (choice) {
                case 1 : customerManagement(); break;   // vào submenu quản lý khách hàng
                case 2 : displayFeastMenus(); break;
                case 3 : placeOrder(); break;
                case 4 : updateOrderInfo(); break;
                case 5 : saveData(); break;
                case 6 : displayOrderList(); break;
                case 0 : System.out.println("Tạm biệt!");
                default : System.out.println("Lựa chọn không hợp lệ!");
            }
        } while (choice != 0);
    }

    // In menu chính ra console
    private void showMenu() {
        System.out.println(
                "===== TRADITIONAL FEAST ORDER MANAGEMENT =====\n" +
                        "1. Customer Management.\n" +
                        "2. Display feast menus.\n" +
                        "3. Place a feast order.\n" +
                        "4. Update order information.\n" +
                        "5. Save data to file.\n" +
                        "6. Display order list.\n" +
                        "0. Quit."
        );
    }

    // ===================== CUSTOMER MANAGEMENT (submenu) =====================

    /**
     * Submenu riêng cho các thao tác liên quan Customer (đăng ký, sửa, tìm, xóa, xem danh sách).
     * Tách khỏi menu chính để gom nhóm chức năng theo entity, tránh menu chính quá dài.
     * Chọn 0 để quay lại menu chính (không thoát chương trình).
     */
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
            choice = in.getInt("Chọn chức năng: ", "^\\d+$");
            switch (choice) {
                case 1 : registerCustomer(); break;
                case 2 : updateCustomerInfo(); break;
                case 3 : searchCustomerByName(); break;
                case 4 : deleteCustomer(); break;
                case 5 : displayCusList(); break;
                case 0 : break; // quay lại run(), không in gì thêm
                default : System.out.println("Lựa chọn không hợp lệ!");
            }
        } while (choice != 0);
    }

    // Đăng ký khách hàng mới — validate từng trường bằng regex trong CusValidation trước khi tạo Customer.
    private void registerCustomer() {
        String id = in.inputAndLoop("Nhập mã KH (VD: C0001): ", CusValidation.CUS_ID_VALID, true);
        String name = in.inputAndLoop("Nhập tên KH: ", CusValidation.NAME_VALID, true);
        String phone = in.inputAndLoop("Nhập SĐT (10 số): ", CusValidation.PHONE_VALID, true);
        String email = in.getString("Nhập email: "); // email không validate theo pattern, nhận tự do

        boolean ok = customerList.addCustomer(new Customer(id, name, phone, email));
        System.out.println(ok ? "Đăng ký thành công!" : "Đăng ký thất bại!");
    }

    // Cập nhật thông tin khách hàng theo ID — tìm object thật trước, sửa trực tiếp trên object rồi mới update.
    private void updateCustomerInfo() {
        String id = in.inputAndLoop("Nhập mã KH cần cập nhật: ", CusValidation.CUS_ID_VALID, true);
        Customer c = customerList.findCustomer(id);
        if (c == null) {
            System.out.println("Không tìm thấy khách hàng!");
            return; // dừng sớm nếu không tồn tại, không hỏi thêm thông tin
        }
        c.setName(in.inputAndLoop("Tên mới: ", CusValidation.NAME_VALID, true));
        c.setPhone(in.inputAndLoop("SĐT mới: ", CusValidation.PHONE_VALID, true));
        c.setEmail(in.getString("Email mới: "));

        boolean ok = customerList.updateCustomer(c);
        System.out.println(ok ? "Cập nhật thành công!" : "Cập nhật thất bại!");
    }

    // Tìm khách hàng theo tên (chấp nhận khớp một phần, không phân biệt hoa/thường — xem CustomerDAO.findByName).
    private void searchCustomerByName() {
        String name = in.getString("Nhập tên cần tìm: ");
        List<Customer> result = customerList.searchByName(name);
        if (result.isEmpty()) {
            System.out.println("Không tìm thấy khách hàng nào!");
        } else {
            result.forEach(System.out::println);
        }
    }

    // Xóa khách hàng theo ID.
    private void deleteCustomer() {
        String id = in.inputAndLoop("Nhập mã KH cần xóa: ", CusValidation.CUS_ID_VALID, true);
        boolean ok = customerList.deleteCustomer(id);
        System.out.println(ok ? "Xóa thành công!" : "Xóa thất bại!");
    }

    // Hiển thị toàn bộ danh sách khách hàng hiện có.
    private void displayCusList() {
        System.out.println("--- Danh sách khách hàng ---");
        List<Customer> customers = customerList.getAllCustomers();
        if (customers.isEmpty()) System.out.println("(Trống)");
        else customers.forEach(System.out::println);
    }

    // ===================== FEAST MENU =====================

    // Hiển thị danh sách các set menu tiệc hiện có (đọc từ FeastMenu.csv qua SetMenuDAO).
    private void displayFeastMenus() {
        List<SetMenu> menus = setMenuList.getAllSetMenus();
        if (menus.isEmpty()) {
            System.out.println("Chưa có set menu nào!");
        } else {
            menus.forEach(System.out::println);
        }
    }

    // ===================== ORDER =====================

    /**
     * Đặt tiệc mới:
     * 1) Kiểm tra khách hàng tồn tại.
     * 2) Hiển thị menu để người dùng chọn set menu, kiểm tra menu tồn tại.
     * 3) Nhập tỉnh/thành, số bàn, ngày tổ chức (parse theo dd/MM/yyyy).
     * 4) Tạo Order (orderCode = null -> Order tự sinh mã) và gọi orderList.addOrder()
     *    (bên trong addOrder sẽ tự lấy lại object Customer/SetMenu thật để tránh dữ liệu rỗng).
     */
    private void placeOrder() {
        String customerID = in.inputAndLoop("Nhập mã KH: ", CusValidation.CUS_ID_VALID, true);
        Customer customer = customerList.findCustomer(customerID);
        if (customer == null) {
            System.out.println("Khách hàng không tồn tại!");
            return;
        }

        displayFeastMenus(); // hiển thị menu để người dùng dễ chọn mã
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

        // orderCode truyền null vì Order tự sinh mã trong constructor (generateOrderCode())
        Order order = new Order(null, customer, province, menu, numOfTables, eventDate);

        boolean ok = orderList.addOrder(order);
        if (ok) {
            double total = menu.getPrice() * numOfTables; // tổng tiền = giá menu * số bàn
            System.out.printf("Đặt tiệc thành công! Mã đơn: %s - Tổng tiền: %,.0f VNĐ%n", order.getOrderCode(), total);
        } else {
            System.out.println("Đặt tiệc thất bại!");
        }
    }

    /**
     * Cập nhật đơn hàng theo mã đơn.
     * Cho phép đổi tỉnh/thành, số bàn, và tùy chọn đổi set menu (hỏi y/n trước khi hiển thị lại danh sách menu).
     */
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
                return; // dừng, không cập nhật gì nếu menu mới không hợp lệ
            }
            o.setMenuID(menu);
        }

        o.setProvince(province);
        o.setNumOfTables(numOfTables);

        boolean ok = orderList.updateOrder(o);
        System.out.println(ok ? "Cập nhật thành công!" : "Cập nhật thất bại!");
    }

    // Hiển thị toàn bộ danh sách đơn hàng hiện có.
    private void displayOrderList() {
        System.out.println("--- Danh sách đơn hàng ---");
        List<Order> orders = orderList.getAllOrders();
        if (orders.isEmpty()) System.out.println("(Trống)");
        else orders.forEach(System.out::println);
    }

    // ===================== SAVE =====================

    // Chỉ mang tính thông báo — dữ liệu thực tế đã được ghi xuống file ngay sau mỗi add/update/delete (xem DAO).
    private void saveData() {
        System.out.println("Dữ liệu đã được lưu tự động sau mỗi thao tác add/update/delete.");
    }
}