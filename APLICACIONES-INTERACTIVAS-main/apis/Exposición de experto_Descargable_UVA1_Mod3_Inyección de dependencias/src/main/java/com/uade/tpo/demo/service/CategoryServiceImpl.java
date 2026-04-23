package com.uade.tpo.demo.service;
 
import java.util.ArrayList;
import java.util.Optional;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.exceptions.BusinessRuleException;
import com.uade.tpo.demo.exceptions.DuplicateException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.CategoryRepository;
import com.uade.tpo.demo.repository.ProductRepository;
 
@Service
public class CategoryServiceImpl implements CategoryService {
 
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;
 
    public ArrayList<Category> getCategories() {
        return new ArrayList<>(categoryRepository.findAll());
    }

    public ArrayList<Category> getActiveCategories() {
        return new ArrayList<>(categoryRepository.findByIsActiveTrue());
    }
 
    public Optional<Category> getCategoryById(int categoryId) {
        return categoryRepository.findById(categoryId);
    }
 
    public Category createCategory(String description, Integer parentId) {
        if (!categoryRepository.findByDescription(description).isEmpty())
            throw new DuplicateException("Category", "description", description);
        Category parent = null;
        if (parentId != null)
            parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new NotFoundException("Category", parentId));
        return categoryRepository.save(Category.builder().description(description).parent(parent).build());
    }

    public Category updateCategory(int categoryId, String description) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category", categoryId));
        boolean takenByAnother = categoryRepository.findByDescription(description).stream()
                .anyMatch(c -> !c.getId().equals(categoryId));
        if (takenByAnother)
            throw new DuplicateException("Category", "description", description);
        category.setDescription(description);
        return categoryRepository.save(category);
    }

    public Category deactivateCategory(int categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category", categoryId));
        category.setActive(false);
        return categoryRepository.save(category);
    }

    public void deleteCategory(int categoryId) {
        if (productRepository.existsByCategory_Id(categoryId))
            throw new BusinessRuleException("No se puede eliminar la categoría porque tiene productos asociados");
        categoryRepository.deleteById(categoryId);
    }
}