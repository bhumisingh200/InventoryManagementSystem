// This class represents a product in the inventory with attributes like id, name, category, price, and quantity.
package model;

// This class represents a product in the inventory with attributes like id, name, category, price, and quantity.
public class Product {

    private int id;
    private String name;
    private String category;
    private double price;
    private int quantity;

    // Constructor to initialize the product attributes
    public Product(int id, String name, String category,
                   double price, int quantity) {

        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
    }

    //To fetch the product detail
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    //To send the product detail
    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}