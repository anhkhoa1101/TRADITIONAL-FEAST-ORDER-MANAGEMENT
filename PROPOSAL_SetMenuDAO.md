# ĐỀ XUẤT CHỈNH SỬA — SetMenuDAO.java (dựa trên MenuFileHelper.java)

Ngày: 2026-09-16
Tác giả: Hermes Agent

---

## 1. NGUYÊN NHÂN LỖI `invalid stream header: EFBBBF43`

Chuỗi lỗi được in ra từ `SetMenuDAO.readAll()`:

```java
System.out.println("Lỗi đọc file: " + e.getMessage());   // -> invalid stream header: EFBBBF43
```

Phân tích 4 byte đầu của file `FeastMenu.csv` (đọc ở dạng hex):

| Byte | Giá trị | Ý nghĩa |
|------|---------|---------|
| EF BB BF | UTF-8 BOM | Ký tự đánh dấu đầu file (thường do Notepad lưu UTF-8) |
| 43  | 'C'     | Chữ cái đầu của dòng `"Code,Name,Price,Ingredients"` |

=> File `FeastMenu.csv` là **file văn bản CSV**, không phải file nhị phân đã `serialize`.

Lỗi xảy ra vì `SetMenuDAO` đang dùng `FileHelper` để đọc file này:

- `FileHelper` dùng `ObjectInputStream` để đọc/ghi object đã `Serializable`.
- `ObjectInputStream` kỳ vọng "magic header" `AC ED 00 05`. Khi gặp `EF BB BF 43` nó ném
  `java.io.StreamCorruptedException: invalid stream header: EFBBBF43`.
- `FileHelper` đúng là dành cho `*.dat` (Customer/Order), **không** dùng cho `*.csv`.

**Kết luận:** Sai công cụ đọc file. Phải chuyển `SetMenuDAO` sang dùng `MenuFileHelper`
(helper đọc/ghi CSV văn bản, đã implement `IFileIO<SetMenu>`).

---

## 2. HƯỚNG SỬA (tuân thủ OOP)

| Nguyên tắc | Hiện tại | Sau khi sửa |
|------------|----------|-------------|
| **is-a**   | `SetMenuDAO implements ISetMenuDAO` ✓<br>`MenuFileHelper implements IFileIO<SetMenu>` ✓ | Giữ nguyên ✓ |
| **has-a**  | `SetMenuDAO` **has-a** `FileHelper<SetMenu>` (sai loại helper) | `SetMenuDAO` **has-a** `IFileIO<SetMenu>` (helper đọc CSV) ✓ |
| **coupling** | DAO phụ thuộc class cụ thể `FileHelper` (chặt) | DAO phụ thuộc interface `IFileIO<SetMenu>` (lỏng) ✓ |

- `MenuFileHelper` IS-A `IFileIO<SetMenu>` → dùng làm helper là đúng quan hệ is-a.
- `SetMenuDAO` KHÔNG được kế thừa từ helper (DAO và file-IO là 2 trách nhiệm khác nhau) →
  dùng quan hệ **has-a** (chứa một `IFileIO<SetMenu>`), không dùng is-a.
- Khai báo field theo **interface** `IFileIO<SetMenu>` thay vì class `FileHelper` để giảm coupling:
  DAO chỉ biết "có thể đọc/ghi SetMenu", không biết chi tiết là binary hay CSV.

---

## 3. CÁC CHỖ CẦN SỬA

### 3.1 SetMenuDAO.java  (thay đổi chính)

**Trước:**
```java
package DataObjects;

import Core.Entities.SetMenu;
import Core.Interfaces.ISetMenuDAO;
import Utilities.FileIO.FileHelper;          // <- SAI: helper đọc nhị phân
import Utilities.FileIO.MenuFileHelper;
import java.util.ArrayList;
import java.util.List;

public class SetMenuDAO implements ISetMenuDAO {

    private static final String FILE_NAME = "src/DataObjects/data/FeastMenu.csv";
    private final FileHelper<SetMenu> fileIO;   // <- SAI kiểu
    private List<SetMenu> menuList;

    public SetMenuDAO() {
        fileIO = new FileHelper<>(FILE_NAME);   // <- SAI: đọc CSV bằng ObjectInputStream
        menuList = readAll();
    }

    @Override
    public List<SetMenu> readAll() {
        try {
            return fileIO.readFromFile();
        } catch (Exception e) {
            System.out.println("Lỗi đọc file: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean writeAll(List<SetMenu> list) {
        try {
            return fileIO.saveToFile(list);
        } catch (Exception e) {
            System.out.println("Lỗi ghi file: " + e.getMessage());
            return false;
        }
    }

    // add / update / delete / findByID giữ nguyên (không đổi logic)
    @Override public boolean add(SetMenu s) { menuList.add(s); return writeAll(menuList); }

    @Override public boolean update(SetMenu s) {
        for (int i = 0; i < menuList.size(); i++) {
            if (menuList.get(i).getMenuID().equals(s.getMenuID())) {
                menuList.set(i, s); return writeAll(menuList);
            }
        }
        return false;
    }

    @Override public boolean delete(String menuID) {
        boolean removed = menuList.removeIf(s -> s.getMenuID().equals(menuID));
        return removed && writeAll(menuList);
    }

    @Override public SetMenu findByID(String menuID) {
        return menuList.stream().filter(s -> s.getMenuID().equals(menuID)).findFirst().orElse(null);
    }
}
```

**Sau (đề xuất):**
```java
package DataObjects;

import Core.Entities.SetMenu;
import Core.Interfaces.ISetMenuDAO;
import Utilities.FileIO.IFileIO;            // <- dùng interface (giảm coupling)
import Utilities.FileIO.MenuFileHelper;
import java.util.ArrayList;
import java.util.List;

public class SetMenuDAO implements ISetMenuDAO {

    private static final String FILE_NAME = "src/DataObjects/data/FeastMenu.csv";
    private final IFileIO<SetMenu> fileIO;    // <- has-a interface, đúng helper CSV
    private List<SetMenu> menuList;

    public SetMenuDAO() {
        fileIO = new MenuFileHelper(FILE_NAME);  // <- đúng: đọc/ghi CSV văn bản
        menuList = readAll();
    }

    @Override
    public List<SetMenu> readAll() {
        try {
            return fileIO.readFromFile();
        } catch (Exception e) {
            System.out.println("Lỗi đọc file: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean writeAll(List<SetMenu> list) {
        try {
            return fileIO.saveToFile(list);
        } catch (Exception e) {
            System.out.println("Lỗi ghi file: " + e.getMessage());
            return false;
        }
    }

    // add / update / delete / findByID GIỮ NGUYÊN (chỉ đổi kiểu field bên trên)
    @Override public boolean add(SetMenu s) { menuList.add(s); return writeAll(menuList); }

    @Override public boolean update(SetMenu s) {
        for (int i = 0; i < menuList.size(); i++) {
            if (menuList.get(i).getMenuID().equals(s.getMenuID())) {
                menuList.set(i, s); return writeAll(menuList);
            }
        }
        return false;
    }

    @Override public boolean delete(String menuID) {
        boolean removed = menuList.removeIf(s -> s.getMenuID().equals(menuID));
        return removed && writeAll(menuList);
    }

    @Override public SetMenu findByID(String menuID) {
        return menuList.stream().filter(s -> s.getMenuID().equals(menuID)).findFirst().orElse(null);
    }
}
```

**Tóm tắt thay đổi SetMenuDAO.java:**
1. Bỏ `import Utilities.FileIO.FileHelper;` (không dùng nữa).
2. Thêm `import Utilities.FileIO.IFileIO;`.
3. Đổi `private final FileHelper<SetMenu> fileIO;` → `private final IFileIO<SetMenu> fileIO;`.
4. Đổi `fileIO = new FileHelper<>(FILE_NAME);` → `fileIO = new MenuFileHelper(FILE_NAME);`.
5. Logic `readAll/writeAll/add/update/delete/findByID` **giữ nguyên** (đã đúng, chỉ đổi kiểu field).

---

### 3.2 MenuFileHelper.java  (chỉnh phụ — BẮT BUỘC để chạy được)

`MenuFileHelper` hiện tại còn 3 vấn đề khiến sửa `SetMenuDAO` xong vẫn không chạy đúng:

1. **Đường dẫn bị hardcode sai:** `private final String FILE_NAME = "src\\fileio\\FeastMenu.csv";`
   → thư mục `fileio` không tồn tại. Phải nhận đường dẫn từ DAO (single source of truth).
2. **Không chỉ định charset:** `new InputStreamReader(file)` dùng charset mặc định của Windows
   (Cp1252) → tiếng Việt (Súp, Gỏi, bông,...) bị móp. Phải dùng `StandardCharsets.UTF_8`.
3. **`saveToFile` chỉ `return true`** mà không ghi gì → `add/update/delete` báo thành công
   nhưng dữ liệu không lưu xuống file.

**Sau (đề xuất):**
```java
package Utilities.FileIO;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import Core.Entities.SetMenu;

public class MenuFileHelper implements IFileIO<SetMenu> {

    private final String filePath;

    // Constructor nhận đường dẫn -> DAO giữ quyền định nghĩa path (single source of truth)
    public MenuFileHelper(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<SetMenu> readFromFile() throws Exception {
        List<SetMenu> list = new ArrayList<>();
        File f = new File(filePath);
        if (!f.exists()) {
            return list;                       // chưa có file -> rỗng, không ném lỗi
        }
        try (FileInputStream file = new FileInputStream(f);
             BufferedReader myInput = new BufferedReader(
                     new InputStreamReader(file, StandardCharsets.UTF_8))) {

            String line;
            boolean first = true;
            while ((line = myInput.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (first) {                   // bỏ dòng header "Code,Name,Price,Ingredients"
                    first = false;
                    continue;
                }
                if (line.startsWith("\uFEFF")) {        // strip BOM nếu có
                    line = line.substring(1);
                }
                list.add(convertToMenu(line));
            }
        }
        return list;
    }

    @Override
    public boolean saveToFile(List<SetMenu> list) throws Exception {
        File f = new File(filePath);
        try (FileOutputStream fos = new FileOutputStream(f);
             BufferedWriter bw = new BufferedWriter(
                     new OutputStreamWriter(fos, StandardCharsets.UTF_8))) {
            bw.write("Code,Name,Price,Ingredients");
            bw.newLine();
            for (SetMenu s : list) {
                bw.write(s.getMenuID() + "," + s.getMenuName() + ","
                        + s.getPrice() + "," + s.getIngredients());
                bw.newLine();
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private SetMenu convertToMenu(String str) {
        String[] p = str.split(",", -1);
        String code = p[0].trim();
        String name = p[1].trim();
        double price = Double.parseDouble(p[2].trim());
        String ingredients = p.length > 3 ? p[3].trim() : "";
        return new SetMenu(code, name, price, ingredients);
    }
}
```

> Lưu ý nhỏ về `convertToMenu`: data hiện tại không có dấu phẩy bên trong chuỗi
> `Ingredients` (các món ngăn cách bằng `;` và `#+`) nên `split(",")` vẫn đúng.
> Nếu sau này ingredients có chứa dấu phẩy, cần parser CSV chuẩn (ví dụ `OpenCSV`)
> hoặc escape dấu phẩy bằng dấu ngoặc kép. Ở mức lab hiện tại, giữ `split` là đủ.

---

## 4. KIỂM CHỨNG SAU KHI SỬA

1. Build: chạy lại dự án (NetBeans / `javac`).
2. Lỗi `invalid stream header: EFBBBF43` phải biến mất.
3. Menu hiển thị đầy đủ 6 món (PW001..PW006) với tiếng Việt đúng (Súp, Gỏi, ...).
4. Thử `add` một món mới rồi thoát/chạy lại → món vẫn còn (kiểm tra `saveToFile` hoạt động).

---

## 5. TÓM TẮT

- Lỗi do **dùng sai helper**: `FileHelper` (nhị phân) đọc file `FeastMenu.csv` (văn bản).
- Sửa `SetMenuDAO` sang dùng `MenuFileHelper` (helper CSV), khai báo field theo interface
  `IFileIO<SetMenu>` để giảm coupling (has-a interface, không is-a class cụ thể).
- Đi kèm phải sửa `MenuFileHelper`: nhận path qua constructor, đọc UTF-8 + strip BOM,
  và implement `saveToFile` thực sự ghi file.
