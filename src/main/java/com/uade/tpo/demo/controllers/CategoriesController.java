package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.entity.dto.CategoryRequest;
import com.uade.tpo.demo.entity.dto.CategoryResponse;
import com.uade.tpo.demo.service.CategoryService;

@RestController
@RequestMapping("categories")
public class CategoriesController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(Authentication auth) {
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_admin"));
        ArrayList<Category> categories = isAdmin
                ? categoryService.getCategories()
                : categoryService.getActiveCategories();
        List<CategoryResponse> result = categories.stream().map(CategoryResponse::from).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable int categoryId) {
        Optional<Category> result = categoryService.getCategoryById(categoryId);
        return result.map(c -> ResponseEntity.ok(CategoryResponse.from(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        Category result = categoryService.createCategory(categoryRequest.getDescription());
        return ResponseEntity.created(URI.create("/categories/" + result.getId())).body(CategoryResponse.from(result));
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> updateCategory(@PathVariable int categoryId,
            @Valid @RequestBody CategoryRequest categoryRequest) {
        Optional<Category> result = categoryService.getCategoryById(categoryId);
        if (result.isPresent()) {
            Category updated = categoryService.updateCategory(categoryId, categoryRequest.getDescription());
            return ResponseEntity.ok(CategoryResponse.from(updated));
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{categoryId}/deactivate")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> deactivateCategory(@PathVariable int categoryId) {
        Category result = categoryService.deactivateCategory(categoryId);
        return ResponseEntity.ok(CategoryResponse.from(result));
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
