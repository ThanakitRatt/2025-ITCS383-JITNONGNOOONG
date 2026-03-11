-- =====================================================================
-- SEED DATA: MharRuengSang Food Delivery Platform
-- =====================================================================
--
-- USAGE (run in this order every time on a fresh database):
--
--   1. Start all backend services (let Hibernate create tables via ddl-auto=update)
--   2. Run demo-users.sql first (creates auth users with IDs 1-6):
--        docker exec -i <postgres_container> psql -U mhar_user -d mhar_auth < docker/demo-users.sql
--   3. Run THIS file:
--        docker exec -i <postgres_container> psql -U mhar_user < docker/seed-data.sql
--
-- WARNING: Run this only ONCE on a fresh/empty database.
--          Running twice will cause duplicate key violations.
--
-- AUTH USERS REFERENCE (created by demo-users.sql, auto-increment IDs):
--   ID 1 = John Doe          (CUSTOMER)    customer@foodexpress.com  / Customer123!
--   ID 2 = Bangkok Street Food (RESTAURANT) restaurant@foodexpress.com / Restaurant123!
--   ID 3 = Mike Chen         (RIDER)       rider@foodexpress.com     / Rider123!
--   ID 4 = Admin User        (ADMIN)       admin@foodexpress.com     / Admin123!
--   ID 5 = Sarah Wilson      (CUSTOMER)    sarah@foodexpress.com     / Sarah123!
--   ID 6 = Sushi Master      (RESTAURANT)  sushi@foodexpress.com     / Sushi123!
--
-- This file adds:
--   ID 7 = Chef Marco Rossi  (RESTAURANT)  italiano@foodexpress.com  / Customer123!
--   ID 8 = American Grill Co (RESTAURANT)  burger@foodexpress.com    / Customer123!
--   ID 9 = Ramen House Owner (RESTAURANT)  ramen@foodexpress.com     / Customer123!
-- =====================================================================

-- =====================================================================
-- SECTION 1: mhar_auth — Additional restaurant owner accounts
-- =====================================================================
\c mhar_auth

INSERT INTO users (name, email, password, phone_number, role, enabled, created_at, updated_at) VALUES
  (
    'Chef Marco Rossi',
    'italiano@foodexpress.com',
    '$2a$10$rVQzVL9nB.KjZ7XWqXzOH.3qUv5Cq5JF7kH8nW9oD6xE8yT9aB1cW',
    '+66878901234',
    'RESTAURANT', true, NOW(), NOW()
  ),
  (
    'American Grill Co',
    'burger@foodexpress.com',
    '$2a$10$rVQzVL9nB.KjZ7XWqXzOH.3qUv5Cq5JF7kH8nW9oD6xE8yT9aB1cW',
    '+66889012345',
    'RESTAURANT', true, NOW(), NOW()
  ),
  (
    'Ramen House Owner',
    'ramen@foodexpress.com',
    '$2a$10$rVQzVL9nB.KjZ7XWqXzOH.3qUv5Cq5JF7kH8nW9oD6xE8yT9aB1cW',
    '+66890123456',
    'RESTAURANT', true, NOW(), NOW()
  );
-- Note: All three reuse the Customer123! BCrypt hash for dev convenience.

-- =====================================================================
-- SECTION 2: mhar_restaurants — Restaurants, Categories, Menu Items
-- =====================================================================
\c mhar_restaurants

-- -------------------------------------------------------------------
-- RESTAURANTS
-- status must match RestaurantStatus enum: PENDING | APPROVED | SUSPENDED | REJECTED
-- owner_id references mhar_auth.users (cross-DB, no FK enforced at DB level)
-- -------------------------------------------------------------------
INSERT INTO restaurants (
  name, description, cuisine_type, address,
  latitude, longitude, phone_number, email,
  opening_time, closing_time, owner_id,
  status, is_active, accepts_orders,
  average_rating, total_reviews,
  minimum_order_amount, delivery_fee, estimated_delivery_time,
  logo_url, cover_image_url,
  created_at, updated_at
) VALUES
(
  'Bangkok Street Food',
  'Authentic Thai street food crafted with fresh herbs and traditional recipes passed down through generations. Experience the real flavors of Bangkok!',
  'THAI',
  '123 Sukhumvit Soi 11, Khlong Toei Nuea, Bangkok 10110',
  13.7390, 100.5600,
  '+66812345678', 'restaurant@foodexpress.com',
  '10:00:00', '22:00:00', 2,
  'APPROVED', true, true,
  4.7, 238,
  80.00, 30.00, 25,
  'https://https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=400&h=400&fit=crop&auto=format',
  'https://https://images.unsplash.com/photo-1562802378-063ec186a863?w=1200&h=500&fit=crop&auto=format',
  NOW(), NOW()
),
(
  'Sushi Master',
  'Premium Japanese cuisine featuring fresh daily-imported fish. Our master sushi chefs bring authentic Edo-style sushi to Bangkok with 20+ years of experience.',
  'JAPANESE',
  '45 Silom Road, Bang Rak, Bangkok 10500',
  13.7248, 100.5183,
  '+66867890123', 'sushi@foodexpress.com',
  '11:00:00', '21:30:00', 6,
  'APPROVED', true, true,
  4.9, 412,
  150.00, 40.00, 30,
  'https://https://images.unsplash.com/photo-1553621042-f6e147245754?w=400&h=400&fit=crop&auto=format',
  'https://https://images.unsplash.com/photo-1617196035154-1e7e6e28b0db?w=1200&h=500&fit=crop&auto=format',
  NOW(), NOW()
),
(
  'Italiano Pizzeria',
  'Neapolitan-style wood-fired pizzas and fresh handmade pasta. Using imported Italian ingredients and traditional family recipes from Naples.',
  'ITALIAN',
  '78 Thong Lo Soi 5, Watthana, Bangkok 10110',
  13.7284, 100.5818,
  '+66878901234', 'italiano@foodexpress.com',
  '11:30:00', '22:30:00', 7,
  'APPROVED', true, true,
  4.6, 187,
  120.00, 35.00, 35,
  'https://https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&h=400&fit=crop&auto=format',
  'https://https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=1200&h=500&fit=crop&auto=format',
  NOW(), NOW()
),
(
  'Big Burger House',
  'Smash burgers made with 100% Wagyu beef patties, fresh brioche buns baked daily. The best American-style burgers in Bangkok!',
  'AMERICAN',
  '22 Ekkamai Soi 4, Watthana, Bangkok 10110',
  13.7181, 100.5857,
  '+66889012345', 'burger@foodexpress.com',
  '10:00:00', '22:00:00', 8,
  'APPROVED', true, true,
  4.5, 321,
  100.00, 30.00, 20,
  'https://https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&h=400&fit=crop&auto=format',
  'https://https://images.unsplash.com/photo-1550547660-d9450f859349?w=1200&h=500&fit=crop&auto=format',
  NOW(), NOW()
),
(
  'Ramen Noodle Bar',
  'Slow-cooked tonkotsu broths simmered for 18 hours. Handmade noodles, premium chashu pork, and seasonal toppings for the ultimate ramen experience.',
  'JAPANESE',
  '99 Ari Soi 4, Phaya Thai, Bangkok 10400',
  13.7804, 100.5453,
  '+66890123456', 'ramen@foodexpress.com',
  '11:00:00', '21:00:00', 9,
  'APPROVED', true, true,
  4.8, 295,
  120.00, 35.00, 30,
  'https://https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=400&h=400&fit=crop&auto=format',
  'https://https://images.unsplash.com/photo-1582878826629-33b175fbe285?w=1200&h=500&fit=crop&auto=format',
  NOW(), NOW()
);

-- -------------------------------------------------------------------
-- MENU CATEGORIES
-- Insertion order determines auto-increment IDs:
--   Bangkok Street Food (restaurant_id=1): IDs 1, 2, 3, 4
--   Sushi Master        (restaurant_id=2): IDs 5, 6, 7, 8
--   Italiano Pizzeria   (restaurant_id=3): IDs 9, 10, 11, 12
--   Big Burger House    (restaurant_id=4): IDs 13, 14, 15
--   Ramen Noodle Bar    (restaurant_id=5): IDs 16, 17, 18
-- -------------------------------------------------------------------

-- Bangkok Street Food
INSERT INTO menu_categories (name, description, is_active, display_order, image_url, restaurant_id, created_at, updated_at) VALUES
  ('Noodle Dishes',     'Stir-fried and soup noodles',            true, 1, 'https://https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=400&h=300&fit=crop&auto=format',  1, NOW(), NOW()),
  ('Soups & Curries',   'Aromatic Thai soups and curries',        true, 2, 'https://https://images.unsplash.com/photo-1455619452474-d2be8b1e70cd?w=400&h=300&fit=crop&auto=format',    1, NOW(), NOW()),
  ('Rice Dishes',       'Fried rice and rice plates',             true, 3, 'https://https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=400&h=300&fit=crop&auto=format',     1, NOW(), NOW()),
  ('Desserts & Drinks', 'Traditional Thai sweets and drinks',     true, 4, 'https://https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&h=300&fit=crop&auto=format', 1, NOW(), NOW());

-- Sushi Master
INSERT INTO menu_categories (name, description, is_active, display_order, image_url, restaurant_id, created_at, updated_at) VALUES
  ('Sushi & Maki Rolls', 'Nigiri sushi and creative maki rolls',    true, 1, 'https://https://images.unsplash.com/photo-1553621042-f6e147245754?w=400&h=300&fit=crop&auto=format',   2, NOW(), NOW()),
  ('Sashimi',            'Freshest cuts of raw fish',               true, 2, 'https://https://images.unsplash.com/photo-1617196033183-421062a1103b?w=400&h=300&fit=crop&auto=format', 2, NOW(), NOW()),
  ('Hot Dishes',         'Cooked Japanese appetizers and mains',    true, 3, 'https://https://images.unsplash.com/photo-1563245372-f21724e3856d?w=400&h=300&fit=crop&auto=format',   2, NOW(), NOW()),
  ('Drinks',             'Japanese beverages',                      true, 4, 'https://https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400&h=300&fit=crop&auto=format',2, NOW(), NOW());

-- Italiano Pizzeria
INSERT INTO menu_categories (name, description, is_active, display_order, image_url, restaurant_id, created_at, updated_at) VALUES
  ('Pizzas',            'Wood-fired Neapolitan pizzas',             true, 1, 'https://https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&h=300&fit=crop&auto=format',   3, NOW(), NOW()),
  ('Pasta',             'Fresh handmade pasta dishes',              true, 2, 'https://https://images.unsplash.com/photo-1551892374-ecf8754cf744?w=400&h=300&fit=crop&auto=format',   3, NOW(), NOW()),
  ('Salads & Starters', 'Fresh salads and appetizers',              true, 3, 'https://https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400&h=300&fit=crop&auto=format',   3, NOW(), NOW()),
  ('Drinks',            'Italian beverages',                        true, 4, 'https://https://images.unsplash.com/photo-1497534446932-c925b458314e?w=400&h=300&fit=crop&auto=format', 3, NOW(), NOW());

-- Big Burger House
INSERT INTO menu_categories (name, description, is_active, display_order, image_url, restaurant_id, created_at, updated_at) VALUES
  ('Burgers',         'Wagyu smash burgers',               true, 1, 'https://https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&h=300&fit=crop&auto=format',  4, NOW(), NOW()),
  ('Sides & Fries',   'Crispy sides and loaded fries',     true, 2, 'https://https://images.unsplash.com/photo-1573080496219-bb964701c2a8?w=400&h=300&fit=crop&auto=format',    4, NOW(), NOW()),
  ('Drinks & Shakes', 'Ice-cold drinks and thick shakes',  true, 3, 'https://https://images.unsplash.com/photo-1572490122747-3e9f7f1614af?w=400&h=300&fit=crop&auto=format',   4, NOW(), NOW());

-- Ramen Noodle Bar
INSERT INTO menu_categories (name, description, is_active, display_order, image_url, restaurant_id, created_at, updated_at) VALUES
  ('Ramen',  'Signature ramen bowls',             true, 1, 'https://https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=400&h=300&fit=crop&auto=format',      5, NOW(), NOW()),
  ('Sides',  'Perfect ramen accompaniments',      true, 2, 'https://https://images.unsplash.com/photo-1563245372-f21724e3856d?w=400&h=300&fit=crop&auto=format', 5, NOW(), NOW()),
  ('Drinks', 'Japanese beverages',                true, 3, 'https://https://images.unsplash.com/photo-1547592166-23ac45744acd?w=400&h=300&fit=crop&auto=format',5, NOW(), NOW());

-- -------------------------------------------------------------------
-- MENU ITEMS  (single INSERT for consistent auto-increment IDs)
-- ID assignment order: IDs 1-14 = Bangkok Street Food
--                      IDs 15-28 = Sushi Master
--                      IDs 29-42 = Italiano Pizzeria
--                      IDs 43-55 = Big Burger House
--                      IDs 56-66 = Ramen Noodle Bar
-- -------------------------------------------------------------------
INSERT INTO menu_items (
  name, description, price, image_url,
  is_available, is_featured, is_vegetarian, is_vegan, is_gluten_free, is_spicy,
  preparation_time, calories, ingredients, allergens,
  display_order, restaurant_id, category_id, created_at, updated_at
) VALUES

-- ===== Bangkok Street Food: Noodle Dishes (category_id=1, restaurant_id=1) =====
(
  'Pad Thai',
  'Thailand''s signature stir-fried rice noodles with shrimp or tofu, bean sprouts, eggs, ground peanuts and fresh lime.',
  120.00, 'https://https://images.unsplash.com/photo-1559314809-0d155014e29e?w=400&h=300&fit=crop&auto=format',
  true, true, false, false, false, false,
  12, 520, 'Rice noodles, shrimp, tofu, eggs, bean sprouts, green onions, peanuts, lime, tamarind sauce', 'Peanuts, Shellfish, Eggs',
  1, 1, 1, NOW(), NOW()
),
(
  'Pad See Ew',
  'Wide rice noodles stir-fried in sweet soy sauce with Chinese broccoli and your choice of protein.',
  110.00, 'https://https://images.unsplash.com/photo-1527515637462-cff94ebb84ec?w=400&h=300&fit=crop&auto=format',
  true, false, false, false, false, false,
  12, 480, 'Wide rice noodles, soy sauce, Chinese broccoli, eggs, chicken', 'Eggs, Soy',
  2, 1, 1, NOW(), NOW()
),
(
  'Pad Kee Mao (Drunken Noodles)',
  'Wide rice noodles tossed in bold chili-basil sauce with vegetables and your choice of protein. A fiery Thai classic.',
  130.00, 'https://https://images.unsplash.com/photo-1533622597524-a1215e26c0a2?w=400&h=300&fit=crop&auto=format',
  true, false, false, false, false, true,
  13, 510, 'Wide rice noodles, Thai basil, fresh chili, garlic, fish sauce, mixed vegetables', 'Soy, Fish',
  3, 1, 1, NOW(), NOW()
),
(
  'Boat Noodles',
  'Traditional Thai boat noodles with rich dark pork broth, tender pork slices, crispy pork rinds and fresh herbs.',
  90.00, 'https://https://images.unsplash.com/photo-1547592166-23ac45744acd?w=400&h=300&fit=crop&auto=format',
  true, false, false, false, false, false,
  10, 340, 'Rice noodles, pork, dark soy broth, crispy pork rinds, bean sprouts, Thai basil', 'Soy, Pork',
  4, 1, 1, NOW(), NOW()
),

-- ===== Bangkok Street Food: Soups & Curries (category_id=2) =====
(
  'Tom Yum Goong',
  'Classic Thai hot-and-sour soup with whole shrimp, mushrooms, lemongrass, kaffir lime leaves and galangal.',
  150.00, 'https://https://images.unsplash.com/photo-1455619452474-d2be8b1e70cd?w=400&h=300&fit=crop&auto=format',
  true, true, false, false, true, true,
  15, 220, 'Shrimp, mushrooms, lemongrass, kaffir lime leaves, galangal, red chili, lime juice, fish sauce', 'Shellfish',
  1, 1, 2, NOW(), NOW()
),
(
  'Tom Kha Gai',
  'Creamy coconut milk soup with chicken, galangal, lemongrass and mushrooms. A gentle, fragrant Thai favorite.',
  140.00, 'https://https://images.unsplash.com/photo-1476718406336-bb5a9690ee2a?w=400&h=300&fit=crop&auto=format',
  true, false, false, false, true, false,
  15, 280, 'Chicken, coconut milk, galangal, lemongrass, mushrooms, fish sauce, lime juice', 'Coconut',
  2, 1, 2, NOW(), NOW()
),
(
  'Green Curry (Kaeng Khiao Wan)',
  'Aromatic green curry with chicken in creamy coconut milk, Thai eggplant, bamboo shoots and fresh basil.',
  160.00, 'https://https://images.unsplash.com/photo-1596797882599-15b1c6ce4ddf?w=400&h=300&fit=crop&auto=format',
  true, true, false, false, true, true,
  18, 380, 'Green curry paste, coconut milk, chicken, Thai eggplant, bamboo shoots, kaffir lime leaves, basil', 'Coconut',
  3, 1, 2, NOW(), NOW()
),
(
  'Massaman Curry',
  'Rich, mildly spiced curry with tender beef, potatoes, peanuts and coconut milk. Influenced by Persian spices.',
  170.00, 'https://https://images.unsplash.com/photo-1455619452474-d2be8b1e70cd?w=400&h=300&fit=crop&auto=format',
  true, false, false, false, true, false,
  20, 450, 'Beef, coconut milk, massaman curry paste, potatoes, peanuts, cardamom, cinnamon', 'Peanuts, Coconut',
  4, 1, 2, NOW(), NOW()
),

-- ===== Bangkok Street Food: Rice Dishes (category_id=3) =====
(
  'Khao Pad (Thai Fried Rice)',
  'Wok-tossed jasmine rice with egg, vegetables, oyster sauce and your choice of chicken, pork or shrimp.',
  100.00, 'https://https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=400&h=300&fit=crop&auto=format',
  true, false, false, false, false, false,
  10, 410, 'Jasmine rice, eggs, mixed vegetables, soy sauce, oyster sauce, garlic', 'Eggs, Soy',
  1, 1, 3, NOW(), NOW()
),
(
  'Khao Man Gai',
  'Poached chicken served over fragrant rice cooked in chicken broth, with ginger dipping sauce and clear soup.',
  120.00, 'https://https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&h=300&fit=crop&auto=format',
  true, true, false, false, true, false,
  15, 480, 'Chicken breast, jasmine rice, ginger, garlic, soy sauce, chicken broth', 'Soy',
  2, 1, 3, NOW(), NOW()
),
(
  'Basil Chicken Rice (Khao Kra Pao)',
  'Ground chicken stir-fried with holy basil, chili and oyster sauce, served over steamed rice with a fried egg.',
  110.00, 'https://https://images.unsplash.com/photo-1562802378-063ec186a863?w=400&h=300&fit=crop&auto=format',
  true, true, false, false, false, true,
  12, 460, 'Ground chicken, holy basil, bird''s eye chili, garlic, oyster sauce, fish sauce, fried egg, steamed rice', 'Eggs, Soy, Fish',
  3, 1, 3, NOW(), NOW()
),

-- ===== Bangkok Street Food: Desserts & Drinks (category_id=4) =====
(
  'Mango Sticky Rice',
  'Sweet glutinous rice topped with fresh ripe mango slices and drizzled with rich coconut cream. Seasonal.',
  80.00, 'https://https://images.unsplash.com/photo-1568718119408-99a3d08e2e0b?w=400&h=300&fit=crop&auto=format',
  true, true, true, true, true, false,
  5, 320, 'Glutinous rice, fresh mango, coconut milk, sugar, salt, sesame seeds', 'Coconut, Sesame',
  1, 1, 4, NOW(), NOW()
),
(
  'Thai Milk Tea (Cha Yen)',
  'Iconic Thai orange tea brewed strong and sweet, served iced with condensed and evaporated milk.',
  60.00, 'https://https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&h=300&fit=crop&auto=format',
  true, true, true, false, true, false,
  3, 180, 'Thai tea powder, condensed milk, evaporated milk, sugar, ice', 'Dairy',
  2, 1, 4, NOW(), NOW()
),
(
  'Pandan Coconut Jelly',
  'Refreshing layered jelly with fragrant pandan and creamy coconut. A classic Thai street food treat.',
  70.00, 'https://https://images.unsplash.com/photo-1488477181771-4695d2499bef?w=400&h=300&fit=crop&auto=format',
  true, false, true, true, true, false,
  3, 140, 'Pandan extract, coconut milk, agar, sugar', 'Coconut',
  3, 1, 4, NOW(), NOW()
),

-- ===== Sushi Master: Sushi & Maki Rolls (category_id=5, restaurant_id=2) =====
(
  'Salmon Avocado Roll',
  'Fresh Atlantic salmon with creamy avocado wrapped in seasoned sushi rice and nori. 8 pcs.',
  220.00, 'https://https://images.unsplash.com/photo-1617196035154-1e7e6e28b0db?w=400&h=300&fit=crop&auto=format',
  true, true, false, false, true, false,
  10, 310, 'Salmon, avocado, sushi rice, nori, sesame seeds', 'Fish, Sesame',
  1, 2, 5, NOW(), NOW()
),
(
  'California Roll',
  'Crab meat, avocado and cucumber wrapped inside-out with tobiko flying fish roe. 8 pcs.',
  180.00, 'https://https://images.unsplash.com/photo-1553621042-f6e147245754?w=400&h=300&fit=crop&auto=format',
  true, false, false, false, true, false,
  10, 280, 'Crab meat, avocado, cucumber, sushi rice, nori, tobiko', 'Shellfish, Fish',
  2, 2, 5, NOW(), NOW()
),
(
  'Spicy Tuna Roll',
  'Minced tuna mixed with spicy mayo and cucumber, rolled with sushi rice and nori. 8 pcs.',
  200.00, 'https://https://images.unsplash.com/photo-1617196033322-c0c22c52caaf?w=400&h=300&fit=crop&auto=format',
  true, false, false, false, true, true,
  10, 295, 'Tuna, spicy mayo, cucumber, sushi rice, nori', 'Fish, Eggs',
  3, 2, 5, NOW(), NOW()
),
(
  'Dragon Roll',
  'Shrimp tempura inside, topped with thin avocado slices and unagi sauce — our signature roll. 8 pcs.',
  280.00, 'https://https://images.unsplash.com/photo-1617196033423-3ac59e3aedf4?w=400&h=300&fit=crop&auto=format',
  true, true, false, false, false, false,
  14, 420, 'Shrimp tempura, cucumber, avocado, sushi rice, nori, unagi sauce, tobiko', 'Shellfish, Fish, Gluten',
  4, 2, 5, NOW(), NOW()
),

-- ===== Sushi Master: Sashimi (category_id=6) =====
(
  'Salmon Sashimi (5 pcs)',
  'Five thick slices of premium, daily-imported Norwegian salmon. Served with wasabi and pickled ginger.',
  250.00, 'https://https://images.unsplash.com/photo-1617196033183-421062a1103b?w=400&h=300&fit=crop&auto=format',
  true, true, false, false, true, false,
  5, 175, 'Fresh salmon, wasabi, pickled ginger, soy sauce for dipping', 'Fish',
  1, 2, 6, NOW(), NOW()
),
(
  'Tuna Sashimi (5 pcs)',
  'Five generous cuts of deep-red bluefin tuna. Clean, rich flavor — a purist''s choice.',
  300.00, 'https://https://images.unsplash.com/photo-1617196035303-f6ad4f09e4e2?w=400&h=300&fit=crop&auto=format',
  true, false, false, false, true, false,
  5, 155, 'Fresh bluefin tuna, wasabi, pickled ginger, soy sauce for dipping', 'Fish',
  2, 2, 6, NOW(), NOW()
),
(
  'Mixed Sashimi (10 pcs)',
  'Chef''s selection of 10 pieces: salmon, tuna, yellowtail, sea bream, and octopus. The ultimate sashimi platter.',
  480.00, 'https://https://images.unsplash.com/photo-1580822184713-fc5ded9f8d0f?w=400&h=300&fit=crop&auto=format',
  true, true, false, false, true, false,
  8, 310, 'Assorted fresh fish, wasabi, pickled ginger, soy sauce for dipping', 'Fish, Shellfish',
  3, 2, 6, NOW(), NOW()
),

-- ===== Sushi Master: Hot Dishes (category_id=7) =====
(
  'Gyoza (6 pcs)',
  'Pan-fried pork and vegetable dumplings, crispy on the bottom, juicy inside. Served with ponzu dipping sauce.',
  120.00, 'https://https://images.unsplash.com/photo-1563245372-f21724e3856d?w=400&h=300&fit=crop&auto=format',
  true, false, false, false, false, false,
  8, 210, 'Pork mince, cabbage, garlic, ginger, dumpling wrappers, ponzu sauce', 'Gluten, Pork',
  1, 2, 7, NOW(), NOW()
),
(
  'Edamame',
  'Lightly salted steamed young soybeans. A simple, healthy Japanese snack.',
  80.00, 'https://https://images.unsplash.com/photo-1543362906-acfc16c67564?w=400&h=300&fit=crop&auto=format',
  true, false, true, true, true, false,
  5, 120, 'Young soybeans, sea salt', 'Soy',
  2, 2, 7, NOW(), NOW()
),
(
  'Tempura Shrimp (4 pcs)',
  'Four jumbo shrimp in a light, delicate tempura batter with tentsuyu dipping sauce and grated daikon.',
  190.00, 'https://https://images.unsplash.com/photo-1632206000219-0d15f20bf35d?w=400&h=300&fit=crop&auto=format',
  true, false, false, false, false, false,
  10, 280, 'Jumbo shrimp, tempura batter, tentsuyu sauce, daikon', 'Shellfish, Gluten, Eggs',
  3, 2, 7, NOW(), NOW()
),
(
  'Miso Soup',
  'Traditional Japanese dashi broth with white miso paste, silken tofu cubes, wakame seaweed and spring onion.',
  70.00, 'https://https://images.unsplash.com/photo-1547592166-23ac45744acd?w=400&h=300&fit=crop&auto=format',
  true, false, true, false, true, false,
  5, 65, 'White miso paste, dashi, silken tofu, wakame, spring onion', 'Soy',
  4, 2, 7, NOW(), NOW()
),

-- ===== Sushi Master: Drinks (category_id=8) =====
(
  'Sake (50 ml)',
  'Chilled premium junmai sake. Clean, slightly dry with a delicate rice aroma.',
  95.00, 'https://https://images.unsplash.com/photo-1608270586263-f0c14b71d534?w=400&h=300&fit=crop&auto=format',
  true, false, true, true, true, false,
  1, 75, 'Premium rice sake', NULL,
  1, 2, 8, NOW(), NOW()
),
(
  'Japanese Green Tea (Ocha)',
  'Brewed sencha green tea served hot or iced. Refreshing and rich in antioxidants.',
  60.00, 'https://https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400&h=300&fit=crop&auto=format',
  true, false, true, true, true, false,
  2, 5, 'Sencha green tea leaves, filtered water', NULL,
  2, 2, 8, NOW(), NOW()
),
(
  'Ramune Soda',
  'Japan''s iconic marble-sealed soda. Available in original, strawberry, and melon flavors.',
  85.00, 'https://https://images.unsplash.com/photo-1543363006-09a7ac5de37a?w=400&h=300&fit=crop&auto=format',
  true, false, true, true, true, false,
  1, 90, 'Carbonated water, sugar, citric acid, natural flavors', NULL,
  3, 2, 8, NOW(), NOW()
),

-- ===== Italiano Pizzeria: Pizzas (category_id=9, restaurant_id=3) =====
(
  'Margherita Pizza',
  'The classic Neapolitan pizza: San Marzano tomato sauce, fresh buffalo mozzarella, basil, extra virgin olive oil.',
  280.00, 'https://https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&h=300&fit=crop&auto=format',
  true, true, true, false, false, false,
  18, 720, 'Pizza dough, San Marzano tomatoes, buffalo mozzarella, fresh basil, olive oil', 'Gluten, Dairy',
  1, 3, 9, NOW(), NOW()
),
(
  'Pepperoni Pizza',
  'Generous amount of imported Italian pepperoni with tomato sauce and stretchy fior di latte mozzarella.',
  320.00, 'https://https://images.unsplash.com/photo-1628840042765-356cda07504e?w=400&h=300&fit=crop&auto=format',
  true, true, false, false, false, true,
  18, 860, 'Pizza dough, tomato sauce, fior di latte mozzarella, Italian pepperoni', 'Gluten, Dairy, Pork',
  2, 3, 9, NOW(), NOW()
),
(
  'Quattro Formaggi',
  'Four cheese pizza with mozzarella, gorgonzola, fontina and parmigiano reggiano on a white pizza base.',
  380.00, 'https://https://images.unsplash.com/photo-1571997473849-4fa1c2234c7a?w=400&h=300&fit=crop&auto=format',
  true, true, true, false, false, false,
  18, 920, 'Pizza dough, mozzarella, gorgonzola, fontina, parmigiano reggiano, olive oil', 'Gluten, Dairy',
  3, 3, 9, NOW(), NOW()
),
(
  'BBQ Chicken Pizza',
  'Smoky BBQ sauce base with grilled chicken, red onion, corn, and smoked mozzarella.',
  350.00, 'https://https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=400&h=300&fit=crop&auto=format',
  true, false, false, false, false, false,
  20, 810, 'Pizza dough, BBQ sauce, grilled chicken, red onion, corn, smoked mozzarella', 'Gluten, Dairy',
  4, 3, 9, NOW(), NOW()
),

-- ===== Italiano Pizzeria: Pasta (category_id=10) =====
(
  'Spaghetti Carbonara',
  'Roman-style carbonara with guanciale, Pecorino Romano, Parmigiano Reggiano and fresh egg yolks. No cream!',
  240.00, 'https://https://images.unsplash.com/photo-1551892374-ecf8754cf744?w=400&h=300&fit=crop&auto=format',
  true, true, false, false, false, false,
  15, 680, 'Spaghetti, guanciale, egg yolks, Pecorino Romano, Parmigiano Reggiano, black pepper', 'Gluten, Eggs, Dairy, Pork',
  1, 3, 10, NOW(), NOW()
),
(
  'Penne Arrabbiata',
  'Penne pasta in a spicy tomato sauce with garlic, red chili and fresh parsley. Simple, fiery, delicious.',
  200.00, 'https://https://images.unsplash.com/photo-1563379926898-05f4575a45d8?w=400&h=300&fit=crop&auto=format',
  true, false, true, true, false, true,
  12, 520, 'Penne, San Marzano tomatoes, garlic, red chili, parsley, olive oil', 'Gluten',
  2, 3, 10, NOW(), NOW()
),
(
  'Fettuccine Alfredo',
  'Fresh fettuccine tossed in a silky sauce of butter, Parmigiano Reggiano and pasta water. Simple perfection.',
  230.00, 'https://https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&h=300&fit=crop&auto=format',
  true, false, true, false, false, false,
  14, 640, 'Fresh fettuccine, butter, Parmigiano Reggiano, pasta water, black pepper', 'Gluten, Dairy, Eggs',
  3, 3, 10, NOW(), NOW()
),
(
  'Lasagna Bolognese',
  'Layers of fresh pasta sheets, slow-cooked Bolognese meat sauce, béchamel and Parmigiano, baked golden.',
  280.00, 'https://https://images.unsplash.com/photo-1574672280600-4accfa5b6f98?w=400&h=300&fit=crop&auto=format',
  true, true, false, false, false, false,
  25, 750, 'Fresh pasta sheets, beef and pork ragu, béchamel sauce, Parmigiano, mozzarella', 'Gluten, Dairy, Eggs, Pork',
  4, 3, 10, NOW(), NOW()
),

-- ===== Italiano Pizzeria: Salads & Starters (category_id=11) =====
(
  'Caesar Salad',
  'Crisp romaine lettuce, homemade Caesar dressing, Parmigiano shavings, golden croutons and anchovies.',
  160.00, 'https://https://images.unsplash.com/photo-1551248429-40975aa4de74?w=400&h=300&fit=crop&auto=format',
  true, false, false, false, false, false,
  8, 310, 'Romaine lettuce, Caesar dressing, Parmigiano, homemade croutons, anchovies', 'Gluten, Dairy, Fish, Eggs',
  1, 3, 11, NOW(), NOW()
),
(
  'Bruschetta al Pomodoro',
  'Toasted ciabatta rubbed with garlic, topped with fresh diced tomatoes, basil and extra virgin olive oil.',
  140.00, 'https://https://images.unsplash.com/photo-1572695157366-5e585ab2b69f?w=400&h=300&fit=crop&auto=format',
  true, false, true, true, false, false,
  5, 210, 'Ciabatta bread, fresh tomatoes, garlic, fresh basil, extra virgin olive oil, sea salt', 'Gluten',
  2, 3, 11, NOW(), NOW()
),
(
  'Garlic Focaccia',
  'Freshly baked Ligurian-style focaccia with rosemary, roasted garlic and flaky sea salt.',
  90.00, 'https://https://images.unsplash.com/photo-1585515656973-b0b67f0c7ac0?w=400&h=300&fit=crop&auto=format',
  true, false, true, true, false, false,
  8, 270, 'Focaccia dough, garlic, fresh rosemary, olive oil, flaky sea salt', 'Gluten',
  3, 3, 11, NOW(), NOW()
),

-- ===== Italiano Pizzeria: Drinks (category_id=12) =====
(
  'San Pellegrino (500 ml)',
  'Italy''s premium sparkling mineral water from the Italian Alps.',
  90.00, 'https://https://images.unsplash.com/photo-1543179116-13c3b9ce2f6e?w=400&h=300&fit=crop&auto=format',
  true, false, true, true, true, false,
  1, 0, 'Natural sparkling mineral water', NULL,
  1, 3, 12, NOW(), NOW()
),
(
  'Fresh Lemonade',
  'Freshly squeezed Sicilian lemon juice with mineral water, cane sugar and a hint of fresh mint. Served iced.',
  80.00, 'https://https://images.unsplash.com/photo-1497534446932-c925b458314e?w=400&h=300&fit=crop&auto=format',
  true, false, true, true, true, false,
  3, 85, 'Fresh lemons, sparkling mineral water, cane sugar, fresh mint', NULL,
  2, 3, 12, NOW(), NOW()
),
(
  'Tiramisu Milkshake',
  'Indulgent milkshake inspired by tiramisu — espresso, mascarpone, cocoa and vanilla ice cream.',
  130.00, 'https://https://images.unsplash.com/photo-1572490122747-3e9f7f1614af?w=400&h=300&fit=crop&auto=format',
  true, true, true, false, true, false,
  5, 380, 'Espresso, mascarpone, cocoa powder, vanilla ice cream, whole milk', 'Dairy, Eggs',
  3, 3, 12, NOW(), NOW()
),

-- ===== Big Burger House: Burgers (category_id=13, restaurant_id=4) =====
(
  'Classic Wagyu Cheeseburger',
  'Double Wagyu beef smash patty, aged cheddar, lettuce, tomato, pickles and house sauce on a brioche bun.',
  190.00, 'https://https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&h=300&fit=crop&auto=format',
  true, true, false, false, false, false,
  12, 680, 'Wagyu beef patties, aged cheddar, lettuce, tomato, pickles, brioche bun, house sauce', 'Gluten, Dairy, Eggs',
  1, 4, 13, NOW(), NOW()
),
(
  'BBQ Bacon Burger',
  'Wagyu smash patty, crispy streaky bacon, smoked BBQ sauce, caramelized onions and Swiss cheese.',
  240.00, 'https://https://images.unsplash.com/photo-1550547660-d9450f859349?w=400&h=300&fit=crop&auto=format',
  true, true, false, false, false, false,
  14, 790, 'Wagyu beef, crispy bacon, Swiss cheese, smoky BBQ sauce, caramelized onions, brioche bun', 'Gluten, Dairy, Pork',
  2, 4, 13, NOW(), NOW()
),
(
  'Crispy Chicken Burger',
  'Buttermilk-fried chicken thigh with jalapeño slaw, sriracha mayo on a toasted sesame brioche bun.',
  200.00, 'https://https://images.unsplash.com/photo-1606755962773-d324e0a13086?w=400&h=300&fit=crop&auto=format',
  true, false, false, false, false, true,
  14, 620, 'Chicken thigh, buttermilk batter, jalapeño slaw, sriracha mayo, sesame brioche bun', 'Gluten, Dairy, Eggs',
  3, 4, 13, NOW(), NOW()
),
(
  'Mushroom & Swiss Burger',
  'Wagyu patty with sautéed portobello mushrooms, Swiss cheese and truffle aioli.',
  220.00, 'https://https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=400&h=300&fit=crop&auto=format',
  true, false, false, false, false, false,
  13, 650, 'Wagyu beef patty, portobello mushrooms, Swiss cheese, truffle aioli, brioche bun', 'Gluten, Dairy',
  4, 4, 13, NOW(), NOW()
),
(
  'Double Smash Burger',
  'Two stacked Wagyu smash patties, double cheddar, pickles, minced onion and special burger sauce. The big one.',
  260.00, 'https://https://images.unsplash.com/photo-1594212699903-ec8a3eca50f5?w=400&h=300&fit=crop&auto=format',
  true, true, false, false, false, false,
  15, 920, 'Double Wagyu beef patties, double cheddar, pickles, minced onion, special sauce, brioche bun', 'Gluten, Dairy, Eggs',
  5, 4, 13, NOW(), NOW()
),

-- ===== Big Burger House: Sides & Fries (category_id=14) =====
(
  'Crispy Shoestring Fries',
  'Golden, super-crispy thin-cut fries seasoned with sea salt. Comes with house dipping sauce.',
  80.00, 'https://https://images.unsplash.com/photo-1573080496219-bb964701c2a8?w=400&h=300&fit=crop&auto=format',
  true, false, true, true, true, false,
  8, 380, 'Potatoes, sea salt, vegetable oil', NULL,
  1, 4, 14, NOW(), NOW()
),
(
  'Loaded Cheese Fries',
  'Shoestring fries smothered in melted cheddar cheese sauce, pickled jalapeños and crispy bacon bits.',
  130.00, 'https://https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=400&h=300&fit=crop&auto=format',
  true, true, false, false, false, true,
  8, 560, 'Fries, cheddar cheese sauce, pickled jalapeños, crispy bacon bits', 'Dairy, Gluten, Pork',
  2, 4, 14, NOW(), NOW()
),
(
  'Onion Rings',
  'Thick-cut onion rings in a light, crunchy beer batter. Served with ranch dipping sauce.',
  90.00, 'https://https://images.unsplash.com/photo-1639024471283-03518883512d?w=400&h=300&fit=crop&auto=format',
  true, false, true, false, false, false,
  8, 310, 'Onion, beer batter, vegetable oil, ranch sauce', 'Gluten, Dairy, Eggs',
  3, 4, 14, NOW(), NOW()
),
(
  'Creamy Coleslaw',
  'House-made coleslaw with cabbage, carrots and a light apple cider vinegar dressing.',
  70.00, 'https://https://images.unsplash.com/photo-1515888844796-5a7a33a65773?w=400&h=300&fit=crop&auto=format',
  true, false, true, false, true, false,
  3, 150, 'Cabbage, carrots, mayonnaise, apple cider vinegar, sugar, salt', 'Eggs',
  4, 4, 14, NOW(), NOW()
),

-- ===== Big Burger House: Drinks & Shakes (category_id=15) =====
(
  'Chocolate Milkshake',
  'Thick-blended milkshake made with premium dark chocolate ice cream and whole milk. Topped with whipped cream.',
  130.00, 'https://https://images.unsplash.com/photo-1572490122747-3e9f7f1614af?w=400&h=300&fit=crop&auto=format',
  true, true, true, false, true, false,
  5, 520, 'Dark chocolate ice cream, whole milk, chocolate syrup, whipped cream', 'Dairy',
  1, 4, 15, NOW(), NOW()
),
(
  'Strawberry Milkshake',
  'Fresh strawberry milkshake blended with real strawberries and vanilla ice cream.',
  130.00, 'https://https://images.unsplash.com/photo-1519225421980-d8b18fa4c5e5?w=400&h=300&fit=crop&auto=format',
  true, false, true, false, true, false,
  5, 480, 'Strawberry ice cream, fresh strawberries, whole milk', 'Dairy',
  2, 4, 15, NOW(), NOW()
),
(
  'Vanilla Milkshake',
  'Classic milkshake with premium Madagascar vanilla bean ice cream. Rich, creamy, timeless.',
  120.00, 'https://https://images.unsplash.com/photo-1570197788417-0e82375c9371?w=400&h=300&fit=crop&auto=format',
  true, false, true, false, true, false,
  5, 450, 'Vanilla bean ice cream, whole milk, Madagascar vanilla extract', 'Dairy',
  3, 4, 15, NOW(), NOW()
),
(
  'Sparkling Lemonade',
  'Freshly squeezed lemonade with sparkling water and a hint of mint. Light and refreshing.',
  70.00, 'https://https://images.unsplash.com/photo-1497534446932-c925b458314e?w=400&h=300&fit=crop&auto=format',
  true, false, true, true, true, false,
  2, 80, 'Fresh lemons, sparkling water, cane sugar, fresh mint', NULL,
  4, 4, 15, NOW(), NOW()
),

-- ===== Ramen Noodle Bar: Ramen (category_id=16, restaurant_id=5) =====
(
  'Tonkotsu Ramen',
  '18-hour slow-cooked pork bone broth, wavy noodles, chashu pork, marinated soft-boiled egg, bamboo shoots and nori.',
  230.00, 'https://https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=400&h=300&fit=crop&auto=format',
  true, true, false, false, false, false,
  15, 680, 'Pork bone broth, wavy ramen noodles, chashu pork, marinated egg, bamboo shoots, corn, nori, green onion', 'Gluten, Eggs, Pork',
  1, 5, 16, NOW(), NOW()
),
(
  'Spicy Miso Ramen',
  'Bold spicy miso broth with ramen noodles, chashu pork, corn, butter, bean sprouts and chili oil.',
  220.00, 'https://https://images.unsplash.com/photo-1591814468924-caf88d1232e1?w=400&h=300&fit=crop&auto=format',
  true, true, false, false, false, true,
  15, 640, 'Spicy miso broth, ramen noodles, chashu pork, corn, butter, bean sprouts, chili oil, nori', 'Gluten, Soy, Pork, Dairy',
  2, 5, 16, NOW(), NOW()
),
(
  'Shoyu Ramen',
  'Light, clear soy sauce-based chicken broth with thin straight noodles, chicken chashu, spinach and menma.',
  200.00, 'https://https://images.unsplash.com/photo-1582878826629-33b175fbe285?w=400&h=300&fit=crop&auto=format',
  true, false, false, false, false, false,
  14, 520, 'Chicken shoyu broth, thin ramen noodles, chicken chashu, spinach, menma, nori, green onion', 'Gluten, Soy',
  3, 5, 16, NOW(), NOW()
),
(
  'Vegan Shiitake Ramen',
  'Rich shiitake mushroom and kombu dashi broth with wheat noodles, tofu, corn, spinach and sesame oil.',
  190.00, 'https://https://images.unsplash.com/photo-1547592166-23ac45744acd?w=400&h=300&fit=crop&auto=format',
  true, false, true, false, false, false,
  14, 440, 'Shiitake-kombu broth, ramen noodles, silken tofu, corn, spinach, sesame oil, nori', 'Gluten, Soy, Sesame',
  4, 5, 16, NOW(), NOW()
),

-- ===== Ramen Noodle Bar: Sides (category_id=17) =====
(
  'Chashu Pork (3 pcs)',
  'Tender melt-in-your-mouth rolled pork belly slow-braised in soy and mirin. Perfect ramen extra topping.',
  95.00, 'https://https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&h=300&fit=crop&auto=format',
  true, false, false, false, false, false,
  5, 160, 'Pork belly, soy sauce, mirin, sake, sugar, ginger', 'Soy, Pork',
  1, 5, 17, NOW(), NOW()
),
(
  'Ajitsuke Tamago (2 pcs)',
  'Two marinated soft-boiled eggs with a jammy yolk, soaked in soy-mirin tare for 24 hours.',
  50.00, 'https://https://images.unsplash.com/photo-1607920578393-5f3dc6e4efa5?w=400&h=300&fit=crop&auto=format',
  true, false, true, false, true, false,
  2, 85, 'Free-range eggs, soy sauce, mirin, sake, sugar', 'Eggs, Soy',
  2, 5, 17, NOW(), NOW()
),
(
  'Gyoza (6 pcs)',
  'Crispy pan-fried pork and cabbage dumplings with garlic-chili ponzu dipping sauce.',
  115.00, 'https://https://images.unsplash.com/photo-1563245372-f21724e3856d?w=400&h=300&fit=crop&auto=format',
  true, true, false, false, false, false,
  8, 215, 'Pork mince, napa cabbage, garlic, ginger, gyoza wrappers, ponzu sauce', 'Gluten, Pork',
  3, 5, 17, NOW(), NOW()
),
(
  'Karaage Chicken (5 pcs)',
  'Japanese fried chicken marinated in ginger-soy, double-fried for extra crunch. Served with Kewpie mayo.',
  120.00, 'https://https://images.unsplash.com/photo-1562802378-063ec186a863?w=400&h=300&fit=crop&auto=format',
  true, true, false, false, false, false,
  10, 310, 'Chicken thigh, soy sauce, ginger, sake, potato starch, Kewpie mayo', 'Gluten, Soy, Eggs',
  4, 5, 17, NOW(), NOW()
),

-- ===== Ramen Noodle Bar: Drinks (category_id=18) =====
(
  'Calpis Soda',
  'Japan''s beloved yogurt-based soft drink, lightly carbonated with a milky-citrus sweetness.',
  75.00, 'https://https://images.unsplash.com/photo-1543363006-09a7ac5de37a?w=400&h=300&fit=crop&auto=format',
  true, false, true, false, true, false,
  1, 110, 'Carbonated water, calpis concentrate, cane sugar, citric acid', 'Dairy',
  1, 5, 18, NOW(), NOW()
),
(
  'Mugicha (Barley Tea)',
  'Traditional Japanese roasted barley tea, served cold. Naturally caffeine-free and refreshing.',
  60.00, 'https://https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400&h=300&fit=crop&auto=format',
  true, false, true, true, true, false,
  2, 5, 'Roasted barley, filtered water', NULL,
  2, 5, 18, NOW(), NOW()
),
(
  'Japanese Iced Coffee',
  'Pour-over-style specialty coffee brewed directly onto ice. Bright, clean and naturally sweet.',
  85.00, 'https://https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400&h=300&fit=crop&auto=format',
  true, false, true, true, true, false,
  3, 10, 'Single-origin coffee beans, filtered water, ice', NULL,
  3, 5, 18, NOW(), NOW()
);

-- =====================================================================
-- SECTION 3: mhar_orders — Sample orders for demonstration
-- =====================================================================
-- NOTE: menu_item_id values correspond to IDs from the items inserted above.
--   Item IDs assigned by insertion order:
--     1  = Pad Thai               (Bangkok Street Food)
--     5  = Tom Yum Goong          (Bangkok Street Food)
--    13  = Thai Milk Tea           (Bangkok Street Food)
--    15  = Salmon Avocado Roll     (Sushi Master)
--    19  = Salmon Sashimi (5 pcs)  (Sushi Master)
--    25  = Miso Soup               (Sushi Master)
--    43  = Classic Wagyu Cheeseburger (Big Burger House)
--    48  = Crispy Shoestring Fries (Big Burger House)
--    56  = Tonkotsu Ramen          (Ramen Noodle Bar)
-- =====================================================================
\c mhar_orders

-- Order 1: John Doe at Bangkok Street Food — DELIVERED (historical)
INSERT INTO orders (
  order_number, customer_id, restaurant_id, rider_id,
  status, total_amount, delivery_fee,
  delivery_address, delivery_latitude, delivery_longitude,
  special_instructions, estimated_delivery_time,
  created_at, updated_at
) VALUES (
  'ORD-2025-0001',
  1, 1, 3,
  'DELIVERED', 330.00, 30.00,
  '88 Sukhumvit Soi 21, Watthana, Bangkok 10110', 13.7392, 100.5641,
  'Please leave at the front desk. Ring the doorbell twice.',
  '2025-03-15 12:40:00',
  '2025-03-15 12:00:00', '2025-03-15 12:52:00'
);

INSERT INTO order_items (order_id, menu_item_id, menu_item_name, quantity, unit_price, total_price) VALUES
  (1, 1,  'Pad Thai',              1, 120.00, 120.00),
  (1, 5,  'Tom Yum Goong',         1, 150.00, 150.00),
  (1, 13, 'Thai Milk Tea (Cha Yen)', 1,  60.00, 60.00);

-- Order 2: Sarah Wilson at Sushi Master — PREPARING (in progress)
INSERT INTO orders (
  order_number, customer_id, restaurant_id, rider_id,
  status, total_amount, delivery_fee,
  delivery_address, delivery_latitude, delivery_longitude,
  special_instructions, estimated_delivery_time,
  created_at, updated_at
) VALUES (
  'ORD-2025-0002',
  5, 2, NULL,
  'PREPARING', 720.00, 40.00,
  '12/3 Silom Road, Silom, Bang Rak, Bangkok 10500', 13.7256, 100.5210,
  'No wasabi on the sashimi please.',
  NOW() + INTERVAL '25 minutes',
  NOW() - INTERVAL '12 minutes', NOW() - INTERVAL '8 minutes'
);

INSERT INTO order_items (order_id, menu_item_id, menu_item_name, quantity, unit_price, total_price) VALUES
  (2, 15, 'Salmon Avocado Roll',    2, 220.00, 440.00),
  (2, 19, 'Salmon Sashimi (5 pcs)', 1, 250.00, 250.00),
  (2, 25, 'Miso Soup',              1,  70.00,  70.00);

-- Order 3: John Doe at Big Burger House — PENDING (just placed)
INSERT INTO orders (
  order_number, customer_id, restaurant_id, rider_id,
  status, total_amount, delivery_fee,
  delivery_address, delivery_latitude, delivery_longitude,
  special_instructions, estimated_delivery_time,
  created_at, updated_at
) VALUES (
  'ORD-2025-0003',
  1, 4, NULL,
  'PENDING', 320.00, 30.00,
  '88 Sukhumvit Soi 21, Watthana, Bangkok 10110', 13.7392, 100.5641,
  'Extra napkins please.',
  NOW() + INTERVAL '30 minutes',
  NOW() - INTERVAL '2 minutes', NOW() - INTERVAL '2 minutes'
);

INSERT INTO order_items (order_id, menu_item_id, menu_item_name, quantity, unit_price, total_price) VALUES
  (3, 43, 'Classic Wagyu Cheeseburger', 1, 190.00, 190.00),
  (3, 48, 'Crispy Shoestring Fries',    1,  80.00,  80.00),
  (3, 53, 'Strawberry Milkshake',       1, 130.00, 130.00);

-- =====================================================================
-- SUMMARY
-- =====================================================================
-- Restaurants created: 5
--   1. Bangkok Street Food  (Thai)     — owner_id=2
--   2. Sushi Master         (Japanese) — owner_id=6
--   3. Italiano Pizzeria    (Italian)  — owner_id=7
--   4. Big Burger House     (American) — owner_id=8
--   5. Ramen Noodle Bar     (Japanese) — owner_id=9
--
-- Menu categories: 18 total (3-4 per restaurant)
-- Menu items: 66 total (11-14 per restaurant)
--
-- Sample orders: 3
--   ORD-2025-0001  John Doe → Bangkok Street Food    DELIVERED  ฿360
--   ORD-2025-0002  Sarah Wilson → Sushi Master       PREPARING  ฿760
--   ORD-2025-0003  John Doe → Big Burger House       PENDING    ฿350
--
-- Demo login credentials:
--   Customer:   customer@foodexpress.com  / Customer123!
--   Restaurant: restaurant@foodexpress.com / Restaurant123!
--   Admin:      admin@foodexpress.com     / Admin123!
-- =====================================================================
