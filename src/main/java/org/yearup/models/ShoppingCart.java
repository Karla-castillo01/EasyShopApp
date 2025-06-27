package org.yearup.models;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ShoppingCart {
    // Using a Map where key is productId and value is ShoppingCartItem
    // This matches the JSON structure like "1": { ...item details... } on Page 12 of PDF
    private Map<Integer, ShoppingCartItem> items;
    private BigDecimal total;

    // Default Constructor
    public ShoppingCart() {
        this.items = new HashMap<>(); // Initialize map
        this.total = BigDecimal.ZERO;
    }

    // --- Getters and Setters ---
    public Map<Integer, ShoppingCartItem> getItems() {
        return items;
    }

    public void setItems(Map<Integer, ShoppingCartItem> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        // Calculate total dynamically based on items, as per typical cart logic
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        if (items != null) {
            for (ShoppingCartItem item : items.values()) {
                if (item.getLineTotal() != null) {
                    calculatedTotal = calculatedTotal.add(item.getLineTotal());
                }
            }
        }
        return calculatedTotal;
    }

    public void setTotal(BigDecimal total) {
        // This setter might not be strictly necessary if getTotal calculates dynamically
        this.total = total;
    }

    // Helper method to add or update an item in the cart
    public void addProduct(ShoppingCartItem item) {
        if (item != null && item.getProduct() != null) {
            items.put(item.getProduct().getProductId(), item);
            // Re-calculate total after adding/updating an item
            this.total = getTotal(); // Update the internal total field
        }
    }

    // Helper method to get an item by product ID
    public ShoppingCartItem getByProductId(int productId) { // <--- THIS METHOD MUST BE PRESENT
        return items.get(productId);
    }

    // Helper method to clear the cart
    public void clear() {
        this.items.clear();
        this.total = BigDecimal.ZERO;
    }
}
