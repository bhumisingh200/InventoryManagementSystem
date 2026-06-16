import java.util.Scanner;
import model.Product;
import service.InventoryService;

public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter your name: ");

        String name = sc.nextLine();
        System.out.println("Welcome " + name + " to the Inventory Management System!");

        boolean running=true;
        InventoryService inventory =new InventoryService();
        while(running){
            System.out.println("\n===== INVENTORY MENU =====");
            System.out.println("1. Add Product");
            System.out.println("2. View Products");
            System.out.println("3. Search Product");
            System.out.println("4. Update Product");
            System.out.println("5. Delete Product");
            System.out.println("6. Exit");

            System.out.println("Enter Your Choice");
            int choice = sc.nextInt();
            
            switch(choice) {
                case 1:
                    System.out.print("Enter Product ID to Add: ");
                    int addid = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Enter Product Name: ");
                    String addname = sc.nextLine();

                    System.out.print("Enter Category: ");
                    String addcategory = sc.nextLine();

                    System.out.print("Enter Price: ");
                    double addprice = sc.nextDouble();

                    System.out.print("Enter Quantity: ");
                    int addquantity = sc.nextInt();

                    Product product = new Product(addid,addname,addcategory,addprice,addquantity);

                    inventory.addProduct(product);
                    break;
                
                case 2:
                    inventory.viewProducts();
                    break;

                case 3:
                    System.out.print("Enter Product ID to Search: ");
                    int searchid = sc.nextInt();
                    inventory.searchProduct(searchid);
                    break;

                case 4:
                    System.out.print("Enter Product ID: ");
                    int updateid = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Enter Product Name: ");
                    String updatename = sc.nextLine();

                    System.out.print("Enter Product Category: ");
                    String updatecategory = sc.nextLine();

                    System.out.print("Enter Product Price: ");
                    double updateprice = sc.nextDouble();

                    System.out.print("Enter Product Quantity: ");
                    int updatequantity = sc.nextInt();

                    inventory.updateProduct(updateid,updatename,updatecategory,updateprice,updatequantity);
                    break;

                case 5:
                    System.out.print("Enter Product ID to delete:");
                    int deleteid = sc.nextInt();
                    inventory.deleteProduct(deleteid);
                    break;

                case 6:
                    running=false;
                    System.out.println("Exiting...");
                    break;

                default:
                    System.out.println("Invalid Choice");
            }
        }

        sc.close();
    }
}