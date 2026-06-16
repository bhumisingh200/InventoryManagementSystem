// This class provides services to manage the inventory, such as adding products and viewing the product list.
package service;

import java.util.ArrayList;
import model.Product;

public class InventoryService {

    private ArrayList<Product> products =new ArrayList<>();

    //To add a new product to the inventory
    public void addProduct(Product product) {

        for(Product p : products){
            if(p.getId() == product.getId()){
                System.out.println("Product ID already exists!");
                return;
            }
        }
            products.add(product);
            System.out.println(product.getName() + " added successfully!");
    }

    //To view all products in the inventory
    public void viewProducts() {
        System.out.println("\n----- PRODUCT LIST -----");
        for(Product p : products) {
                 System.out.println(
                    "ID: " + p.getId()
                    + " | Name: " + p.getName()
                    + " | Category: " + p.getCategory()
                    + " | Price: " + p.getPrice()
                    + " | Quantity: " + p.getQuantity());
        }
    }

    //To search for a product by its ID
    public void searchProduct(int id) {
        for(Product p : products) {
            if(p.getId() == id) {
                System.out.println("\nProduct Found!");
                System.out.println(
                    "ID: " + p.getId()
                    + " | Name: " + p.getName()
                    + " | Category: " + p.getCategory()
                    + " | Price: " + p.getPrice()
                    + " | Quantity: " + p.getQuantity());
                    return;
                }
        }
        System.out.println("\nProduct with ID " + id + " not found.");
    }

    //To delete a product from the inventory by its ID
    public void deleteProduct(int id) {
        for(Product p : products) {
            if(p.getId() == id) {
                products.remove(p);
                System.out.println(p.getName() + " deleted successfully!");
                 return;
            }
        }
        System.out.println("\nProduct with ID " + id + " not found.");
    }

    public void updateProduct(
        int id,
        String newName,
        String newCategory,
        double newPrice,
        int newQuantity) {

            for(Product p : products) {
                if(p.getId() == id) {
                    p.setName(newName);
                    p.setCategory(newCategory);
                    p.setPrice(newPrice);
                    p.setQuantity(newQuantity);
                    System.out.println("Product updated successfully!");
                    return;
                }
            }
            System.out.println("Product not found!");
    }
}