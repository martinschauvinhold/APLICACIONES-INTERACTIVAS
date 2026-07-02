import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { Provider } from 'react-redux';
import { store } from './store';
import { fetchCatalog } from './features/products/productsSlice';
import { fetchCategories } from './features/categories/categoriesSlice';
import { logout } from './features/auth/authSlice';
import { setUnauthorizedHandler } from './api/client';
import App from './App';
import './index.css';

// Cuando el backend rechaza el token (401), limpiar la sesión de Redux
setUnauthorizedHandler(() => store.dispatch(logout()));

// Cargar catálogo y categorías al iniciar la app
store.dispatch(fetchCatalog());
store.dispatch(fetchCategories());

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <Provider store={store}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </Provider>
  </React.StrictMode>
);
