// Usuario semilla de sesión. Es lo único que queda del catálogo mockeado: el
// resto (productos, categorías, direcciones) ya viene del backend. SEED_USER se
// mantiene como placeholder de la sesión hasta que se migre auth a Redux.
export const SEED_USER = {
  user_id: 1,
  username: 'martin123',
  email: 'martin@mail.com',
  first_name: 'Martín',
  last_name: 'García',
  role: 'buyer',
  phone: '1155667788',
};
