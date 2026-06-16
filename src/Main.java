// This is the main class that serves as the entry point of the Inventory Management System application. 
// It demonstrates how to use the InventoryService to manage products in the inventory.
import model.Product;
import service.InventoryService;

public class Main {

    public static void main(String[] args) {

        InventoryService inventory =new InventoryService();

        inventory.addProduct(
                new Product(
                        101,
                        "Laptop",
                        "Electronics",
                        55000,
                        10));

        inventory.addProduct(
                new Product(
                        102,
                        "Mouse",
                        "Electronics",
                        500,
                        50));

        inventory.addProduct(
                new Product(
                        103,
                        "Keyboard",
                        "Electronics",
                        1500,
                        20));

        inventory.viewProducts();
        inventory.searchProduct(102);// Searching for a product by its ID
        inventory.searchProduct(999);// Searching for a non-existing product
    }
}