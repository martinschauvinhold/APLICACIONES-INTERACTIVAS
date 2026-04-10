package com.uade.tpo.demo.service;
 
import java.util.ArrayList;
import java.util.Optional;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.exceptions.CategoryDuplicateException;
import com.uade.tpo.demo.repository.CategoryRepository;
 
@Service
public class CategoryServiceImpl implements CategoryService {
 
    @Autowired
    private CategoryRepository categoryRepository;
 
    public ArrayList<Category> getCategories() {
        return new ArrayList<>(categoryRepository.findAll());
    }
 
    public Optional<Category> getCategoryById(int categoryId) {
        return categoryRepository.findById(categoryId);
    }
 
    public Category createCategory(String description) throws CategoryDuplicateException {
        if (!categoryRepository.findByDescription(description).isEmpty())
            throw new CategoryDuplicateException();
        return categoryRepository.save(Category.builder().description(description).build());
    }
 
    public Category updateCategory(int categoryId, String description) {
        Category category = categoryRepository.findById(categoryId).get();
        category.setDescription(description);
        return categoryRepository.save(category);
    }
 
    public void deleteCategory(int categoryId) {
        categoryRepository.deleteById(categoryId);
    }
}