# ✨ EasyShop Backend API
## Project Overview
This project implements the backend API for an e-commerce application named "EasyShop". It provides RESTful endpoints for managing products, categories, user authentication, and shopping cart functionalities. The API is built with Spring Boot and interacts with a MySQL database.

### 🚀 Key Features
* **User Management & Security**: Secure registration, login, JWT-based access, and role-based permissions (admin vs. user).

* **Product Catalog**: Lists all products, provides details by ID, and can be extended for search/filtering.

* **Product Categories**: Lists all categories and products within specific categories.

* **Shopping Cart**: Manages user-specific carts, allowing additions, quantity updates, removals, and full clearance.

### 📸 Application Screens
* **Homepage with Products**:
 
![Home screen](https://github.com/user-attachments/assets/a4e21fa9-9bca-423e-b806-a7bce256bf5a)

* **Login Page:**

![admin login](https://github.com/user-attachments/assets/ae15f8f1-4bbc-4b07-a086-7d1bba04d24d)

* **Shopping Cart View:**

![cart](https://github.com/user-attachments/assets/d45e77c6-20f2-44fc-a821-98f001d70dd8)

### 🧑‍💻 Code Highlight: Shopping Cart Updates
One interesting part of the code is the ```addProductToCart``` method in ```MySqlShoppingCartDao.java```. This method makes adding items to the cart a smooth experience for the user, even if they add the same item multiple times.

Instead of just adding a new line every time, this code is :

* **Checking if the item is already there**: It first looks to see if the product is already in the user's shopping cart.

* **Updating Quantity (Not Duplicates)**: If the product is already in the cart, it simply increases the quantity of that item instead of creating a new entry. This keeps the cart neat and prevents clutter.

* **Consistently handling Transactions**: This whole process happens within a database "transaction." All changes (checking, adding, or updating) either complete successfully together, or if anything goes wrong, all changes are undone. This guarantees the shopping cart data is always accurate and doesn't get messed up halfway through an operation.

This piece of code ensures a reliable and intuitive cart experience.

``` java
// Inside MySqlShoppingCartDao.java
@Override
public void addProductToCart(int userId, int productId) {
Connection connection = null;
try {
connection = getConnection();
connection.setAutoCommit(false); // Starts a database transaction

        // SQL logic to check if product exists in cart
        String checkSql = "SELECT quantity FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        //  (PreparedStatement and ResultSet logic for check)

        if (rs.next()) {
            // If product is found, update its quantity
            String updateSql = "UPDATE shopping_cart SET quantity = ?, date_added = ? WHERE user_id = ? AND product_id = ?";
            //  (PreparedStatement for update) 
        } else {
            // If product is not found, insert as a new item
            String insertSql = "INSERT INTO shopping_cart (user_id, product_id, quantity, date_added) VALUES (?, ?, ?, ?)";
            // (PreparedStatement for insert)
        }

        connection.commit(); // Makes all changes permanent if no errors
    } catch (SQLException e) {
        // If an error happens, roll back all changes to keep data clean
        try { if (connection != null) connection.rollback(); } catch (SQLException ex) { /* log error */ }
        throw new RuntimeException("Error adding product to cart.", e);
    } finally {
        // Always return the connection to its default auto-commit state
        try { if (connection != null) connection.setAutoCommit(true); } catch (SQLException e) { /* log error */ }
    }
}
```
### 🎯 API Endpoints
The API base URL is ```http://localhost:8080```. All responses are JSON.

**Authentication & Users**

* ```POST /api/login```: Authenticate and get JWT token.

* ```POST /api/users```: Register new user.

**Product & Category Listing**

* ```GET /products```: All products.

* ```GET /products/{id}```: Specific product.

* ```GET /categories```: All categories.

* ```GET /categories/{categoryId}/products```: Products in a category.

**Shopping Cart (Requires Login)**

* ```GET /cart```: View your cart.

* ```POST /cart/products/{productId}```: Add product 

* ```PUT /cart/products/{productId}```: Update quantity 

* ```DELETE /cart/products/{productId}```: Remove a product.

* ``` DELETE /cart```: Clear entire cart.
