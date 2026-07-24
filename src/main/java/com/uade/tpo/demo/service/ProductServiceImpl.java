package com.uade.tpo.demo.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.ProductImage;
import com.uade.tpo.demo.entity.ProductVariant;
import com.uade.tpo.demo.entity.Role;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.ProductRequest;
import com.uade.tpo.demo.entity.dto.ProductResponse;
import com.uade.tpo.demo.exceptions.BusinessRuleException;
import com.uade.tpo.demo.exceptions.ConflictException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.CategoryRepository;
import com.uade.tpo.demo.repository.InventoryRepository;
import com.uade.tpo.demo.repository.OrderItemRepository;
import com.uade.tpo.demo.repository.PriceTierRepository;
import com.uade.tpo.demo.repository.ProductImageRepository;
import com.uade.tpo.demo.repository.ProductRepository;
import com.uade.tpo.demo.repository.ProductVariantRepository;
import com.uade.tpo.demo.repository.ReviewRepository;
import com.uade.tpo.demo.repository.TagRepository;
import com.uade.tpo.demo.repository.UserRepository;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private StorageService storageService;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private PriceTierRepository priceTierRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    public Page<ProductResponse> getProducts(Integer categoryId, Integer sellerId, String search, Boolean active,
            Integer viewerSellerId, Pageable pageable) {
        String normalizedSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        Page<Product> page = productRepository.search(categoryId, sellerId, active, normalizedSearch, viewerSellerId,
                pageable);

        // Imágenes primarias en BATCH para no incurrir en N+1 al armar el listado.
        List<Integer> productIds = page.getContent().stream().map(Product::getId).toList();
        Map<Integer, String> primaryImageByProduct = productIds.isEmpty()
                ? Map.of()
                : productImageRepository.findByProduct_IdInAndIsPrimaryTrue(productIds).stream()
                        .collect(Collectors.toMap(
                                image -> image.getProduct().getId(),
                                ProductImage::getUrl,
                                (first, second) -> first));

        return page.map(product -> ProductResponse.from(product, primaryImageByProduct.get(product.getId())));
    }

    public Optional<Product> getProductById(int productId) {
        return productRepository.findById(productId);
    }

    public ProductResponse toResponse(Product product) {
        String imageUrl = productImageRepository.findFirstByProduct_IdAndIsPrimaryTrue(product.getId())
                .map(ProductImage::getUrl)
                .orElse(null);
        return ProductResponse.from(product, imageUrl);
    }

    public ProductResponse uploadPrimaryImage(int productId, MultipartFile file) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product", productId));
        String url = storageService.store(file);

        // Mantener una sola imagen primaria: bajar las anteriores antes de agregar la nueva.
        productImageRepository.findByProductId(productId).stream()
                .filter(ProductImage::isPrimary)
                .forEach(image -> {
                    image.setPrimary(false);
                    productImageRepository.save(image);
                });

        productImageRepository.save(ProductImage.builder()
                .product(product)
                .url(url)
                .isPrimary(true)
                .build());
        return ProductResponse.from(product, url);
    }

    public Product createProduct(ProductRequest productRequest) {
        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category", productRequest.getCategoryId()));
        User seller = userRepository.findById(productRequest.getSellerId())
                .orElseThrow(() -> new NotFoundException("User", productRequest.getSellerId()));
        if (seller.getRole() != Role.seller) {
            throw new BusinessRuleException("El usuario " + seller.getId() + " no es un vendedor");
        }
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .brand(productRequest.getBrand())
                .imageUrl(productRequest.getImageUrl())
                .category(category)
                .seller(seller)
                .isActive(true)
                .updatedAt(new Date())
                .build();
        return productRepository.save(product);
    }

    public Product updateProduct(int productId, ProductRequest productRequest) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product", productId));
        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category", productRequest.getCategoryId()));
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setBrand(productRequest.getBrand());
        if (productRequest.getImageUrl() != null) product.setImageUrl(productRequest.getImageUrl());
        product.setCategory(category);
        product.setUpdatedAt(new Date());
        // El vendedor de un producto es fijo: update no lo cambia.
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(int productId) {
        // Con ventas reales no se borra: perderíamos el detalle de pedidos ya
        // facturados. Sin ventas, sí se puede — variantes/imágenes/reseñas se
        // borran en cascada a mano (no hay @OneToMany con cascade en las entidades).
        if (orderItemRepository.existsByVariant_ProductId(productId)) {
            throw new ConflictException("No se puede borrar un producto con ventas asociadas.");
        }

        try {
            List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
            for (ProductVariant variant : variants) {
                inventoryRepository.deleteAll(inventoryRepository.findByVariantId(variant.getId()));
                priceTierRepository.deleteAll(priceTierRepository.findByVariantId(variant.getId()));
            }
            productVariantRepository.deleteAll(variants);
            productImageRepository.deleteAll(productImageRepository.findByProductId(productId));
            reviewRepository.deleteAll(reviewRepository.findByProductId(productId));
            tagRepository.deleteAll(tagRepository.findByProductId(productId));
            productRepository.deleteById(productId);
            productRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException(
                    "No se puede borrar un producto con imágenes, variantes o ventas asociadas.");
        }
    }
}
