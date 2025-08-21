-- Create the database
CREATE DATABASE IF NOT EXISTS pahanaedu;
USE pahanaedu;

-- Users table for authentication
CREATE TABLE IF NOT EXISTS users (
                                     username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role ENUM('ADMIN', 'USER') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Customers table
CREATE TABLE IF NOT EXISTS customers (
                                         account_no VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(15),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Items table
CREATE TABLE IF NOT EXISTS items (
                                     item_code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    unit_price DECIMAL(10, 2) NOT NULL,
    qty_on_hand INT NOT NULL DEFAULT 0,
    reorder_level INT DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bills table
CREATE TABLE IF NOT EXISTS bills (
                                     bill_id INT AUTO_INCREMENT PRIMARY KEY,
                                     invoice_number VARCHAR(20) UNIQUE NOT NULL,
    customer_account_no VARCHAR(20),
    bill_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(10, 2) NOT NULL,
    tax_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(10, 2) NOT NULL,
    payment_method ENUM('CASH', 'CARD', 'BANK_TRANSFER') DEFAULT 'CASH',
    payment_status ENUM('PENDING', 'PAID', 'PARTIALLY_PAID') DEFAULT 'PENDING',
    status ENUM('DRAFT', 'COMPLETED', 'CANCELLED') DEFAULT 'DRAFT',
    notes TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_account_no) REFERENCES customers(account_no) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(username) ON DELETE SET NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bill items table
CREATE TABLE IF NOT EXISTS bill_items (
                                          bill_item_id INT AUTO_INCREMENT PRIMARY KEY,
                                          bill_id INT NOT NULL,
                                          item_code VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    discount_percentage DECIMAL(5, 2) DEFAULT 0,
    total_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (bill_id) REFERENCES bills(bill_id) ON DELETE CASCADE,
    FOREIGN KEY (item_code) REFERENCES items(item_code) ON DELETE RESTRICT
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
                                        payment_id INT AUTO_INCREMENT PRIMARY KEY,
                                        bill_id INT NOT NULL,
                                        amount DECIMAL(10, 2) NOT NULL,
    payment_method ENUM('CASH', 'CARD', 'BANK_TRANSFER') NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reference_number VARCHAR(50),
    notes TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (bill_id) REFERENCES bills(bill_id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(username) ON DELETE SET NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert default admin user (password: admin123)
INSERT INTO users (username, password, full_name, email, role)
VALUES ('admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'System Administrator', 'admin@pahanaedu.lk', 'ADMIN')
    ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- Create indexes for better performance
CREATE INDEX idx_bills_customer ON bills(customer_account_no);
CREATE INDEX idx_bills_invoice ON bills(invoice_number);
CREATE INDEX idx_bill_items_bill ON bill_items(bill_id);
CREATE INDEX idx_bill_items_item ON bill_items(item_code);
CREATE INDEX idx_payments_bill ON payments(bill_id);

-- Create a view for dashboard statistics
CREATE OR REPLACE VIEW dashboard_stats AS
SELECT
    (SELECT COUNT(*) FROM customers) AS total_customers,
    (SELECT COUNT(*) FROM bills WHERE status = 'COMPLETED') AS total_orders,
    (SELECT COALESCE(SUM(total_amount), 0) FROM bills WHERE status = 'COMPLETED') AS total_revenue,
    (SELECT COUNT(*) FROM items WHERE qty_on_hand <= reorder_level) AS low_stock_items;

-- Create a view for sales report
CREATE OR REPLACE VIEW sales_report AS
SELECT
    b.invoice_number,
    b.bill_date,
    c.name AS customer_name,
    c.phone AS customer_phone,
    b.subtotal,
    b.tax_amount,
    b.discount_amount,
    b.total_amount,
    b.payment_method,
    b.payment_status,
    u.full_name AS cashier
FROM
    bills b
        LEFT JOIN
    customers c ON b.customer_account_no = c.account_no
        LEFT JOIN
    users u ON b.created_by = u.username
WHERE
    b.status = 'COMPLETED'
ORDER BY
    b.bill_date DESC;