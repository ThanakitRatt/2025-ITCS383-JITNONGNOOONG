const express = require('express');
const router = express.Router();
const db = require('../config/db');

const successResponse = (data, message = 'Success') => ({
  success: true,
  message,
  data
});

const serverErrorResponse = (res, error, contextMessage) => {
  if (contextMessage) {
    console.error(contextMessage, error);
  }

  return res.status(500).json({ success: false, message: 'Server error', error: error.message });
};

const paginatedResponse = (rows) => successResponse({
  content: rows,
  totalElements: rows.length,
  totalPages: 1,
  size: rows.length,
  number: 0,
  first: true,
  last: true
});

const baseOrderSelect = `
  SELECT
    o.id,
    o.order_number AS orderNumber,
    o.customer_id AS customerId,
    o.restaurant_id AS restaurantId,
    o.rider_id AS riderId,
    o.status,
    o.total_amount AS totalAmount,
    o.delivery_fee AS deliveryFee,
    o.delivery_address AS deliveryAddress,
    o.delivery_latitude AS deliveryLatitude,
    o.delivery_longitude AS deliveryLongitude,
    o.special_instructions AS specialInstructions,
    o.estimated_delivery_time AS estimatedDeliveryTime,
    o.actual_delivery_time AS actualDeliveryTime,
    o.created_at AS createdAt,
    o.updated_at AS updatedAt,
    r.name AS restaurantName,
    rr.id AS restaurantReviewId,
    rr.rating AS restaurantRating,
    rr.review_text AS restaurantReviewText,
    rr.created_at AS restaurantReviewedAt
  FROM orders o
  LEFT JOIN restaurants r ON o.restaurant_id = r.id
  LEFT JOIN restaurant_reviews rr ON rr.order_id = o.id
`;

const baseOrderItemsSelect = `
  SELECT
    oi.id,
    oi.order_id AS orderId,
    oi.menu_item_id AS menuItemId,
    COALESCE(mi.name, oi.menu_item_name) AS menuItemName,
    oi.quantity,
    oi.unit_price AS unitPrice,
    oi.total_price AS totalPrice,
    oi.special_instructions AS specialRequests
  FROM order_items oi
  LEFT JOIN menu_items mi ON oi.menu_item_id = mi.id
  WHERE oi.order_id = ?
`;

const riderOrderItemsSelect = `
  SELECT oi.*, mi.name as menu_item_name 
  FROM order_items oi 
  LEFT JOIN menu_items mi ON oi.menu_item_id = mi.id 
  WHERE oi.order_id = ?
`;

const attachOrderItems = async (rows, queryText = baseOrderItemsSelect, includeSnakeCase = true) => {
  for (const row of rows) {
    const [items] = await db.query(queryText, [row.id]);
    if (includeSnakeCase) {
      row.order_items = items;
    }
    row.orderItems = items;
  }

  return rows;
};

const singleOrderSelect = `
  SELECT
    o.id,
    o.order_number AS orderNumber,
    o.customer_id AS customerId,
    o.restaurant_id AS restaurantId,
    o.rider_id AS riderId,
    o.status,
    o.total_amount AS totalAmount,
    o.delivery_fee AS deliveryFee,
    o.delivery_address AS deliveryAddress,
    o.delivery_latitude AS deliveryLatitude,
    o.delivery_longitude AS deliveryLongitude,
    o.special_instructions AS specialInstructions,
    o.estimated_delivery_time AS estimatedDeliveryTime,
    o.actual_delivery_time AS actualDeliveryTime,
    o.created_at AS createdAt,
    o.updated_at AS updatedAt,
    r.name AS restaurantName,
    rr.id AS restaurantReviewId,
    rr.rating AS restaurantRating,
    rr.review_text AS restaurantReviewText,
    rr.created_at AS restaurantReviewedAt
  FROM orders o
  LEFT JOIN restaurants r ON o.restaurant_id = r.id
  LEFT JOIN restaurant_reviews rr ON rr.order_id = o.id
`;

const singleOrderWithCustomerSelect = `
  SELECT
    order_view.*,
    u.name AS customerName,
    u.phone_number AS customerPhoneNumber
  FROM (${singleOrderSelect}) order_view
  LEFT JOIN users u ON order_view.customerId = u.id
  WHERE order_view.id = ?
`;

const singleOrderSelectById = `
  ${singleOrderSelect}
  WHERE o.id = ?
`;

// Admin Stats
router.get('/admin/stats', async (req, res) => {
  try {
    const [[{ totalOrders }]] = await db.query('SELECT COUNT(*) as totalOrders FROM orders');
    const [[{ todayOrders }]] = await db.query('SELECT COUNT(*) as todayOrders FROM orders WHERE DATE(created_at) = CURDATE()');
    const [[{ monthOrders }]] = await db.query('SELECT COUNT(*) as monthOrders FROM orders WHERE MONTH(created_at) = MONTH(CURDATE()) AND YEAR(created_at) = YEAR(CURDATE())');
    
    // Revenue is calculated from delivered orders (assuming we calculate revenue basically based on total_amount)
    const [[{ todayRevenue }]] = await db.query(`SELECT COALESCE(SUM(total_amount), 0) as todayRevenue FROM orders WHERE DATE(created_at) = CURDATE() AND status = 'DELIVERED'`);
    const [[{ monthRevenue }]] = await db.query(`SELECT COALESCE(SUM(total_amount), 0) as monthRevenue FROM orders WHERE MONTH(created_at) = MONTH(CURDATE()) AND YEAR(created_at) = YEAR(CURDATE()) AND status = 'DELIVERED'`);

    res.json(successResponse({
      todayOrders: Number(todayOrders),
      monthOrders: Number(monthOrders),
      totalOrders: Number(totalOrders),
      todayRevenue: Number(todayRevenue),
      monthRevenue: Number(monthRevenue)
    }));
  } catch (error) {
    serverErrorResponse(res, error, 'Error fetching admin stats:');
  }
});

// Get all orders (paginated format expected)
router.get('/', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM orders');
    res.json(paginatedResponse(rows));
  } catch (error) {
    serverErrorResponse(res, error);
  }
});

// Create new order
router.post('/', async (req, res) => {
  try {
    const { customerId, restaurantId, orderItems, items, totalAmount, deliveryAddress } = req.body;
    
    // The frontend sends camelCase, map accordingly, and extract nested tokens.
    let cid = parseInt(customerId || 1, 10);
    // Silent catch for lingering mock sessions from frontend localStorage
    if (cid === 100) cid = 1;
    if (cid === 101) cid = 5;
    if (cid === 200) cid = 3;
    if (cid === 300) cid = 4;
    
    const rid = restaurantId || req.body.restaurant_id;
    
    const itemsList = orderItems || items || req.body.items || [];
    // Compute total amount if not explicitly passed
    const amount = totalAmount || req.body.total_amount || itemsList.reduce((sum, item) => sum + (item.quantity * (item.unitPrice || item.price || 0)), 0);

    const address = deliveryAddress || req.body.delivery_address || '';

    // Insert order header
    const [result] = await db.query(
      'INSERT INTO orders (order_number, customer_id, restaurant_id, total_amount, delivery_fee, status, delivery_address, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())',
      [`MR${Date.now()}`, cid, rid, amount, 35.00, 'PENDING', address]
    );

    const newOrderId = result.insertId;
    
    // Insert items if they exist
    if (itemsList && itemsList.length > 0) {
      const itemsData = itemsList.map(item => [
        newOrderId,
        item.menuItemId || item.menu_item_id || item.id,
        item.menuItemName || item.menu_item_name || item.name,
        item.quantity,
        item.unitPrice || item.unit_price || item.price,
        (item.quantity * (item.unitPrice || item.unit_price || item.price))
      ]);

      await db.query(
        'INSERT INTO order_items (order_id, menu_item_id, menu_item_name, quantity, unit_price, total_price) VALUES ?',
        [itemsData]
      );
    }
    
    res.status(201).json(successResponse({
      id: newOrderId,
      orderNumber: `MR${Date.now()}`,
      status: 'PENDING',
      message: 'Order created'
    }));
  } catch (error) {
    serverErrorResponse(res, error);
  }
});

// Get order by user mapping
// Get orders by restaurant ID
router.get('/restaurant/:restaurantId', async (req, res) => {
  try {
    const targetRestaurantId = req.params.restaurantId === "100" ? 1 : req.params.restaurantId;
    
    const [rows] = await db.query(
      `${baseOrderSelect}
       WHERE o.restaurant_id = ?
       ORDER BY o.created_at DESC`,
      [targetRestaurantId]
    );

    await attachOrderItems(rows);
    res.json(paginatedResponse(rows));
  } catch (error) {
    serverErrorResponse(res, error);
  }
});

router.get('/customer/:customerId', async (req, res) => {
  try {
    const [rows] = await db.query(
      `${baseOrderSelect}
       WHERE o.customer_id = ?
       ORDER BY o.created_at DESC`,
      [req.params.customerId === "100" ? 1 : req.params.customerId]
    );

    await attachOrderItems(rows);
    res.json(paginatedResponse(rows));
  } catch (error) {
    serverErrorResponse(res, error);
  }
});

// Get available orders for riders
router.get('/rider/available', async (req, res) => {
  try {
    const [rows] = await db.query(`
      SELECT o.*, r.name as restaurant_name 
      FROM orders o 
      LEFT JOIN restaurants r ON o.restaurant_id = r.id 
      WHERE o.status = 'READY_FOR_PICKUP' AND o.rider_id IS NULL
      ORDER BY o.created_at DESC
    `);

    await attachOrderItems(rows, riderOrderItemsSelect, false);
    res.json(paginatedResponse(rows));
  } catch (error) {
    serverErrorResponse(res, error, 'Error fetching rider available orders:');
  }
});

// Get orders assigned to a specific rider
router.get('/rider/:riderId', async (req, res) => {
  try {
    const riderId = req.params.riderId;
    const [rows] = await db.query(`
      SELECT o.*, r.name as restaurant_name 
      FROM orders o 
      LEFT JOIN restaurants r ON o.restaurant_id = r.id 
      WHERE o.rider_id = ?
      ORDER BY o.created_at DESC
    `, [riderId]);

    await attachOrderItems(rows, riderOrderItemsSelect, false);
    res.json(paginatedResponse(rows));
  } catch (error) {
    serverErrorResponse(res, error, 'Error fetching rider orders:');
  }
});

// Get single order by ID
router.get('/:id', async (req, res) => {
  try {
    const [orders] = await db.query(singleOrderWithCustomerSelect, [req.params.id]);
      
    if (orders.length === 0) return res.status(404).json({ success: false, message: 'Order not found' });
    
    const [items] = await db.query(baseOrderItemsSelect, [req.params.id]);
    
    res.json(successResponse({ ...orders[0], items, orderItems: items }));
  } catch (error) {
    serverErrorResponse(res, error);
  }
});

// Cancel Order
router.delete('/:id', async (req, res) => {
  try {
    await db.query('UPDATE orders SET status = "CANCELLED" WHERE id = ?', [req.params.id]);
    res.json(successResponse(null, 'Order cancelled'));
  } catch (error) {
    serverErrorResponse(res, error);
  }
});

// Update Order Status
router.put('/:id/status', async (req, res) => {
  try {
    const { newStatus, updatedBy, notes } = req.body;
    
    // If the status is changing to PICKED_UP, assume the updatedBy is the rider
    if (newStatus === 'PICKED_UP') {
      await db.query('UPDATE orders SET status = ?, rider_id = ? WHERE id = ?', [newStatus, updatedBy, req.params.id]);
    } else {
      await db.query('UPDATE orders SET status = ? WHERE id = ?', [newStatus, req.params.id]);
    }
    
    // Fetch the updated order back
    const [orders] = await db.query(singleOrderSelectById, [req.params.id]);
      
    if (orders.length === 0) return res.status(404).json({ success: false, message: 'Order not found' });
    
    const [items] = await db.query(baseOrderItemsSelect, [req.params.id]);
    
    // Assign items and normalize casing to match React frontend models 
    orders[0].orderItems = items;
    res.json(successResponse(orders[0], 'Order status updated'));
  } catch (error) {
    serverErrorResponse(res, error, 'Error updating order:');
  }
});

module.exports = router;
