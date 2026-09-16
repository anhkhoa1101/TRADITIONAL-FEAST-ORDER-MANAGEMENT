package BusinessObject;

import Core.Entities.Customer;
import Core.Interfaces.ICustomerDAO;
import DataObjects.CustomerDAO;

import java.util.List;

public class CustomerList {

    private final ICustomerDAO customerDAO;

    public CustomerList() {
        customerDAO = new CustomerDAO();
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

    public List<Customer> searchByName(String name) {
        return customerDAO.findByName(name);
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.readAll();
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
}