
package Core.Entities;

import java.io.Serializable;

public class SetMenu implements Serializable{
    private static final long serialVersionUID = 1L;
    private String menuID;
    private String menuName;
    private double price;
    private String ingredients;

    public SetMenu() {
    }

    public SetMenu(String menuID, String menuName, double price, String ingredients) {
        this.menuID = menuID;
        this.menuName = menuName;
        this.price = price;
        this.ingredients = ingredients;
    }

    public String getMenuID() {return menuID;}

    public void setMenuID(String menuID) {this.menuID = menuID;}

    public String getMenuName() {return menuName;}

    public void setMenuName(String menuName) {this.menuName = menuName;}

    public double getPrice() {return price;}

    public void setPrice(double price) {this.price = price;}

    public String getIngredients() {return ingredients;}

    public void setIngredients(String ingredients) {this.ingredients = ingredients;}

    @Override
    public String toString() {
        return String.format(
                "\n--------------------------------------------------------------\n" +
                        "Menu ID     : %s%n" +
                        "Menu name   : %s%n" +
                        "Price       : %s%n" +
                        "Ingredients : %s%n" +
                "\n--------------------------------------------------------------\n",
                menuID, menuName, price, formatIngredients(ingredients)
        );
    }

    private String formatIngredients(String ingredients) {
        return ingredients
                .replace("#", "\n")
                .replace("+ ", "")
                .replace(": ", ":\n  - ")
                .replace("; ", "\n  - ");
    }
}
