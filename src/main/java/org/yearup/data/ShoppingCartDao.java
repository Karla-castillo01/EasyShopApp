package org.yearup.data;

import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem; // Needed for add/update methods
import org.yearup.models.Product; // Needed for building ShoppingCartItem

public interface ShoppingCartDao {
    // Retrieves the shopping cart for a specific user, including product details
    ShoppingCart getByUserId(int userId);

    // Adds a product to the user's shopping cart.
    // If product already exists, it should increment quantity.
    void addProductToCart(int userId, int productId); // <--- THIS METHOD MUST BE PRESENT

    // Updates the quantity of a specific product in the user's cart.
    void updateProductQuantity(int userId, int productId, int quantity);

    // Removes a specific product from the user's cart.
    void removeProductFromCart(int userId, int productId);

    // Clears all items from a user's shopping cart.
    void clearCart(int userId);
}