package Core.Interfaces;

import Core.Entities.Order;
import java.util.List;

public interface IOrderDAO extends IBaseDAO<Order> {
    // Bổ sung method đặc thù cho Order
    List<Order> findByCustomerID(String customerID);

}