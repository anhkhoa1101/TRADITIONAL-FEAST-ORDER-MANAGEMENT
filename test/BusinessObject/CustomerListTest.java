package BusinessObject;

import Core.Entities.Customer;
import Core.Interfaces.ICustomerDAO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test cho CustomerList — cô lập hoàn toàn khỏi file thật (.dat / .csv)
 * nhờ Constructor Injection: truyền mock ICustomerDAO vào constructor.
 *
 * Chạy bằng JUnit 4 + Mockito 3 (tương thích Java 8).
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerListTest {

    @Mock
    private ICustomerDAO fakeDAO;

    // Helper: tạo CustomerList với DAO giả, KHÔNG đụng file thật
    private CustomerList newListWith(ICustomerDAO dao) {
        return new CustomerList(dao);
    }

    @Test
    public void addCustomer_shouldFail_whenIdAlreadyExists() {
        // Arrange: giả lập DAO trả về khách hàng đã tồn tại
        when(fakeDAO.findByID("C0001"))
                .thenReturn(new Customer("C0001", "X", "0912345678", "x@x.com"));

        CustomerList list = newListWith(fakeDAO);   // không đụng file thật

        // Act: cố thêm khách hàng có ID trùng
        boolean result = list.addCustomer(
                new Customer("C0001", "Y", "0987654321", "y@y.com"));

        // Assert: phải fail và KHÔNG bao giờ gọi DAO.add(...)
        assertFalse(result);
        verify(fakeDAO, never()).add(any());
    }

    @Test
    public void addCustomer_shouldFail_whenIdIsEmpty() {
        CustomerList list = newListWith(fakeDAO);
        boolean result = list.addCustomer(
                new Customer("", "Khoa", "0912345678", "a@b.com"));
        assertFalse(result);
        verify(fakeDAO, never()).add(any());
    }

    @Test
    public void addCustomer_shouldSucceed_whenNewValidCustomer() {
        // Arrange: DAO chưa có khách này
        when(fakeDAO.findByID("C0099")).thenReturn(null);
        when(fakeDAO.add(any(Customer.class))).thenReturn(true);

        CustomerList list = newListWith(fakeDAO);
        boolean result = list.addCustomer(
                new Customer("C0099", "Khoa", "0912345678", "a@b.com"));

        assertFalse(!result);   // result == true
        verify(fakeDAO).add(any(Customer.class));
    }
}
