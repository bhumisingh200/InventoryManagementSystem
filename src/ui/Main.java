package ui;

import database.ProductDAO;

public class Main {

    public static void main(String[] args) {
        ProductDAO dao = new ProductDAO();
        dao.inventoryDashboard();
        dao.viewProducts();
    }
}
