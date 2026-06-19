package controller;

import database.ProductDAO;
import model.Product;

import java.util.List;

public class ProductController {

    private ProductDAO dao = new ProductDAO();

    public void add(Product p) {
        dao.addProduct(p);
    }

    public void viewAll() {
        dao.viewProducts();
    }

    public void search(int id) {
        dao.searchProduct(id);
    }

    public void update(Product p) {
        dao.updateProduct(
                p.getId(),
                p.getName(),
                p.getCategory(),
                p.getPrice(),
                p.getQuantity()
        );
    }

    public void delete(int id) {
        dao.deleteProduct(id);
    }

    public void dashboard() {
        dao.inventoryDashboard();
    }

    public void lowStock() {
        dao.lowStockProducts();
    }

    public void searchByCategory(String cat) {
        dao.searchByCategory(cat);
    }

    public void export() {
        dao.exportReport();
    }

    public List<Product> getAllProducts() {
    return dao.getAllProducts();
}
}