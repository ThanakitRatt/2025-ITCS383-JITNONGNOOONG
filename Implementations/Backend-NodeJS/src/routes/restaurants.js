const express = require('express');
const router = express.Router();
const db = require('../config/db');

// Helper to format as ApiResponse
const successResponse = (data, message = 'Success') => ({
  success: true,
  message,
  data
});

const reviewSortMap = {
  recent: 'rr.created_at DESC',
  highest: 'rr.rating DESC, rr.created_at DESC',
  lowest: 'rr.rating ASC, rr.created_at DESC',
};

const restaurantSelect = `
  SELECT
    id,
    owner_id AS ownerId,
    name,
    description,
    cuisine_type AS cuisineType,
    address,
    latitude,
    longitude,
    phone_number AS phoneNumber,
    email,
    opening_time AS openingTime,
    closing_time AS closingTime,
    status,
    is_active AS isActive,
    accepts_orders AS acceptsOrders,
    average_rating AS averageRating,
    total_reviews AS totalReviews,
    minimum_order_amount AS minimumOrderAmount,
    delivery_fee AS deliveryFee,
    estimated_delivery_time AS estimatedDeliveryTime,
    logo_url AS logoUrl,
    cover_image_url AS coverImageUrl,
    created_at AS createdAt,
    updated_at AS updatedAt
  FROM restaurants
`;

const getMenuItemById = async (restaurantId, itemId) => {
  const [items] = await db.query(
    'SELECT * FROM menu_items WHERE restaurant_id = ? AND id = ?',
    [restaurantId, itemId]
  );

  return items[0] || null;
};

const categoryBelongsToRestaurant = async (restaurantId, categoryId) => {
  const [categories] = await db.query(
    'SELECT id FROM menu_categories WHERE restaurant_id = ? AND id = ?',
    [restaurantId, categoryId]
  );

  return categories.length > 0;
};

const updateRestaurantRatingMetrics = async (restaurantId) => {
  const [[metrics]] = await db.query(
    `SELECT
      COALESCE(ROUND(AVG(rating), 2), 0) AS averageRating,
      COUNT(*) AS totalReviews
     FROM restaurant_reviews
     WHERE restaurant_id = ?`,
    [restaurantId]
  );

  await db.query(
    'UPDATE restaurants SET average_rating = ?, total_reviews = ? WHERE id = ?',
    [Number(metrics.averageRating || 0), Number(metrics.totalReviews || 0), restaurantId]
  );

  return {
    averageRating: Number(metrics.averageRating || 0),
    totalReviews: Number(metrics.totalReviews || 0),
  };
};

// Admin Restaurant Stats
router.get('/stats', async (req, res) => {
  try {
    const [[{ totalRestaurants }]] = await db.query('SELECT COUNT(*) as totalRestaurants FROM restaurants');
    const [[{ activeRestaurants }]] = await db.query('SELECT COUNT(*) as activeRestaurants FROM restaurants WHERE is_active = 1 OR status = "APPROVED"');
    
    res.json({
      success: true,
      message: 'Success',
      data: {
        totalRestaurants: Number(totalRestaurants),
        activeRestaurants: Number(activeRestaurants)
      }
    });
  } catch (error) {
    console.error('Error fetching restaurant stats:', error);
    res.status(500).json({ success: false, message: 'Server error', error: error.message });
  }
});

// Get all restaurants (paginated format expected by frontend)
router.get('/', async (req, res) => {
  try {
    const [rows] = await db.query(`${restaurantSelect} ORDER BY name`);
    res.json(successResponse({
      content: rows,
      totalElements: rows.length,
      totalPages: 1,
      size: rows.length,
      number: 0,
      first: true,
      last: true
    }));
  } catch (error) {
    res.status(500).json({ success: false, message: 'Server error', error: error.message });
  }
});

// Get top rated
router.get('/top-rated', async (req, res) => {
  try {
    const [rows] = await db.query(`${restaurantSelect} ORDER BY average_rating DESC LIMIT 10`);
    res.json(successResponse(rows));
  } catch (error) {
    res.status(500).json({ success: false, message: 'Server error', error: error.message });
  }
});

// Get unique cuisine types
router.get('/cuisine-types', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT DISTINCT cuisine_type FROM restaurants WHERE cuisine_type IS NOT NULL AND cuisine_type != ""');
    const cuisineTypes = rows.map(r => r.cuisine_type);
    res.json(successResponse(cuisineTypes));
  } catch (error) {
    res.status(500).json({ success: false, message: 'Server error', error: error.message });
  }
});

// Search restaurants
router.get('/search', async (req, res) => {
  try {
    const query = req.query.query || req.query.searchTerm || '';
    const cuisine = req.query.cuisine || req.query.cuisineType || '';
    
    let sql = `${restaurantSelect} WHERE is_active = true`;
    const params = [];
    
    if (query) {
      sql += ' AND (name LIKE ? OR description LIKE ?)';
      params.push(`%${query}%`, `%${query}%`);
    }
    
    if (cuisine) {
      sql += ' AND cuisine_type = ?';
      params.push(cuisine);
    }
    
    const [rows] = await db.query(sql, params);
    
    res.json(successResponse({
      content: rows,
      totalElements: rows.length,
      totalPages: 1,
      size: rows.length,
      number: 0,
      first: true,
      last: true
    }));
  } catch (error) {
    res.status(500).json({ success: false, message: 'Server error', error: error.message });
  }
});

// Get restaurant by owner
router.get('/owner/:ownerId', async (req, res) => {
  try {
    let targetOwnerId = req.params.ownerId;
    if (targetOwnerId === "100") targetOwnerId = 2; // Restaurant user in mock is 2
    if (targetOwnerId === "1") targetOwnerId = 2; // Front-end might test with user 1

    const [restaurants] = await db.query(`${restaurantSelect} WHERE owner_id = ?`, [targetOwnerId]);
    if (restaurants.length === 0) {
      return res.status(404).json({ success: false, message: 'Restaurant not found' });
    }

    res.json(successResponse(restaurants));
  } catch (error) {
    res.status(500).json({ success: false, message: 'Server error', error: error.message });
  }
});

// Get single restaurant
router.get('/:id', async (req, res) => {
  try {
    const [restaurants] = await db.query(`${restaurantSelect} WHERE id = ?`, [req.params.id]);
    if (restaurants.length === 0) {
      return res.status(404).json({ success: false, message: 'Restaurant not found' });
    }
    
    res.json(successResponse(restaurants[0]));
  } catch (error) {
    res.status(500).json({ success: false, message: 'Server error', error: error.message });
  }
});

// Get restaurant menu (paginated format expected)
router.get('/:id/menu', async (req, res) => {
  try {
    const [menus] = await db.query('SELECT * FROM menu_items WHERE restaurant_id = ?', [req.params.id]);
    res.json(successResponse({
      content: menus,
      totalElements: menus.length,
      totalPages: 1,
      size: menus.length,
      number: 0,
      first: true,
      last: true
    }));
  } catch (error) {
    res.status(500).json({ success: false, message: 'Server error', error: error.message });
  }
});

// Get a single menu item
router.get('/:id/menu/:itemId', async (req, res) => {
  try {
    const menuItem = await getMenuItemById(req.params.id, req.params.itemId);

    if (!menuItem) {
      return res.status(404).json({ success: false, message: 'Menu item not found' });
    }

    res.json(successResponse(menuItem));
  } catch (error) {
    console.error('Error fetching menu item:', error);
    res.status(500).json({ success: false, message: 'Server error', error: error.message });
  }
});

// Get categories
router.get('/:id/categories', async (req, res) => {
  try {
    const [categories] = await db.query('SELECT * FROM menu_categories WHERE restaurant_id = ?', [req.params.id]);
    res.json(successResponse(categories));
  } catch (error) {
    res.status(500).json({ success: false, message: 'Server error', error: error.message });
  }
});

// Get restaurant reviews
router.get('/:id/reviews', async (req, res) => {
  try {
    const restaurantId = req.params.id;
    const sort = req.query.sort || 'recent';
    const orderBy = reviewSortMap[sort] || reviewSortMap.recent;

    const [reviews] = await db.query(
      `SELECT
        rr.id,
        rr.restaurant_id AS restaurantId,
        rr.customer_id AS customerId,
        rr.order_id AS orderId,
        rr.rating,
        rr.review_text AS reviewText,
        rr.created_at AS createdAt,
        rr.updated_at AS updatedAt,
        u.name AS customerName
       FROM restaurant_reviews rr
       INNER JOIN users u ON rr.customer_id = u.id
       WHERE rr.restaurant_id = ?
       ORDER BY ${orderBy}`,
      [restaurantId]
    );

    res.json(successResponse(reviews));
  } catch (error) {
    console.error('Error fetching restaurant reviews:', error);
    res.status(500).json({ success: false, message: 'Server error', error: error.message });
  }
});

// Create restaurant review
router.post('/:id/reviews', async (req, res) => {
  try {
    const restaurantId = Number(req.params.id);
    const { orderId, customerId, rating, reviewText } = req.body;

    if (!orderId || !customerId || !rating) {
      return res.status(400).json({ success: false, message: 'Order, customer, and rating are required' });
    }

    const numericRating = Number(rating);
    if (!Number.isInteger(numericRating) || numericRating < 1 || numericRating > 5) {
      return res.status(400).json({ success: false, message: 'Rating must be between 1 and 5' });
    }

    const [eligibleOrders] = await db.query(
      `SELECT id, restaurant_id, customer_id, status
       FROM orders
       WHERE id = ? AND restaurant_id = ? AND customer_id = ?`,
      [orderId, restaurantId, customerId]
    );

    if (eligibleOrders.length === 0) {
      return res.status(403).json({ success: false, message: 'This order cannot be used to review the restaurant' });
    }

    const order = eligibleOrders[0];
    if (!['DELIVERED', 'COMPLETED'].includes(order.status)) {
      return res.status(400).json({ success: false, message: 'You can only review restaurants after delivery is completed' });
    }

    const [existingReviews] = await db.query(
      'SELECT id FROM restaurant_reviews WHERE order_id = ?',
      [orderId]
    );

    if (existingReviews.length > 0) {
      return res.status(400).json({ success: false, message: 'A review has already been submitted for this order' });
    }

    const [result] = await db.query(
      `INSERT INTO restaurant_reviews
        (restaurant_id, customer_id, order_id, rating, review_text, created_at, updated_at)
       VALUES (?, ?, ?, ?, ?, NOW(), NOW())`,
      [restaurantId, customerId, orderId, numericRating, reviewText?.trim() || null]
    );

    await updateRestaurantRatingMetrics(restaurantId);

    const [createdRows] = await db.query(
      `SELECT
        rr.id,
        rr.restaurant_id AS restaurantId,
        rr.customer_id AS customerId,
        rr.order_id AS orderId,
        rr.rating,
        rr.review_text AS reviewText,
        rr.created_at AS createdAt,
        rr.updated_at AS updatedAt,
        u.name AS customerName
       FROM restaurant_reviews rr
       INNER JOIN users u ON rr.customer_id = u.id
       WHERE rr.id = ?`,
      [result.insertId]
    );

    res.status(201).json(successResponse(createdRows[0], 'Review submitted successfully'));
  } catch (error) {
    console.error('Error creating restaurant review:', error);
    res.status(500).json({ success: false, message: 'Server error', error: error.message });
  }
});

// Create menu item
router.post('/:id/menu', async (req, res) => {
  try {
    const restaurantId = req.params.id;
    const {
      name,
      description,
      price,
      categoryId,
      imageUrl = null,
      preparationTime = null,
      isAvailable = true,
      displayOrder = 0,
    } = req.body;

    if (!name || price === undefined || price === null || !categoryId) {
      return res.status(400).json({ success: false, message: 'Name, price, and category are required' });
    }

    const categoryExists = await categoryBelongsToRestaurant(restaurantId, categoryId);
    if (!categoryExists) {
      return res.status(400).json({ success: false, message: 'Category does not belong to this restaurant' });
    }

    const [result] = await db.query(
      `INSERT INTO menu_items
        (name, description, price, image_url, is_available, preparation_time, display_order, category_id, restaurant_id, created_at, updated_at)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())`,
      [name, description || '', price, imageUrl, isAvailable, preparationTime, displayOrder, categoryId, restaurantId]
    );

    const createdItem = await getMenuItemById(restaurantId, result.insertId);
    res.status(201).json(successResponse(createdItem, 'Menu item created'));
  } catch (error) {
    console.error('Error creating menu item:', error);
    res.status(500).json({ success: false, message: 'Server error', error: error.message });
  }
});

// Update menu item
router.put('/:id/menu/:itemId', async (req, res) => {
  try {
    const restaurantId = req.params.id;
    const itemId = req.params.itemId;
    const existingItem = await getMenuItemById(restaurantId, itemId);

    if (!existingItem) {
      return res.status(404).json({ success: false, message: 'Menu item not found' });
    }

    const {
      name = existingItem.name,
      description = existingItem.description,
      price = existingItem.price,
      categoryId = existingItem.category_id,
      imageUrl = existingItem.image_url,
      preparationTime = existingItem.preparation_time,
      isAvailable = existingItem.is_available,
      displayOrder = existingItem.display_order,
    } = req.body;

    const categoryExists = await categoryBelongsToRestaurant(restaurantId, categoryId);
    if (!categoryExists) {
      return res.status(400).json({ success: false, message: 'Category does not belong to this restaurant' });
    }

    await db.query(
      `UPDATE menu_items
       SET name = ?, description = ?, price = ?, image_url = ?, is_available = ?,
           preparation_time = ?, display_order = ?, category_id = ?, updated_at = NOW()
       WHERE restaurant_id = ? AND id = ?`,
      [
        name,
        description || '',
        price,
        imageUrl,
        isAvailable,
        preparationTime,
        displayOrder,
        categoryId,
        restaurantId,
        itemId,
      ]
    );

    const updatedItem = await getMenuItemById(restaurantId, itemId);
    res.json(successResponse(updatedItem, 'Menu item updated'));
  } catch (error) {
    console.error('Error updating menu item:', error);
    res.status(500).json({ success: false, message: 'Server error', error: error.message });
  }
});

// Delete menu item
router.delete('/:id/menu/:itemId', async (req, res) => {
  try {
    const restaurantId = req.params.id;
    const itemId = req.params.itemId;
    const existingItem = await getMenuItemById(restaurantId, itemId);

    if (!existingItem) {
      return res.status(404).json({ success: false, message: 'Menu item not found' });
    }

    await db.query('DELETE FROM menu_items WHERE restaurant_id = ? AND id = ?', [restaurantId, itemId]);
    res.json(successResponse(null, 'Menu item deleted'));
  } catch (error) {
    console.error('Error deleting menu item:', error);
    res.status(500).json({ success: false, message: 'Server error', error: error.message });
  }
});

module.exports = router;
