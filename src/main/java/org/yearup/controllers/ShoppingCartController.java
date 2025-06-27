package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.User;

import java.security.Principal; // Import for Principal object
import java.util.Map; // For the PUT quantity update body

// Convert this class to a REST controller
@RestController
// Map the base URL for this controller
@RequestMapping("cart")
// Allow cross-origin requests for frontend integration
@CrossOrigin
// Only logged-in users should have access to these actions
// @PreAuthorize("isAuthenticated()") // Can be applied at class level for all methods
public class ShoppingCartController
{
    // A shopping cart requires
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao; // To get the user_id from the username

    // Each method in this controller requires a Principal object as a parameter
    // Use @Autowired for dependency injection
    @Autowired
    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
    }

    // GET: Get the shopping cart for the current user
    // URL: http://localhost:8080/cart
    // Requires a logged-in user
    @GetMapping
    @PreAuthorize("isAuthenticated()") // Ensures only authenticated users can access
    public ShoppingCart getCart(Principal principal)
    {
        try
        {
            // Get the currently logged-in username
            String userName = principal.getName();
            // Find database user by username to get their userId
            User user = userDao.getByUserName(userName);
            if (user == null) {
                // This case should ideally not happen if isAuthenticated() works,
                // but it's a good defensive check
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
            }
            int userId = user.getId();

            // Use the shoppingCartDao to get all items in the cart and return the cart
            return shoppingCartDao.getByUserId(userId);
        }
        catch(Exception e)
        {
            // Log the exception for debugging purposes
            System.err.println("Error getting shopping cart: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving shopping cart.", e);
        }
    }

    // POST: Add a product to the cart
    // URL: https://localhost:8080/cart/products/15 (15 is the productId to be added)
    // Requires a logged-in user
    @PostMapping("products/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addProductToCart(@PathVariable int productId, Principal principal)
    {
        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
            }
            int userId = user.getId();

            shoppingCartDao.addProductToCart(userId, productId);
            return new ResponseEntity<>(HttpStatus.CREATED); // 201 Created for successful addition/increment
        } catch (Exception e) {
            System.err.println("Error adding product to cart: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error adding product to cart.", e);
        }
    }

    // PUT: Update an existing product's quantity in the cart
    // URL: https://localhost:8080/cart/products/15 (15 is the productId to be updated)
    // BODY: { "quantity": 3 }
    // Requires a logged-in user
    @PutMapping("products/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateProductQuantity(@PathVariable int productId,
                                                      @RequestBody Map<String, Integer> requestBody,
                                                      Principal principal)
    {
        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
            }
            int userId = user.getId();

            Integer quantity = requestBody.get("quantity");
            if (quantity == null || quantity < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be a non-negative number.");
            }

            // Check if product exists in cart before attempting to update its quantity
            ShoppingCart cart = shoppingCartDao.getByUserId(userId);
            if (cart.getByProductId(productId) == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with ID " + productId + " not found in user's cart.");
            }

            shoppingCartDao.updateProductQuantity(userId, productId, quantity);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content for successful update
        } catch (Exception e) {
            System.err.println("Error updating product quantity in cart: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating product quantity in cart.", e);
        }
    }

    // DELETE: Remove a specific product from the current user's cart
    // URL: https://localhost:8080/cart/products/{productId} (added this based on common practice)
    // The PDF only shows DELETE /cart to clear ALL items. Let's add this for individual item removal.
    @DeleteMapping("products/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeProductFromCart(@PathVariable int productId, Principal principal) {
        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
            }
            int userId = user.getId();

            // Check if product exists in cart before attempting to remove
            ShoppingCart cart = shoppingCartDao.getByUserId(userId);
            if (cart.getByProductId(productId) == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with ID " + productId + " not found in user's cart.");
            }

            shoppingCartDao.removeProductFromCart(userId, productId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content for successful removal
        } catch (Exception e) {
            System.err.println("Error removing product from cart: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error removing product from cart.", e);
        }
    }


    // DELETE: Clear all products from the current user's cart
    // URL: https://localhost:8080/cart
    // Requires a logged-in user
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> clearCart(Principal principal)
    {
        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
            }
            int userId = user.getId();

            shoppingCartDao.clearCart(userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content for successful clear
        } catch (Exception e) {
            System.err.println("Error clearing shopping cart: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error clearing shopping cart.", e);
        }
    }
}
