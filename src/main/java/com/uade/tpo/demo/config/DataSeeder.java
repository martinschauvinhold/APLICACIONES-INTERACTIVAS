package com.uade.tpo.demo.config;

import java.math.BigDecimal;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.entity.Coupon;
import com.uade.tpo.demo.entity.Discount;
import com.uade.tpo.demo.entity.Inventory;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.ProductVariant;
import com.uade.tpo.demo.entity.Review;
import com.uade.tpo.demo.entity.Role;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.Warehouse;
import com.uade.tpo.demo.repository.CategoryRepository;
import com.uade.tpo.demo.repository.CouponRepository;
import com.uade.tpo.demo.repository.DiscountRepository;
import com.uade.tpo.demo.repository.InventoryRepository;
import com.uade.tpo.demo.repository.ProductRepository;
import com.uade.tpo.demo.repository.ProductVariantRepository;
import com.uade.tpo.demo.repository.ReviewRepository;
import com.uade.tpo.demo.repository.UserRepository;
import com.uade.tpo.demo.repository.WarehouseRepository;

/**
 * DataSeeder — siembra datos de desarrollo (usuarios + catálogo + cupón +
 * reseñas) al arrancar, SOLO si la base está vacía (idempotente). Así cualquiera
 * que levante el backend con un perfil de dev arranca con la base poblada, sin
 * pasos manuales.
 *
 * Activo solo en los perfiles de dev (docker-mysql / mysql / docker); en tests
 * NO se crea el bean (corren sin perfil), así no interfiere con el suite.
 */
@Component
@Profile({ "docker-mysql", "mysql", "docker" })
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private WarehouseRepository warehouseRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private DiscountRepository discountRepository;
    @Autowired private CouponRepository couponRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("[seed] La base ya tiene datos; no se siembra nada.");
            return;
        }
        try {
            seed();
            log.info("[seed] Datos de desarrollo cargados (usuarios + catálogo + cupón + reseñas).");
        } catch (Exception e) {
            // No tiramos la app abajo por un fallo de seed (solo es data de dev).
            log.error("[seed] Falló el seed de datos: {}", e.getMessage(), e);
        }
    }

    private void seed() {
        Date now = new Date();

        // ── Usuarios ──
        User seller = userRepository.save(user("seller_test", "seller_test@test.com", "Test1234!", "Seller", "Test", Role.seller, "1100000001", now));
        userRepository.save(user("admin", "admin@mail.com", "Test1234!", "Admin", "Root", Role.admin, "1100000000", now));
        User ana = userRepository.save(user("ana", "ana@mail.com", "Password1!", "Ana", "García", Role.buyer, "1100000002", now));
        userRepository.save(user("bruno", "bruno@mail.com", "Password1!", "Bruno", "López", Role.buyer, "1100000003", now));
        userRepository.save(user("carla", "carla@mail.com", "Password1!", "Carla", "Pérez", Role.buyer, "1100000004", now));

        // ── Categorías ──
        Category smartphones = categoryRepository.save(category("Smartphones", "smartphones"));
        Category notebooks = categoryRepository.save(category("Notebooks", "notebooks"));
        Category audio = categoryRepository.save(category("Audio", "audio"));
        Category monitores = categoryRepository.save(category("Monitores", "monitores"));
        Category accesorios = categoryRepository.save(category("Accesorios", "accesorios"));

        // ── Depósitos ──
        Warehouse central = warehouseRepository.save(warehouse("Depósito Central", "CABA", "1140000000"));
        warehouseRepository.save(warehouse("Depósito Sur", "Avellaneda", "1140000001"));

        // ── Productos + variantes + inventario ──
        Product galaxy = seedProduct(seller, smartphones, central, "Galaxy S24", "Samsung", "Smartphone Android de última generación", "{\"color\":\"negro\",\"capacidad\":\"256GB\"}", "899999.00", 25);
        seedProduct(seller, smartphones, central, "iPhone 15", "Apple", "Smartphone iOS", "{\"color\":\"azul\",\"capacidad\":\"128GB\"}", "1199999.00", 15);
        seedProduct(seller, notebooks, central, "MacBook Air M3", "Apple", "Notebook ultraliviana", "{\"ram\":\"16GB\",\"ssd\":\"512GB\"}", "2199999.00", 8);
        seedProduct(seller, notebooks, central, "ThinkPad X1 Carbon", "Lenovo", "Notebook empresarial", "{\"ram\":\"16GB\",\"ssd\":\"1TB\"}", "1899999.00", 6);
        seedProduct(seller, audio, central, "WH-1000XM5", "Sony", "Auriculares con cancelación de ruido", "{\"color\":\"negro\"}", "449999.00", 30);
        seedProduct(seller, audio, central, "ATH-M50x", "Audio-Technica", "Auriculares de estudio", "{\"color\":\"negro\"}", "189999.00", 40);
        seedProduct(seller, monitores, central, "U2723QE", "Dell", "Monitor 27 pulgadas 4K", "{\"pulgadas\":\"27\"}", "729999.00", 12);
        seedProduct(seller, accesorios, central, "MX Keys S", "Logitech", "Teclado inalámbrico", "{\"layout\":\"español\"}", "129999.00", 50);

        // ── Descuento + cupón ──
        Discount discount = discountRepository.save(Discount.builder()
                .name("Bienvenida 10%")
                .discountType("PERCENTAGE")
                .value(new BigDecimal("10.00"))
                .appliesTo("ALL")
                .build());
        couponRepository.save(Coupon.builder()
                .discount(discount)
                .code("BIENVENIDA10")
                .usageLimit(100)
                .build());

        // ── Reseñas ──
        reviewRepository.save(review(ana, galaxy, 5, "Excelente, llegó rápido y tal cual la descripción."));
        reviewRepository.save(review(ana, galaxy, 4, "Muy bueno por el precio."));
    }

    private Product seedProduct(User seller, Category category, Warehouse warehouse, String name, String brand,
            String description, String attributes, String price, int stock) {
        Product product = productRepository.save(Product.builder()
                .name(name).description(description).brand(brand)
                .category(category).seller(seller).isActive(true).updatedAt(new Date()).build());
        ProductVariant variant = variantRepository.save(ProductVariant.builder()
                .product(product)
                .sku(name.replaceAll("\\s+", "-").toUpperCase() + "-STD")
                .attributes(attributes)
                .basePrice(new BigDecimal(price))
                .updatedAt(new Date()).build());
        inventoryRepository.save(Inventory.builder()
                .variant(variant).warehouse(warehouse).stockQuantity(stock).lastUpdated(new Date()).build());
        return product;
    }

    private User user(String username, String email, String rawPassword, String firstName, String lastName,
            Role role, String phone, Date createdAt) {
        return User.builder()
                .username(username).email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .firstName(firstName).lastName(lastName).role(role).phone(phone).createdAt(createdAt).build();
    }

    private Category category(String description, String slug) {
        return Category.builder().description(description).slug(slug).build();
    }

    private Warehouse warehouse(String name, String location, String contactPhone) {
        return Warehouse.builder().name(name).location(location).contactPhone(contactPhone).build();
    }

    private Review review(User user, Product product, int rating, String comment) {
        return Review.builder().user(user).product(product).rating(rating).comment(comment).createdAt(new Date()).build();
    }
}
