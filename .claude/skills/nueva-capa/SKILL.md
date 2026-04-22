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

### Paso 2 — Enums de estado (si aplica)

Si la entidad tiene campos de estado/tipo, crear primero el enum:

```java
// src/main/java/com/uade/tpo/demo/entity/NombreStatus.java
public enum NombreStatus { PENDING, ACTIVE, CLOSED }
```

Reglas:
- Archivo separado en `entity/`, nombre `<Entidad>Status` o `<Entidad>Type`
- Valores en `UPPER_SNAKE_CASE`
- Nunca usar `String` para representar estados — siempre enum

### Paso 3 — Entidad JPA

Crear `src/main/java/com/uade/tpo/demo/entity/NombreEntidad.java`:
- `@Data @Builder @NoArgsConstructor @AllArgsConstructor @Entity @Table(name = "TABLA")`
- `@Column(name = ...)` explícito en cada campo
- ID como `Integer` con `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- Campos de estado con `@Enumerated(EnumType.STRING)` y tipo enum (no `String`)

### Paso 4 — DTO record

Crear `src/main/java/com/uade/tpo/demo/entity/dto/NombreRequest.java`:
- `public record NombreRequest(...) {}`
- Bean Validation en cada campo: `@NotBlank`, `@NotNull`, `@Min`, `@Max` según corresponda
- Campos de estado usan el tipo enum directamente (no `String`)
- Solo campos que el cliente puede enviar — sin `id`

### Paso 5 — Repository

Crear `src/main/java/com/uade/tpo/demo/repository/NombreRepository.java`:
- Extender `JpaRepository<NombreEntidad, Integer>`
- Agregar solo los métodos de query que el service realmente necesite

### Paso 6 — Service interface

Crear `src/main/java/com/uade/tpo/demo/service/NombreService.java`:
- Métodos: `getAll`, `getById`, `create`, `update`, `delete`
- Sin implementación, sin anotaciones Spring

### Paso 7 — Service implementation

Crear `src/main/java/com/uade/tpo/demo/service/NombreServiceImpl.java`:
- `@Service @RequiredArgsConstructor` — NO `@Autowired` en campo
- `final` en el repository inyectado
- `@Transactional` en `create`, `update` y `delete`
- Lanzar `NotFoundException` cuando `findById` retorna vacío
- Lanzar `DuplicateException` cuando corresponda (campos únicos)
- Lanzar `BusinessRuleException` para reglas de negocio específicas
- Comparar estados con `==` / `!=` sobre el enum, no con `.equals()` de String

### Paso 8 — Controller

Crear `src/main/java/com/uade/tpo/demo/controllers/NombresController.java`:
- `@RestController @RequestMapping("endpoint") @RequiredArgsConstructor`
- `@Valid` en todos los `@RequestBody`
- El controller NO verifica existencia con `findById` — el service lanza `NotFoundException`
- `POST` retorna `201 Created` con `Location` header usando `URI.create`
- `DELETE` retorna `204 No Content`
- **Recurso no encontrado → siempre `notFound()` (404), nunca `noContent()` (204)**:
  ```java
  // ❌ MAL — devuelve 204, el cliente lo interpreta como éxito sin body
  return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());

  // ✅ BIEN — devuelve 404
  return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  ```

### Paso 9 — Verificar

Correr `./mvnw test` y confirmar que todos los tests pasan antes de dar la tarea por terminada.

---

## Checklist de revisión

- [ ] Clases, métodos y variables en inglés — sin excepción en código de producción
- [ ] Campos de estado usan enum con `@Enumerated(EnumType.STRING)` — nunca `String`
- [ ] Enum definido en `entity/` con valores en `UPPER_SNAKE_CASE`
- [ ] Tests del service cubren happy path y casos de error
- [ ] DTO es un record con Bean Validation y tipos enum donde corresponde
- [ ] ServiceImpl usa `@RequiredArgsConstructor` (no `@Autowired`)
- [ ] Métodos de escritura tienen `@Transactional`
- [ ] Controller usa `@Valid` en request body
- [ ] Recurso no encontrado usa `notFound()` (404), nunca `noContent()` (204)
- [ ] `./mvnw test` pasa sin errores
