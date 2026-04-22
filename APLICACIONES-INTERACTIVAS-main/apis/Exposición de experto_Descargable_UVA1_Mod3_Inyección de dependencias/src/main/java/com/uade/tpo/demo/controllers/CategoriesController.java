package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.entity.dto.CategoryRequest;
import com.uade.tpo.demo.exceptions.CategoryDuplicateException;
import com.uade.tpo.demo.service.CategoryService;

@RestController
@RequestMapping("categories")
public class CategoriesController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ArrayList<Category>> getCategories() {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<Category> getCategoryById(@PathVariable int categoryId) {
        Optional<Category> result = categoryService.getCategoryById(categoryId);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> createCategory(@RequestBody CategoryRequest categoryRequest)
            throws CategoryDuplicateException {
        Category result = categoryService.createCategory(categoryRequest.getDescription());
        return ResponseEntity.created(URI.create("/categories/" + result.getId())).body(result);
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> updateCategory(@PathVariable int categoryId,
            @RequestBody CategoryRequest categoryRequest) {
        Optional<Category> result = categoryService.getCategoryById(categoryId);
        if (result.isPresent()) {
            Category updated = categoryService.updateCategory(categoryId, categoryRequest.getDescription());
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> deleteCategory(@PathVariable int categoryId) {
        Optional<Category> result = categoryService.getCategoryById(categoryId);
        if (result.isPresent()) {
            categoryService.deleteCategory(categoryId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
