package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {
    private ProductDao productDao;

    @Autowired
    public MySqlShoppingCartDao(DataSource dataSource, ProductDao productDao) {
        super(dataSource);
        this.productDao = productDao;
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        ShoppingCart cart = new ShoppingCart();
        String sql = "SELECT sc.user_id, sc.product_id, sc.quantity " +
                "FROM shopping_cart sc " +
                "WHERE sc.user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet row = statement.executeQuery()) {
                while (row.next()) {
                    int productId = row.getInt("product_id");
                    int quantity = row.getInt("quantity");

                    Product product = productDao.getById(productId);

                    if (product != null) {
                        ShoppingCartItem item = new ShoppingCartItem();
                        item.setProduct(product);
                        item.setQuantity(quantity);
                        item.setLineTotal(product.getPrice().multiply(new java.math.BigDecimal(quantity)));

                        cart.addProduct(item);
                    } else {
                        System.err.println("Product with ID " + productId + " in cart not found in products table. Skipping item.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting shopping cart by user ID: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error getting shopping cart by user ID.", e);
        }
        return cart;
    }

    @Override
    public void addProductToCart(int userId, int productId) {
        String checkSql = "SELECT quantity FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        String insertSql = "INSERT INTO shopping_cart (user_id, product_id, quantity, date_added) VALUES (?, ?, ?, ?)";
        String updateSql = "UPDATE shopping_cart SET quantity = ?, date_added = ? WHERE user_id = ? AND product_id = ?";

        // Declare connection outside the try-with-resources to make it accessible in catch block
        Connection connection = null; // Initialize to null
        try {
            connection = getConnection(); // Get connection here
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {
                checkStatement.setInt(1, userId);
                checkStatement.setInt(2, productId);

                try (ResultSet rs = checkStatement.executeQuery()) {
                    LocalDateTime now = LocalDateTime.now();
                    String formattedDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    if (rs.next()) {
                        // Product exists, update quantity
                        int currentQuantity = rs.getInt("quantity");
                        try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                            updateStatement.setInt(1, currentQuantity + 1);
                            updateStatement.setString(2, formattedDateTime);
                            updateStatement.setInt(3, userId);
                            updateStatement.setInt(4, productId);
                            updateStatement.executeUpdate();
                        }
                    } else {
                        // Product does not exist, insert new row
                        try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                            insertStatement.setInt(1, userId);
                            insertStatement.setInt(2, productId);
                            insertStatement.setInt(3, 1); // Add with quantity 1
                            insertStatement.setString(4, formattedDateTime);
                            insertStatement.executeUpdate();
                        }
                    }
                }
            }
            connection.commit(); // Commit transaction
        } catch (SQLException e) {
            try {
                if (connection != null) connection.rollback(); // Access connection directly
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }
            System.err.println("Error adding product to cart: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error adding product to cart.", e);
        }
    }

    @Override
    public void updateProductQuantity(int userId, int productId, int quantity) {
        String sql = "UPDATE shopping_cart SET quantity = ?, date_added = ? WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            LocalDateTime now = LocalDateTime.now();
            String formattedDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            statement.setInt(1, quantity);
            statement.setString(2, formattedDateTime);
            statement.setInt(3, userId);
            statement.setInt(4, productId);

            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating product quantity in cart: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error updating product quantity in cart.", e);
        }
    }

    @Override
    public void removeProductFromCart(int userId, int productId) {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setInt(2, productId);

            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error removing product from cart: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error removing product from cart.", e);
        }
    }

    @Override
    public void clearCart(int userId) {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error clearing shopping cart: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error clearing shopping cart.", e);
        }
    }

    protected ShoppingCartItem mapRowToShoppingCartItem(ResultSet row) throws SQLException {
        int productId = row.getInt("product_id");
        int quantity = row.getInt("quantity");

        Product product = productDao.getById(productId);

        if (product == null) {
            System.err.println("Warning: Product with ID " + productId + " not found for shopping cart item.");
            return null;
        }

        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setLineTotal(product.getPrice().multiply(new java.math.BigDecimal(quantity)));

        return item;
    }
}
