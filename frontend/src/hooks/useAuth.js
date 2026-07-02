import { useDispatch, useSelector } from 'react-redux';
import { login, register, logout, clearError } from '../features/auth/authSlice';
import { selectUser, selectIsAuthenticated, selectAuthLoading, selectAuthError } from '../features/auth/authSlice';
import { fetchAddressesByUser, createAddress } from '../features/addresses/addressesSlice';
import { fetchOrdersByUser } from '../features/orders/ordersSlice';
import { fetchUsers, createUser } from '../features/users/usersSlice';
import { selectAddresses } from '../features/addresses/addressesSlice';
import { selectUsers } from '../features/users/usersSlice';

export function useAuth() {
  const dispatch = useDispatch();
  const user = useSelector(selectUser);
  const isAuthenticated = useSelector(selectIsAuthenticated);
  const loading = useSelector(selectAuthLoading);
  const error = useSelector(selectAuthError);
  const addresses = useSelector(selectAddresses);
  const users = useSelector(selectUsers);

  const handleLogin = async (credentials) => {
    const result = await dispatch(login(credentials));
    if (login.rejected.match(result)) {
      throw new Error(result.payload || 'No se pudo iniciar sesión');
    }
    const userId = result.payload.user_id;
    dispatch(fetchAddressesByUser(userId));
    dispatch(fetchOrdersByUser(userId));
    if (result.payload.role === 'admin') dispatch(fetchUsers());
    return result;
  };

  const handleRegister = async (form) => {
    const result = await dispatch(register(form));
    if (register.rejected.match(result)) {
      throw new Error(result.payload || 'No se pudo registrar');
    }
    const userId = result.payload.user_id;
    dispatch(fetchAddressesByUser(userId));
    dispatch(fetchOrdersByUser(userId));
    return result;
  };

  const handleLogout = () => dispatch(logout());

  const handleCreateUser = (data) => dispatch(createUser(data));

  const handleAddAddress = (form) => {
    if (!user) return;
    return dispatch(createAddress({ userId: user.user_id, ...form }));
  };

  return {
    user,
    isAuthenticated,
    loading,
    error,
    addresses,
    users,
    role: user?.role,
    login: handleLogin,
    register: handleRegister,
    logout: handleLogout,
    createUser: handleCreateUser,
    addAddress: handleAddAddress,
    clearError: () => dispatch(clearError()),
  };
}
