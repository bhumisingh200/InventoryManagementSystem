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
}