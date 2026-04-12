const test = require('node:test');
const assert = require('node:assert/strict');

const {
  loadModuleWithMocks,
  getRouteHandler,
  createMockResponse,
  createQueryMock,
} = require('../helpers/router-test-utils');

function loadRestaurantsRoute(querySequence = []) {
  const dbMock = { query: createQueryMock(querySequence) };
  const { loadedModule, cleanup } = loadModuleWithMocks('../../src/routes/restaurants', {
    '../config/db': dbMock,
  });

  return {
    router: loadedModule,
    dbMock,
    cleanup,
  };
}

test('GET /stats returns restaurant totals', async () => {
  const { router, cleanup } = loadRestaurantsRoute([
    [[{ totalRestaurants: '8' }]],
    [[{ activeRestaurants: '5' }]],
  ]);
  const handler = getRouteHandler(router, 'get', '/stats');
  const res = createMockResponse();

  await handler({}, res);

  assert.equal(res.statusCode, 200);
  assert.deepEqual(res.body.data, {
    totalRestaurants: 8,
    activeRestaurants: 5,
  });

  cleanup();
});

test('GET / returns paginated restaurants', async () => {
  const { router, cleanup } = loadRestaurantsRoute([
    [[{ id: 1, name: 'Thai Place' }, { id: 2, name: 'Burger House' }]],
  ]);
  const handler = getRouteHandler(router, 'get', '/');
  const res = createMockResponse();

  await handler({}, res);

  assert.equal(res.statusCode, 200);
  assert.equal(res.body.data.content.length, 2);
  assert.equal(res.body.data.totalElements, 2);

  cleanup();
});

test('GET /top-rated returns top-rated restaurants', async () => {
  const { router, cleanup } = loadRestaurantsRoute([
    [[{ id: 1, name: 'Top Spot', averageRating: 4.9 }]],
  ]);
  const handler = getRouteHandler(router, 'get', '/top-rated');
  const res = createMockResponse();

  await handler({}, res);

  assert.equal(res.statusCode, 200);
  assert.equal(res.body.data[0].name, 'Top Spot');

  cleanup();
});

test('GET /cuisine-types returns cuisine strings', async () => {
  const { router, cleanup } = loadRestaurantsRoute([
    [[{ cuisine_type: 'THAI' }, { cuisine_type: 'ITALIAN' }]],
  ]);
  const handler = getRouteHandler(router, 'get', '/cuisine-types');
  const res = createMockResponse();

  await handler({}, res);

  assert.deepEqual(res.body.data, ['THAI', 'ITALIAN']);

  cleanup();
});

test('GET /search filters by query and cuisine', async () => {
  const { router, dbMock, cleanup } = loadRestaurantsRoute([
    [[{ id: 3, name: 'Thai Search Result' }]],
  ]);
  const handler = getRouteHandler(router, 'get', '/search');
  const res = createMockResponse();

  await handler({
    query: {
      searchTerm: 'thai',
      cuisineType: 'THAI',
    },
  }, res);

  assert.equal(res.statusCode, 200);
  assert.equal(res.body.data.content.length, 1);
  assert.match(dbMock.query.calls[0][0], /name LIKE \? OR description LIKE \?/);
  assert.equal(dbMock.query.calls[0][1][2], 'THAI');

  cleanup();
});

test('GET /owner/:ownerId remaps mock owner ids and returns 404 when not found', async () => {
  const { router, dbMock, cleanup } = loadRestaurantsRoute([
    [[]],
  ]);
  const handler = getRouteHandler(router, 'get', '/owner/:ownerId');
  const res = createMockResponse();

  await handler({ params: { ownerId: '100' } }, res);

  assert.equal(res.statusCode, 404);
  assert.equal(res.body.message, 'Restaurant not found');
  assert.equal(dbMock.query.calls[0][1][0], 2);

  cleanup();
});

test('GET /:id returns a single restaurant', async () => {
  const { router, cleanup } = loadRestaurantsRoute([
    [[{ id: 9, name: 'One Restaurant' }]],
  ]);
  const handler = getRouteHandler(router, 'get', '/:id');
  const res = createMockResponse();

  await handler({ params: { id: '9' } }, res);

  assert.equal(res.statusCode, 200);
  assert.equal(res.body.data.id, 9);

  cleanup();
});

test('GET /:id/menu returns paginated menu items', async () => {
  const { router, cleanup } = loadRestaurantsRoute([
    [[{ id: 1, name: 'Pad Thai' }]],
  ]);
  const handler = getRouteHandler(router, 'get', '/:id/menu');
  const res = createMockResponse();

  await handler({ params: { id: '7' } }, res);

  assert.equal(res.statusCode, 200);
  assert.equal(res.body.data.totalElements, 1);

  cleanup();
});

test('GET /:id/menu/:itemId returns 404 when the menu item does not exist', async () => {
  const { router, cleanup } = loadRestaurantsRoute([
    [[]],
  ]);
  const handler = getRouteHandler(router, 'get', '/:id/menu/:itemId');
  const res = createMockResponse();

  await handler({ params: { id: '7', itemId: '999' } }, res);

  assert.equal(res.statusCode, 404);
  assert.equal(res.body.message, 'Menu item not found');

  cleanup();
});

test('GET /:id/categories returns categories', async () => {
  const { router, cleanup } = loadRestaurantsRoute([
    [[{ id: 1, name: 'Main Dishes' }]],
  ]);
  const handler = getRouteHandler(router, 'get', '/:id/categories');
  const res = createMockResponse();

  await handler({ params: { id: '5' } }, res);

  assert.equal(res.statusCode, 200);
  assert.equal(res.body.data.length, 1);

  cleanup();
});

test('GET /:id/reviews sorts reviews using the requested sort order', async () => {
  const { router, dbMock, cleanup } = loadRestaurantsRoute([
    [[{ id: 1, customerName: 'A', rating: 5 }]],
  ]);
  const handler = getRouteHandler(router, 'get', '/:id/reviews');
  const res = createMockResponse();

  await handler({ params: { id: '5' }, query: { sort: 'highest' } }, res);

  assert.equal(res.statusCode, 200);
  assert.equal(res.body.data.length, 1);
  assert.match(dbMock.query.calls[0][0], /ORDER BY rr\.rating DESC, rr\.created_at DESC/);

  cleanup();
});

test('POST /:id/reviews validates missing review fields', async () => {
  const { router, cleanup } = loadRestaurantsRoute();
  const handler = getRouteHandler(router, 'post', '/:id/reviews');
  const res = createMockResponse();

  await handler({ params: { id: '5' }, body: { orderId: '', customerId: '', rating: '' } }, res);

  assert.equal(res.statusCode, 400);
  assert.equal(res.body.message, 'Order, customer, and rating are required');

  cleanup();
});

test('POST /:id/reviews rejects out-of-range ratings', async () => {
  const { router, cleanup } = loadRestaurantsRoute();
  const handler = getRouteHandler(router, 'post', '/:id/reviews');
  const res = createMockResponse();

  await handler({ params: { id: '5' }, body: { orderId: 1, customerId: 2, rating: 7 } }, res);

  assert.equal(res.statusCode, 400);
  assert.equal(res.body.message, 'Rating must be between 1 and 5');

  cleanup();
});

test('POST /:id/reviews rejects orders that do not belong to the customer and restaurant', async () => {
  const { router, cleanup } = loadRestaurantsRoute([
    [[]],
  ]);
  const handler = getRouteHandler(router, 'post', '/:id/reviews');
  const res = createMockResponse();

  await handler({ params: { id: '5' }, body: { orderId: 1, customerId: 2, rating: 5 } }, res);

  assert.equal(res.statusCode, 403);
  assert.equal(res.body.message, 'This order cannot be used to review the restaurant');

  cleanup();
});

test('POST /:id/reviews rejects reviews before delivery completion', async () => {
  const { router, cleanup } = loadRestaurantsRoute([
    [[{ id: 1, restaurant_id: 5, customer_id: 2, status: 'PREPARING' }]],
  ]);
  const handler = getRouteHandler(router, 'post', '/:id/reviews');
  const res = createMockResponse();

  await handler({ params: { id: '5' }, body: { orderId: 1, customerId: 2, rating: 5 } }, res);

  assert.equal(res.statusCode, 400);
  assert.equal(res.body.message, 'You can only review restaurants after delivery is completed');

  cleanup();
});

test('POST /:id/reviews rejects duplicate reviews', async () => {
  const { router, cleanup } = loadRestaurantsRoute([
    [[{ id: 1, restaurant_id: 5, customer_id: 2, status: 'DELIVERED' }]],
    [[{ id: 11 }]],
  ]);
  const handler = getRouteHandler(router, 'post', '/:id/reviews');
  const res = createMockResponse();

  await handler({ params: { id: '5' }, body: { orderId: 1, customerId: 2, rating: 5 } }, res);

  assert.equal(res.statusCode, 400);
  assert.equal(res.body.message, 'A review has already been submitted for this order');

  cleanup();
});

test('POST /:id/reviews creates a review and refreshes restaurant metrics', async () => {
  const { router, dbMock, cleanup } = loadRestaurantsRoute([
    [[{ id: 1, restaurant_id: 5, customer_id: 2, status: 'DELIVERED' }]],
    [[]],
    [{ insertId: 77 }],
    [[{ averageRating: '4.5', totalReviews: '8' }]],
    [{ affectedRows: 1 }],
    [[{
      id: 77,
      restaurantId: 5,
      customerId: 2,
      orderId: 1,
      rating: 5,
      reviewText: 'Excellent',
      customerName: 'Test User',
    }]],
  ]);
  const handler = getRouteHandler(router, 'post', '/:id/reviews');
  const res = createMockResponse();

  await handler({
    params: { id: '5' },
    body: { orderId: 1, customerId: 2, rating: 5, reviewText: 'Excellent' },
  }, res);

  assert.equal(res.statusCode, 201);
  assert.equal(res.body.message, 'Review submitted successfully');
  assert.equal(res.body.data.id, 77);
  assert.equal(dbMock.query.calls.length, 6);

  cleanup();
});

test('POST /:id/menu validates required menu fields', async () => {
  const { router, cleanup } = loadRestaurantsRoute();
  const handler = getRouteHandler(router, 'post', '/:id/menu');
  const res = createMockResponse();

  await handler({ params: { id: '3' }, body: { name: '', price: null, categoryId: '' } }, res);

  assert.equal(res.statusCode, 400);
  assert.equal(res.body.message, 'Name, price, and category are required');

  cleanup();
});

test('POST /:id/menu rejects categories from another restaurant', async () => {
  const { router, cleanup } = loadRestaurantsRoute([
    [[]],
  ]);
  const handler = getRouteHandler(router, 'post', '/:id/menu');
  const res = createMockResponse();

  await handler({
    params: { id: '3' },
    body: { name: 'Pad Thai', price: 120, categoryId: 9 },
  }, res);

  assert.equal(res.statusCode, 400);
  assert.equal(res.body.message, 'Category does not belong to this restaurant');

  cleanup();
});

test('POST /:id/menu creates a menu item', async () => {
  const { router, cleanup } = loadRestaurantsRoute([
    [[{ id: 9 }]],
    [{ insertId: 21 }],
    [[{
      id: 21,
      restaurant_id: 3,
      name: 'New Dish',
      price: 150,
    }]],
  ]);
  const handler = getRouteHandler(router, 'post', '/:id/menu');
  const res = createMockResponse();

  await handler({
    params: { id: '3' },
    body: { name: 'New Dish', description: 'Nice', price: 150, categoryId: 9 },
  }, res);

  assert.equal(res.statusCode, 201);
  assert.equal(res.body.message, 'Menu item created');
  assert.equal(res.body.data.id, 21);

  cleanup();
});

test('PUT /:id/menu/:itemId returns 404 when editing a missing item', async () => {
  const { router, cleanup } = loadRestaurantsRoute([
    [[]],
  ]);
  const handler = getRouteHandler(router, 'put', '/:id/menu/:itemId');
  const res = createMockResponse();

  await handler({ params: { id: '3', itemId: '50' }, body: {} }, res);

  assert.equal(res.statusCode, 404);
  assert.equal(res.body.message, 'Menu item not found');

  cleanup();
});

test('PUT /:id/menu/:itemId rejects categories from another restaurant', async () => {
  const { router, cleanup } = loadRestaurantsRoute([
    [[{ id: 50, name: 'Old Dish', category_id: 9 }]],
    [[]],
  ]);
  const handler = getRouteHandler(router, 'put', '/:id/menu/:itemId');
  const res = createMockResponse();

  await handler({
    params: { id: '3', itemId: '50' },
    body: { categoryId: 88 },
  }, res);

  assert.equal(res.statusCode, 400);
  assert.equal(res.body.message, 'Category does not belong to this restaurant');

  cleanup();
});

test('PUT /:id/menu/:itemId updates a menu item', async () => {
  const { router, cleanup } = loadRestaurantsRoute([
    [[{
      id: 50,
      name: 'Old Dish',
      description: 'Old',
      price: 90,
      category_id: 9,
      image_url: null,
      preparation_time: 15,
      is_available: true,
      display_order: 0,
    }]],
    [[{ id: 9 }]],
    [{ affectedRows: 1 }],
    [[{
      id: 50,
      restaurant_id: 3,
      name: 'Updated Dish',
      price: 140,
    }]],
  ]);
  const handler = getRouteHandler(router, 'put', '/:id/menu/:itemId');
  const res = createMockResponse();

  await handler({
    params: { id: '3', itemId: '50' },
    body: { name: 'Updated Dish', price: 140, categoryId: 9 },
  }, res);

  assert.equal(res.statusCode, 200);
  assert.equal(res.body.message, 'Menu item updated');
  assert.equal(res.body.data.name, 'Updated Dish');

  cleanup();
});

test('DELETE /:id/menu/:itemId returns 404 for a missing item', async () => {
  const { router, cleanup } = loadRestaurantsRoute([
    [[]],
  ]);
  const handler = getRouteHandler(router, 'delete', '/:id/menu/:itemId');
  const res = createMockResponse();

  await handler({ params: { id: '3', itemId: '999' } }, res);

  assert.equal(res.statusCode, 404);
  assert.equal(res.body.message, 'Menu item not found');

  cleanup();
});

test('DELETE /:id/menu/:itemId deletes an existing item', async () => {
  const { router, cleanup } = loadRestaurantsRoute([
    [[{ id: 60, name: 'Delete Me' }]],
    [{ affectedRows: 1 }],
  ]);
  const handler = getRouteHandler(router, 'delete', '/:id/menu/:itemId');
  const res = createMockResponse();

  await handler({ params: { id: '3', itemId: '60' } }, res);

  assert.equal(res.statusCode, 200);
  assert.equal(res.body.message, 'Menu item deleted');

  cleanup();
});
