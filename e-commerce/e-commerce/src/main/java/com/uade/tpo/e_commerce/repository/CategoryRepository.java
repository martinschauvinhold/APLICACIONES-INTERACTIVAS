package com.uade.tpo.e_commerce.repository;

import java.util.ArrayList;
import java.util.Arrays;
import com.uade.tpo.e_commerce.entity.Category;

public class CategoryRepository {
    public ArrayList<Category> categories= new ArrayList<Category>(
        Arrays.asList(Category.builder().description("Oro").id(1).build(),
        Category.builder().description("Plata").id(2).build(),
        Category.builder().description("Platino").id(3).build(),
        Category.builder().description("Gemas Preciosas").id(4).build())
    );

   public ArrayList<Category> getCategories() {
        return this.categories;
    }

    
    public Category getCategoryById(int categoryId) {
        return this.categories.stream()
            .filter(c -> c.getId() == categoryId)
            .findFirst()
            .orElse(null);
    }

    public Category createCategory(int newCategoryId, String Description) {
        Category newCategory = Category.builder().id(newCategoryId).description(Description).build();
        this.categories.add(newCategory);
        return newCategory;
    }
}

