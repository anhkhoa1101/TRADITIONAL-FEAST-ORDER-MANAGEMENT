package BusinessObject;

import Core.Entities.Customer;
import Core.Interfaces.ICustomerDAO;


import java.util.List;
import java.util.ArrayList;


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
            System.out.println("Invalid customer information!");
            return false;
        }
        if (isExist(c.getId())) {
            System.out.println("Customer ID already exists: " + c.getId());
            return false;
        }
        return customerDAO.add(c);
    }

    public boolean updateCustomer(Customer c) {
        if (!isExist(c.getId())) {
            System.out.println("Customer not found: " + c.getId());
            return false;
        }
        return customerDAO.update(c);
    }

    public boolean deleteCustomer(String id) {
        if (!isExist(id)) {
            System.out.println("Customer to delete not found: " + id);
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


    public List<Customer> findByName(String name) {
        return sortByName(customerDAO.findByName(name));
    }

    private String getGivenName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "";
        String[] parts = fullName.trim().split("\\s+");
        return parts[parts.length - 1];
    }


    /**
     * SORT
     */

    private List<Customer> sortByName(List<Customer> list) {
        List<Customer> result = new ArrayList<>(list);

        result.sort((c1, c2) ->
                getGivenName(c1.getName())
                        .compareTo(getGivenName(c2.getName()))
        );

        return result;
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