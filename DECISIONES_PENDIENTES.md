# Decisiones pendientes — Análisis y recomendaciones

> Análisis de las 6 decisiones pendientes documentadas en `TESTING.md`, con pros/contras de cada alternativa y la recomendación.

---

## 1. Endpoints de lectura: ¿públicos o con token?

Aplica a: `/categories`, `/products`, `/variants`, `/reviews`, `/coupons`, `/deliveries/{id}`, `/deliveries/order/{orderId}`, `/tracking/{id}`, `/tracking/delivery/{deliveryId}`, `/support/tickets/{id}`.

### Opción A — Todo requiere token (estado actual)

- ✅ Más simple de razonar: una sola regla (`anyRequest().authenticated()`).
- ✅ Habilita rate limiting/auditoría por usuario, no solo por IP.
- ❌ Fricción para vista de catálogo: el front necesita un "token de invitado" o forzar login antes de mostrar productos.

### Opción B — Público lo realmente "vidriera"

Públicos: `/categories`, `/products`, `/variants`, `/reviews/product/{id}`.

- ✅ Patrón estándar de e-commerce (Mercado Libre, Amazon te dejan ver el catálogo sin login).
- ✅ Mejor UX y SEO si algún día hay front web.
- ❌ Hay que separar bien qué endpoints quedan dentro y cuáles afuera, mantener dos listas.

### Opción C — Todo público para lectura

Incluyendo deliveries, tracking, coupons, tickets.

- ❌ Mala idea: tracking y deliveries exponen direcciones de envío y número de orden; tickets exponen mensajes; coupons exponen códigos de descuento. Son datos sensibles aunque sean GET.

### Recomendación: **Opción B**

Públicos solo `GET /categories`, `GET /products`, `GET /variants`, `GET /reviews` y `GET /reviews/product/{id}`. El resto sigue con token.

> **Nota:** tracking y deliveries deberían incluso estar **scoped al dueño de la orden** (hoy cualquier token los lee), pero eso es otra discusión.

---

## 2. Postman: ¿agregar header o cambiar SecurityConfig?

Depende de la decisión #1.

Si vas por la **Opción B**, hacés ambas cosas:

1. Actualizás `SecurityConfig` para los endpoints públicos.
2. Agregás `Authorization: Bearer {{token}}` a los ~30 requests que les falta.

### Recomendación

Aprovechar para meter una **variable de colección + script de pre-request** que setee el token automáticamente desde el `/auth/login`. Te ahorra mantenimiento futuro.

---

## 3. Logout: ¿solo sesión actual o todas las del usuario?

Hoy: `deleteByUser` borra todas.

### Solo la actual

- ✅ Comportamiento estándar (Google, GitHub). Cerrar sesión en el cel no te tira de la PC.
- ✅ Más útil con multi-dispositivo, que es lo común.
- ❌ Necesitás identificar la sesión activa por el `jti` del token o por un ID en la tabla `sessions`.

### Todas (estado actual)

- ✅ Trivial de implementar.
- ✅ Pánico-friendly: si te robaron el celular, un logout te saca de todos lados.
- ❌ Inconsistente con expectativas del usuario.

### Recomendación: **solo la actual**

Y agregar un endpoint aparte tipo `POST /auth/logout-all` para el caso de pánico. Dos comportamientos, dos endpoints distintos.

### ✅ Decisión tomada: **se mantienen todas las sesiones cerradas**

Por decisión de negocio, para el alcance actual del proyecto se prioriza la simplicidad y el caso "pánico" (un solo logout te saca de todos los dispositivos). **Se quiere iterar a futuro** hacia el modelo "solo la sesión actual + endpoint `/auth/logout-all` aparte", pero queda fuera del scope inmediato.

---

## 4. Rate limiting: ¿son adecuados los límites?

Actualmente: 10 req/min para `/auth/*`, 100 req/min para el resto (por IP).

### Análisis

- **10/min para `/auth/*`:** razonable contra brute force, pero molesto si el usuario se equivoca al loguear desde la app + Postman a la vez. Subiría a **20/min**.
- **100/min general:** muy bajo si el front carga catálogo + imágenes. Para una SPA real, **300-500/min** es más típico. Para este proyecto, dejaría 100 y documentaría que es a propósito.

### Recomendación

Dejarlo como está, **documentar** el porqué en el README, y mencionar que en producción se ajustaría con métricas reales. No vale la pena tunear sin datos.

### ✅ Decisión tomada: **se mantienen los valores actuales**

En las pruebas manuales y la corrida completa de la colección de Postman no se observaron falsos positivos ni fricciones con los límites actuales. Para el alcance del proyecto son adecuados.

---

## 5. Cómo se crea el primer admin

### Estado actual

El SQL `create_database(apis).sql` ya tiene los INSERTs con hashes BCrypt para dos usuarios de prueba (`admin@mail.com` y `seller_test@test.com`, ambos con password `Test1234!`). El problema es que **no hay un comando que los aplique** — el `Makefile` actual solo crea la DB y el usuario, pero no ejecuta los seeds. Hoy hay que copiar/pegar manualmente las queries en SQL Server.

### Opción A — Nuevo target `make seed-db` (recomendada)

Agregar un target en el `Makefile` que ejecute los INSERTs del seed dentro del contenedor SQL Server, idempotente (no crea duplicados si ya existen).

```makefile
.PHONY: seed-db
seed-db:
	@printf "$(CYAN)[  db    ]$(RESET) Cargando datos iniciales (admin + seller de prueba)...\n"
	docker exec -i $(CONTAINER) $(SQLCMD) -d "E-commerce" -Q " \
	    IF NOT EXISTS (SELECT 1 FROM USERS WHERE email = 'admin@mail.com') \
	    BEGIN INSERT INTO USERS (username, email, password_hash, first_name, last_name, role, phone) \
	    VALUES ('admin_test', 'admin@mail.com', '\$$2a\$$10\$$kFEOgt8Y9MUNY1Kfnzup/ekGXh.8dALD2ymXPSMb2Jo4WGYAI42si', 'Admin', 'Test', 'admin', NULL); END; \
	    IF NOT EXISTS (SELECT 1 FROM USERS WHERE email = 'seller_test@test.com') \
	    BEGIN INSERT INTO USERS (username, email, password_hash, first_name, last_name, role, phone) \
	    VALUES ('seller_test', 'seller_test@test.com', '\$$2a\$$10\$$x8Tjy23gKQIHT.8WtSq3eOrv06s9H8zjneK3gah46jlWWy0gyOdJG', 'Seller', 'Test', 'seller', NULL); END;"
	@printf "$(GREEN)[  db    ]$(RESET) Seed listo. Login: admin@mail.com / Test1234!\n"
```

Y modificar `start-all` para que lo incluya:
```makefile
start-all: start-db init-db seed-db start-app
```

- ✅ **Cero pasos manuales**: clonás el repo, corrés `make start-all`, ya tenés admin loguéable.
- ✅ Idempotente: si los usuarios ya existen, no hace nada.
- ✅ Reusa el SQL que ya está versionado, no duplica el hash.
- ❌ El hash sigue versionado, pero está atado a `Test1234!` que es claramente "credencial de desarrollo".

### Opción B — `CommandLineRunner` en Spring Boot

Bean que al arrancar la app verifica si hay algún admin en la DB, y si no, crea uno leyendo `INITIAL_ADMIN_EMAIL` / `INITIAL_ADMIN_PASSWORD` desde env vars o `application.properties`.

- ✅ No depende del motor de DB (funciona en H2/SQL Server por igual).
- ✅ Sin hash en el repo si las env vars vienen de afuera.
- ❌ Más código (~30 líneas + tests) por algo que se ejecuta una vez.
- ❌ Si las env vars no se setean, hay que decidir qué default usar — terminás en Opción A camuflada.

### Recomendación: **Opción A**

El `Makefile` ya es la "interfaz de comandos" del proyecto (`make start-all`, `make init-db`, etc.). Agregar `seed-db` es coherente con esa convención y resuelve el caso de "clono el repo y necesito loguearme" en un solo comando. Documentar en el README que las credenciales son solo para desarrollo y deben cambiarse antes de producción.

### ✅ Implementación

- **Archivo nuevo:** `seed-users.sql` con los INSERTs idempotentes para admin + seller (`IF NOT EXISTS`).
- **Makefile:** target nuevo `seed-db` que pasa `seed-users.sql` por stdin a `sqlcmd` dentro del contenedor, e imprime las credenciales al terminar.
- **README:** sección nueva "Credenciales de desarrollo" con la tabla y warning de solo-dev. Sección "Crear el primer admin" reescrita para apuntar al nuevo flujo.
- **Help del Makefile:** actualizado para mostrar el flujo de primera vez en dos pasos.

**Por qué dos pasos y no encadenar `seed-db` a `start-all`:**

`start-app` es bloqueante (corre la app en foreground), así que no se puede poner `seed-db` después en la cadena. Y `seed-db` no puede ir antes porque las tablas las crea Hibernate al arrancar la app por primera vez (`spring.jpa.hibernate.ddl-auto=update`). El flujo natural es:

1. `make start-all` → levanta DB y arranca la app (Hibernate crea las tablas).
2. En otra terminal, una vez que aparece `Started DemoApplication`: `make seed-db`.

Ambos pasos están documentados en el `make help`. El seed es idempotente, así que correrlo más veces no rompe nada.

### Tabla de credenciales

| Rol     | Email                  | Password    |
|---------|------------------------|-------------|
| `admin` | `admin@mail.com`       | `Test1234!` |
| `seller`| `seller_test@test.com` | `Test1234!` |

⚠️ Solo para desarrollo. Cambiar antes de producción.

---

## 6. Multi-rol por usuario (decisión de negocio #1)

> ¿Puede una persona tener cuenta de comprador y vendedor al mismo tiempo con el mismo email?

La nota en `TESTING.md` ya dice "se pospone". Algunas precisiones:

- La dirección correcta es **`@ElementCollection<Role>`** (no `@ManyToMany` — los roles son un enum, no entidad).
- Es un día de trabajo: migración + ajustar JWT claims + ajustar `@PreAuthorize`.
- La alternativa de `PATCH /users/{id}/role` **no resuelve el caso real** (la persona quiere ser ambas cosas, no migrar de una a la otra).

### Recomendación

**Posponer está bien**, pero dejar escrito en el README que el modelo actual asume rol único por diseño.

---

## Resumen ejecutivo

| # | Decisión                | Estado | Resolución                                                                                |
|---|-------------------------|--------|-------------------------------------------------------------------------------------------|
| 1 | Endpoints públicos      | ✅ Implementado | Solo catálogo (categories/products/variants/reviews) — `permitAll()` en `SecurityConfig` |
| 2 | Postman                 | ✅ Implementado | Variable `{{token}}` + script post-request en login + headers en 54 requests             |
| 3 | Logout                  | ✅ Decidido     | Se mantiene "cierra todas las sesiones" por simplicidad — iterar a futuro                |
| 4 | Rate limiting           | ✅ Decidido     | Se mantienen los valores actuales (validados en pruebas)                                  |
| 5 | Primer admin            | ✅ Implementado | `seed-users.sql` + `make seed-db` + sección de credenciales en el README                |
| 6 | Multi-rol               | ✅ Decidido     | Posponer, documentar limitación de rol único en el README                                 |
