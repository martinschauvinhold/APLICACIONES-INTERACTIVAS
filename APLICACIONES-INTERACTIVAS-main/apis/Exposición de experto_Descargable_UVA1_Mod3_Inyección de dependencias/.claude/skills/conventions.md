# Convenciones del proyecto E-Commerce API

Archivo de referencia compartido. Todas las skills de este proyecto lo usan como fuente de verdad.

---

## Idioma del código

Todo el código debe estar en **inglés**: clases, métodos, variables, parámetros, campos, interfaces.

```java
// MAL
boolean tieneDeliveryDespachado = ...
public List<Delivery> obtenerPorOrden(Integer orderId) { ... }

// BIEN
boolean hasDispatchedDelivery = ...
public List<Delivery> getByOrderId(Integer orderId) { ... }
```

Excepción permitida: mensajes de error en strings (los que ve el usuario final pueden ir en español).
En tests, los nombres de los métodos `@Test` pueden ir en español (`deberiaRetornar404_cuandoIdNoExiste`), pero las clases y variables de test en inglés.

---

## Estructura de paquetes

```
com.uade.tpo.demo/
├── controllers/       — REST controllers
├── entity/            — JPA entities
│   └── dto/           — Records de request
├── repository/        — Interfaces JpaRepository
├── service/           — Interfaces de servicio + implementaciones
└── exceptions/        — Jerarquía de excepciones + GlobalExceptionHandler
```

---

## Entidades JPA

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "NOMBRE_EN_MAYUSCULAS")
public class NombreEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nombre_entidad_id")
    private Integer id;

    @Column(name = "nombre_columna", nullable = false)
    private String campo;
}
```

Reglas:
- Nombre de tabla: `UPPER_SNAKE_CASE` en `@Table(name = ...)`
- Nombre de columna: `lower_snake_case` en `@Column(name = ...)`
- Siempre definir `@Column(name = ...)` explícitamente — evita conflictos con palabras reservadas de SQL Server
- `@Builder` + `@NoArgsConstructor` + `@AllArgsConstructor` siempre juntos
- IDs como `Integer` (no `int`) para poder recibir `null`

---

## Enums para campos de estado

Cualquier campo que represente un estado, tipo o modo **debe ser un enum**, nunca un `String`.

### Definición del enum

```java
// src/main/java/com/uade/tpo/demo/entity/NombreStatus.java
public enum NombreStatus {
    PENDING,
    ACTIVE,
    CLOSED
}
```

Reglas:
- Definir el enum en el paquete `entity/` (mismo nivel que las entidades)
- Valores en `UPPER_SNAKE_CASE`
- Nombre del enum: `<Entidad>Status` o `<Entidad>Type` según corresponda

### En la entidad JPA

```java
@Enumerated(EnumType.STRING)   // ← obligatorio — guarda el nombre del enum, no el ordinal
@Column(name = "status", nullable = false)
private NombreStatus status;
```

Nunca usar `EnumType.ORDINAL` — si se reordena el enum, los datos históricos quedan inconsistentes.

### En el DTO record

```java
public record NombreRequest(
    @NotNull NombreStatus status,   // ← tipo enum, no String
    ...
) {}
```

### En el service

```java
// Comparar con == / !=, nunca con .equals() de String
if (entity.getStatus() != NombreStatus.APPROVED) {
    throw new BusinessRuleException("...");
}
entity.setStatus(NombreStatus.CLOSED);
```

### Enums existentes en el proyecto

| Enum | Valores |
|------|---------|
| `DeliveryStatus` | `PENDING, DISPATCHED, DELIVERED, RETURNED` |
| `ReturnStatus` | `PENDING, APPROVED, REJECTED, COMPLETED` |
| `RefundStatus` | `PENDING, PROCESSED, FAILED` |
| `TrackingStatus` | `IN_TRANSIT, DELAYED, DELIVERED, RETURNED` |
| `TicketStatus` | `OPEN, PENDING, CLOSED` |

---

## DTOs — Records (NO clases @Data)

```java
// src/main/java/com/uade/tpo/demo/entity/dto/NombreRequest.java
public record NombreRequest(
    @NotBlank String campo,
    @NotNull Integer otroId
) {}
```

Reglas:
- Siempre `record`, nunca clase con `@Data`
- Usar Bean Validation: `@NotBlank` para Strings, `@NotNull` para objetos, `@Min`/`@Max` para números
- Solo campos que el cliente puede enviar (no incluir `id`, ni campos internos)

---

## Repositories

```java
@Repository
public interface NombreRepository extends JpaRepository<NombreEntidad, Integer> {
    // queries derivadas del nombre del método o @Query si son complejas
    List<NombreEntidad> findByCampo(String campo);
}
```

Reglas:
- Extender siempre `JpaRepository<Entidad, TipoId>`
- Preferir queries por nombre de método antes de `@Query`
- Nunca inyectar el `EntityManager` directamente — todo pasa por el repository

---

## Services

### Interfaz
```java
public interface NombreService {
    NombreEntidad getById(Integer id);
    List<NombreEntidad> getAll();
    NombreEntidad create(NombreRequest request);
    NombreEntidad update(Integer id, NombreRequest request);
    void delete(Integer id);
}
```

### Implementación
```java
@Service
@RequiredArgsConstructor   // inyección por constructor — no @Autowired en campo
public class NombreServiceImpl implements NombreService {

    private final NombreRepository nombreRepository;

    @Override
    public NombreEntidad getById(Integer id) {
        return nombreRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("NombreEntidad", id));
    }

    @Override
    @Transactional
    public NombreEntidad create(NombreRequest request) {
        // lógica de negocio aquí
        return nombreRepository.save(...);
    }
}
```

Reglas:
- `@RequiredArgsConstructor` de Lombok en lugar de `@Autowired` — permite que Mockito inyecte mocks en tests
- `@Transactional` en todos los métodos que escriben en la DB (`create`, `update`, `delete`)
- El service lanza excepciones de negocio (de `exceptions/`), nunca retorna `null` ni `Optional`
- El controller NO maneja lógica de negocio — todo va en el service

---

## Controllers

```java
@RestController
@RequestMapping("nombres")
@RequiredArgsConstructor
public class NombresController {

    private final NombreService nombreService;

    @GetMapping
    public ResponseEntity<List<NombreEntidad>> getAll() {
        return ResponseEntity.ok(nombreService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NombreEntidad> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(nombreService.getById(id));
    }

    @PostMapping
    public ResponseEntity<NombreEntidad> create(@Valid @RequestBody NombreRequest request) {
        NombreEntidad created = nombreService.create(request);
        return ResponseEntity.created(URI.create("/nombres/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NombreEntidad> update(@PathVariable Integer id,
                                                 @Valid @RequestBody NombreRequest request) {
        return ResponseEntity.ok(nombreService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        nombreService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

Reglas:
- `@Valid` siempre en `@RequestBody` — activa Bean Validation del record
- El controller no llama `findById` para verificar existencia: el service lanza `NotFoundException`
- Los métodos del controller son simples: delegan al service y retornan `ResponseEntity`
- Sin lógica de negocio ni manejo de `Optional` en el controller

---

## Excepciones

### Jerarquía

```
RuntimeException
└── EcommerceException          — base del proyecto
    ├── NotFoundException       — recurso no encontrado (404)
    ├── DuplicateException      — recurso duplicado (409)
    └── BusinessRuleException   — violación de regla de negocio (422)
```

### Uso en el service
```java
// 404
throw new NotFoundException("Product", id);

// 409
throw new DuplicateException("Category", "description", request.description());

// 422
throw new BusinessRuleException("No se puede cancelar un pedido ya enviado");
```

### GlobalExceptionHandler
Existe en `exceptions/GlobalExceptionHandler.java` con `@ControllerAdvice`.
Mapea cada excepción a su HTTP status y retorna un `ApiError` record.
Nunca duplicar ese mapeo en los controllers.

---

## Tests

### Service test (Mockito — sin Spring context)
```java
@ExtendWith(MockitoExtension.class)
class NombreServiceTest {

    @Mock
    private NombreRepository nombreRepository;

    @InjectMocks
    private NombreServiceImpl nombreService;

    @Test
    void create_deberiaRetornarEntidad_cuandoDatosValidos() {
        // Arrange
        var request = new NombreRequest("valor");
        var entidadGuardada = NombreEntidad.builder().id(1).campo("valor").build();
        when(nombreRepository.save(any())).thenReturn(entidadGuardada);

        // Act
        var result = nombreService.create(request);

        // Assert
        assertThat(result.getId()).isEqualTo(1);
        verify(nombreRepository).save(any());
    }
}
```

### Repository test (@DataJpaTest — H2)
```java
@DataJpaTest
class NombreRepositoryTest {
    @Autowired
    private NombreRepository nombreRepository;
    // tests de queries custom
}
```

### Controller test (@WebMvcTest — mock del service)
```java
@WebMvcTest(NombresController.class)
class NombresControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean NombreService nombreService;
}
```

Reglas:
- Nombre del test: `deberiaAccion_cuandoCondicion` o `metodo_deberiaResultado_cuandoCondicion`
- Estructura: Arrange / Act / Assert (con comentarios)
- Un `@Test` por comportamiento — no mezclar escenarios
- Tests de excepción obligatorios para cada `throw` en el service
