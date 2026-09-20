# ĐÁNH GIÁ DỰ ÁN — TRADITIONAL FEAST ORDER MANAGEMENT (Dockerized)

> Tài liệu đánh giá (review) toàn diện dự án Java console quản lý đặt tiệc, đóng gói bằng Docker.
> Người đánh giá: Hermes Agent · Ngày: 2026-09-19
> Đối tượng: sinh viên / giảng viên môn Lập trình hướng đối tượng – Kiến trúc phần mềm.

---

## 0. TỔNG QUAN

| Hạng mục | Thông tin |
|---|---|
| Tên dự án | Traditional Feast Order Management (Quản lý đặt tiệc truyền thống) |
| Ngôn ngữ | Java 8 (1.8) |
| Build tool | Apache Ant (NetBeans project) |
| Kiểu ứng dụng | Console (dòng lệnh), đơn luồng |
| Mô hình lưu trữ | File: Java Serialization (`.dat`) cho Customer/Order, CSV (`.csv`) cho SetMenu |
| Kiến trúc | 4 tầng: `Core` (Entity + Interface) → `BusinessObject` (Service) → `DataObjects` (DAO) → `Utilities` (FileIO/Validation/Inputter) + `Presentation` (Menu/Program) |
| Container | Docker (base `eclipse-temurin:8-jdk-jammy`, build bằng `ant jar`) |
| Số lượng file mã nguồn | ~21 file `.java` + 2 file doc thiết kế (`PROPOSAL_*.md`) + `README.md` + `Dockerfile` |

**Nhận xét nhanh:** Đây là một dự án **lab/môn học chất lượng khá cao** về mặt kiến trúc (phân tầng rõ ràng, áp dụng DAO + Dependency Inversion + Generic interface hợp lý). Tuy nhiên về mặt **triển khai Docker** và một vài **logic nghiệp vụ nhỏ** còn tồn tại lỗi đáng kể cần sửa trước khi coi là hoàn chỉnh.

---

## 1. ĐIỂM MẠNH (STRENGTHS)

### 1.1. Kiến trúc phân tầng rõ ràng, tuân thủ nguyên lý OOP
- Tách biệt 4 trách nhiệm: **STORE ENTITY** (`Core.Entities`), **CONTROL/RAM** (`BusinessObject`), **SAVE/FILE** (`DataObjects`), **UTILS** (`Utilities`).
- `BusinessObject` không bao giờ gọi trực tiếp `FileHelper` mà luôn đi qua DAO → ranh giới tầng được giữ đúng (README §2.4 cũng tự mô tả đúng).
- Không có lớp "god class" làm mọi thứ; mỗi lớp có một nhiệm vụ (Single Responsibility khá tốt với quy mô lab).

### 1.2. Áp dụng DAO Pattern + Generic Interface + Dependency Inversion
- `IBaseDAO<E>` generic định nghĩa hợp đồng chung (`readAll/add/update/delete/findByID`).
- `ICustomerDAO extends IBaseDAO<Customer>`, `IOrderDAO`, `ISetMenuDAO` mở rộng method riêng (`findByName`, `findByCustomerID`).
- `BusinessObject` phụ thuộc vào **interface** (`ICustomerDAO`) chứ không phụ thuộc class cụ thể `CustomerDAO` → DIP (Dependency Inversion Principle) được áp dụng thực tế, không chỉ nói suông.
- Các class `CustomerList/OrderList/SetMenuList` có **constructor injection** (`CustomerList(ICustomerDAO)`) → dễ mock khi test (thấy rõ ở `CustomerListTest`).

### 1.3. Xử lý encoding UTF-8 có ý thức
- `Inputter` dùng `new Scanner(new InputStreamReader(System.in, StandardCharsets.UTF_8))`.
- `Program.main` set `System.out` sang `PrintStream(..., "UTF-8")` (đúng cách cho Java 8, có bọc `try/catch UnsupportedEncodingException`).
- `MenuFileHelper` đọc/ghi CSV bằng `StandardCharsets.UTF_8` + strip BOM (`\uFEFF`).
→ Tiếng Việt hiển thị đúng, thể hiện người làm đã gặp và xử lý bài toán encoding (xem thêm `PROPOSAL_CustomerEncoding.md`).

### 1.4. Có tài liệu thiết kế root-cause analysis chất lượng
- `PROPOSAL_CustomerEncoding.md`: phân tích hexdump, kết luận lệch mã hóa console CP1252 ↔ JVM UTF-8, đề xuất fix đúng nguyên tắc OOP. Rất tốt.
- `PROPOSAL_SetMenuDAO.md`: phân tích lỗi `invalid stream header: EFBBBF43` do dùng `FileHelper` (nhị phân) đọc file CSV, chuyển sang `MenuFileHelper`. Thể hiện tư duy debug hệ thống.
- `README.md`: phân tích Entity→Attribute→Class→Fields, vẽ Sequence Diagram Mermaid cho 3 luồng chính. Tài liệu **xuất sắc** so với mặt bằng đồ án sinh viên.

### 1.5. Có Unit Test (JUnit + Mockito)
- `CustomerListTest` cô lập DAO bằng `@Mock ICustomerDAO`, chạy JUnit 4 + Mockito 3 (tương thích Java 8).
- Test cover 3 trường hợp: trùng ID, ID rỗng, thêm mới hợp lệ → verify không gọi `add` khi fail. Đúng hướng TDD/isolation.

### 1.6. Validate đầu vào bằng regex tập trung
- `BaseValidation` định nghĩa regex dùng chung (`INTEGER_VALID`, `POSITIVE_INT_VALID`, `DOUBLE_VALID`...), `CusValidation`/`OrderValidation` kế thừa và mở rộng. Tránh lặp code.

### 1.7. `Order` lưu nguyên object `Customer`/`SetMenu` (composition)
- Nhờ `Serializable`, khi đọc lại `orders.dat` vẫn giữ đủ thông tin liên kết, không cần "join" thủ công. `OrderList.addOrder` còn lấy lại object thật từ DB để tránh Order chứa object "rỗng" chỉ có ID. Thiết kế hợp lý.

---

## 2. ĐIỂM YẾU / LỖI (WEAKNESSES & BUGS)

### 🔴 2.1. BUG: Mã đơn hàng sinh sai định dạng (`Order.generateOrderCode`)
```java
SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddhhmmss");
```
Trong `SimpleDateFormat` của Java:
- `M` (viết hoa) = **tháng**, `m` (viết thường) = **phút**.
- `H` (viết hoa) = giờ 24h, `h` (viết thường) = giờ 12h (AM/PM).

Pattern `"yyyymmddhhmmss"` thực tế được giải thích là:
`yyyy`(năm) + `mm`(**phút**) + `dd`(ngày) + `hh`(giờ 12h) + `mm`(**phút**) + `ss`(giây).

→ **Hậu quả:** (1) Mã đơn **không có tháng**; (2) trường **phút bị lặp 2 lần** (vị trí thứ 3 và thứ 5); (3) giờ dùng `hh` 12h nên không phân biệt sáng/chiều.
Ví dụ lúc `19/09/2026 15:45:30` sinh ra: `2026` `09` `19` `03` `45` `30` = `20260919034530` (năm-phút-ngày-giờ12-phút-giây) — hoàn toàn thiếu tháng, sai nghĩa.

**Sửa đề xuất:**
```java
SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
```
(Đồng thời `README.md` §1.3 ghi pattern là `yyyymmddhhmmss` — tài liệu cũng mang bug này, cần sửa theo.)

### 🔴 2.2. BUG/MẸU UX: `Inputter.getInt` không loop, trả về `0` khi sai
```java
public int getInt(String mess, String pattern) {
    int result = 0;
    String temp = getString(mess);
    if (BaseValidation.isValid(temp, pattern)) result = Integer.parseInt(temp);
    return result;   // <- trả về 0 nếu không khớp pattern
}
```
Ở `Menu.placeOrder()`:
```java
int numOfTables = in.getInt("Nhập số bàn: ", OrderValidation.NUM_TABLES_VALID);
```
Nếu người dùng nhập sai (vd gõ chữ), `numOfTables = 0`, **không được yêu cầu nhập lại**, sau đó `OrderList.addOrder` check `numOfTables <= 0` → báo thất bại. Trải nghiệm người dùng kém (phải thoát rồi vào lại). `registerCustomer` thì dùng `inputAndLoop` (có loop) nên ổn; riêng `numOfTables` và `updateOrderInfo` dùng `getInt` không loop.

**Sửa:** cho `getInt`/`getDouble` nhận thêm tham số `loop`, hoặc trong `Menu` dùng `inputAndLoop` + parse, hoặc ép `getInt` loop tới khi hợp lệ.

### 🟡 2.3. RAM không được chia sẻ giữa các DAO instance (thiết kế lỏng)
- `Menu` tạo `new CustomerList()`, `new OrderList()`, `new SetMenuList()` độc lập.
- `OrderList()` constructor lại tạo **thêm** `new CustomerList()` và `new SetMenuList()` riêng bên trong.
- Mỗi `CustomerList` chứa 1 `CustomerDAO` riêng, mỗi DAO load file vào `List` RAM riêng khi khởi tạo.

May mắn là mỗi `add/update/delete` **ghi file ngay lập tức**, nên khi `OrderList` đọc Customer từ file (DAO riêng) vẫn thấy khách mới (vì `Menu.customerList` đã ghi file trước đó). Nhưng hệ quả:
- Đọc file liên tục (I/O nhiều) mỗi khi thao tác.
- Nếu sau này chuyển sang RAM-only (không ghi file mỗi bước), các instance sẽ **không thấy nhau** → bug ngầm.
- Không dùng Singleton/Shared instance cho DAO.

**Đề xuất:** dùng Dependency Injection tập trung (truyền chung 1 instance `CustomerDAO`/`CustomerList` vào `OrderList`), hoặc áp dụng Singleton cho các DAO.

### 🟡 2.4. SetMenu là dữ liệu tham chiếu nhưng dùng chung DAO có thể ghi đè file gốc
`SetMenuDAO` dùng `MenuFileHelper` đọc/ghi `FeastMenu.csv`. File này là **thực đơn mặc định (reference data)**. Dù hiện tại `Menu` chỉ gọi `getAllSetMenus`/`findByID` (không add/update/delete menu), nhưng `SetMenuDAO.add/update/delete` vẫn có thể **ghi đè `FeastMenu.csv`** nếu bị gọi. Nguy cơ: mất thực đơn gốc sau khi chạy thao tác nào đó, hoặc file bị "nhiễm" dữ liệu user.

**Đề xuất:** tách thực đơn mặc trị (read-only) khỏi thực đơn do user quản lý, hoặc đặt `FeastMenu.csv` ở chế độ read-only (không cho `saveToFile` ghi đè file gốc).

### 🟡 2.5. Validate chưa chặt
- `EMAIL` **không được validate** (`Menu.registerCustomer` gọi `in.getString` tự do) → email rác/thiếu vẫn lưu được.
- `NAME_VALID = "^.{2,25}$"` chỉ check độ dài, chấp nhận ký tự đặc biệt (`@#$`).
- `DATE_VALID = "^\\d{2}/\\d{2}/\\d{4}$"` chỉ check format, không check ngày hợp lệ (vd `31/02/2026` vẫn qua, và `SimpleDateFormat` mặc định `lenient=true` sẽ "tự động sửa" thành tháng 3).
- `CUS_ID_VALID = "^[CCGgKk]\\d{4}$"` chấp nhận cả chữ thường (`g`, `k`, `c`) dù ví dụ là `C0001`.

### 🟡 2.6. Không có xử lý đồng thời (concurrency) / race condition mã đơn
- Mã đơn sinh từ `new Date()` → nếu 2 đơn tạo **cùng giây**, `orderCode` trùng → `isExist` sẽ chặn đơn sau (fail). Ở app console đơn luồng thì hiếm, nhưng là thiết kế mong manh.
- App đơn luồng nên không cần synchronize, nhưng nếu sau này thành multi-user (web) sẽ đụng ngay.

### 🟡 2.7. Đường dẫn file hardcode tương đối
```java
private static final String FILE_NAME = "src/DataObjects/Data/customers.dat";
```
Phụ thuộc vào **working directory** khi chạy jar. Phải chạy từ gốc project (`/app` trong Docker) thì mới đúng. Chạy từ thư mục khác → `FileNotFound`. Nên dùng đường dẫn tuyệt đối或从 classpath resource.

### 🟢 2.8. Sai lệch nhỏ giữa README và code (tài liệu không khớp thực tế)
- `README.md` §2.3 ghi `SetMenuDAO` lưu vào `src/DataObjects/data/setmenus.dat`, nhưng **thực tế** code lưu vào `src/DataObjects/Data/FeastMenu.csv` (`Data` viết hoa, và là `.csv` chứ không phải `.dat`).
- `README.md` §4 ghi cấu trúc `Data/ (customers.dat, orders.dat, setmenus.dat)` — thiếu `FeastMenu.csv`, ghi sai `setmenus.dat`.
→ Tài liệu rất tốt nhưng cần cập nhật cho khớp code để người chấm bài không bị hoang mang.

---

## 3. ĐÁNH GIÁ DOCKERFILE

```dockerfile
FROM eclipse-temurin:8-jdk-jammy
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
RUN apt-get update && apt-get install -y ant && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY . .
RUN ant -f build.xml jar
CMD ["java", "-cp", "dist/Project.jar", "Presentation.Program"]
```

### 🔴 3.1. `COPY . .` copy mọi thứ → image phình to, lộ source
- Copy cả `.git/`, `nbproject/`, `.idea/`, `build/` (đã compile), `lib/`, `*.iml`.
- Làm image nặng, chứa code nguồn + build artifacts không cần thiết, và lộ lịch sử git.

**Sửa:** thêm `.dockerignore`:
```
.git
.idea
nbproject
build
*.iml
```
→ Chỉ copy `src/`, `lib/`, `build.xml`, `manifest.mf`, `Dockerfile`.

### 🔴 3.2. Dữ liệu không persist (mất khi xóa container)
- `customers.dat` / `orders.dat` được ghi vào `/app/src/DataObjects/Data/` (trong writable layer của container).
- Khi `docker rm` container → **mọi dữ liệu khách/đơn bị mất**.
- `Menu` có chức năng "Save data" nhưng thực chất ghi vào filesystem của container, không ra host.

**Sửa:** mount volume:
```dockerfile
VOLUME ["/app/src/DataObjects/Data"]
```
và chạy: `docker run -v feast-data:/app/src/DataObjects/Data ...`. Hoặc chuyển sang DB (SQLite/H2) cho đúng bài toán có state.

### 🟡 3.3. Dùng JDK làm runtime (nặng)
- Base `eclipse-temurin:8-jdk-jammy` chứa cả JDK + cài thêm `ant` → image rất lớn (hàng trăm MB đến ~600MB+).
- Chỉ cần JRE để chạy jar.

**Sửa (multi-stage build):**
```dockerfile
FROM eclipse-temurin:8-jdk-jammy AS build
RUN apt-get update && apt-get install -y ant && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY . .
RUN ant -f build.xml jar

FROM eclipse-temurin:8-jre-jammy
WORKDIR /app
COPY --from=build /app/dist/Project.jar ./dist/Project.jar
COPY --from=build /app/src/DataObjects/Data ./src/DataObjects/Data
CMD ["java", "-cp", "dist/Project.jar", "Presentation.Program"]
```
→ Image nhẹ hơn nhiều.

### 🟡 3.4. Chạy với user root
- Container mặc định chạy `root`. Nên thêm `RUN useradd -m appuser && USER appuser` (cần chú ý quyền ghi thư mục data).

### 🟢 3.5. Không có HEALTHCHECK / không có entrypoint script
- App console đọc `System.in` → không phù hợp chạy như service daemon trong Docker (Docker thích process không tương tác). Hiện tại chạy `java` trực tiếp, khi attach vào container mới dùng được. Với mục đích demo/lab là chấp nhận được, nhưng về "best practice Docker" một app console tương tác không phải là workload lý tưởng cho container.

### 🟢 3.6. `CMD` đúng
- `java -cp dist/Project.jar Presentation.Program` hoạt động vì `ant jar` sinh `dist/Project.jar` và `Main-Class` không bắt buộc khi chỉ định class rõ. OK.

---

## 4. BẢO MẬT (SECURITY)

| Vấn đề | Mức | Mô tả |
|---|---|---|
| Java Serialization đọc file từ disk | Trung bình | `FileHelper` deserializes object từ `customers.dat`/`orders.dat`. Nếu file bị sửa độc ý → deserialization attack (gadget chain). App local đơn user → rủi ro thấp, nhưng là anti-pattern. |
| Không validate email/ngày chặt | Thấp | Dữ liệu rác vào hệ thống. |
| Chạy container bằng root | Trung bình | Nên drop privileges. |
| `COPY . .` lộ source/git | Thấp | Không nhạy cảm với lab, nhưng không nên. |
| Không có auth/network exposure | Tốt | App console local, không mở port → ít bề mặt tấn công. |

→ Tổng thể **rủi ro thấp** (app cá nhân, không mạng), nhưng serialization và root user là 2 điểm nên sửa nếu đưa lên môi trường thật.

---

## 5. KIỂM THỬ (TESTING)

- ✅ Có `CustomerListTest` (JUnit 4 + Mockito 3) với 3 test case hợp lý, cô lập DAO bằng mock.
- ❌ Chỉ test `CustomerList`; chưa có test cho `OrderList`, `SetMenuList`, các DAO, `FileHelper`, `MenuFileHelper`, `Inputter`, `Validation`.
- ❌ Dockerfile chỉ `ant jar`, **không chạy test** (`ant test`/JUnit) → lỗi (vd bug SimpleDateFormat) lọt qua CI/Docker build.
- ❌ Không có CI (GitHub Actions) để chạy build+test tự động.

**Đề xuất:** thêm target test trong quy trình, viết thêm test cho `OrderList.addOrder` (validate numOfTables, customer/menu tồn tại), và test `generateOrderCode` (sau khi sửa format).

---

## 6. CHẤM ĐIỂM (THEO TIÊU CHÍ)

| Tiêu chí | Điểm (/10) | Ghi chú |
|---|---|---|
| Kiến trúc & Thiết kế (DAO, phân tầng, DIP) | 8.5 | Rất tốt với quy mô lab |
| Mã nguồn & Clean Code | 7.5 | Đặt tên rõ, comment hợp lý; còn hardcode path, RAM không share |
| Đúng đắn nghiệp vụ (Correctness) | 6.0 | **Trừ điểm vì bug SimpleDateFormat** và getInt không loop |
| Kiểm thử (Testing) | 4.0 | Chỉ 1 test file, không chạy trong build, coverage thấp |
| Docker & Triển khai | 4.0 | COPY ., không .dockerignore, JDK thay JRE, không persist data, root |
| Tài liệu (README + PROPOSAL) | 9.0 | Xuất sắc; chỉ sai lệch nhỏ đường dẫn/file SetMenu |
| Bảo mật | 5.0 | App local an toàn, nhưng serialization + root |
| Khả năng mở rộng (Scalability) | 5.0 | File-based, đơn luồng, khó scale lên multi-user |
| **TRUNG BÌNH** | **~6.1 / 10** | |

---

## 7. KHUYẾN NGHỊ CẢI THIỆN (PRIORITY)

### Ưu tiên cao (fix ngay)
1. **Sửa `Order.generateOrderCode`**: `"yyyymmddhhmmss"` → `"yyyyMMddHHmmss"`. (§2.1)
2. **Sửa `Inputter.getInt/getDouble`** có vòng lặp hoặc đổi `Menu` dùng `inputAndLoop` cho `numOfTables`. (§2.2)
3. **Thêm `.dockerignore`** + chuyển sang **multi-stage build (JRE runtime)**. (§3.1, §3.3)
4. **Mount volume** cho thư mục data để không mất dữ liệu khi xóa container. (§3.2)

### Ưu tiên trung bình
5. Chia sẻ instance DAO (DI/Singleton) thay vì mỗi lớp mới 1 DAO riêng. (§2.3)
6. Tách thực đơn mặc định (read-only) khỏi DAO ghi được. (§2.4)
7. Validate chặt hơn: email, ngày thực tế, tên. (§2.5)
8. Cập nhật `README.md` cho khớp code (`FeastMenu.csv`, `Data/` viết hoa). (§2.8)
9. Chạy `ant test` trong Dockerfile / thêm GitHub Actions CI. (§5)
10. Thêm `USER` non-root trong Dockerfile. (§3.4)

### Ưu tiên thấp (nice-to-have)
11. Chuyển sang SQLite/H2 thay vì file serialization (an toàn hơn, query dễ hơn).
12. Thêm HEALTHCHECK / wrapper script nếu muốn chạy như service.
13. Viết thêm unit test cho `OrderList`, `SetMenuDAO`, `MenuFileHelper`.

---

## 8. KẾT LUẬN

Dự án **Traditional Feast Order Management** là một bài tập/thực hành **kiến trúc phần mềm tốt**, thể hiện rõ tư duy DAO Pattern, phân tầng trách nhiệm, Dependency Inversion và có tài liệu thiết kế root-cause rất ấn tượng (`PROPOSAL_*.md`, `README.md` có Sequence Diagram). Đây là mức **trên trung bình** so với đồ án sinh viên cùng cấp.

Tuy nhiên, để đạt mức **xuất sắc / production-ready**, cần khắc phục:
- **1 bug logic nghiêm trọng** (mã đơn sai định dạng do nhầm `mm`/`MM`),
- **1 lỗi UX** (`getInt` không loop),
- và đặc biệt **nâng cấp Dockerfile** (`.dockerignore`, multi-stage JRE, volume persist, non-root) vì hiện tại Dockerfile mới chỉ "chạy được" chứ chưa tuân thủ best practice và sẽ **mất toàn bộ dữ liệu** khi container bị xóa.

**Đánh giá tổng thể: 6.1 / 10** — Đạt yêu cầu môn học, còn dư địa cải thiện rõ rệt ở tầng Docker và vài chi tiết correctness.

---

*Đánh giá dựa trên việc đọc trực tiếp toàn bộ source code (`src/`), `Dockerfile`, `build.xml`, `README.md`, `PROPOSAL_*.md` và `CustomerListTest.java` tại thời điểm 2026-09-19.*
