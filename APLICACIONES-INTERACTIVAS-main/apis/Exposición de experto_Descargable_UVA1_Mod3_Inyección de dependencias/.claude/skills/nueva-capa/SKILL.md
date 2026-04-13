---
name: nueva-capa
description: Crea un módulo completo (entity, DTO record, repository, service, controller) para el e-commerce siguiendo TDD
---

# /nueva-capa

Crea un módulo vertical completo para el proyecto e-commerce: entity, DTO como record, repository, service (interfaz + impl) y controller REST.

Seguir las convenciones definidas en `.claude/skills/conventions.md`.

---

## Antes de escribir código

1. Si no se especificó el nombre de la entidad, preguntarlo.
2. Confirmar los campos principales de la entidad (nombres, tipos, relaciones con otras entidades).
3. Confirmar el nombre del endpoint REST (ej: `products`, `warehouses`).

---

## Flujo obligatorio: TDD

**Siempre escribir los tests del service PRIMERO.** Recién después crear las clases de producción.

### Paso 1 — Test del service (antes de crear nada más)

Crear `src/test/java/com/uade/tpo/demo/service/NombreServiceTest.java`:

- Usar `@ExtendWith(MockitoExtension.class)` — sin Spring context, sin H2
- Mockear el repository con `@Mock`
- Inyectar el impl con `@InjectMocks`
- Cubrir obligatoriamente:
  - `create` con datos válidos → retorna entidad guardada
  - `getById` con id existente → retorna la entidad
  - `getById` con id inexistente → lanza `NotFoundException`
  - `update` con id existente → retorna entidad actualizada
  - `delete` con id existente → verifica que se llamó `deleteById`
  - Cualquier regla de negocio específica del módulo

### Paso 2 — Entidad JPA

Crear `src/main/java/com/uade/tpo/demo/entity/NombreEntidad.java`:
- `@Data @Builder @NoArgsConstructor @AllArgsConstructor @Entity @Table(name = "TABLA")`
- `@Column(name = ...)` explícito en cada campo
- ID como `Integer` con `@GeneratedValue(strategy = GenerationType.IDENTITY)`

### Paso 3 — DTO record

Crear `src/main/java/com/uade/tpo/demo/entity/dto/NombreRequest.java`:
- `public record NombreRequest(...) {}`
- Bean Validation en cada campo: `@NotBlank`, `@NotNull`, `@Min`, `@Max` según corresponda
- Solo campos que el cliente puede enviar — sin `id`

### Paso 4 — Repository

Crear `src/main/java/com/uade/tpo/demo/repository/NombreRepository.java`:
- Extender `JpaRepository<NombreEntidad, Integer>`
- Agregar solo los métodos de query que el service realmente necesite

### Paso 5 — Service interface

Crear `src/main/java/com/uade/tpo/demo/service/NombreService.java`:
- Métodos: `getAll`, `getById`, `create`, `update`, `delete`
- Sin implementación, sin anotaciones Spring

### Paso 6 — Service implementation

Crear `src/main/java/com/uade/tpo/demo/service/NombreServiceImpl.java`:
- `@Service @RequiredArgsConstructor` — NO `@Autowired` en campo
- `final` en el repository inyectado
- `@Transactional` en `create`, `update` y `delete`
- Lanzar `NotFoundException` cuando `findById` retorna vacío
- Lanzar `DuplicateException` cuando corresponda (campos únicos)
- Lanzar `BusinessRuleException` para reglas de negocio específicas

### Paso 7 — Controller

Crear `src/main/java/com/uade/tpo/demo/controllers/NombresController.java`:
- `@RestController @RequestMapping("endpoint") @RequiredArgsConstructor`
- `@Valid` en todos los `@RequestBody`
- El controller NO verifica existencia con `findById` — el service lanza `NotFoundException`
- `POST` retorna `201 Created` con `Location` header usando `URI.create`
- `DELETE` retorna `204 No Content`

### Paso 8 — Verificar

Correr `./mvnw test` y confirmar que todos los tests pasan antes de dar la tarea por terminada.

---

## Checklist de revisión

- [ ] Tests del service cubren happy path y casos de error
- [ ] DTO es un record con Bean Validation
- [ ] ServiceImpl usa `@RequiredArgsConstructor` (no `@Autowired`)
- [ ] Métodos de escritura tienen `@Transactional`
- [ ] Controller usa `@Valid` en request body
- [ ] `./mvnw test` pasa sin errores
