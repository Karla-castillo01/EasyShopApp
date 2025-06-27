package org.yearup.controllers;
import org.springframework.http.ResponseEntity;
// Import statements for Spring annotations and other necessary classes
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; // Import this
import org.yearup.data.CategoryDao;
import org.yearup.data.ProductDao;
import org.yearup.models.Category;
import org.yearup.models.Product;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("categories")
@CrossOrigin
public class CategoriesController
{
    private CategoryDao categoryDao;
    private ProductDao productDao;


    @Autowired
    public CategoriesController(CategoryDao categoryDao, ProductDao productDao)
    {
        this.categoryDao = categoryDao;
        this.productDao = productDao;
    }

    @GetMapping
    public List<Category> getAll()
    {
        return categoryDao.getAllCategories();
    }

    // @GetMapping("{id}") annotation activated here
    @GetMapping("{id}")
    public Category getById(@PathVariable int id)
    {
        Category category = categoryDao.getById(id);
        if (category == null) {
            // If category is not found, throw a ResponseStatusException with 404 Not Found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found.");
        }
        return category;
    }


    // https://localhost:8080/categories/1/products
    @GetMapping("{categoryId}/products")
    public List<Product> getProductsById(@PathVariable int categoryId)
    {
        return productDao.getProductsByCategoryId(categoryId);
    }

    // @PostMapping and @PreAuthorize annotations activated here
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> addCategory(@RequestBody Category category)
    {
        Category createdCategory = categoryDao.create(category);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    // @PutMapping("{id}") and @PreAuthorize annotations activated here
    @PutMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void updateCategory(@PathVariable int id, @RequestBody Category category)
    {
        categoryDao.update(id, category);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable int id)
    {

        Category existingCategory = categoryDao.getById(id);
        if (existingCategory == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category to delete not found.");
        }
        categoryDao.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
