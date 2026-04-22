-- ============================================
-- SEED DE USUARIOS DE DESARROLLO
-- ============================================
-- Crea un admin y un seller de prueba si no existen.
-- Idempotente: se puede correr múltiples veces sin duplicar.
--
-- Credenciales (ambos usuarios):
--   - admin@mail.com         / Test1234!  (rol: admin)
--   - seller_test@test.com   / Test1234!  (rol: seller)
--
-- ⚠️  Solo para desarrollo. NO usar estas credenciales en producción.
-- ============================================

USE [E-commerce];
GO

IF NOT EXISTS (SELECT 1 FROM USERS WHERE email = 'admin@mail.com')
BEGIN
    INSERT INTO USERS (username, email, password_hash, first_name, last_name, role, phone)
    VALUES ('admin_test', 'admin@mail.com',
            '$2a$10$kFEOgt8Y9MUNY1Kfnzup/ekGXh.8dALD2ymXPSMb2Jo4WGYAI42si',
            'Admin', 'Test', 'admin', NULL);
    PRINT 'Admin de desarrollo creado: admin@mail.com';
END
ELSE
    PRINT 'Admin de desarrollo ya existe, no se modifica.';
GO

IF NOT EXISTS (SELECT 1 FROM USERS WHERE email = 'seller_test@test.com')
BEGIN
    INSERT INTO USERS (username, email, password_hash, first_name, last_name, role, phone)
    VALUES ('seller_test', 'seller_test@test.com',
            '$2a$10$x8Tjy23gKQIHT.8WtSq3eOrv06s9H8zjneK3gah46jlWWy0gyOdJG',
            'Seller', 'Test', 'seller', NULL);
    PRINT 'Seller de desarrollo creado: seller_test@test.com';
END
ELSE
    PRINT 'Seller de desarrollo ya existe, no se modifica.';
GO
