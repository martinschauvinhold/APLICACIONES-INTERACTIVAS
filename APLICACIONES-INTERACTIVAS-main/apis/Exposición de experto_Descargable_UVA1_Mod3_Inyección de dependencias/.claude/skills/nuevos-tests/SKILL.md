---
name: nuevos-tests
description: Genera tests para una capa existente del e-commerce (service con Mockito, repository con DataJpaTest, controller con WebMvcTest)
---

# /nuevos-tests

Genera tests para código existente del proyecto e-commerce. Se usa cuando ya existe una clase sin tests, o para ampliar cobertura de tests incompletos.

Para código nuevo, usar `/nueva-capa` que ya sigue TDD.

Seguir las convenciones definidas en `.claude/skills/conventions.md`.

---

## Antes de escribir tests

1. Leer la clase a testear completa — entender todos los métodos y ramas.
2. Identificar la capa: service, repository o controller.
3. Listar los casos a cubrir antes de escribir el primer test (happy path + cada error posible).

---

## Tests de Service — Mockito (sin Spring context)

**Cuándo**: siempre que se tenga un `ServiceImpl`.

**Setup**:
```java
@ExtendWith(MockitoExtension.class)
class NombreServiceTest {

    @Mock
    private NombreRepository nombreRepository;

    @InjectMocks
    private NombreServiceImpl nombreService;
}
```

**Casos obligatorios para cada service**:

| Método | Casos a cubrir |
|--------|----------------|
| `getAll` | retorna lista del repository |
| `getById` | id existente → retorna entidad |
| `getById` | id inexistente → lanza `NotFoundException` |
| `create` | datos válidos → guarda y retorna entidad |
| `create` | duplicado (si aplica) → lanza `DuplicateException` |
| `create` | violación de regla de negocio (si aplica) → lanza `BusinessRuleException` |
| `update` | id existente → actualiza y retorna entidad |
| `update` | id inexistente → lanza `NotFoundException` |
| `delete` | id existente → llama `deleteById` |
| `delete` | id inexistente → lanza `NotFoundException` |

**Verificar comportamiento, no implementación**:
```java
@Test
void getById_deberiaRetornarEntidad_cuandoIdExiste() {
    // Arrange
    var entidad = NombreEntidad.builder().id(1).campo("valor").build();
    when(nombreRepository.findById(1)).thenReturn(Optional.of(entidad));

    // Act
    var result = nombreService.getById(1);

    // Assert
    assertThat(result.getId()).isEqualTo(1);
    assertThat(result.getCampo()).isEqualTo("valor");
}

@Test
void getById_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
    when(nombreRepository.findById(99)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> nombreService.getById(99))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("99");
}
```

---

## Tests de Repository — @DataJpaTest (H2)

**Cuándo**: cuando el repository tiene métodos custom más allá de los de `JpaRepository`.

**Setup**:
```java
@DataJpaTest
class NombreRepositoryTest {

    @Autowired
    private NombreRepository nombreRepository;

    @Autowired
    private TestEntityManager em;
}
```

**Qué testear**: solo los métodos definidos en la interfaz del repository, no los heredados de `JpaRepository`.

```java
@Test
void findByCampo_deberiaRetornarEntidades_cuandoCampoCoincide() {
    // Arrange — usar TestEntityManager para persistir datos de prueba
    em.persistAndFlush(NombreEntidad.builder().campo("valor").build());

    // Act
    var resultado = nombreRepository.findByCampo("valor");

    // Assert
    assertThat(resultado).hasSize(1);
    assertThat(resultado.get(0).getCampo()).isEqualTo("valor");
}
```

No testear `save`, `findById`, `deleteById` — son de Spring Data y están probados por el framework.

---

## Tests de Controller — @WebMvcTest (mock del service)

**Cuándo**: para verificar que el controller mapea bien los endpoints, delega al service y retorna los HTTP status correctos.

**Setup**:
```java
@WebMvcTest(NombresController.class)
class NombresControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NombreService nombreService;

    @Autowired
    private ObjectMapper objectMapper;
}
```

**Casos a cubrir**:

```java
@Test
void getById_deberiaRetornar200_cuandoRecursoExiste() throws Exception {
    var entidad = NombreEntidad.builder().id(1).campo("valor").build();
    when(nombreService.getById(1)).thenReturn(entidad);

    mockMvc.perform(get("/nombres/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1));
}

@Test
void getById_deberiaRetornar404_cuandoRecursoNoExiste() throws Exception {
    // Si el service retorna Optional.empty(), el controller debe usar notFound() — nunca noContent()
    when(nombreService.getById(99)).thenThrow(new NotFoundException("Nombre", 99));

    mockMvc.perform(get("/nombres/99"))
        .andExpect(status().isNotFound()); // 404, no 204
}

@Test
void create_deberiaRetornar201_cuandoDatosValidos() throws Exception {
    var request = new NombreRequest("valor");
    var creado = NombreEntidad.builder().id(1).campo("valor").build();
    when(nombreService.create(any())).thenReturn(creado);

    mockMvc.perform(post("/nombres")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"));
}

@Test
void create_deberiaRetornar400_cuandoBodyInvalido() throws Exception {
    mockMvc.perform(post("/nombres")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());
}
```

---

## Datos de prueba — contexto del negocio

Este e-commerce vende **productos tecnológicos** (celulares, tablets, accesorios). Los datos de prueba deben ser coherentes con ese contexto.

| Campo | Ejemplos correctos | Ejemplos incorrectos |
|-------|--------------------|----------------------|
| `reason` (devolución) | `"Pantalla rota"`, `"Batería defectuosa"`, `"Dispositivo no enciende"`, `"Cámara con falla"` | `"Talle incorrecto"`, `"Color equivocado"` |
| `subject` (ticket) | `"No funciona el pago"`, `"Quiero cancelar mi pedido"`, `"Mi celular llegó dañado"` | cualquier cosa de ropa o alimentos |
| `shippingMethod` | `"correo express"`, `"moto mensajero"`, `"retiro en sucursal"` | referencias a otros rubros |
| `checkpoint` (tracking) | `"En depósito"`, `"En camino"`, `"Entregado"` | — (son genéricos, están bien) |

Usar datos específicos y realistas — evitar strings genéricos como `"campo"`, `"valor"`, `"test"` cuando el contexto permite algo más descriptivo.

---

## Reglas generales

- Nombre de método: `metodo_deberiaResultado_cuandoCondicion`
- Un `@Test` por escenario — no agrupar casos en el mismo test
- Estructura siempre: `// Arrange / // Act / // Assert`
- No usar `@SpringBootTest` para tests de service — es lento y carga toda la app innecesariamente
- Usar `assertThat` de AssertJ (ya incluido con Spring Boot Test) — no `assertEquals` de JUnit
- Correr `./mvnw test` después de escribir tests y asegurarse que todos pasan
