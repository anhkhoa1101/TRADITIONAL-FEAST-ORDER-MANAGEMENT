package DataObjects;

import Core.Entities.Order;
import Core.Interfaces.IOrderDAO;
import Utilities.FileIO.FileHelper;
import Utilities.FileIO.IFileIO;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO implements IOrderDAO {

    private static final String FILE_NAME = "src/DataObjects/data/orders.dat";
    private final IFileIO<Order> fileIO;
    private List<Order> orderList;

    public OrderDAO() {
        fileIO = new FileHelper<>(FILE_NAME);
        orderList = readAll();
    }

    @Override
    public List<Order> readAll() {
        try {
            return fileIO.readFromFile();
        } catch (Exception e) {
            System.out.println("Lỗi đọc file: " + e.getMessage());
            return new ArrayList<>();
        }
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

    @Override
    public boolean add(Order o) {
        orderList.add(o);
        return writeAll(orderList);
    }

    @Override
    public boolean update(Order o) {
        for (int i = 0; i < orderList.size(); i++) {
            if (orderList.get(i).getOrderCode().equals(o.getOrderCode())) {
                orderList.set(i, o);
                return writeAll(orderList);
            }
        }
        return false;
    }

    @Override
    public boolean delete(String orderCode) {
        boolean removed = orderList.removeIf(o -> o.getOrderCode().equals(orderCode));
        return removed && writeAll(orderList);
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