package Core.Interfaces;

import Core.Entities.Customer;
import java.util.List;

public interface ICustomerDAO extends IBaseDAO<Customer> {
    // Bổ sung method đặc thù cho Customer (nếu cần)
    List<Customer> findByName(String name);
}