const test = require('node:test');
const assert = require('node:assert/strict');

const {
  loadModuleWithMocks,
  getRouteHandler,
  createMockResponse,
  createQueryMock,
} = require('../helpers/router-test-utils');

function loadOrdersRoute(querySequence = []) {
  const dbMock = { query: createQueryMock(querySequence) };
  const { loadedModule, cleanup } = loadModuleWithMocks('../../src/routes/orders', {
    '../config/db': dbMock,
  });

  return {
    router: loadedModule,
    dbMock,
    cleanup,
  };
}

test('GET /admin/stats returns aggregated counts and revenue', async () => {
  const { router, cleanup } = loadOrdersRoute([
    [[{ totalOrders: '12' }]],
    [[{ todayOrders: '4' }]],
    [[{ monthOrders: '9' }]],
    [[{ todayRevenue: '450.5' }]],
    [[{ monthRevenue: '1200' }]],
  ]);
  const handler = getRouteHandler(router, 'get', '/admin/stats');
  const res = createMockResponse();

  await handler({}, res);

  assert.equal(res.statusCode, 200);
  assert.deepEqual(res.body.data, {
    todayOrders: 4,
    monthOrders: 9,
    totalOrders: 12,
    todayRevenue: 450.5,
    monthRevenue: 1200,
  });

  cleanup();
});

test('GET / returns all orders in paginated form', async () => {
  const { router, cleanup } = loadOrdersRoute([
    [[{ id: 1 }, { id: 2 }]],
  ]);
  const handler = getRouteHandler(router, 'get', '/');
  const res = createMockResponse();

  await handler({}, res);

  assert.equal(res.statusCode, 200);
  assert.equal(res.body.data.totalElements, 2);

  cleanup();
});

test('GET / returns 500 when the orders query fails', async () => {
  const { router, cleanup } = loadOrdersRoute([
    new Error('orders unavailable'),
  ]);
  const handler = getRouteHandler(router, 'get', '/');
  const res = createMockResponse();

  await handler({}, res);

  assert.equal(res.statusCode, 500);
  assert.equal(res.body.message, 'Server error');

  cleanup();
});

test('POST / creates an order, remaps mock customer ids, and inserts items', async () => {
  const { router, dbMock, cleanup } = loadOrdersRoute([
    [{ insertId: 55 }],
    [{ affectedRows: 2 }],
  ]);
  const handler = getRouteHandler(router, 'post', '/');
  const res = createMockResponse();

  await handler({
    body: {
      customerId: '100',
      restaurantId: 9,
      deliveryAddress: '123 Test Street',
      orderItems: [
        { menuItemId: 1, menuItemName: 'Pad Thai', quantity: 2, unitPrice: 90 },
        { menuItemId: 2, menuItemName: 'Thai Tea', quantity: 1, unitPrice: 45 },
      ],
    },
  }, res);

  assert.equal(res.statusCode, 201);
  assert.equal(res.body.success, true);
  assert.equal(res.body.data.id, 55);
  assert.equal(dbMock.query.calls[0][1][1], 1);
  assert.equal(dbMock.query.calls[0][1][3], 225);
  assert.equal(dbMock.query.calls[1][1][0].length, 2);

  cleanup();
});

test('POST / returns 500 when order creation fails', async () => {
  const { router, cleanup } = loadOrdersRoute([
    new Error('insert failed'),
  ]);
  const handler = getRouteHandler(router, 'post', '/');
  const res = createMockResponse();

  await handler({
    body: {
      customerId: '1',
      restaurantId: 9,
      items: [{ id: 1, name: 'Pad Thai', quantity: 1, price: 90 }],
    },
  }, res);

  assert.equal(res.statusCode, 500);
  assert.equal(res.body.message, 'Server error');

  cleanup();
});

test('GET /customer/:customerId remaps frontend mock ids and attaches order items', async () => {
  const { router, dbMock, cleanup } = loadOrdersRoute([
    [[{
      id: 10,
      orderNumber: 'MR1001',
      customerId: 1,
      restaurantId: 2,
      status: 'PENDING',
      totalAmount: 120,
    }]],
    [[{
      id: 1,
      orderId: 10,
      menuItemName: 'Noodles',
      quantity: 1,
      totalPrice: 120,
    }]],
  ]);
  const handler = getRouteHandler(router, 'get', '/customer/:customerId');
  const res = createMockResponse();

  await handler({ params: { customerId: '100' } }, res);

  assert.equal(res.statusCode, 200);
  assert.equal(dbMock.query.calls[0][1][0], 1);
  assert.equal(res.body.data.content[0].orderItems.length, 1);
  assert.equal(res.body.data.content[0].order_items.length, 1);

  cleanup();
});

test('GET /restaurant/:restaurantId remaps mock restaurant ids and attaches order items', async () => {
  const { router, dbMock, cleanup } = loadOrdersRoute([
    [[{
      id: 20,
      orderNumber: 'MR2001',
      restaurantId: 1,
      status: 'CONFIRMED',
      totalAmount: 200,
    }]],
    [[{
      id: 3,
      orderId: 20,
      menuItemName: 'Fried Rice',
      quantity: 2,
      totalPrice: 200,
    }]],
  ]);
  const handler = getRouteHandler(router, 'get', '/restaurant/:restaurantId');
  const res = createMockResponse();

  await handler({ params: { restaurantId: '100' } }, res);

  assert.equal(res.statusCode, 200);
  assert.equal(dbMock.query.calls[0][1][0], 1);
  assert.equal(res.body.data.content[0].orderItems.length, 1);

  cleanup();
});

test('GET /restaurant/:restaurantId returns 500 when loading orders fails', async () => {
  const { router, cleanup } = loadOrdersRoute([
    new Error('restaurant orders unavailable'),
  ]);
  const handler = getRouteHandler(router, 'get', '/restaurant/:restaurantId');
  const res = createMockResponse();

  await handler({ params: { restaurantId: '2' } }, res);

  assert.equal(res.statusCode, 500);
  assert.equal(res.body.message, 'Server error');

  cleanup();
});

test('GET /rider/available returns available rider orders with items', async () => {
  const { router, cleanup } = loadOrdersRoute([
    [[{ id: 31, restaurant_name: 'Thai Place' }]],
    [[{ id: 1, orderId: 31, menu_item_name: 'Soup' }]],
  ]);
  const handler = getRouteHandler(router, 'get', '/rider/available');
  const res = createMockResponse();

  await handler({}, res);

  assert.equal(res.statusCode, 200);
  assert.equal(res.body.data.content[0].orderItems.length, 1);

  cleanup();
});

test('GET /rider/available returns 500 when available-order loading fails', async () => {
  const { router, cleanup } = loadOrdersRoute([
    new Error('available orders unavailable'),
  ]);
  const handler = getRouteHandler(router, 'get', '/rider/available');
  const res = createMockResponse();

  await handler({}, res);

  assert.equal(res.statusCode, 500);
  assert.equal(res.body.message, 'Server error');

  cleanup();
});

test('GET /rider/:riderId returns assigned rider orders with items', async () => {
  const { router, cleanup } = loadOrdersRoute([
    [[{ id: 41, rider_id: 12, restaurant_name: 'Thai Place' }]],
    [[{ id: 1, orderId: 41, menu_item_name: 'Soup' }]],
  ]);
  const handler = getRouteHandler(router, 'get', '/rider/:riderId');
  const res = createMockResponse();

  await handler({ params: { riderId: '12' } }, res);

  assert.equal(res.statusCode, 200);
  assert.equal(res.body.data.content[0].orderItems.length, 1);

  cleanup();
});

test('GET /:id returns 404 when the order does not exist', async () => {
  const { router, cleanup } = loadOrdersRoute([
    [[]],
  ]);
  const handler = getRouteHandler(router, 'get', '/:id');
  const res = createMockResponse();

  await handler({ params: { id: '999' } }, res);

  assert.equal(res.statusCode, 404);
  assert.equal(res.body.message, 'Order not found');

  cleanup();
});

test('GET /:id returns a single order with items', async () => {
  const { router, cleanup } = loadOrdersRoute([
    [[{
      id: 77,
      orderNumber: 'MR777',
      customerId: 1,
      restaurantId: 2,
      status: 'DELIVERED',
      totalAmount: 300,
    }]],
    [[{
      id: 5,
      orderId: 77,
      menuItemName: 'Pad Thai',
      quantity: 2,
      totalPrice: 300,
    }]],
  ]);
  const handler = getRouteHandler(router, 'get', '/:id');
  const res = createMockResponse();

  await handler({ params: { id: '77' } }, res);

  assert.equal(res.statusCode, 200);
  assert.equal(res.body.data.orderItems.length, 1);
  assert.equal(res.body.data.items.length, 1);

  cleanup();
});

test('GET /:id returns 500 when order lookup fails', async () => {
  const { router, cleanup } = loadOrdersRoute([
    new Error('lookup failed'),
  ]);
  const handler = getRouteHandler(router, 'get', '/:id');
  const res = createMockResponse();

  await handler({ params: { id: '77' } }, res);

  assert.equal(res.statusCode, 500);
  assert.equal(res.body.message, 'Server error');

  cleanup();
});

test('PUT /:id/status assigns the rider when status changes to PICKED_UP', async () => {
  const { router, dbMock, cleanup } = loadOrdersRoute([
    [{ affectedRows: 1 }],
    [[{
      id: 8,
      orderNumber: 'MR2001',
      restaurantId: 4,
      riderId: 12,
      status: 'PICKED_UP',
      totalAmount: 180,
    }]],
    [[{
      id: 3,
      orderId: 8,
      menuItemName: 'Burger',
      quantity: 2,
      totalPrice: 180,
    }]],
  ]);
  const handler = getRouteHandler(router, 'put', '/:id/status');
  const res = createMockResponse();

  await handler({
    params: { id: '8' },
    body: { newStatus: 'PICKED_UP', updatedBy: 12, notes: 'picked up now' },
  }, res);

  assert.equal(res.statusCode, 200);
  assert.equal(dbMock.query.calls[0][1][0], 'PICKED_UP');
  assert.equal(dbMock.query.calls[0][1][1], 12);
  assert.equal(res.body.message, 'Order status updated');
  assert.equal(res.body.data.orderItems.length, 1);

  cleanup();
});

test('PUT /:id/status updates a non-picked-up status without assigning a rider', async () => {
  const { router, dbMock, cleanup } = loadOrdersRoute([
    [{ affectedRows: 1 }],
    [[{
      id: 8,
      orderNumber: 'MR2002',
      restaurantId: 4,
      riderId: null,
      status: 'CONFIRMED',
      totalAmount: 180,
    }]],
    [[{
      id: 3,
      orderId: 8,
      menuItemName: 'Burger',
      quantity: 2,
      totalPrice: 180,
    }]],
  ]);
  const handler = getRouteHandler(router, 'put', '/:id/status');
  const res = createMockResponse();

  await handler({
    params: { id: '8' },
    body: { newStatus: 'CONFIRMED', updatedBy: 12 },
  }, res);

  assert.equal(res.statusCode, 200);
  assert.equal(dbMock.query.calls[0][1].length, 2);
  assert.equal(res.body.data.status, 'CONFIRMED');

  cleanup();
});

test('PUT /:id/status returns 404 when the updated order cannot be found', async () => {
  const { router, cleanup } = loadOrdersRoute([
    [{ affectedRows: 1 }],
    [[]],
  ]);
  const handler = getRouteHandler(router, 'put', '/:id/status');
  const res = createMockResponse();

  await handler({
    params: { id: '8' },
    body: { newStatus: 'CONFIRMED', updatedBy: 12 },
  }, res);

  assert.equal(res.statusCode, 404);
  assert.equal(res.body.message, 'Order not found');

  cleanup();
});

test('PUT /:id/status returns 500 when the status update fails', async () => {
  const { router, cleanup } = loadOrdersRoute([
    new Error('status update failed'),
  ]);
  const handler = getRouteHandler(router, 'put', '/:id/status');
  const res = createMockResponse();

  await handler({
    params: { id: '8' },
    body: { newStatus: 'PICKED_UP', updatedBy: 12 },
  }, res);

  assert.equal(res.statusCode, 500);
  assert.equal(res.body.message, 'Server error');

  cleanup();
});

test('DELETE /:id cancels an order', async () => {
  const { router, cleanup } = loadOrdersRoute([
    [{ affectedRows: 1 }],
  ]);
  const handler = getRouteHandler(router, 'delete', '/:id');
  const res = createMockResponse();

  await handler({ params: { id: '44' } }, res);

  assert.equal(res.statusCode, 200);
  assert.equal(res.body.message, 'Order cancelled');

  cleanup();
});

test('DELETE /:id returns 500 when cancellation fails', async () => {
  const { router, cleanup } = loadOrdersRoute([
    new Error('cancel failed'),
  ]);
  const handler = getRouteHandler(router, 'delete', '/:id');
  const res = createMockResponse();

  await handler({ params: { id: '44' } }, res);

  assert.equal(res.statusCode, 500);
  assert.equal(res.body.message, 'Server error');

  cleanup();
});
