package Presentation;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
/**
 *
 * @author khoa0
 */
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
