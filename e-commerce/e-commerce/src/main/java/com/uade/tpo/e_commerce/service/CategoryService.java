package com.uade.tpo.e_commerce.service;

import com.uade.tpo.e_commerce.entity.Category;
import com.uade.tpo.e_commerce.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final CategoryRepository repository = new CategoryRepository();

    public List<Category> getCategories() {
        return repository.getCategories();
    }

    public Category getCategoryById(int categoryId) {
        return repository.getCategories().stream()
            .filter(c -> c.getId() == categoryId)
            .findFirst()
            .orElse(null);
    }

    public Category createCategory(Category category) {
        repository.getCategories().add(category);
        return category;
    }
}

