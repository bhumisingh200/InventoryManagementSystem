// This class represents a product in the inventory with attributes like id, name, category, price, quantity, and image path.
package model;

public class Product {

    private int id;
    private String name;
    private String category;
    private double price;
    private int quantity;
    private String imagePath;

    // Constructors
    public Product(int id, String name, String category, double price, int quantity) {
        this(id, name, category, price, quantity, null);
    }

    public Product(int id, String name, String category, double price, int quantity, String imagePath) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.imagePath = imagePath;
    }

    // Getters and Setters
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

    public String getImagePath() {
        return imagePath;
    }

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

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}