# TRADITIONAL FEAST ORDER MANAGEMENT (Dự án LAB1)

Quản lý đặt tiệc cưới/tiệc truyền thống: quản lý khách hàng, set menu (thực đơn
tiệc), và đơn đặt tiệc. Dự án được xây dựng bằng Java (NetBeans), lưu trữ dữ liệu
dưới dạng file nhị phân (`.dat`) thông qua cơ chế Serialization.

Tác giả: khoa0

---

## 1. ENTITY -> ATTRIBUTE -> CLASS -> FIELDS

Dự án có 3 thực thể (Entity) cốt lõi, đặt trong package `Core.Entities`. Mỗi entity
là một lớp Java biểu diễn một bảng/đối tượng dữ liệu, có các thuộc tính (Attribute)
tương ứng với các trường (Fields) private kèm theo getter/setter.

### 1.1. Entity: Customer (Khách hàng)
- Class: `Core.Entities.Customer` (implements `Serializable`)
- Fields:
  | Field      | Kiểu dữ liệu | Mô tả                |
  |------------|--------------|----------------------|
  | id         | String       | Mã khách hàng (VD: C0001) |
  | name       | String       | Tên khách hàng       |
  | phone      | String       | Số điện thoại (10 số) |
  | email      | String       | Email                |
- Constructor: `Customer()`, `Customer(id, name, phone, email)`

### 1.2. Entity: SetMenu (Thực đơn tiệc / Set menu)
- Class: `Core.Entities.SetMenu` (implements `Serializable`)
- Fields:
  | Field       | Kiểu dữ liệu | Mô tả                |
  |-------------|--------------|----------------------|
  | menuID      | String       | Mã set menu (VD: M01) |
  | menuName    | String       | Tên món tiệc         |
  | price       | double       | Giá tiền (>= 0)      |
  | ingredients | String       | Nguyên liệu          |
- Constructor: `SetMenu()`, `SetMenu(menuID, menuName, price, ingredients)`

### 1.3. Entity: Order (Đơn đặt tiệc)
- Class: `Core.Entities.Order` (implements `Serializable`)
- Fields:
  | Field       | Kiểu dữ liệu | Mô tả                                    |
  |-------------|--------------|------------------------------------------|
  | orderCode   | String       | Mã đơn (tự sinh: yyyymmddhhmmss)         |
  | customerID  | Customer     | Khách hàng (object nhúng, lấy id để so)  |
  | province    | String       | Tỉnh/thành tổ chức tiệc                  |
  | menuID      | SetMenu      | Set menu được đặt (object nhúng)          |
  | numOfTables | int          | Số bàn (phải > 0)                        |
  | eventDate   | Date         | Ngày tổ chức                             |
- Method riêng: `generateOrderCode()` — tự sinh mã đơn từ thời gian hiện tại.
- Constructor:
  - `Order()` — sinh mã đơn, gán ngày hiện tại, menu/customer = null.
  - `Order(orderCode, customerID, province, menuID, numOfTables, eventDate)` — luôn
    gọi `generateOrderCode()` để sinh mã (orderCode truyền vào bị bỏ qua).

### 1.4. Quan hệ giữa các Entity
- `Order` có quan hệ "chứa" (composition/embed) với `Customer` và `SetMenu` dưới
  dạng object (chứ không phải chỉ lưu String ID). Khi thêm đơn, hệ thống sẽ lấy
  object thật từ `CustomerList` / `SetMenuList` để gán lại vào Order, tránh trường
  hợp Order chỉ chứa object "rỗng" (chỉ có mã).

---

## 2. DESIGN PATTERN (Các mẫu thiết kế & phân lớp)

Dự án tách biệt rõ ràng 4 nhóm trách nhiệm (separation of concerns):

### 2.1. STORE ENTITY (Lưu trữ thực thể) — package `Core`
Là nơi định nghĩa "hình dáng" dữ liệu và các hợp đồng (interface) mà không chứa
logic xử lý.

- `Core.Entities` — các lớp thực thể: `Customer`, `Order`, `SetMenu`.
- `Core.Interfaces` — các interface định nghĩa hành vi:
  - `IBaseDAO<E>` — interface generic chung cho mọi DAO:
    `readAll()`, `writeAll(List<E>)`, `add(E)`, `update(E)`, `delete(String)`,
    `findByID(String)`.
  - `ICustomerDAO` / `IOrderDAO` / `ISetMenuDAO` — kế thừa `IBaseDAO`, mở rộng thêm
    các hàm riêng (VD: `findByName`, `findByCustomerID`).
  - `Validation`, `Workable` — các interface/abstract hỗ trợ validate &行为.

**Mục đích:** định nghĩa entity + hợp đồng, giúp các lớp trên tách rời khỏi cách
thực thi cụ thể (Dependency Inversion).

### 2.2. CONTROL (RAM — quản lý bộ nhớ & nghiệp vụ) — package `BusinessObject`
Đóng vai trò "Service / Business Layer". Giữ danh sách đối tượng trong RAM thông
qua các DAO, thực hiện validate và xử lý logic nghiệp vụ (tìm kiếm, lọc, tính toán,
kiểm tra tồn tại), nhưng KHÔNG tự đọc/ghi file.

- `CustomerList`
  - Field: `ICustomerDAO customerDAO` (interface -> đảo ngược phụ thuộc).
  - Methods: `addCustomer(Customer)`, `updateCustomer(Customer)`,
    `deleteCustomer(String)`, `searchByName(String)`, `getAllCustomers()`,
    `findCustomer(String)`, `isExist(String)`.
- `SetMenuList`
  - Field: `ISetMenuDAO setMenuDAO`.
  - Methods: `addSetMenu`, `updateSetMenu`, `deleteSetMenu`, `getAllSetMenus`,
    `findByID`, `isExist`.
- `OrderList`
  - Fields: `IOrderDAO orderDAO`, `CustomerList customerList`, `SetMenuList setMenuList`
    (compose 2 list kia để kiểm tra khách/menu có thực sự tồn tại).
  - Methods: `addOrder(Order)` (kiểm tra customer & menu tồn tại, số bàn > 0),
    `updateOrder`, `deleteOrder`, `getAllOrders`, `findOrder`, `isExist`,
    `getOrdersByCustomer`, `getTotalRevenue()` (tổng doanh thu = price * numOfTables).

**Pattern áp dụng:** Service / Business Layer + Dependency Inversion (gọi qua
interface DAO thay vì lớp cụ thể).

### 2.3. SAVE (FILE — lưu trữ file) — package `DataObjects`
Đóng vai trò "Data Access Object (DAO) Pattern". Chịu trách nhiệm đọc/ghi dữ liệu
xuống file nhị phân, tách hoàn toàn logic truy cập dữ liệu ra khỏi logic nghiệp vụ
(theo hướng phát triển ghi trong ghi chú dự án: không để BusinessObject đọc/ghi file
trực tiếp).

- `CustomerDAO` (implements `ICustomerDAO`)
  - File: `src/DataObjects/data/customers.dat`
  - Giữ `List<Customer> customerList` (nạp từ file khi khởi tạo), ghi lại file sau
    mỗi `add/update/delete`.
  - Methods: `readAll`, `writeAll`, `add`, `update`, `delete`, `findByID`,
    `findByName`.
- `OrderDAO` (implements `IOrderDAO`)
  - File: `src/DataObjects/data/orders.dat`
  - Methods tương tự + `findByCustomerID(String)`.
- `SetMenuDAO` (implements `ISetMenuDAO`)
  - File: `src/DataObjects/data/setmenus.dat`
  - Methods: `readAll`, `writeAll`, `add`, `update`, `delete`, `findByID`.

Cơ chế lưu file dùng **Serialization** (ObjectInputStream / ObjectOutputStream),
đọc đến hết file bằng `EOFException`.

### 2.4. UTILS (Tiện ích) — package `Utilities`
Các lớp hỗ trợ dùng chung, không chứa nghiệp vụ:

- `Utilities.FileIO`
  - `IFileIO<E>` — interface đọc/ghi file generic.
  - `FileHelper<E>` — lớp generic đọc/ghi danh sách object xuống file nhị phân:
    `readFromFile()`, `saveToFile(List<E>)`.
- `Utilities.Validation`
  - `BaseValidation` — interface chứa các regex chuẩn (`INTEGER_VALID`,
    `POSITIVE_INT_VALID`, `DOUBLE_VALID`, `POSITIVE_DOUBLE_VALID`) và hàm
    `isValid(value, pattern)`.
  - `CusValidation` — regex riêng cho khách hàng (`CUS_ID_VALID`, `NAME_VALID`,
    `PHONE_VALID`).
  - `OrderValidation` — regex cho đơn (`PROVINCE_VALID`, `NUM_TABLES_VALID`,
    `DATE_VALID`).
- `Utilities.Inputter` — lớp thu nhập dữ liệu từ bàn phím (`getString`,
  `getInt`, `getDouble`, `inputAndLoop` — nhập và lặp lại nếu không khớp regex).

### 2.5. PRESENTATION (Giao diện người dùng) — package `Presentation`
- `Program` — lớp chứa `main()`, khởi tạo `Menu` và gọi `menu.run()`.
- `Menu` — hiển thị menu dạng text, đọc input, gọi các BusinessObject tương ứng.
  Các chức năng:
  1. Register customers (đăng ký KH)
  2. Update customer information
  3. Search customer by name
  4. Display feast menus
  5. Place a feast order (đặt tiệc)
  6. Update order information
  7. Save data to file (thực tế dữ liệu đã lưu tự động sau mỗi thao tác)
  8. Display Customer/Order lists
  0. Quit

### 2.6. Tóm tắt kiến trúc (luồng phân lớp)
```
Presentation (Menu/Program)
      |  gọi
      v
BusinessObject (CustomerList / OrderList / SetMenuList)   <-- CONTROL / RAM + logic
      |  gọi qua interface DAO
      v
DataObjects (CustomerDAO / OrderDAO / SetMenuDAO)          <-- SAVE / FILE
      |  dùng
      v
Utilities.FileIO.FileHelper  +  Core.Entities (Store Entity)
```

---

## 3. SEQUENCE DIAGRAM (Luồng xử lý — Class nào, Phương thức nào)

Dưới đây là 2 luồng xử lý tiêu biểu mô tả tuần tự các class và method được gọi.

### 3.1. Luồng: Đăng ký khách hàng (Chức năng 1)

```
Actor (User)
  |-- Menu.registerCustomer()
  |     |-- Inputter.inputAndLoop(...)           // nhập & validate id, name, phone
  |     |-- Inputter.getString(...)              // nhập email
  |     |-- CustomerList.addCustomer(new Customer(...))
  |     |     |-- (nội bộ) kiểm tra null/rỗng, isExist(id)
  |     |     |-- CustomerDAO.add(customer)
  |     |           |-- customerList.add(c)
  |     |           |-- FileHelper.saveToFile(customerList)   // ghi customers.dat
  |     |-- in ra "Đăng ký thành công / thất bại"
```

Các class & method theo thứ tự:
1. `Presentation.Menu` → `registerCustomer()`
2. `Utilities.Inputter` → `inputAndLoop()`, `getString()`
3. `BusinessObject.CustomerList` → `addCustomer(Customer)`
4. `DataObjects.CustomerDAO` → `add(Customer)`
5. `Utilities.FileIO.FileHelper` → `saveToFile(List<Customer>)`

### 3.2. Luồng: Đặt tiệc (Chức năng 5 — Place order)

```
Actor (User)
  |-- Menu.placeOrder()
  |     |-- Inputter.inputAndLoop(...)           // nhập mã KH
  |     |-- CustomerList.findCustomer(id)        // kiểm tra KH tồn tại
  |     |-- SetMenuList.getAllSetMenus()         // hiển thị menu
  |     |-- Inputter.getString(...)              // nhập mã menu
  |     |-- SetMenuList.findByID(menuID)         // kiểm tra menu tồn tại
  |     |-- Inputter.inputAndLoop/getInt(...)    // nhập tỉnh, số bàn, ngày
  |     |-- Order order = new Order(null, customer, province, menu, numTables, date)
  |     |                                  // Order tự sinh orderCode
  |     |-- OrderList.addOrder(order)
  |     |     |-- CustomerList.findCustomer(...) // lấy object KH thật
  |     |     |-- SetMenuList.findByID(...)      // lấy object menu thật
  |     |     |-- kiểm tra số bàn > 0
  |     |     |-- OrderDAO.add(order)
  |     |           |-- orderList.add(order)
  |     |           |-- FileHelper.saveToFile(orderList)  // ghi orders.dat
  |     |-- in ra mã đơn & tổng tiền (price * numOfTables)
```

Các class & method theo thứ tự:
1. `Presentation.Menu` → `placeOrder()`
2. `BusinessObject.CustomerList` → `findCustomer(String)`
3. `BusinessObject.SetMenuList` → `getAllSetMenus()`, `findByID(String)`
4. `Core.Entities.Order` → constructor (gọi `generateOrderCode()`)
5. `BusinessObject.OrderList` → `addOrder(Order)`
   - (bên trong gọi lại `CustomerList.findCustomer`, `SetMenuList.findByID`)
6. `DataObjects.OrderDAO` → `add(Order)`
7. `Utilities.FileIO.FileHelper` → `saveToFile(List<Order>)`

### 3.3. Ghi chú về luồng lưu dữ liệu
- Mỗi `add/update/delete` ở DAO đều gọi `FileHelper.saveToFile(...)` ngay lập tức
  => dữ liệu được persist tự động. Menu chức năng 7 (`saveData`) chỉ in thông báo,
  không làm thêm thao tác ghi (vì đã lưu tự động).
- `Order` lưu nguyên object `Customer` và `SetMenu` (do implements Serializable),
  nên khi đọc lại từ file vẫn giữ đầy đủ thông tin liên kết.

---

## 4. CÁCH BIÊN DỊCH & CHẠY

- Mở project bằng NetBeans (đã có `nbproject/`, `build.xml`, `manifest.mf`).
- Chạy lớp `Presentation.Program` (chứa `main`).
- Dữ liệu được lưu tại:
  - `src/DataObjects/data/customers.dat`
  - `src/DataObjects/data/orders.dat`
  - `src/DataObjects/data/setmenus.dat`
- Build CLI (nếu dùng Ant): `ant run` hoặc `ant jar` trong thư mục Project.

---

## 5. CẤU TRÚC THƯ MỤC

```
Project/
├── src/
│   ├── Core/
│   │   ├── Entities/      (Customer, Order, SetMenu)        [STORE ENTITY]
│   │   └── Interfaces/    (IBaseDAO, ICustomerDAO, IOrderDAO, ISetMenuDAO,...)
│   ├── BusinessObject/    (CustomerList, OrderList, SetMenuList)  [CONTROL/RAM]
│   ├── DataObjects/       (CustomerDAO, OrderDAO, SetMenuDAO)     [SAVE/FILE]
│   │   └── data/          (customers.dat, orders.dat, setmenus.dat)
│   ├── Utilities/
│   │   ├── FileIO/         (IFileIO, FileHelper)             [UTILS]
│   │   ├── Validation/     (BaseValidation, CusValidation, OrderValidation)
│   │   └── Inputter.java
│   └── Presentation/      (Program, Menu)
├── build/                 (kết quả biên dịch)
├── nbproject/             (cấu hình NetBeans)
├── build.xml, manifest.mf
└── README.md
```
#   T R A D I T I O N A L - F E A S T - O R D E R - M A N A G E M E N T  
 