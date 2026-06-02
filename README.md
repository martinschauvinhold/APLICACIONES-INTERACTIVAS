# VECTOR.TECH — Maqueta Frontend (TPO APIs)

Maqueta del frontend de **VECTOR.TECH**, un e-commerce de productos tecnológicos
en modalidad **minorista y mayorista**. Construida con **React + Vite** y
**React Router DOM**, siguiendo el diseño de la maqueta de Figma del TPO.

---

## 🚀 Cómo ejecutar el proyecto

Requisitos: tener instalado **Node.js 18+**.

```bash
# 1. Instalar dependencias
npm install

# 2. Levantar el servidor de desarrollo
npm run dev
```

Luego abrir en el navegador la URL que muestra la consola
(por defecto: **http://localhost:5173**).

Otros comandos:

```bash
npm run build     # genera la versión de producción en /dist
npm run preview   # previsualiza la build de producción
```

---

## 🗂️ Estructura del proyecto

```
vector-tech/
├── index.html
├── package.json
├── vite.config.js
├── public/
│   └── favicon.svg
└── src/
    ├── main.jsx              # punto de entrada
    ├── App.jsx               # definición de TODAS las rutas (React Router)
    ├── index.css             # estilos globales
    ├── assets/
    ├── components/           # componentes reutilizables
    │   ├── LiveTicker.jsx
    │   ├── Navbar.jsx
    │   ├── Footer.jsx
    │   ├── ProductCard.jsx
    │   ├── OrderCard.jsx
    │   ├── QuantityStepper.jsx
    │   ├── RoleToggle.jsx
    │   └── Stars.jsx
    ├── layouts/              # estructuras compartidas
    │   ├── StoreLayout.jsx   # ticker + navbar + footer (tienda)
    │   └── AuthLayout.jsx    # pantalla partida (login / registro)
    ├── pages/                # vistas principales
    │   ├── Login.jsx
    │   ├── Registro.jsx
    │   ├── Catalogo.jsx
    │   ├── DetalleProducto.jsx
    │   ├── Carrito.jsx
    │   ├── Checkout.jsx
    │   ├── Confirmacion.jsx
    │   ├── MisPedidos.jsx
    │   └── Contacto.jsx
    ├── data/                 # datos de ejemplo y utilidades
    │   ├── products.js
    │   ├── orders.js
    │   └── format.js
    └── hooks/
        └── useCart.js        # hook personalizado del carrito (useState)
```

---

## 🧭 Rutas (React Router DOM)

| Ruta              | Vista               | Descripción                          |
| ----------------- | ------------------- | ------------------------------------ |
| `/login`          | Login               | Inicio de sesión                     |
| `/registro`       | Registro            | Creación de cuenta                   |
| `/`               | Catálogo            | Home / listado de productos          |
| `/productos`      | Catálogo            | Alias del catálogo                   |
| `/detalle/:id`    | Detalle de Producto | **Ruta dinámica** por id de producto |
| `/carrito`        | Carrito             | Carrito de compras                   |
| `/checkout`       | Checkout            | Método de pago y resumen             |
| `/confirmacion`   | Confirmación        | Pedido confirmado + seguimiento      |
| `/mis-pedidos`    | Mis Pedidos         | Historial de pedidos                 |
| `/contacto`       | Contacto            | Formulario de contacto               |

Navegación con `<Link>` / `<NavLink>` y hooks `useNavigate`, `useParams`,
`useLocation`.

---

## ✅ Consignas cubiertas

1. **Estructura de componentes y vistas** — carpetas `/components`, `/pages`,
   `/layouts`, `/data`, `/hooks`. Cada componente en su `.jsx`.
2. **Routing** — React Router DOM con rutas principales + **ruta dinámica**
   `/detalle/:id`, usando `<Link>`/`<NavLink>` y hooks de navegación.
3. **Comunicación entre componentes** — paso de información mediante **props**
   (productos, pedidos, carrito) y **estado local con `useState`** en múltiples
   componentes (formularios de login/registro/contacto, contador de cantidad,
   filtros de catálogo, cupón del carrito, método de pago del checkout, pestañas
   de "Mis pedidos").
4. **Maquetado visual** — estilos coherentes con la maqueta de Figma
   (paleta negro + verde, diseño responsivo) en CSS puro.

---

## 🛒 Funcionalidad destacada

- El **carrito es funcional**: agregar productos desde el catálogo o el detalle,
  modificar cantidades y eliminar items actualiza el contador del navbar, el
  resumen del pedido y el checkout en tiempo real (estado compartido vía props).
- El **cupón `TECH10`** aplica 10% de descuento sobre el subtotal.

---

_Trabajo Práctico Obligatorio — Aplicaciones Interactivas._
