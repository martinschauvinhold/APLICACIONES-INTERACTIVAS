package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.Optional;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.Role;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.ProductRequest;
import com.uade.tpo.demo.entity.dto.ProductResponse;
import com.uade.tpo.demo.service.AuthorizationService;
import com.uade.tpo.demo.service.ProductService;

@RestController
@RequestMapping("products")
public class ProductsController {

    @Autowired
    private ProductService productService;

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer sellerId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        // Un producto inactivo solo lo ve su dueño: si el caller es un vendedor,
        // ve además sus propios inactivos; el público/buyer solo ve activos.
        Integer viewerSellerId = authorizationService.currentUserOrEmpty()
                .filter(user -> user.getRole() == Role.seller)
                .map(User::getId)
                .orElse(null);
        return ResponseEntity.ok(
                productService.getProducts(categoryId, sellerId, search, active, viewerSellerId, pageable));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable int productId) {
        Optional<Product> result = productService.getProductById(productId);
        return result.map(p -> ResponseEntity.ok(productService.toResponse(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('seller')")
    public ResponseEntity<Object> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        // El vendedor solo crea productos a su propio nombre. El admin administra
        // vendedores, no el catálogo: no participa de las mutaciones de productos.
        productRequest.setSellerId(authorizationService.currentUser().getId());
        Product result = productService.createProduct(productRequest);
        return ResponseEntity.created(URI.create("/products/" + result.getId()))
                .body(productService.toResponse(result));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('seller')")
    public ResponseEntity<Object> updateProduct(@PathVariable int productId,
            @Valid @RequestBody ProductRequest productRequest) {
        Optional<Product> result = productService.getProductById(productId);
        if (result.isPresent()) {
            requireOwner(result.get());
            Product updated = productService.updateProduct(productId, productRequest);
            return ResponseEntity.ok(productService.toResponse(updated));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Sube una imagen (multipart) y la fija como primaria del producto. Solo el
     * vendedor dueño puede hacerlo. Devuelve el ProductResponse con imageUrl.
     */
    @PostMapping(path = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('seller')")
    public ResponseEntity<Object> uploadProductImage(@PathVariable int productId,
            @RequestParam("file") MultipartFile file) {
        Optional<Product> result = productService.getProductById(productId);
        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        requireOwner(result.get());
        return ResponseEntity.ok(productService.uploadPrimaryImage(productId, file));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('seller')")
    public ResponseEntity<Object> deleteProduct(@PathVariable int productId) {
        Optional<Product> result = productService.getProductById(productId);
        if (result.isPresent()) {
            requireOwner(result.get());
            productService.deleteProduct(productId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Solo el vendedor dueño opera sobre el producto — el admin no administra el
     * catálogo. Todo producto pertenece a exactamente un vendedor (relación
     * obligatoria), así que el dueño siempre está presente.
     */
    private void requireOwner(Product product) {
        authorizationService.requireSelf(product.getSeller().getId());
    }
}
