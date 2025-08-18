-- Create database
CREATE DATABASE IF NOT EXISTS pahanaedu;
USE pahanaedu;

-- Create customers table
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    account_no VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert sample data
INSERT INTO customers (account_no, name, address, phone) VALUES
('ACC001', 'John Doe', '123 Main St, Colombo', '+94771234567'),
('ACC002', 'Jane Smith', '456 Galle Road, Kandy', '+94772345678'),
('ACC003', 'Mike Johnson', '789 Temple Road, Gampaha', '+94773456789'),
('ACC004', 'Sarah Wilson', '321 Lake Road, Negombo', '+94774567890'),
('ACC005', 'David Brown', '654 Hill Street, Matara', '+94775678901');

-- Verify data
SELECT * FROM customers;
