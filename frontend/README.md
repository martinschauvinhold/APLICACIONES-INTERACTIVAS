# Vector Tech — Frontend E-commerce

Frontend de un e-commerce de productos tecnológicos, desarrollado para el
**TPO de Aplicaciones Interactivas**. Se conecta al backend Spring Boot
incluido en este mismo repositorio (carpeta raíz).

Construido con **React + Vite**, **React Router DOM** para la navegación,
**Redux Toolkit** para el estado global (slices por dominio en
`src/features/`) y **Axios** para todas las peticiones HTTP. No usa datos
mockeados: todo el catálogo, usuarios, pedidos, etc. vienen del backend real.

---

## 🚀 Cómo ejecutar el proyecto

Requisitos: **Node.js 18+**, **npm**, y el backend corriendo (ver el
`README.md` de la raíz del repositorio).

```bash
# 1. Instalar dependencias
npm install

# 2. Levantar el servidor de desarrollo
npm run dev
```

El proyecto abre en `http://localhost:5173` y espera al backend en
`http://localhost:8080` (configurable con la variable de entorno
`VITE_API_URL`, ver `src/api/axios.js`).

Otros comandos:

```bash
npm run build     # build de producción (carpeta dist/)
npm run preview   # previsualiza el build
npm run lint      # corre ESLint
```

---

## Estructura

```
src/
  api/          # cliente Axios (axios.js) + helpers de sesión (client.js)
  features/     # slices de Redux Toolkit, uno por dominio (auth, cart, orders, ...)
  hooks/        # hooks que envuelven useSelector/useDispatch por dominio
  views/        # pantallas (una por ruta)
  components/   # componentes reutilizables
  store/        # configuración del store y selectores derivados
```

---

## Roles y flujos

- **Comprador (buyer)**: navega el catálogo, arma el carrito, hace checkout,
  ve y cancela sus pedidos.
- **Vendedor (seller)**: gestiona sus productos, precios, stock y tiers
  mayoristas, ve sus ventas.
- **Admin**: gestiona usuarios y ve todos los pedidos.

---

## 👥 Proyecto

Trabajo Práctico Grupal — **Aplicaciones Interactivas** · 2026
