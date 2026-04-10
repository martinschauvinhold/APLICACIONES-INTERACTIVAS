package com.uade.tpo.demo.service;
 
import java.util.ArrayList;
import java.util.Optional;
 
import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.exceptions.CategoryDuplicateException;
 
public interface CategoryService {
    public ArrayList<Category> getCategories();
 
    public Optional<Category> getCategoryById(int categoryId);
 
    public Category createCategory(String description) throws CategoryDuplicateException;
 
    public Category updateCategory(int categoryId, String description);
 
    public void deleteCategory(int categoryId);
}
 