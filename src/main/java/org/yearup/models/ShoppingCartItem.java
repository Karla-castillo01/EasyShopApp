package org.yearup.models;

import java.math.BigDecimal; // Import for price/lineTotal

public class ShoppingCartItem {
    private Product product;
    private int quantity;
    // private BigDecimal discountPercent; // REMOVE THIS LINE
    private BigDecimal lineTotal;

    // Default Constructor
    public ShoppingCartItem() {
        this.product = new Product();
        this.quantity = 0;
        // this.discountPercent = BigDecimal.ZERO; // REMOVE OR COMMENT OUT THIS LINE
        this.lineTotal = BigDecimal.ZERO;
    }

    // Constructor with all fields (Adjust parameters)
    // REMOVE THE discountPercent PARAMETER
    public ShoppingCartItem(Product product, int quantity, BigDecimal lineTotal) {
        this.product = product;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    // --- Getters and Setters ---
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // REMOVE THESE GETTER AND SETTER FOR discountPercent
    // public BigDecimal getDiscountPercent() {
    //     return discountPercent;
    // }
    //
    // public void setDiscountPercent(BigDecimal discountPercent) {
    //     this.discountPercent = discountPercent;
    // }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    // toString for easy debugging (adjust if you removed discountPercent)
    @Override
    public String toString() {
        return "ShoppingCartItem{" +
                "product=" + product +
                ", quantity=" + quantity +
                ", lineTotal=" + lineTotal + // REMOVED discountPercent
                '}';
    }
}
