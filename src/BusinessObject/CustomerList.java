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

    /**
     * CONSTRUCTOR
     */

    public CustomerList(ICustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    /**
     * CRUD
     */

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

    public List<Customer> getAllCustomers() {
        return sortByName(customerDAO.readAll());
    }

    /**
     * SEARCH
     */

    public Customer findCustomer(String id) {
        return customerDAO.findByID(id);
    }

    public List<Customer> searchByName(String name) {
        return sortByName(customerDAO.findByName(name));
    }

    private static String getGivenName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "";
        String[] parts = fullName.trim().split("\\s+");
        return parts[parts.length - 1];
    }

    /**
     * SORT
     */

    private List<Customer> sortByName(List<Customer> list) {
        Collator collator = Collator.getInstance(new Locale("vi", "VN"));
        return list.stream()
                .sorted(Comparator.comparing(
                        (Customer c) -> getGivenName(c.getName()),
                        collator::compare))
                .collect(Collectors.toList());
    }

    /**
     * CHECK
     */

    public boolean isExist(String id) {
        return customerDAO.findByID(id) != null;
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * SAVE TO FILE
     */

    public boolean saveToFile() {
        return customerDAO.save();
    }


}