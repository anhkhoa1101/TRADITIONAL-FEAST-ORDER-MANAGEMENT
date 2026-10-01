package Core.Entities;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Order implements Serializable{
    private static final long serialVersionUID = 1L;
    private String orderCode;
    private Customer customerID;
    private SetMenu menuID;
    private int numOfTables;
    private Date eventDate;

    private String generateOrderCode(){
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(now);
    }
    public Order() {
        this.orderCode = generateOrderCode();
        this.menuID = null;
        this.customerID = null;
        this.eventDate = new Date();
    }

    public Order(Customer customerID, SetMenu menuID, int numOfTables, Date eventDate) {
        this.orderCode = generateOrderCode();
        this.customerID = customerID;
        this.menuID = menuID;
        this.numOfTables = numOfTables;
        this.eventDate = eventDate;
    }

    public String getOrderCode() {return orderCode;}

    public void setOrderCode(String orderCode) {this.orderCode = orderCode;}

    public Customer getCustomerID() {return customerID;}

    public void setCustomerID(Customer customerID) {this.customerID = customerID;}

    public SetMenu getMenuID() {return menuID;}

    public void setMenuID(SetMenu menuID) {this.menuID = menuID;}

    public int getNumOfTables() {return numOfTables;}

    public void setNumOfTables(int numOfTables) {this.numOfTables = numOfTables;}

    public Date getEventDate() {return eventDate;}

    public void setEventDate(Date eventDate) {this.eventDate = eventDate;}

    @Override
    public String toString() {
        return String.format(
                "\n--------------------------------------------------------------\n" +
                        "Order code    : %s%n" +
                        "Customer      : %s%n" +
                        "Menu ID       : %s%n" +
                        "Number tables : %d%n" +
                        "Event date    : %s%n" +
                "\n--------------------------------------------------------------\n",
                orderCode, customerID, menuID, numOfTables, eventDate
        );
    }
}
