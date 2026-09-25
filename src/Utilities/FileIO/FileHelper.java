package Utilities.FileIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

import java.io.EOFException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class FileHelper<E> implements IFileIO<E> {

    private final String filePath;

    public FileHelper(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Đọc toàn bộ dữ liệu từ file nhị phân.
     * Dùng EOFException để xác định điểm kết thúc file.
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<E> readFromFile() throws Exception {
        List<E> result = new ArrayList<>();
        File f = new File(filePath);

        if (!f.exists()) {
            System.out.println("File not found ..!\"" + filePath + "\"");
            return result;
        }

        try (FileInputStream fis = new FileInputStream(f);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            while (true) {
                E item = (E) ois.readObject();
                result.add(item);
            }

        } catch (EOFException eof) {
            // Đã đọc hết dữ liệu -> điều kiện dừng hợp lệ, không phải lỗi
        }

        return result;
    }

    /**
     * Ghi danh sách object xuống file nhị phân.
     * Trả về true nếu ghi thành công, false nếu thất bại.
     */

    @Override
    public boolean saveToFile(List<E> list) throws Exception {
        File f = new File(filePath);

        try (FileOutputStream fos = new FileOutputStream(f);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            for (E item : list) {
                oos.writeObject(item);
            }
            return true;

        } catch (IOException ex) {
            return false;
        }
    }
}