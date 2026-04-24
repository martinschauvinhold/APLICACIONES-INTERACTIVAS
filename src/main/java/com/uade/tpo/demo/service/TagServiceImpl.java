package com.uade.tpo.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.Tag;
import com.uade.tpo.demo.entity.dto.TagRequest;
import com.uade.tpo.demo.exceptions.BusinessRuleException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.CategoryRepository;
import com.uade.tpo.demo.repository.ProductRepository;
import com.uade.tpo.demo.repository.TagRepository;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Tag> getTagsByProduct(int productId) {
        return tagRepository.findByProductId(productId);
    }

    public List<Tag> getTagsByCategory(int categoryId) {
        return tagRepository.findByCategoryId(categoryId);
    }

    public Optional<Tag> getTagById(int tagId) {
        return tagRepository.findById(tagId);
    }

    public Tag createTag(TagRequest request) {
        if (request.getProductId() == null && request.getCategoryId() == null)
            throw new BusinessRuleException("El tag debe estar asociado a un producto o una categoria");

        Product product = null;
        if (request.getProductId() != null)
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product", request.getProductId()));

        Category category = null;
        if (request.getCategoryId() != null)
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category", request.getCategoryId()));

        Tag tag = Tag.builder()
                .name(request.getName())
                .product(product)
                .category(category)
                .build();
        return tagRepository.save(tag);
    }

    public void deleteTag(int tagId) {
        if (!tagRepository.existsById(tagId))
            throw new NotFoundException("Tag", tagId);
        tagRepository.deleteById(tagId);
    }
}