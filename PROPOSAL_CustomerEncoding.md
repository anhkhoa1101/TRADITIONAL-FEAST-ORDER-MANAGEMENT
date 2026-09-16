# ĐỀ XUẤT FIX — MẤT CHỮ TIẾNG VIỆT KHI NHẬP KH (Customer)

Ngày: 2026-09-16
Tác giả: Hermes Agent

---

## 1. TRIỆU CHỨNG

Nhập `Nghĩa Ròm` → kết quả lưu/hiển thị: `Ngh?a Róm` (mất dấu, lẫn ký tự lạ).

`Customer{id='C0002', name='Ngh?a Róm', phone='0123456789', email='1111akhoa1111@gmail.com'}`

---

## 2. PHÂN TÍCH GỐC RỄ (đã xác minh bằng hexdump file .dat)

File `src/DataObjects/Data/customers.dat` lưu dạng nhị phân (ObjectOutputStream),
hex của trường `name`:

```
C0001: 4e 67 75 79 3f 6e 20 48 6f ef bf bd 6e 67 ...
       =  N  g  u  y  ?  n     H  o  [U+FFFD] n  g  ...   -> "Nguy?n Ho?ng Anh Khoat"
C0002: 4e 67 68 3f 61 20 52 ef bf bd 6d
       =  N  g  h  ?  a     R  [U+FFFD] m           -> "Ngh?a R?m"
```

=> Dữ liệu **đã bị hỏng ở dạng byte bên trong file**, không phải lỗi in ra màn hình.
Chữ `?` (0x3F) và `U+FFFD` (ef bf bd = ký tự thay thế) là dấu hiệu kinh điển của
**lệch mã hóa (charset mismatch)** giữa hai đầu:

- Console (cửa sổ terminal) gửi/nhận byte theo một bảng mã.
- JVM (`Scanner(System.in)` và `System.out`) giải mã theo bảng mã **khác**.

Phân tích cụ thể từ 2 ký tự:
- `ĩ` (trong "Nghĩa") → thành `?` (0x3F): bảng mã console **không có** ký tự `ĩ`
  (VD: Windows-1252 / CP1252 thiếu `ĩ`), nên bàn phím/environment thay bằng `?`.
- `ò` (trong "Ròm") → thành `U+FFFD`: console gửi `ò` là 1 byte (CP1252 = 0xF2),
  nhưng `Scanner` lại giải mã theo **UTF-8** → 0xF2 không phải UTF-8 hợp lệ → thay bằng U+FFFD.

=> Kết luận: **Console đang dùng CP1252, còn JVM mặc định lại giải mã theo UTF-8.**
Hai bên không thống nhất → mất chữ tiếng Việt.

Chú ý: lỗi này nằm ở khâu NHẬP (console ↔ Scanner), KHÔNG nằm ở `Customer`,
`CustomerDAO`, `FileHelper` (ObjectOutputStream lưu Unicode rất tốt). Vì vậy:
- `Core.Entities.Customer` : KHÔNG cần sửa.
- `DataObjects.CustomerDAO` / `Utilities.FileIO.FileHelper` : KHÔNG cần sửa.
- `Utilities.Inputter` (chỗ đọc `Scanner`) : CẦN sửa (gắn charset UTF-8 rõ ràng).
- `Presentation.Program` / `Presentation.Menu` (chỗ in `System.out`) : CẦN sửa (gắn charset UTF-8 rõ ràng).

---

## 3. HƯỚNG FIX (tuân thủ OOP)

| Nguyên tắc | Nhận xét |
|------------|----------|
| is-a | Không thay đổi quan hệ kế thừa. `Inputter` vẫn là lớp tiện ích (utility), `Program` vẫn là entry-point. |
| has-a | `Inputter` **has-a** `Scanner`. Ta chỉ đổi cách khởi tạo `Scanner` (truyền `InputStreamReader` + `UTF-8`) — vẫn là has-a, không đổi cấu trúc. |
| coupling | `Inputter` tự lo encoding bên trong; `Menu`/`Customer` không cần biết chi tiết. Không tăng coupling. |

Fix tập trung vào 1 lớp `Inputter` (điểm đọc input duy nhất) + 1 chỗ set encoding cho
`System.out` (ở `Program.main`), đúng nguyên tắc "đóng gói" (encapsulation): lớp đọc
input chịu trách nhiệm mã hóa, các lớp trên không bị ảnh hưởng.

---

## 4. CÁC CHỖ CẦN SỬA

### 4.1 Utilities/Inputter.java — dùng Scanner UTF-8 (BẮT BUỘC)

**Trước:**
```java
package Utilities;

import Utilities.Validation.BaseValidation;
import java.util.Scanner;

public class Inputter {
    private Scanner scanner;

    public Inputter() {
        this.scanner = new Scanner(System.in);   // <- dùng charset mặc định (lệch với console)
    }

    public String getString(String mess) {
        System.out.println(mess);
        return scanner.nextLine();
    }
    // ...
}
```

**Sau (đề xuất):**
```java
package Utilities;

import Utilities.Validation.BaseValidation;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Inputter {
    private final Scanner scanner;

    public Inputter() {
        // Bắt buộc chỉ định UTF-8 để khớp với console UTF-8 (Windows Terminal / chcp 65001)
        this.scanner = new Scanner(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));
    }

    public String getString(String mess) {
        System.out.println(mess);
        return scanner.nextLine();
    }
    // getInt / getDouble / inputAndLoop giữ nguyên (không đổi logic)
}
```

> Chỉ đổi constructor `Scanner`. Các method `getString/getInt/getDouble/inputAndLoop`
> giữ nguyên 100%.

### 4.2 Presentation/Program.java — set System.out sang UTF-8 (BẮT BUỘC để hiển thị đúng)

**Trước:**
```java
public class Program {
    public static void main(String[] args) {
        Menu menu = new Menu();
        menu.run();
    }
}
```

**Sau (đề xuất):**
```java
package Presentation;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class Program {
    public static void main(String[] args) {
        try {
            // Java 8: PrintStream(OutputStream, boolean, String) tồn tại và ném
            // UnsupportedEncodingException (checked) -> phải bọc try/catch.
            // (Overload nhận Charset chỉ có từ Java 10 trở lên.)
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // UTF-8 luôn được JVM hỗ trợ, nhánh này thực tế không bao giờ xảy ra.
            // Nếu lỗi, giữ nguyên System.out mặc định của JVM.
        }
        Menu menu = new Menu();
        menu.run();
    }
}
```

> ⚠️ LỖI ĐÃ GẶP: `Unhandled exception: java.io.UnsupportedEncodingException`
> Nguyên nhân: dự án chạy **Java 8** (1.8.0_202). Constructor
> `PrintStream(OutputStream, boolean, Charset)` (nhận `StandardCharsets.UTF_8`) **chỉ có từ Java 10**.
> Trên Java 8, NetBeans ép sang overload `PrintStream(OutputStream, boolean, String)`, và overload này
> ném checked exception `UnsupportedEncodingException` chưa được bắt → báo lỗi.
> Cách fix đúng Java 8: truyền chuỗi `"UTF-8"` và bọc `try/catch (UnsupportedEncodingException)`.
> `Inputter.java` dùng `InputStreamReader(System.in, StandardCharsets.UTF_8)` thì an toàn trên Java 8
> (overload `InputStreamReader(InputStream, Charset)` có từ Java 8).

> Nhờ `Menu` dùng `System.out.println(...)` để in, chỉ cần set 1 lần ở `Program.main`
> là toàn bộ output (kể cả `displayLists`, `toString` của Customer) đều UTF-8.

---

## 5. BƯỚC QUAN TRỌNG: KHỚP TERMINAL VỚI UTF-8

Code trên chỉ đúng khi **terminal thật sự gửi UTF-8**. Nếu console vẫn CP1252 mà ta ép
Java UTF-8 thì input sẽ bị lệch ngược. Chọn 1 trong các cách:

1. **Windows Terminal / Git Bash** (mặc định UTF-8) — cách đơn giản nhất, khuyên dùng.
2. **CMD / PowerShell cũ**: gõ `chcp 65001` trước khi chạy `java`.
3. **NetBeans**: chuột phải project → Properties → Sources → Encoding → chọn `UTF-8`;
   và chạy trong cửa sổ Output của NetBeans (mặc định UTF-8 trên bản mới).
4. **Chạy bằng tham số JVM** (đề phòng): `java -Dfile.encoding=UTF-8 -cp ... Presentation.Program`

=> Quy tắc: **Console và Java phải cùng UTF-8**.

---

## 6. DỮ LIỆU CŨ ĐÃ BỊ HỎNG — CẦN XỬ LÝ

Hai dòng `C0001`, `C0002` trong `customers.dat` đã lưu `?` / U+FFFD vĩnh viễn (không thể
"cứu" lại thành "Nguyễn"/"Nghĩa" được nữa). Sau khi sửa code, bạn có 2 chọn:

- **Cách A (gọn):** xóa file `src/DataObjects/Data/customers.dat` (và bản build
  `build/classes/DataObjects/Data/customers.dat`) để đăng ký lại từ đầu.
- **Cách B:** dùng chức năng Update (menu 2) nhập lại tên đúng cho C0001/C0002.

> Lưu ý: `customers.dat` nằm ở 2 chỗ (source + build). Xóa cả 2 hoặc chạy Clean/Build
> lại để tránh đọc nhầm bản cũ.

---

## 7. KIỂM CHỨNG

1. Sửa `Inputter.java` + `Program.java` như mục 4.
2. Chạy trong terminal UTF-8 (Windows Terminal / `chcp 65001`).
3. Chức năng 1 → nhập `Nghĩa Ròm` → in ra phải đúng `Nghĩa Ròm` (không còn `?`).
4. Chức năng 8 → danh sách hiển thị tiếng Việt đúng.
5. Thoát/chạy lại → dữ liệu vẫn đúng (vì đã lưu Unicode đúng vào .dat).

---

## 8. TÓM TẮT

- Lỗi do **lệch mã hóa**: console CP1252 ↔ JVM UTF-8 (hoặc ngược lại).
- Sửa `Inputter`: `Scanner` gắn `StandardCharsets.UTF_8`.
- Sửa `Program.main`: `System.out` gắn `PrintStream` UTF-8.
- Đảm bảo terminal cũng UTF-8 (`chcp 65001` / Windows Terminal / NetBeans UTF-8).
- Dữ liệu cũ (C0001/C0002) đã hỏng → xóa .dat hoặc update lại.
- Không sửa `Customer`, `CustomerDAO`, `FileHelper` (chúng xử lý Unicode đúng).
