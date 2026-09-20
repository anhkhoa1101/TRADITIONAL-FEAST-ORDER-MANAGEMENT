package DataObjects;

import Core.Entities.Order;
import Core.Interfaces.IOrderDAO;
import Utilities.FileIO.FileHelper;
import Utilities.FileIO.IFileIO;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO implements IOrderDAO {

    private static final String FILE_NAME = "src/DataObjects/Data/orders.dat";
    private final IFileIO<Order> fileIO;
    private List<Order> orderList;

    public OrderDAO() {
        fileIO = new FileHelper<>(FILE_NAME);
        orderList = loadFromFile();             // SỬA: nạp file 1 lần lúc khởi động
    }

    // MỚI: đọc file -> RAM (chỉ dùng trong constructor)
    private List<Order> loadFromFile() {
        try {
            return fileIO.readFromFile();
        } catch (Exception e) {
            System.out.println("Lỗi đọc file: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // SỬA: trả về list đang ở RAM, không đọc file
    @Override
    public List<Order> readAll() {
        return new ArrayList<>(orderList);
    }

    @Override
    public boolean writeAll(List<Order> list) {
        try {
            return fileIO.saveToFile(list);
        } catch (Exception e) {
            System.out.println("Lỗi ghi file: " + e.getMessage());
            return false;
        }
    }

    // MỚI: ghi RAM xuống file (gọi từ menu Save)
    @Override
    public boolean save() {
        return writeAll(orderList);
    }

    @Override
    public boolean add(Order o) {
        return orderList.add(o);                // SỬA: chỉ RAM
    }

    @Override
    public boolean update(Order o) {
        for (int i = 0; i < orderList.size(); i++) {
            if (orderList.get(i).getOrderCode().equals(o.getOrderCode())) {
                orderList.set(i, o);
                return true;                    // SỬA: chỉ RAM
            }
        }
        return false;
    }

    @Override
    public boolean delete(String orderCode) {
        return orderList.removeIf(o -> o.getOrderCode().equals(orderCode));   // SỬA: chỉ RAM
    }

    @Override
    public Order findByID(String orderCode) {
        return orderList.stream().filter(o -> o.getOrderCode().equals(orderCode)).findFirst().orElse(null);
    }

    @Override
    public List<Order> findByCustomerID(String customerID) {
        List<Order> result = new ArrayList<>();
        for (Order o : orderList) {
            // customerID trong Order là object Customer, phải lấy .getId() để so sánh
            if (o.getCustomerID() != null && o.getCustomerID().getId().equals(customerID)) {
                result.add(o);
            }
        }
        return result;
    }
}