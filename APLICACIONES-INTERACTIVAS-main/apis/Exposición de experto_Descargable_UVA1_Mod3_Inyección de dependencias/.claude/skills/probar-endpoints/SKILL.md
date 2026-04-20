---
name: probar-endpoints
description: Prueba los endpoints del e-commerce contra el servidor local y actualiza TESTING.md con los resultados. Usar cuando el usuario pide "probar", "testear" o "revisar" una sección de endpoints (por ejemplo "probá auth", "testeá categorías", "revisá los endpoints de pedidos").
---

# /probar-endpoints

Prueba una sección de endpoints del e-commerce de forma estructurada y registra los resultados en `TESTING.md`.

## Antes de empezar

1. Verificar que el servidor está corriendo en `http://localhost:8080`:
   ```bash
   curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/auth/login -X POST -H "Content-Type: application/json" -d '{"email":"x","password":"x"}'
   ```
   Si no devuelve un código HTTP, avisar al usuario que levante el servidor antes de continuar.

2. Leer `TESTING.md` para ver qué sección se va a probar y qué endpoints tiene.

3. Leer el controller correspondiente en `src/main/java/com/uade/tpo/demo/controllers/` para entender la auth requerida y la lógica esperada.

---

## Flujo de prueba

### Paso 1 — Obtener token de prueba

Registrar un usuario de prueba (o hacer login si ya existe). Guardar el token para usarlo en los requests que lo requieran:

```bash
curl -s -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"tester_<seccion>","email":"tester_<seccion>@mail.com","password":"password123","firstName":"Test","lastName":"User"}'
```

Si devuelve 409 (ya existe), hacer login en su lugar.

Si la sección requiere rol admin o seller, indicar al usuario que provea un token con ese rol, ya que no se puede crear uno por API sin un admin previo.

### Paso 2 — Ejecutar los casos de prueba

Para cada endpoint de la sección, ejecutar **al menos** estos escenarios:

#### Escenarios obligatorios

| Tipo | Descripción |
|------|-------------|
| Happy path | Request válido con auth correcta → esperar 2xx |
| Sin token | Request sin `Authorization` → esperar 401 |
| Rol incorrecto | Request con token de rol equivocado → esperar 403 (si aplica) |
| Body inválido | Request con campos vacíos o malformados → esperar 400 (si aplica) |
| Recurso inexistente | Request con ID que no existe → esperar 404 (si aplica) |
| Duplicado | Crear el mismo recurso dos veces → esperar 409 (si aplica) |

#### Formato del curl

```bash
# Sin auth
curl -s -w "\nSTATUS:%{http_code}" -X <MÉTODO> http://localhost:8080/<ruta> \
  -H "Content-Type: application/json" \
  -d '<body>'

# Con auth
curl -s -w "\nSTATUS:%{http_code}" -X <MÉTODO> http://localhost:8080/<ruta> \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '<body>'
```

### Paso 3 — Evaluar cada resultado

Para cada request, comparar el status HTTP recibido con el esperado:

- ✅ **OK** — el status y el body son los esperados
- ❌ **Falla** — el status difiere del esperado (documentar qué se esperaba y qué llegó)
- ⚠️ **Comportamiento inesperado** — el status es correcto pero el body tiene algo raro (campo faltante, mensaje confuso, campo que no debería estar, etc.)

### Paso 4 — Actualizar TESTING.md

Completar la columna **Resultado** de cada fila de la sección probada con ✅ / ❌ / ⚠️ y agregar notas donde haya algo para reportar.

Si se encuentran bugs o comportamientos inesperados, agregarlos también en la sección **Preguntas abiertas** del archivo.

---

## Reglas

- Nunca modificar datos productivos — usar siempre datos de prueba con prefijo `test_` o `tester_`
- Si un endpoint de escritura (POST/PUT/DELETE) crea datos en la DB, intentar limpiarlo al final (DELETE si está disponible)
- Si el servidor no está corriendo, no inventar resultados — pedirle al usuario que lo levante
- No asumir que un endpoint funciona porque el test anterior funcionó — probar cada uno por separado
- Reportar el resultado real, no el esperado — si algo falla, documentarlo tal cual

---

## Checklist de cierre

Al terminar la sección:

- [ ] Todos los endpoints de la sección tienen resultado en TESTING.md
- [ ] Los bugs encontrados están en Preguntas abiertas
- [ ] Se probaron tanto los happy paths como los casos de error
- [ ] Se verificó que `passwordHash` no aparece en ninguna respuesta que devuelva un usuario
- [ ] Cobertura de tests verificada (ver paso 5)

---

### Paso 5 — Verificar y completar cobertura de tests

Después de documentar en TESTING.md, revisar si la lógica probada tiene cobertura de tests unitarios:

1. Identificar qué `ServiceImpl` y repositories custom fueron ejercitados en esta sección.
2. Revisar si existe un `*ServiceTest.java` y `*RepositoryTest.java` para cada uno.
3. Si se encontraron bugs y se corrigieron, verificar que los casos corregidos estén cubiertos por un test.
4. Si hay lógica nueva o sin tests, invocar `/nuevos-tests` indicando:
   - Qué clase testear (`CategoryServiceImpl`, `ProductRepository`, etc.)
   - Qué métodos o ramas son nuevas o están sin cubrir
   - Si hay excepciones nuevas que deben verificarse

**Ejemplo de invocación:**
> `/nuevos-tests CategoryServiceImpl — agregar test para deactivateCategory y deleteCategory con productos`

No inventar resultados de tests — ejecutar `./mvnw test -Dtest="NombreTest"` y reportar el resultado real.
