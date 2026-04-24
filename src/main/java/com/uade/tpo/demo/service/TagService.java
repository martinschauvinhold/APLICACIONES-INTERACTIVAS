package com.uade.tpo.demo.service;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.entity.Tag;
import com.uade.tpo.demo.entity.dto.TagRequest;

public interface TagService {
    List<Tag> getTagsByProduct(int productId);
    List<Tag> getTagsByCategory(int categoryId);
    Optional<Tag> getTagById(int tagId);
    Tag createTag(TagRequest request);
    void deleteTag(int tagId);
}