package DataObjects;

import Core.Entities.Customer;
import Core.Interfaces.ICustomerDAO;
import Utilities.FileIO.FileHelper;
import Utilities.FileIO.IFileIO;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO implements ICustomerDAO {

    private static final String FILE_NAME = "src/DataObjects/Data/customers.dat";
    private final IFileIO<Customer> fileIO;
    private List<Customer> customerList;

    public CustomerDAO() {
        fileIO = new FileHelper<>(FILE_NAME);
        customerList = loadFromFile();          // SỬA: nạp file 1 lần lúc khởi động
    }

    // MỚI: đọc file -> RAM (chỉ dùng trong constructor)
    private List<Customer> loadFromFile() {
        try {
            return fileIO.readFromFile();
        } catch (Exception e) {
            System.out.println("Lỗi đọc file: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // SỬA: trả về list đang ở RAM, không đọc file
    @Override
    public List<Customer> readAll() {
        return new ArrayList<>(customerList);
    }

    @Override
    public boolean writeAll(List<Customer> list) {
        try {
            return fileIO.saveToFile(list);
        } catch (Exception e) {
            System.out.println("Lỗi ghi file: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean save() {
        return writeAll(customerList);
    }

    @Override
    public boolean add(Customer c) {
        return customerList.add(c);
    }

    @Override
    public boolean update(Customer c) {
        for (int i = 0; i < customerList.size(); i++) {
            if (customerList.get(i).getId().equals(c.getId())) {
                customerList.set(i, c);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(String id) {
        return customerList.removeIf(c -> c.getId().equals(id));
    }

    @Override
    public Customer findByID(String id) {
        return customerList.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<Customer> findByName(String name) {
        List<Customer> result = new ArrayList<>();
        for (Customer c : customerList) {
            if (c.getName().toLowerCase().contains(name.toLowerCase())) {
                result.add(c);
            }
        }
        return result;
    }
}