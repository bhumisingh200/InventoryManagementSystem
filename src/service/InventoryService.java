// This class provides services to manage the inventory, such as adding products and viewing the product list.
package service;

//import java.util.ArrayList; (ARRAY)
import java.util.HashMap;
import model.Product;

public class InventoryService {

    private static final int LOW_STOCK_LIMIT = 10; //Low Stock Alert with logic (static final)

    private HashMap<Integer, Product> products =new HashMap<>(); //In the place of array I used Hashmap to reduce time complexity O(n) to O(1)

    //To add a new product to the inventory
    public void addProduct(Product product) {

        //for(Product p : products){   (ARRAY)
            //To prevent dublicate ID
            //for(Product p : products) { (ARRAY)
            //if(p.getId() == product.getId()) {  (ARRAY)
            if(products.containsKey(product.getId())) {
                System.out.println("Product ID already exists!");
                return;
            }
            //products.add(product); (ARRAY)
            products.put(product.getId(),product);
            System.out.println(product.getName() + " added successfully!");
    }

    //To view all products in the inventory
    public void viewProducts() {
        System.out.println("\n===== PRODUCT LIST =====");
        //for(Product p : products) { (ARRAY)
        for(Product p : products.values()) {
                 System.out.println(
                    "ID: " + p.getId()
                    + " || Name: " + p.getName()
                    + " ||Category: " + p.getCategory()
                    + " || Price: " + p.getPrice()
                    + " || Quantity: " + p.getQuantity());

                    if(p.getQuantity() < LOW_STOCK_LIMIT){
                        System.out.print("⚠ LOW STOCK ALERT");
                    }
        }
    }

    //To search for a product by its ID
    public void searchProduct(int id) {
        //for(Product p : products) { (ARRAY)
            //if(p.getId() == id) {  (ARRAY)
        Product p=products.get(id);
            if(p!=null){
                System.out.println("\nProduct Found!");
                System.out.println(
                    "ID: " + p.getId()
                    + " | Name: " + p.getName()
                    + " | Category: "+ p.getCategory()
                    + " | Price: " + p.getPrice()
                    + " | Quantity: " + p.getQuantity());
                    return;
            }
            System.out.println("\nProduct with ID " + id + " not found.");
    }

    //To delete a product from the inventory by its ID
    public void deleteProduct(int id) {
        //for(Product p : products) {  (ARRAY)
            //if(p.getId() == id) {  (ARRAY)
        Product removedProduct = products.remove(id); //(HashMap)
            if(removedProduct != null) {              //(HashMap)

                //products.remove(p); (ARRAY)
                //System.out.println(p.getName() + " deleted successfully!"); (ARRAY)
                System.out.println(removedProduct.getName()+ " deleted successfully!");
                 return;
            }else{
                System.out.println("\nProduct with ID " + id + " not found.");
            }
    }

    public void updateProduct(
        int id,
        String newName,
        String newCategory,
        double newPrice,
        int newQuantity) {

            //for(Product p : products) {   //(ARRAY)
                //if(p.getId() == id) {     //(ARRAY)
                 Product p = products.get(id);  //(HashMap)
                 if(p != null) {                //(HashMap)
                    p.setName(newName);
                    p.setCategory(newCategory);
                    p.setPrice(newPrice);
                    p.setQuantity(newQuantity);
                    System.out.println("Product updated successfully!");
                    return;
                }else{
                    System.out.println("Product not found!");
                }
    }

    //Total Product, Total Value & Highest Stock Product
    public void inventoryStats() {
        //To calculate no. of product
        int totalProducts = products.size();
        //initial value
        double totalValue = 0;
        //initial
        Product highestStockProduct = null;
        
        
        //for(Product p : products) {  (ARRAY)
        for(Product p : products.values()){     //HashMp
            totalValue += p.getPrice() * p.getQuantity();
            if(highestStockProduct == null || p.getQuantity() > highestStockProduct.getQuantity()) {
                 highestStockProduct = p;
                }
        }
        System.out.println("\n===== INVENTORY STATS =====");
        System.out.println("Total Products: " + totalProducts);
        System.out.println("Total Inventory Value: ₹" + totalValue);
        
        if(highestStockProduct != null) {
            System.out.println(
                "Highest Stock Product: "+ highestStockProduct.getName()+ " ("+highestStockProduct.getQuantity()+ " units)");
        }
    }
}