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

import java.util.ArrayList;
import java.util.List;

import java.util.Date;
import java.time.LocalDate;
import java.time.ZoneId;
public class Menu {

    private final Inputter in;
    private final CustomerList customerList;
    private final OrderList orderList;
    private final SetMenuList setMenuList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    {
        dateFormat.setLenient(false);
    }
    private static final String LINE = "---------------------------------------------------------------------------";

    /**
     * CONSTRUCTOR
     */
    public Menu(Inputter in, CustomerList customerList, OrderList orderList, SetMenuList setMenuList) {
        this.in = in;
        this.customerList = customerList;
        this.orderList = orderList;
        this.setMenuList = setMenuList;
    }

    /**
     * RUN
     */
    public void run() {
        int choice;
        do {
            showMenu();
            choice = in.getInt("Select an option: ", "^\\d+$");
            switch (choice) {
                case 1 : customerManagement(); break;
                case 2 : displayFeastMenus(); break;
                case 3 : placeOrder(); break;
                case 4 : updateOrderInfo(); break;
                case 5 : saveData(); break;
                case 6 : displayOrderList(); break;
                case 7 : displayInvoices(); break;
                case 8 : searchInvoicesByCustomer(); break;
                case 9: findOrderMore10();
                case 0 : System.out.println("Goodbye!"); break;
                default: System.out.println("Invalid choice!");
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
                        "8. Search order based customer\n" +
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
     * PRINT TABLE
     */
    private void printCustomerTable(List<Customer> customers) {
        System.out.println("Customers information:");
        System.out.println(LINE);
        System.out.printf("%-6s| %-26s| %-12s| %s%n", "Code", "Customer Name", "Phone", "Email");
        System.out.println(LINE);
        for (Customer c : customers) {
            System.out.printf("%-6s| %-26s| %-12s| %s%n",
                    c.getId(), formatName(c.getName()), c.getPhone(), c.getEmail());
        }
        System.out.println(LINE);
    }

    private void printInvoiceTable(List<Order> orders) {
        System.out.printf("%-15s| %-6s| %-25s| %-6s| %-6s| %-16s| %15s%n",
                "Order Code", "Cus ID", "Customer Name", "Menu", "Tables", "Date", "Total");

        for (Order o : orders) {
            System.out.printf("%-15s| %-6s| %-25s| %-6s| %-6d| %-16s| %,15.0f%n",
                    o.getOrderCode(),
                    o.getCustomerID().getId(),
                    o.getCustomerID().getName(),
                    o.getMenuID().getMenuID(),
                    o.getNumOfTables(),
                    dateFormat.format(o.getEventDate()),
                    orderList.calcOrderTotal(o));
        }
    }



    /**
     * Đọc lựa chọn số nguyên và điều hướng theo bảng case do caller cung cấp.
     * Dùng chung cho run() và customerManagement() để tránh lặp cấu trúc do-while + switch.
     */
    private int readChoice() {
        return in.getInt("Select an option: ", "^\\d+$");
    }


    /**
     * CUSTOMER MANAGEMENT
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
            choice = readChoice();
            switch (choice) {
                case 1 : registerCustomer(); break;
                case 2 : updateCustomerInfo(); break;
                case 3 : searchCustomerByName(); break;
                case 4 : deleteCustomer(); break;
                case 5 : displayCusList(); break;
                case 0 : break;
                default : System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }

    // ===================== Register Customer =====================
    private void registerCustomer() {
        String id = in.inputAndLoop("Enter customer ID (e.g., C0001): ", CusValidation.CUS_ID_VALID, true);
        String name = in.inputAndLoop("Enter customer name: ", CusValidation.NAME_VALID, true);
        String phone = in.inputAndLoop("Enter phone number (10 digits): ", CusValidation.PHONE_VALID, true);
        String email = in.inputAndLoop("Enter email: ", CusValidation.EMAIL_PATTERN, true);

        boolean ok = customerList.addCustomer(new Customer(id, name, phone, email));
        System.out.println(ok ? "Registration successful!" : "Registration failed!");
    }

    // ===================== Update Customer =====================
    private void updateCustomerInfo() {
        String id = in.inputAndLoop("Enter customer ID to update: ", CusValidation.CUS_ID_VALID, true);
        Customer c = customerList.findCustomer(id);
        if (c == null) {
            System.out.println("Customer not found!");
            return;
        }
        c.setName(in.inputAndLoop("New name: ", CusValidation.NAME_VALID, true));
        c.setPhone(in.inputAndLoop("New phone number: ", CusValidation.PHONE_VALID, true));
        c.setEmail(in.getString("New email: "));

        boolean ok = customerList.updateCustomer(c);
        System.out.println(ok ? "Update successful!" : "Update failed!");
    }

    // ===================== DELETE CUSTOMER =====================
    private void deleteCustomer() {
        String id = in.inputAndLoop("Enter customer ID to delete: ", CusValidation.CUS_ID_VALID, true);
        boolean ok = customerList.deleteCustomer(id);
        System.out.println(ok ? "Delete successful!" : "Delete failed!");
    }

    // ===================== SEARCH CUSTOMER =====================
    private void searchCustomerByName() {
        String name = in.getString("Enter name to search: ");
        List<Customer> result = customerList.findByName(name);
        if (result.isEmpty()) {
            System.out.println("No customers found!");
        } else {
            printCustomerTable(result);
        }
    }

    /**
     * DISPLAY
     */
    private void displayOrderList() {
        printList("--- Order list ---", orderList.getAllOrders(), "(Empty)");
    }

    private void displayInvoices() {
        List<Order> orders = orderList.getAllOrders();
        if (orders.isEmpty()) {
            System.out.println("(No orders yet)");
            return;
        }

        System.out.println("--- Invoice list ---");
        System.out.printf("%-15s| %-6s| %-25s| %-6s| %-6s| %-11s| %15s%n",
                "Order Code", "Cus ID", "Customer Name", "Menu", "Tables", "Date", "Total");

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

        System.out.printf("%nTotal revenue (%d orders): %,.0f VND%n",
                orders.size(), orderList.getTotalRevenue());
    }

    private void displayCusList() {
        List<Customer> customers = customerList.getAllCustomers();
        if (customers.isEmpty()) {
            System.out.println("(Empty)");
        } else {
            printCustomerTable(customers);
        }
    }

    private void displayFeastMenus() {
        printList("--- Set menu list ---", setMenuList.getAllSetMenus(), "No set menus available!");
    }


    /**
     * ORDER
     */

    private void findOrderMore10(){
        List<Order> results = orderList.findTableMost10();
        System.out.println("------List order more 10 table------");
        printInvoiceTable(results);
    }
    private void placeOrder() {
        String customerID = in.inputAndLoop("Enter customer ID: ", CusValidation.CUS_ID_VALID, true);
        Customer customer = customerList.findCustomer(customerID);
        if (customer == null) {
            System.out.println("Customer does not exist!");
            return;
        }

        displayFeastMenus();
        String menuID = in.getString("Enter the set menu ID to order: ");
        SetMenu menu = setMenuList.findByID(menuID);
        if (menu == null) {
            System.out.println("Set menu does not exist!");
            return;
        }

        int numOfTables = in.getInt("Enter number of tables: ", OrderValidation.NUM_TABLES_VALID);

        Date eventDate;
        String dateStr = in.inputAndLoop("Enter event date (dd/MM/yyyy): ", OrderValidation.DATE_VALID, true);
        try {
            eventDate = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            System.out.println("Invalid date!");
            return;
        }

        Order order = new Order(customer, menu, numOfTables, eventDate);

        boolean ok = orderList.addOrder(order);
        if (ok) {
            double total = menu.getPrice() * numOfTables;
            System.out.printf("Order placed successfully! Order code: %s - Total: %,.0f VND%n", order.getOrderCode(), total);
        } else {
            System.out.println("Failed to place order!");
        }
    }

    /**
     * UPDATE
     */
    private void updateOrderInfo() {
        displayOrderList();
        String orderCode = in.getString("Enter order code to update: ");
        Order o = orderList.findOrder(orderCode);
        if (o == null) {
            System.out.println("Order not found!");
            return;
        }

        // Không cho sửa nếu đã đến (hoặc qua) ngày tổ chức tiệc
        int numOfTables = in.getInt("New number of tables: ", OrderValidation.NUM_TABLES_VALID);

        // Nhập ngày giờ tổ chức mới, phải sau hôm nay
        Date newDate = null;
        while (newDate == null) {
            String dateStr = in.inputAndLoop("New event date (dd/MM/yyyy): ",
                    OrderValidation.DATE_VALID, true);
            try {
                Date d = dateFormat.parse(dateStr);
                if (toLocalDate(d).isAfter(LocalDate.now())) {
                    newDate = d;
                } else if (!toLocalDate(o.getEventDate()).isAfter(LocalDate.now())) {
                    System.out.println("Cannot update: the event date has been reached or passed ("
                            + dateFormat.format(o.getEventDate()) + ")!");
                    return;
                }
                else {
                    System.out.println("The new event date must be after today!");
                }
            } catch (ParseException e) {
                System.out.println("Invalid date!");
            }
        }

        SetMenu newMenu = null;
        String changeMenu = in.getString("Change set menu? (y/n): ");
        if (changeMenu.equalsIgnoreCase("y")) {
            displayFeastMenus();
            String menuID = in.getString("Enter new set menu ID: ");
            newMenu = setMenuList.findByID(menuID);
            if (newMenu == null) {
                System.out.println("Set menu does not exist!");
                return; // chưa sửa gì vào o
            }
        }

        // Mọi thứ hợp lệ -> mới áp dụng thay đổi
        o.setNumOfTables(numOfTables);
        o.setEventDate(newDate);
        if (newMenu != null) o.setMenuID(newMenu);

        boolean ok = orderList.updateOrder(o);
        System.out.println(ok ? "Update successful!" : "Update failed!");
    }


    /**
     * SEARCH TABLE
     */
    private void searchInvoicesByCustomer() {
        String id = in.getString("Enter customer ID to search invoices: ").trim();
        if (id.isEmpty()) {
            System.out.println("Customer ID must not be empty!");
            return;
        }

        Customer customer = customerList.findCustomer(id);
        // Gom hóa đơn của tất cả khách có tên khớp //
        List<Order> orders = orderList.getOrdersByCustomer(customer.getId());
        if (orders.isEmpty()) {
            System.out.println("No invoices found for customer \"" + id + "\".");
            return;
        }

        System.out.println("--- Invoices of customer \"" + id + "\" ---");
        printInvoiceTable(orders);

        double total = 0;
        for (Order o : orders) {
            total += orderList.calcOrderTotal(o);
        }
        System.out.printf("%nTotal amount (%d orders): %,.0f VND%n", orders.size(), total);
    }

    private String formatName(String fullName) {
        if (fullName == null) return "";
        String name = fullName.trim().replaceAll("\\s+", " ");
        int lastSpace = name.lastIndexOf(' ');
        if (lastSpace < 0) return name;
        String firstName = name.substring(lastSpace + 1);
        String rest = name.substring(0, lastSpace);
        return firstName + ", " + rest;
    }

    // Đổi Date -> LocalDate (theo múi giờ máy) để so sánh theo NGÀY, bỏ qua giờ phút
    private LocalDate toLocalDate(Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // ===================== SAVE =====================
    private void saveData() {
        boolean customerOk = customerList.saveToFile();
        boolean orderOk = orderList.saveToFile();
        System.out.println(customerOk && orderOk
                ? "Data saved successfully!"
                : "Failed to save data!");
    }
}