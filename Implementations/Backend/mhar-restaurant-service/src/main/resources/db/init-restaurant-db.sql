-- Restaurant Service Database Initialization Script
-- This script creates the tables for the restaurant service

-- Create the database schema if it doesn't exist
-- CREATE SCHEMA IF NOT EXISTS restaurant_service;

-- Use the schema
-- SET search_path TO restaurant_service;

-- Create sequence for restaurant IDs
CREATE SEQUENCE IF NOT EXISTS restaurant_id_seq START WITH 1 INCREMENT BY 1;

-- Create sequence for menu item IDs
CREATE SEQUENCE IF NOT EXISTS menu_item_id_seq START WITH 1 INCREMENT BY 1;

-- Create sequence for menu category IDs
CREATE SEQUENCE IF NOT EXISTS menu_category_id_seq START WITH 1 INCREMENT BY 1;

-- Create restaurants table
CREATE TABLE IF NOT EXISTS restaurants (
    id BIGINT PRIMARY KEY DEFAULT nextval('restaurant_id_seq'),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    cuisine_type VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(255) NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    delivery_radius DECIMAL(5, 2) DEFAULT 5.00,
    delivery_fee DECIMAL(10, 2) DEFAULT 0.00,
    minimum_order_amount DECIMAL(10, 2) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'SUSPENDED', 'REJECTED')),
    accepts_orders BOOLEAN DEFAULT true,
    average_rating DECIMAL(3, 2) DEFAULT 0.00,
    total_reviews INTEGER DEFAULT 0,
    image_url VARCHAR(500),
    banner_url VARCHAR(500),
    opening_hours JSON,
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create menu categories table
CREATE TABLE IF NOT EXISTS menu_categories (
    id BIGINT PRIMARY KEY DEFAULT nextval('menu_category_id_seq'),
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    display_order INTEGER DEFAULT 0,
    image_url VARCHAR(500),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create menu items table
CREATE TABLE IF NOT EXISTS menu_items (
    id BIGINT PRIMARY KEY DEFAULT nextval('menu_item_id_seq'),
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES menu_categories(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    original_price DECIMAL(10, 2),
    is_available BOOLEAN DEFAULT true,
    is_featured BOOLEAN DEFAULT false,
    image_url VARCHAR(500),
    preparation_time_minutes INTEGER,
    dietary_preferences TEXT[], -- Array of dietary preferences like 'VEGETARIAN', 'VEGAN', 'GLUTEN_FREE'
    spice_level VARCHAR(20) CHECK (spice_level IN ('MILD', 'MEDIUM', 'HOT', 'EXTRA_HOT')),
    calories INTEGER,
    ingredients TEXT,
    allergens TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_restaurants_status ON restaurants(status);
CREATE INDEX IF NOT EXISTS idx_restaurants_owner_id ON restaurants(owner_id);
CREATE INDEX IF NOT EXISTS idx_restaurants_cuisine_type ON restaurants(cuisine_type);
CREATE INDEX IF NOT EXISTS idx_restaurants_location ON restaurants(latitude, longitude);
CREATE INDEX IF NOT EXISTS idx_restaurants_rating ON restaurants(average_rating);
CREATE INDEX IF NOT EXISTS idx_restaurants_accepts_orders ON restaurants(accepts_orders);

CREATE INDEX IF NOT EXISTS idx_menu_categories_restaurant_id ON menu_categories(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_menu_categories_active ON menu_categories(is_active);
CREATE INDEX IF NOT EXISTS idx_menu_categories_display_order ON menu_categories(display_order);

CREATE INDEX IF NOT EXISTS idx_menu_items_restaurant_id ON menu_items(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_menu_items_category_id ON menu_items(category_id);
CREATE INDEX IF NOT EXISTS idx_menu_items_available ON menu_items(is_available);
CREATE INDEX IF NOT EXISTS idx_menu_items_featured ON menu_items(is_featured);
CREATE INDEX IF NOT EXISTS idx_menu_items_price ON menu_items(price);

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers to tables
CREATE TRIGGER update_restaurants_updated_at 
    BEFORE UPDATE ON restaurants 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_menu_categories_updated_at 
    BEFORE UPDATE ON menu_categories 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_menu_items_updated_at 
    BEFORE UPDATE ON menu_items 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data for testing (optional)
INSERT INTO restaurants (name, description, cuisine_type, phone, email, address, latitude, longitude, delivery_radius, delivery_fee, minimum_order_amount, status, owner_id) VALUES 
('Thai Delight', 'Authentic Thai cuisine with fresh ingredients', 'THAI', '+66-2-123-4567', 'info@thaidelight.com', '123 Sukhumvit Rd, Bangkok', 13.7563, 100.5018, 10.00, 25.00, 100.00, 'APPROVED', 1),
('Pizza Palace', 'Wood-fired pizzas and Italian specialties', 'ITALIAN', '+66-2-234-5678', 'orders@pizzapalace.com', '456 Silom Rd, Bangkok', 13.7307, 100.5418, 8.00, 30.00, 150.00, 'APPROVED', 2),
('Burger Junction', 'Gourmet burgers and American favorites', 'AMERICAN', '+66-2-345-6789', 'hello@burgerjunction.com', '789 Phloenchit Rd, Bangkok', 13.7440, 100.5500, 6.00, 20.00, 80.00, 'APPROVED', 3)
ON CONFLICT DO NOTHING;

-- Insert sample menu categories
INSERT INTO menu_categories (restaurant_id, name, description, display_order) VALUES 
(1, 'Appetizers', 'Start your meal with these delicious appetizers', 1),
(1, 'Main Courses', 'Traditional Thai main dishes', 2),
(1, 'Desserts', 'Sweet endings to your meal', 3),
(2, 'Pizzas', 'Wood-fired artisan pizzas', 1),
(2, 'Pastas', 'Fresh homemade pasta dishes', 2),
(2, 'Salads', 'Fresh and healthy salad options', 3),
(3, 'Burgers', 'Gourmet burger selection', 1),
(3, 'Sides', 'Perfect companions to your meal', 2),
(3, 'Beverages', 'Refreshing drinks', 3)
ON CONFLICT DO NOTHING;

-- Insert sample menu items
INSERT INTO menu_items (restaurant_id, category_id, name, description, price, is_available, is_featured, preparation_time_minutes, dietary_preferences, spice_level) VALUES 
(1, 1, 'Tom Yum Goong', 'Spicy and sour soup with prawns', 150.00, true, true, 15, ARRAY['GLUTEN_FREE'], 'HOT'),
(1, 1, 'Spring Rolls', 'Fresh vegetable spring rolls', 80.00, true, false, 10, ARRAY['VEGETARIAN'], 'MILD'),
(1, 2, 'Pad Thai', 'Classic Thai stir-fried noodles', 180.00, true, true, 20, NULL, 'MEDIUM'),
(1, 2, 'Green Curry', 'Aromatic green curry with choice of protein', 220.00, true, true, 25, ARRAY['GLUTEN_FREE'], 'HOT'),
(2, 4, 'Margherita Pizza', 'Classic pizza with tomato, mozzarella, and basil', 280.00, true, true, 18, ARRAY['VEGETARIAN'], NULL),
(2, 4, 'Pepperoni Pizza', 'Traditional pepperoni with mozzarella cheese', 320.00, true, false, 18, NULL, NULL),
(2, 5, 'Spaghetti Carbonara', 'Classic Italian pasta with eggs, cheese, and pancetta', 260.00, true, false, 15, NULL, NULL),
(3, 7, 'Classic Cheeseburger', 'Beef patty with cheese, lettuce, and tomato', 180.00, true, true, 12, NULL, NULL),
(3, 7, 'Veggie Burger', 'Plant-based patty with fresh vegetables', 160.00, true, false, 12, ARRAY['VEGETARIAN'], NULL),
(3, 8, 'French Fries', 'Crispy golden french fries', 60.00, true, false, 8, ARRAY['VEGETARIAN'], NULL)
ON CONFLICT DO NOTHING;
