package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao; // Assuming ProductDao interface exists
import org.yearup.models.Product; // Assuming Product model exists

import java.util.List;

@RestController
@RequestMapping("products")
@CrossOrigin // Allows cross-origin requests, important for frontend
public class ProductsController
{
    private ProductDao productDao; // Inject ProductDao

    @Autowired
    public ProductsController(ProductDao productDao)
    {
        this.productDao = productDao;
    }

    // GET: Get All Products
    @GetMapping
    public List<Product> getAllProducts()
    {

        return productDao.search(null, null, null, null);
    }

    @GetMapping("{id}")
    public Product getProductById(@PathVariable int id)
    {
        Product product = productDao.getById(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id);
        }
        return product;
    }

    // Only accessible to ADMINs
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> addProduct(@RequestBody Product product)
    {
        try {
            Product createdProduct = productDao.create(product);
            return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error creating product: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating product.", e);
        }
    }


    @PutMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateProduct(@PathVariable int id, @RequestBody Product product)
    {
        // First, check if the product exists before attempting to update
        if (productDao.getById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product to update not found with ID: " + id);
        }
        try {
            productDao.update(id, product);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content for successful update
        } catch (Exception e) {
            System.err.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating product.", e);
        }
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable int id)
    {
        // Check if the product exists before attempting to delete
        if (productDao.getById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product to delete not found with ID: " + id);
        }
        try {
            productDao.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content for successful deletion
        } catch (Exception e) {
            System.err.println("Error deleting product: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting product.", e);
        }
    }
}
