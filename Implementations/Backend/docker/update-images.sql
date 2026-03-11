-- =====================================================================
-- Update all images from picsum.photos to specific Unsplash food photos
-- Run: PGPASSWORD=mhar_password psql -U mhar_user -h localhost -p 5432 -d mhar_restaurants -f docker/update-images.sql
-- =====================================================================

-- Restaurants: logos (400x400) and covers (1200x500)
UPDATE restaurants SET logo_url='https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=400&h=400&fit=crop&auto=format', cover_image_url='https://images.unsplash.com/photo-1562802378-063ec186a863?w=1200&h=500&fit=crop&auto=format' WHERE name='Bangkok Street Food';
UPDATE restaurants SET logo_url='https://images.unsplash.com/photo-1553621042-f6e147245754?w=400&h=400&fit=crop&auto=format', cover_image_url='https://images.unsplash.com/photo-1617196035154-1e7e6e28b0db?w=1200&h=500&fit=crop&auto=format' WHERE name='Sushi Master';
UPDATE restaurants SET logo_url='https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&h=400&fit=crop&auto=format', cover_image_url='https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=1200&h=500&fit=crop&auto=format' WHERE name='Italiano Pizzeria';
UPDATE restaurants SET logo_url='https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&h=400&fit=crop&auto=format', cover_image_url='https://images.unsplash.com/photo-1550547660-d9450f859349?w=1200&h=500&fit=crop&auto=format' WHERE name='Big Burger House';
UPDATE restaurants SET logo_url='https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=400&h=400&fit=crop&auto=format', cover_image_url='https://images.unsplash.com/photo-1582878826629-33b175fbe285?w=1200&h=500&fit=crop&auto=format' WHERE name='Ramen Noodle Bar';

-- Menu categories (400x300)
UPDATE menu_categories SET image_url='https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=400&h=300&fit=crop&auto=format' WHERE name='Noodle Dishes';
UPDATE menu_categories SET image_url='https://images.unsplash.com/photo-1455619452474-d2be8b1e70cd?w=400&h=300&fit=crop&auto=format' WHERE name='Soups & Curries';
UPDATE menu_categories SET image_url='https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=400&h=300&fit=crop&auto=format' WHERE name='Rice Dishes';
UPDATE menu_categories SET image_url='https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&h=300&fit=crop&auto=format' WHERE name='Desserts & Drinks';
UPDATE menu_categories SET image_url='https://images.unsplash.com/photo-1553621042-f6e147245754?w=400&h=300&fit=crop&auto=format' WHERE name='Sushi & Maki Rolls';
UPDATE menu_categories SET image_url='https://images.unsplash.com/photo-1617196033183-421062a1103b?w=400&h=300&fit=crop&auto=format' WHERE name='Sashimi';
UPDATE menu_categories SET image_url='https://images.unsplash.com/photo-1563245372-f21724e3856d?w=400&h=300&fit=crop&auto=format' WHERE name='Hot Dishes';
UPDATE menu_categories SET image_url='https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400&h=300&fit=crop&auto=format' WHERE name='Drinks' AND restaurant_id=2;
UPDATE menu_categories SET image_url='https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&h=300&fit=crop&auto=format' WHERE name='Pizzas';
UPDATE menu_categories SET image_url='https://images.unsplash.com/photo-1551892374-ecf8754cf744?w=400&h=300&fit=crop&auto=format' WHERE name='Pasta';
UPDATE menu_categories SET image_url='https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400&h=300&fit=crop&auto=format' WHERE name='Salads & Starters';
UPDATE menu_categories SET image_url='https://images.unsplash.com/photo-1497534446932-c925b458314e?w=400&h=300&fit=crop&auto=format' WHERE name='Drinks' AND restaurant_id=3;
UPDATE menu_categories SET image_url='https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&h=300&fit=crop&auto=format' WHERE name='Burgers';
UPDATE menu_categories SET image_url='https://images.unsplash.com/photo-1573080496219-bb964701c2a8?w=400&h=300&fit=crop&auto=format' WHERE name='Sides & Fries';
UPDATE menu_categories SET image_url='https://images.unsplash.com/photo-1572490122747-3e9f7f1614af?w=400&h=300&fit=crop&auto=format' WHERE name='Drinks & Shakes';
UPDATE menu_categories SET image_url='https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=400&h=300&fit=crop&auto=format' WHERE name='Ramen';
UPDATE menu_categories SET image_url='https://images.unsplash.com/photo-1563245372-f21724e3856d?w=400&h=300&fit=crop&auto=format' WHERE name='Sides' AND restaurant_id=5;
UPDATE menu_categories SET image_url='https://images.unsplash.com/photo-1547592166-23ac45744acd?w=400&h=300&fit=crop&auto=format' WHERE name='Drinks' AND restaurant_id=5;

-- Menu items (400x300)
-- Bangkok Street Food
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1559314809-0d155014e29e?w=400&h=300&fit=crop&auto=format' WHERE name='Pad Thai';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1527515637462-cff94ebb84ec?w=400&h=300&fit=crop&auto=format' WHERE name='Pad See Ew';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1533622597524-a1215e26c0a2?w=400&h=300&fit=crop&auto=format' WHERE name='Pad Kee Mao (Drunken Noodles)';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1547592166-23ac45744acd?w=400&h=300&fit=crop&auto=format' WHERE name='Boat Noodles';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1455619452474-d2be8b1e70cd?w=400&h=300&fit=crop&auto=format' WHERE name='Tom Yum Goong';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1476718406336-bb5a9690ee2a?w=400&h=300&fit=crop&auto=format' WHERE name='Tom Kha Gai';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1596797882599-15b1c6ce4ddf?w=400&h=300&fit=crop&auto=format' WHERE name='Green Curry (Kaeng Khiao Wan)';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1455619452474-d2be8b1e70cd?w=400&h=300&fit=crop&auto=format' WHERE name='Massaman Curry';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=400&h=300&fit=crop&auto=format' WHERE name='Khao Pad (Thai Fried Rice)';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&h=300&fit=crop&auto=format' WHERE name='Khao Man Gai';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1562802378-063ec186a863?w=400&h=300&fit=crop&auto=format' WHERE name='Basil Chicken Rice (Khao Kra Pao)';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1568718119408-99a3d08e2e0b?w=400&h=300&fit=crop&auto=format' WHERE name='Mango Sticky Rice';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&h=300&fit=crop&auto=format' WHERE name='Thai Milk Tea (Cha Yen)';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1488477181771-4695d2499bef?w=400&h=300&fit=crop&auto=format' WHERE name='Pandan Coconut Jelly';
-- Sushi Master
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1617196035154-1e7e6e28b0db?w=400&h=300&fit=crop&auto=format' WHERE name='Salmon Avocado Roll';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1553621042-f6e147245754?w=400&h=300&fit=crop&auto=format' WHERE name='California Roll';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1617196033322-c0c22c52caaf?w=400&h=300&fit=crop&auto=format' WHERE name='Spicy Tuna Roll';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1617196033423-3ac59e3aedf4?w=400&h=300&fit=crop&auto=format' WHERE name='Dragon Roll';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1617196033183-421062a1103b?w=400&h=300&fit=crop&auto=format' WHERE name='Salmon Sashimi (5 pcs)';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1617196035303-f6ad4f09e4e2?w=400&h=300&fit=crop&auto=format' WHERE name='Tuna Sashimi (5 pcs)';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1580822184713-fc5ded9f8d0f?w=400&h=300&fit=crop&auto=format' WHERE name='Mixed Sashimi (10 pcs)';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1563245372-f21724e3856d?w=400&h=300&fit=crop&auto=format' WHERE name='Gyoza (6 pcs)' AND restaurant_id=2;
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1543362906-acfc16c67564?w=400&h=300&fit=crop&auto=format' WHERE name='Edamame';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1632206000219-0d15f20bf35d?w=400&h=300&fit=crop&auto=format' WHERE name='Tempura Shrimp (4 pcs)';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1547592166-23ac45744acd?w=400&h=300&fit=crop&auto=format' WHERE name='Miso Soup';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1608270586263-f0c14b71d534?w=400&h=300&fit=crop&auto=format' WHERE name='Sake (50 ml)';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400&h=300&fit=crop&auto=format' WHERE name='Japanese Green Tea (Ocha)';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1543363006-09a7ac5de37a?w=400&h=300&fit=crop&auto=format' WHERE name='Ramune Soda';
-- Italiano Pizzeria
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&h=300&fit=crop&auto=format' WHERE name='Margherita Pizza';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1628840042765-356cda07504e?w=400&h=300&fit=crop&auto=format' WHERE name='Pepperoni Pizza';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1571997473849-4fa1c2234c7a?w=400&h=300&fit=crop&auto=format' WHERE name='Quattro Formaggi';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=400&h=300&fit=crop&auto=format' WHERE name='BBQ Chicken Pizza';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1551892374-ecf8754cf744?w=400&h=300&fit=crop&auto=format' WHERE name='Spaghetti Carbonara';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1563379926898-05f4575a45d8?w=400&h=300&fit=crop&auto=format' WHERE name='Penne Arrabbiata';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&h=300&fit=crop&auto=format' WHERE name='Fettuccine Alfredo';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1574672280600-4accfa5b6f98?w=400&h=300&fit=crop&auto=format' WHERE name='Lasagna Bolognese';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1551248429-40975aa4de74?w=400&h=300&fit=crop&auto=format' WHERE name='Caesar Salad';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1572695157366-5e585ab2b69f?w=400&h=300&fit=crop&auto=format' WHERE name='Bruschetta al Pomodoro';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1585515656973-b0b67f0c7ac0?w=400&h=300&fit=crop&auto=format' WHERE name='Garlic Focaccia';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1543179116-13c3b9ce2f6e?w=400&h=300&fit=crop&auto=format' WHERE name='San Pellegrino (500 ml)';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1497534446932-c925b458314e?w=400&h=300&fit=crop&auto=format' WHERE name='Fresh Lemonade';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1572490122747-3e9f7f1614af?w=400&h=300&fit=crop&auto=format' WHERE name='Tiramisu Milkshake';
-- Big Burger House
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&h=300&fit=crop&auto=format' WHERE name='Classic Wagyu Cheeseburger';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1550547660-d9450f859349?w=400&h=300&fit=crop&auto=format' WHERE name='BBQ Bacon Burger';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1606755962773-d324e0a13086?w=400&h=300&fit=crop&auto=format' WHERE name='Crispy Chicken Burger';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=400&h=300&fit=crop&auto=format' WHERE name='Mushroom & Swiss Burger';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1594212699903-ec8a3eca50f5?w=400&h=300&fit=crop&auto=format' WHERE name='Double Smash Burger';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1573080496219-bb964701c2a8?w=400&h=300&fit=crop&auto=format' WHERE name='Crispy Shoestring Fries';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=400&h=300&fit=crop&auto=format' WHERE name='Loaded Cheese Fries';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1639024471283-03518883512d?w=400&h=300&fit=crop&auto=format' WHERE name='Onion Rings';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1515888844796-5a7a33a65773?w=400&h=300&fit=crop&auto=format' WHERE name='Creamy Coleslaw';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1572490122747-3e9f7f1614af?w=400&h=300&fit=crop&auto=format' WHERE name='Chocolate Milkshake';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1519225421980-d8b18fa4c5e5?w=400&h=300&fit=crop&auto=format' WHERE name='Strawberry Milkshake';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1570197788417-0e82375c9371?w=400&h=300&fit=crop&auto=format' WHERE name='Vanilla Milkshake';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1497534446932-c925b458314e?w=400&h=300&fit=crop&auto=format' WHERE name='Sparkling Lemonade';
-- Ramen Noodle Bar
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=400&h=300&fit=crop&auto=format' WHERE name='Tonkotsu Ramen';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1591814468924-caf88d1232e1?w=400&h=300&fit=crop&auto=format' WHERE name='Spicy Miso Ramen';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1582878826629-33b175fbe285?w=400&h=300&fit=crop&auto=format' WHERE name='Shoyu Ramen';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1547592166-23ac45744acd?w=400&h=300&fit=crop&auto=format' WHERE name='Vegan Shiitake Ramen';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&h=300&fit=crop&auto=format' WHERE name='Chashu Pork (3 pcs)';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1607920578393-5f3dc6e4efa5?w=400&h=300&fit=crop&auto=format' WHERE name='Ajitsuke Tamago (2 pcs)';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1563245372-f21724e3856d?w=400&h=300&fit=crop&auto=format' WHERE name='Gyoza (6 pcs)' AND restaurant_id=5;
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1562802378-063ec186a863?w=400&h=300&fit=crop&auto=format' WHERE name='Karaage Chicken (5 pcs)';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1543363006-09a7ac5de37a?w=400&h=300&fit=crop&auto=format' WHERE name='Calpis Soda';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400&h=300&fit=crop&auto=format' WHERE name='Mugicha (Barley Tea)';
UPDATE menu_items SET image_url='https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400&h=300&fit=crop&auto=format' WHERE name='Japanese Iced Coffee';
