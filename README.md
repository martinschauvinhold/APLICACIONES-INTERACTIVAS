# Vector Tech — Frontend E-commerce

Maqueta funcional del frontend de un e-commerce de productos tecnológicos,
desarrollada para el **TPO de Aplicaciones Interactivas**.

Construido con **React + Vite**, **React Router DOM** para la navegación,
**Redux Toolkit** para el estado global y componentes reutilizables con props
y hooks (`useState`, `useEffect`, `useSelector`, `useDispatch`, `useParams`,
`useNavigate`).

---

## 🚀 Cómo ejecutar el proyecto

Requisitos: **Node.js 18+** y **npm**.

```bash
# 1. Instalar dependencias
npm install

# 2. Levantar el servidor de desarrollo
npm run dev
```

El proyecto abre automáticamente en `http://localhost:5173`.

Otros comandos:

```bash
npm run build     # build de producción (carpeta dist/)
npm run preview   # previsualiza el build
```

---

## 🗂️ Estructura del proyecto

```
vector-tech-vite/
├── index.html               # HTML raíz (monta #root)
├── package.json             # dependencias y scripts
├── vite.config.js           # configuración de Vite
└── src/
    ├── main.jsx             # punto de entrada: Provider + BrowserRouter
    ├── App.jsx              # define las rutas (React Router DOM)
    │
    ├── api/
    │   └── client.js        # cliente HTTP para el backend Spring Boot
    │
    ├── components/          # componentes reutilizables (props + hooks)
    │   ├── Nav.jsx           # barra de navegación + búsqueda + categorías
    │   ├── Footer.jsx
    │   ├── CartDrawer.jsx    # drawer lateral del carrito
    │   ├── QuickViewModal.jsx
    │   ├── ProductCard.jsx
    │   ├── ProductImage.jsx
    │   ├── SpecSheet.jsx
    │   ├── TrustBand.jsx
    │   ├── AuthShell.jsx     # layout compartido login/register
    │   ├── Btn.jsx
    │   ├── Stars.jsx
    │   ├── Tag.jsx
    │   ├── Field.jsx
    │   ├── Stepper.jsx
    │   └── Empty.jsx
    │
    ├── pages/               # vistas principales (una por ruta)
    │   ├── Catalog.jsx       # /            catálogo + hero + categorías
    │   ├── Product.jsx       # /producto/:id  detalle del producto
    │   ├── Cart.jsx          # /carrito     carrito + cupones
    │   ├── Checkout.jsx      # /checkout    flujo de 3 pasos
    │   ├── Confirmation.jsx  # /confirmacion/:id  confirmación + tracking
    │   ├── Orders.jsx        # /pedidos     historial de pedidos
    │   ├── Login.jsx         # /login
    │   └── Register.jsx      # /register
    │
    ├── store/               # Redux Toolkit
    │   ├── index.js          # configureStore
    │   ├── selectors.js      # selectores derivados
    │   └── slices/
    │       ├── catalogSlice.js
    │       ├── cartSlice.js
    │       ├── checkoutSlice.js
    │       ├── sessionSlice.js
    │       ├── ordersSlice.js
    │       └── uiSlice.js
    │
    ├── data/                # datos semilla (modo demo)
    │   ├── products.js
    │   ├── categories.js
    │   └── coupons.js
    │
    ├── utils/
    │   └── format.js         # helpers (formato de moneda, etc.)
    │
    └── styles/
        ├── global.css        # tokens + nav + catálogo + componentes
        └── screens.css       # detalle, carrito, checkout, auth, etc.
```

---

## 🧭 Navegación entre vistas

| Ruta | Vista | Descripción |
|---|---|---|
| `/` | **Catálogo** | Hero, categorías, productos destacados y grilla con filtros |
| `/producto/:id` | **Detalle** | Galería, variantes, precios por cantidad, ficha técnica |
| `/carrito` | **Carrito** | Líneas editables, cupones, resumen de totales |
| `/checkout` | **Checkout** | Flujo de 3 pasos: dirección → pago → revisión |
| `/confirmacion/:id` | **Confirmación** | Pedido confirmado + seguimiento del envío |
| `/pedidos` | **Mis pedidos** | Historial con estado de cada pedido |
| `/login` | **Login** | Inicio de sesión |
| `/register` | **Registro** | Alta de usuario con selector de rol |

La navegación se maneja con **React Router DOM** (`<Routes>`, `<Route>`,
`Link`, `NavLink`, `useNavigate`, `useParams`).

---

## 🧩 Conceptos de React aplicados

- **Componentes y props**: cada pieza de UI (`Btn`, `Stars`, `ProductCard`,
  `Stepper`, etc.) recibe props y es reutilizable.
- **Hooks**:
  - `useState` — formularios, selección de variantes, cantidad, cupones.
  - `useEffect` — hidratación de datos, simulación de tracking, listeners.
  - `useParams` / `useNavigate` — ruteo dinámico.
  - `useSelector` / `useDispatch` — estado global con Redux.
- **Estado global**: Redux Toolkit con 6 slices (catalog, cart, checkout,
  session, orders, ui).

---

## 🔌 Integración con el backend (opcional)

El frontend funciona en **modo demo** con datos semilla por defecto. Para
conectar con un backend Spring Boot:

1. El cliente (`src/api/client.js`) apunta a `http://localhost:8080`.
   Cambiá la URL en ese archivo si tu backend corre en otro puerto.
2. Configurá **CORS** en el backend para aceptar peticiones del frontend.
3. El flujo de checkout sigue los endpoints del proyecto:
   `POST /orders` → `POST /order-items` → `POST /payments`
   → `PUT /orders/{id}` → `POST /deliveries`.

Si el backend no responde, el frontend cae automáticamente al modo demo,
así que la maqueta siempre es navegable.

### Cupones de prueba (modo demo)

- `TECH10` — 10% de descuento
- `BIENVENIDA` — 15% de descuento
- `ENVIOGRATIS` — envío gratis

---

## 👥 Proyecto

Trabajo Práctico Grupal — **Aplicaciones Interactivas** · 2026
