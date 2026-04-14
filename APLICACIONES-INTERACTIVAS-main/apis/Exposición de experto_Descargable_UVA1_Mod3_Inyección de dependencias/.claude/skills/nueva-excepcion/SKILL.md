---
name: nueva-excepcion
description: Implementa el manejo de excepciones del proyecto: jerarquía base, GlobalExceptionHandler y excepciones de negocio específicas
---

# /nueva-excepcion

Implementa o extiende el manejo de excepciones del e-commerce.

Seguir las convenciones definidas en `.claude/skills/conventions.md`.

---

## Jerarquía de excepciones del proyecto

Antes de crear cualquier excepción nueva, verificar que la jerarquía base ya existe en `exceptions/`.
Si no existe, crearla primero.

### Clases base (crear si no existen)

**`EcommerceException`** — base de todas las excepciones del dominio:
```java
public abstract class EcommerceException extends RuntimeException {
    public EcommerceException(String message) {
        super(message);
    }
}
```

**`NotFoundException`** — recurso no encontrado → HTTP 404:
```java
public class NotFoundException extends EcommerceException {
    public NotFoundException(String recurso, Object id) {
        super(recurso + " con id " + id + " no encontrado");
    }
}
```

**`DuplicateException`** — recurso duplicado → HTTP 409:
```java
public class DuplicateException extends EcommerceException {
    public DuplicateException(String recurso, String campo, Object valor) {
        super(recurso + " con " + campo + " '" + valor + "' ya existe");
    }
}
```

**`BusinessRuleException`** — violación de regla de negocio → HTTP 422:
```java
public class BusinessRuleException extends EcommerceException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
```

### ApiError — formato de respuesta de error

```java
// entity/dto/ApiError.java
public record ApiError(
    int status,
    String error,
    String message,
    String timestamp
) {}
```

### GlobalExceptionHandler — manejador central

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateException ex) {
        return build(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRule(BusinessRuleException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ApiError(
            400, "Validation Error", mensaje,
            LocalDateTime.now().toString()
        ));
    }

    private ResponseEntity<ApiError> build(HttpStatus status, EcommerceException ex) {
        return ResponseEntity.status(status).body(new ApiError(
            status.value(), status.getReasonPhrase(), ex.getMessage(),
            LocalDateTime.now().toString()
        ));
    }
}
```

---

## Flujo según el pedido

### Si el pedido es "agregar manejo de excepciones al proyecto"
1. Crear `EcommerceException`, `NotFoundException`, `DuplicateException`, `BusinessRuleException`
2. Crear `ApiError` record en `entity/dto/`
3. Crear `GlobalExceptionHandler` en `exceptions/`
4. Escribir tests del handler (ver sección de tests)

### Si el pedido es "agregar una excepción de negocio nueva"
1. Verificar si encaja en `NotFoundException`, `DuplicateException` o `BusinessRuleException`
2. Si encaja: usarla directamente en el service con el mensaje apropiado
3. Si no encaja: crear una nueva subclase de `EcommerceException` + agregar `@ExceptionHandler` en `GlobalExceptionHandler`
4. Nunca usar `@ResponseStatus` en la excepción — el mapeo HTTP va solo en `GlobalExceptionHandler`

### Si el pedido es "el controller todavía maneja errores inline"
Refactorizar el controller para que delegue en el service:
- Eliminar los `if (result.isPresent()) ... return notFound()` del controller
- El service debe lanzar `NotFoundException` internamente
- El controller queda limpio: llama al service y retorna `ResponseEntity.ok(...)`

---

## Tests obligatorios del GlobalExceptionHandler

Crear `src/test/java/com/uade/tpo/demo/exceptions/GlobalExceptionHandlerTest.java`:

```java
@WebMvcTest(controllers = AlgunController.class)
class GlobalExceptionHandlerTest {

    @Autowired MockMvc mockMvc;
    @MockBean AlgunService algunService;

    @Test
    void deberiaRetornar404_cuandoRecursoNoExiste() {
        when(algunService.getById(99)).thenThrow(new NotFoundException("Recurso", 99));
        // perform GET y verificar status 404 + body con message
    }

    @Test
    void deberiaRetornar409_cuandoRecursoDuplicado() { ... }

    @Test
    void deberiaRetornar422_cuandoViolacionDeRegla() { ... }

    @Test
    void deberiaRetornar400_cuandoValidacionFalla() { ... }
}
```

---

## Reglas

- **Nunca** `@ResponseStatus` en la clase de excepción — todo el mapeo HTTP en `GlobalExceptionHandler`
- **Nunca** `try/catch` en controllers para excepciones de dominio — para eso existe el handler
- **Nunca** retornar `null` desde el service — siempre lanzar la excepción correspondiente
- Los mensajes de error deben ser informativos: incluir qué recurso y qué valor causó el problema
