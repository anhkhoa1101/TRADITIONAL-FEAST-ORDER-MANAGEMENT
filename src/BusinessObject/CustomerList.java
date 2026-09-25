package BusinessObject;

import Core.Entities.Customer;
import Core.Interfaces.ICustomerDAO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.text.Collator;
import java.util.Locale;

public class CustomerList {

    private final ICustomerDAO customerDAO;

    public CustomerList(ICustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    public boolean addCustomer(Customer c) {
        if (c == null || isNullOrEmpty(c.getId()) || isNullOrEmpty(c.getName())) {
            System.out.println("Thông tin khách hàng không hợp lệ!");
            return false;
        }
        if (isExist(c.getId())) {
            System.out.println("Mã khách hàng đã tồn tại: " + c.getId());
            return false;
        }
        return customerDAO.add(c);
    }

    public boolean updateCustomer(Customer c) {
        if (!isExist(c.getId())) {
            System.out.println("Không tìm thấy khách hàng: " + c.getId());
            return false;
        }
        return customerDAO.update(c);
    }

    public boolean deleteCustomer(String id) {
        if (!isExist(id)) {
            System.out.println("Không tìm thấy khách hàng để xóa: " + id);
            return false;
        }
        return customerDAO.delete(id);
    }

    /**
     * Tìm theo tên (chấp nhận khớp một phần), kết quả sắp xếp alphabet (Function 3).
     */
    public List<Customer> searchByName(String name) {
        return sortByName(customerDAO.findByName(name));
    }

    /**
     * Toàn bộ danh sách khách hàng, sắp xếp alphabet theo tên (Function 8).
     */
    public List<Customer> getAllCustomers() {
        return sortByName(customerDAO.readAll());
    }

    private List<Customer> sortByName(List<Customer> list) {
        Collator collator = Collator.getInstance(new Locale("vi", "VN"));
        return list.stream()
                .sorted(Comparator.comparing(
                        (Customer c) -> getGivenName(c.getName()),
                        collator::compare))
                .collect(Collectors.toList());
    }

    public Customer findCustomer(String id) {
        return customerDAO.findByID(id);
    }
    private static String getGivenName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "";
        String[] parts = fullName.trim().split("\\s+");
        return parts[parts.length - 1];
    }

    public boolean isExist(String id) {
        return customerDAO.findByID(id) != null;
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public boolean saveToFile() {
        return customerDAO.save();
    }


}