package Presentation;

import BusinessObject.CustomerList;
import BusinessObject.OrderList;
import BusinessObject.SetMenuList;
import Utilities.Inputter;

import DataObjects.OrderDAO;
import DataObjects.CustomerDAO;
import DataObjects.SetMenuDAO;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class Program {
    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // UTF-8 luôn được JVM hỗ trợ, nhánh này thực tế không bao giờ xảy ra.
        }

        Inputter in = new Inputter();
        CustomerList customerList = new CustomerList(new CustomerDAO());
        SetMenuList setMenuList = new SetMenuList(new SetMenuDAO());
        OrderList orderList = new OrderList(new OrderDAO(), customerList, setMenuList); // dùng chung 2 object trên

        Menu menu = new Menu(in, customerList, orderList, setMenuList);
        menu.run();
    }
}