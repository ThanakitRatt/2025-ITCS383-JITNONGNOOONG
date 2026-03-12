-- Reset Orders for Testing
DELETE FROM order_items;
DELETE FROM orders;

-- Order 1: Bangkok Street Food - PENDING
INSERT INTO orders (id, order_number, customer_id, restaurant_id, status, total_amount, delivery_fee, delivery_address, delivery_latitude, delivery_longitude, special_instructions, estimated_delivery_time, created_at, updated_at)
VALUES (1, 'ORD-2025-0001', 1, 1, 'PENDING', 330.00, 30.00, '88 Sukhumvit Soi 21, Watthana, Bangkok 10110', 13.7392, 100.5641, 'Please leave at the front desk.', NOW() + INTERVAL '30 minutes', NOW(), NOW());

INSERT INTO order_items (order_id, menu_item_id, menu_item_name, quantity, unit_price, total_price) VALUES
(1, 1, 'Pad Thai', 1, 120.00, 120.00),
(1, 5, 'Tom Yum Goong', 1, 150.00, 150.00),
(1, 13, 'Thai Milk Tea', 1, 60.00, 60.00);

-- Order 2: Sushi Master - PENDING
INSERT INTO orders (id, order_number, customer_id, restaurant_id, status, total_amount, delivery_fee, delivery_address, delivery_latitude, delivery_longitude, special_instructions, estimated_delivery_time, created_at, updated_at)
VALUES (2, 'ORD-2025-0002', 5, 2, 'PENDING', 720.00, 40.00, '12/3 Silom Road, Silom, Bang Rak, Bangkok 10500', 13.7256, 100.5210, 'No wasabi please.', NOW() + INTERVAL '30 minutes', NOW(), NOW());

INSERT INTO order_items (order_id, menu_item_id, menu_item_name, quantity, unit_price, total_price) VALUES
(2, 15, 'Salmon Avocado Roll', 2, 220.00, 440.00),
(2, 19, 'Salmon Sashimi (5 pcs)', 1, 250.00, 250.00),
(2, 25, 'Miso Soup', 1, 70.00, 70.00);

-- Order 3: Big Burger House - CONFIRMED
INSERT INTO orders (id, order_number, customer_id, restaurant_id, status, total_amount, delivery_fee, delivery_address, delivery_latitude, delivery_longitude, special_instructions, estimated_delivery_time, created_at, updated_at)
VALUES (3, 'ORD-2025-0003', 1, 4, 'CONFIRMED', 320.00, 30.00, '88 Sukhumvit Soi 21, Watthana, Bangkok 10110', 13.7392, 100.5641, 'Extra napkins please.', NOW() + INTERVAL '30 minutes', NOW(), NOW());

INSERT INTO order_items (order_id, menu_item_id, menu_item_name, quantity, unit_price, total_price) VALUES
(3, 43, 'Classic Wagyu Cheeseburger', 1, 190.00, 190.00),
(3, 48, 'Crispy Shoestring Fries', 1, 80.00, 80.00),
(3, 53, 'Strawberry Milkshake', 1, 130.00, 130.00);
