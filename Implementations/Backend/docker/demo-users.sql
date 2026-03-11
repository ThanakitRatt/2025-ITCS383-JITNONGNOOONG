-- Demo Users for MharRuengSang Food Delivery Platform
-- Run this script after starting the backend to populate demo users
-- Note: Passwords are BCrypt hashed. Plain text passwords are in comments.

-- Customer Account
-- Email: customer@foodexpress.com
-- Password: Customer123!
INSERT INTO users (name, email, password, phone_number, role, enabled, created_at, updated_at)
VALUES (
  'John Doe',
  'customer@foodexpress.com',
  '$2a$10$rVQzVL9nB.KjZ7XWqXzOH.3qUv5Cq5JF7kH8nW9oD6xE8yT9aB1cW',
  '+66812345678',
  'CUSTOMER',
  true,
  NOW(),
  NOW()
);

-- Restaurant Account
-- Email: restaurant@foodexpress.com  
-- Password: Restaurant123!
INSERT INTO users (name, email, password, phone_number, role, enabled, created_at, updated_at)
VALUES (
  'Bangkok Street Food',
  'restaurant@foodexpress.com',
  '$2a$10$aXWrZ8nF.LmS3YVnYpKxX.6qUv8Dq3KF9kJ6oX7pE4yF6zU7bC8dY',
  '+66823456789',
  'RESTAURANT',
  true,
  NOW(),
  NOW()
);

-- Rider Account
-- Email: rider@foodexpress.com
-- Password: Rider123!
INSERT INTO users (name, email, password, phone_number, role, enabled, created_at, updated_at)
VALUES (
  'Mike Chen',
  'rider@foodexpress.com',
  '$2a$10$bYXsA9oG.MnT4ZWoZqLyY.7rVw9Er4LG0lK7pY8qF5zG7aV8cD9eZ',
  '+66834567890',
  'RIDER',
  true,
  NOW(),
  NOW()
);

-- Admin Account
-- Email: admin@foodexpress.com
-- Password: Admin123!
INSERT INTO users (name, email, password, phone_number, role, enabled, created_at, updated_at)
VALUES (
  'Admin User',
  'admin@foodexpress.com',
  '$2a$10$cZYtB0pH.NoU5aXpArMzZ.8sWx0Fs5MH1mL8qZ9rG6aH8bW9dE0fA',
  '+66845678901',
  'ADMIN',
  true,
  NOW(),
  NOW()
);

-- Additional Customer for testing
-- Email: sarah@foodexpress.com
-- Password: Sarah123!
INSERT INTO users (name, email, password, phone_number, role, enabled, created_at, updated_at)
VALUES (
  'Sarah Wilson',
  'sarah@foodexpress.com',
  '$2a$10$dAZuC1qI.OpV6bYqBsNaA.9tXy1Gt6NI2nM9rA0sH7bI9cX0eF1gB',
  '+66856789012',
  'CUSTOMER',
  true,
  NOW(),
  NOW()
);

-- Second Restaurant for testing
-- Email: sushi@foodexpress.com
-- Password: Sushi123!
INSERT INTO users (name, email, password, phone_number, role, enabled, created_at, updated_at)
VALUES (
  'Sushi Master',
  'sushi@foodexpress.com',
  '$2a$10$eBalD2rJ.PqW7cZrCtObB.0uYz2Hu7OJ3oN0sB1tI8cJ0dY1fG2hC',
  '+66867890123',
  'RESTAURANT',
  true,
  NOW(),
  NOW()
);

-- Note: In production, these hashes are examples. Generate real BCrypt hashes using:
-- PasswordEncoder encoder = new BCryptPasswordEncoder();
-- String hash = encoder.encode("YourPassword123!");
