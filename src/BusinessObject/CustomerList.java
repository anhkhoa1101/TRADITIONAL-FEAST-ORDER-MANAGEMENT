package BusinessObject;

import Core.Entities.Customer;
import Core.Interfaces.ICustomerDAO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
        return list.stream()
                .sorted(Comparator.comparing(Customer::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public Customer findCustomer(String id) {
        return customerDAO.findByID(id);
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