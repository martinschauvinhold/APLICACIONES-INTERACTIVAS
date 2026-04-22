package com.uade.tpo.demo.service;
 
import java.util.ArrayList;
import java.util.Optional;
 
import com.uade.tpo.demo.entity.Category;

public interface CategoryService {
    ArrayList<Category> getCategories();

    ArrayList<Category> getActiveCategories();

    Optional<Category> getCategoryById(int categoryId);

    Category createCategory(String description);
 
    Category updateCategory(int categoryId, String description);

    Category deactivateCategory(int categoryId);

    void deleteCategory(int categoryId);
}
 