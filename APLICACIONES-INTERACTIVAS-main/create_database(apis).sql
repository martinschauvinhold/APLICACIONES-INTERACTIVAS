-- ============================================
-- SCRIPT DE CREACIÓN DE BASE DE DATOS
-- E-Commerce Database
-- SQL Server
-- ============================================



USE [E-commerce];
GO

ALTER LOGIN sa ENABLE;
ALTER LOGIN sa WITH PASSWORD = 'Admin1234!';

-- ============================================
-- USUARIOS Y AUTENTICACIÓN
-- ============================================

CREATE TABLE USERS (
    user_id     INT PRIMARY KEY IDENTITY(1,1),
    username    VARCHAR(100) NOT NULL UNIQUE,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name  VARCHAR(100),
    last_name   VARCHAR(100),
    role        VARCHAR(20) NOT NULL CHECK (role IN ('buyer', 'seller', 'admin')),
    phone       VARCHAR(20),
    created_at  DATETIME DEFAULT GETDATE()
);

CREATE TABLE ADDRESSES (
    address_id      INT PRIMARY KEY IDENTITY(1,1),
    user_id         INT NOT NULL FOREIGN KEY REFERENCES USERS(user_id),
    street          VARCHAR(200),
    city            VARCHAR(100),
    state           VARCHAR(100),
    zip_code        VARCHAR(20),
    reference_note  VARCHAR(255)
);

CREATE TABLE SESSIONS (
    session_id  INT PRIMARY KEY IDENTITY(1,1),
    user_id     INT NOT NULL FOREIGN KEY REFERENCES USERS(user_id),
    created_at  DATETIME DEFAULT GETDATE(),
    expires_at  DATETIME,
    device_info VARCHAR(255),
    ip_address  VARCHAR(50)
);

CREATE TABLE SUPPORT_TICKETS (
    ticket_id   INT PRIMARY KEY IDENTITY(1,1),
    user_id     INT NOT NULL FOREIGN KEY REFERENCES USERS(user_id),
    subject     VARCHAR(255),
    status      VARCHAR(20) CHECK (status IN ('open', 'pending', 'closed')),
    created_at  DATETIME DEFAULT GETDATE()
);

CREATE TABLE MESSAGES (
    message_id  INT PRIMARY KEY IDENTITY(1,1),
    ticket_id   INT NOT NULL FOREIGN KEY REFERENCES SUPPORT_TICKETS(ticket_id),
    sender_id   INT NOT NULL FOREIGN KEY REFERENCES USERS(user_id),
    content     TEXT,
    sent_at     DATETIME DEFAULT GETDATE()
);

CREATE TABLE NOTIFICATIONS (
    notification_id INT PRIMARY KEY IDENTITY(1,1),
    user_id         INT NOT NULL FOREIGN KEY REFERENCES USERS(user_id),
    type            VARCHAR(100),
    message         VARCHAR(500),
    is_read         BIT DEFAULT 0,
    created_at      DATETIME DEFAULT GETDATE()
);

-- ============================================
-- CATÁLOGO
-- ============================================

CREATE TABLE CATEGORIES (
    category_id INT PRIMARY KEY IDENTITY(1,1),
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(100),
    is_active   BIT DEFAULT 1
);

CREATE TABLE PRODUCTS (
    product_id  INT PRIMARY KEY IDENTITY(1,1),
    category_id INT NOT NULL FOREIGN KEY REFERENCES CATEGORIES(category_id),
    seller_id   INT NOT NULL FOREIGN KEY REFERENCES USERS(user_id),
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    brand       VARCHAR(100),
    is_active   BIT DEFAULT 1,
    updated_at  DATETIME DEFAULT GETDATE()
);

CREATE TABLE PRODUCT_VARIANTS (
    variant_id  INT PRIMARY KEY IDENTITY(1,1),
    product_id  INT NOT NULL FOREIGN KEY REFERENCES PRODUCTS(product_id),
    sku         VARCHAR(100) NOT NULL UNIQUE,
    attributes  NVARCHAR(MAX), -- JSON
    base_price  DECIMAL(10,2) NOT NULL,
    updated_at  DATETIME DEFAULT GETDATE()
);

CREATE TABLE PRODUCT_IMAGES (
    image_id    INT PRIMARY KEY IDENTITY(1,1),
    product_id  INT NOT NULL FOREIGN KEY REFERENCES PRODUCTS(product_id),
    url         VARCHAR(500),
    is_primary  BIT DEFAULT 0,
    sort_order  INT DEFAULT 0
);

CREATE TABLE PRICE_TIERS (
    tier_id      INT PRIMARY KEY IDENTITY(1,1),
    variant_id   INT NOT NULL FOREIGN KEY REFERENCES PRODUCT_VARIANTS(variant_id),
    min_quantity INT NOT NULL,
    unit_price   DECIMAL(10,2) NOT NULL,
    currency     VARCHAR(10) DEFAULT 'ARS'
);

CREATE TABLE TAGS (
    tag_id      INT PRIMARY KEY IDENTITY(1,1),
    product_id  INT FOREIGN KEY REFERENCES PRODUCTS(product_id),
    category_id INT FOREIGN KEY REFERENCES CATEGORIES(category_id),
    name        VARCHAR(100) NOT NULL
);

CREATE TABLE REVIEWS (
    review_id   INT PRIMARY KEY IDENTITY(1,1),
    user_id     INT NOT NULL FOREIGN KEY REFERENCES USERS(user_id),
    product_id  INT NOT NULL FOREIGN KEY REFERENCES PRODUCTS(product_id),
    rating      INT CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    created_at  DATETIME DEFAULT GETDATE()
);

CREATE TABLE DISCOUNTS (
    discount_id     INT PRIMARY KEY IDENTITY(1,1),
    name            VARCHAR(200),
    discount_type   VARCHAR(50),
    value           DECIMAL(10,2),
    applies_to      VARCHAR(50),
    product_id      INT FOREIGN KEY REFERENCES PRODUCTS(product_id),
    category_id     INT FOREIGN KEY REFERENCES CATEGORIES(category_id),
    min_price       DECIMAL(10,2),
    starts_at       DATETIME,
    expires_at      DATETIME,
    is_active       BIT DEFAULT 1
);

CREATE TABLE COUPONS (
    coupon_id   INT PRIMARY KEY IDENTITY(1,1),
    discount_id INT NOT NULL FOREIGN KEY REFERENCES DISCOUNTS(discount_id),
    code        VARCHAR(100) NOT NULL UNIQUE,
    usage_limit INT,
    times_used  INT DEFAULT 0,
    expires_at  DATETIME,
    is_active   BIT DEFAULT 1
);

-- ============================================
-- LOGÍSTICA
-- ============================================

CREATE TABLE WAREHOUSES (
    warehouse_id    INT PRIMARY KEY IDENTITY(1,1),
    name            VARCHAR(100),
    location        VARCHAR(255),
    contact_phone   VARCHAR(20)
);

CREATE TABLE INVENTORY (
    inventory_id    INT PRIMARY KEY IDENTITY(1,1),
    variant_id      INT NOT NULL FOREIGN KEY REFERENCES PRODUCT_VARIANTS(variant_id),
    warehouse_id    INT FOREIGN KEY REFERENCES WAREHOUSES(warehouse_id),
    stock_quantity  INT DEFAULT 0,
    last_updated    DATETIME DEFAULT GETDATE()
);

-- ============================================
-- PEDIDOS Y PAGOS
-- ============================================

CREATE TABLE ORDERS (
    order_id            INT PRIMARY KEY IDENTITY(1,1),
    user_id             INT NOT NULL FOREIGN KEY REFERENCES USERS(user_id),
    shipping_address_id INT NOT NULL FOREIGN KEY REFERENCES ADDRESSES(address_id),
    status              VARCHAR(50),
    total_amount        DECIMAL(10,2),
    currency            VARCHAR(10) DEFAULT 'ARS',
    created_at          DATETIME DEFAULT GETDATE(),
    updated_at          DATETIME DEFAULT GETDATE()
);

CREATE TABLE ORDER_ITEMS (
    order_item_id       INT PRIMARY KEY IDENTITY(1,1),
    order_id            INT NOT NULL FOREIGN KEY REFERENCES ORDERS(order_id),
    variant_id          INT NOT NULL FOREIGN KEY REFERENCES PRODUCT_VARIANTS(variant_id),
    quantity            INT NOT NULL,
    unit_price_at_time  DECIMAL(10,2) NOT NULL,
    discount_applied    DECIMAL(10,2),
    subtotal            DECIMAL(10,2) NOT NULL
);

CREATE TABLE PAYMENTS (
    payment_id      INT PRIMARY KEY IDENTITY(1,1),
    order_id        INT NOT NULL FOREIGN KEY REFERENCES ORDERS(order_id),
    payment_method  VARCHAR(100),
    transaction_id  VARCHAR(200),
    payment_status  VARCHAR(50),
    paid_at         DATETIME
);

CREATE TABLE DELIVERIES (
    delivery_id     INT PRIMARY KEY IDENTITY(1,1),
    order_id        INT NOT NULL FOREIGN KEY REFERENCES ORDERS(order_id),
    shipping_method VARCHAR(100),
    tracking_number VARCHAR(200),
    delivery_status VARCHAR(50),
    dispatched_at   DATETIME
);

CREATE TABLE SHIPMENT_TRACKING (
    tracking_id INT PRIMARY KEY IDENTITY(1,1),
    delivery_id INT NOT NULL FOREIGN KEY REFERENCES DELIVERIES(delivery_id),
    [checkpoint]  VARCHAR(255),
    status      VARCHAR(50),
    recorded_at DATETIME DEFAULT GETDATE()
);

-- ============================================
-- POSTVENTA
-- ============================================

CREATE TABLE RETURNS (
    return_id       INT PRIMARY KEY IDENTITY(1,1),
    order_id        INT NOT NULL FOREIGN KEY REFERENCES ORDERS(order_id),
    reason          TEXT,
    status          VARCHAR(50),
    requested_at    DATETIME DEFAULT GETDATE()
);

CREATE TABLE REFUNDS (
    refund_id       INT PRIMARY KEY IDENTITY(1,1),
    return_id       INT NOT NULL FOREIGN KEY REFERENCES RETURNS(return_id),
    amount          DECIMAL(10,2),
    currency        VARCHAR(10) DEFAULT 'ARS',
    status          VARCHAR(50),
    processed_at    DATETIME
);

-- ============================================
-- USUARIOS DE PRUEBA (password: Test1234!)
-- Ejecutar una sola vez después de crear las tablas.
-- ============================================

INSERT INTO USERS (username, email, password_hash, first_name, last_name, role, phone)
VALUES ('seller_test', 'seller_test@test.com', '$2a$10$x8Tjy23gKQIHT.8WtSq3eOrv06s9H8zjneK3gah46jlWWy0gyOdJG', 'Seller', 'Test', 'seller', NULL);

INSERT INTO USERS (username, email, password_hash, first_name, last_name, role, phone)
VALUES ('admin_test', 'admin_test@test.com', '$2a$10$kFEOgt8Y9MUNY1Kfnzup/ekGXh.8dALD2ymXPSMb2Jo4WGYAI42si', 'Admin', 'Test', 'admin', NULL);
