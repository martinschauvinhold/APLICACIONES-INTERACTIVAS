import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Configuración base de Vite con el plugin oficial de React.
export default defineConfig({
  plugins: [react()],
})
