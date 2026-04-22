package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Tag;
import com.uade.tpo.demo.entity.dto.TagRequest;
import com.uade.tpo.demo.service.TagService;

@RestController
@RequestMapping("tags")
public class TagsController {

    @Autowired
    private TagService tagService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Tag>> getTagsByProduct(@PathVariable int productId) {
        return ResponseEntity.ok(tagService.getTagsByProduct(productId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Tag>> getTagsByCategory(@PathVariable int categoryId) {
        return ResponseEntity.ok(tagService.getTagsByCategory(categoryId));
    }

    @GetMapping("/{tagId}")
    public ResponseEntity<Tag> getTagById(@PathVariable int tagId) {
        Optional<Tag> result = tagService.getTagById(tagId);
        if (result.isPresent())
            return ResponseEntity.ok(result.get());
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<Object> createTag(@RequestBody TagRequest request) {
        Tag result = tagService.createTag(request);
        return ResponseEntity.created(URI.create("/tags/" + result.getId())).body(result);
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Object> deleteTag(@PathVariable int tagId) {
        Optional<Tag> result = tagService.getTagById(tagId);
        if (result.isPresent()) {
            tagService.deleteTag(tagId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
