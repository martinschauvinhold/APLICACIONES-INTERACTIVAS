import { createSlice } from '@reduxjs/toolkit';
import { SEED_USER, SEED_ADDRESSES } from '../../data/products';

const initialState = {
  user: SEED_USER,
  addresses: SEED_ADDRESSES,
  isAuthenticated: true,
};

const sessionSlice = createSlice({
  name: 'session',
  initialState,
  reducers: {
    addAddress: (state, action) => {
      const a = action.payload;
      state.addresses.push({
        ...a,
        address_id: state.addresses.length + 1,
        user_id: state.user.user_id,
      });
    },
    login: (state, action) => {
      state.user.email = action.payload.email;
      if (action.payload.role) state.user.role = action.payload.role;
      state.isAuthenticated = true;
    },
    // Demo: entrar con un rol específico (buyer/admin) sin backend.
    // Útil porque /auth/register del backend siempre crea un buyer.
    loginAs: (state, action) => {
      const role = action.payload.role;
      state.user = {
        ...state.user,
        role,
        first_name: role === 'admin' ? 'Admin' : 'Martín',
        email: role === 'admin' ? 'admin@vector.tech' : 'martin@mail.com',
      };
      state.isAuthenticated = true;
    },
    register: (state, action) => {
      const p = action.payload;
      state.user = {
        user_id: 1,
        username: p.username,
        email: p.email,
        first_name: p.first_name,
        last_name: p.last_name,
        role: p.role,
        phone: '',
      };
      state.isAuthenticated = true;
    },
    logout: (state) => { state.isAuthenticated = false; },
  },
});

export const { addAddress, login, loginAs, register, logout } = sessionSlice.actions;
export default sessionSlice.reducer;
