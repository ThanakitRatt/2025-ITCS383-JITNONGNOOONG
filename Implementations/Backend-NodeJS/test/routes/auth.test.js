const test = require('node:test');
const assert = require('node:assert/strict');

const {
  loadModuleWithMocks,
  getRouteHandler,
  createMockResponse,
  createQueryMock,
} = require('../helpers/router-test-utils');

function loadAuthRoute({ querySequence = [], bcryptMocks = {}, jwtMocks = {} } = {}) {
  const dbMock = { query: createQueryMock(querySequence) };
  const bcryptMock = {
    genSalt: async () => 'salt',
    hash: async () => 'hashed-password',
    compare: async () => false,
    ...bcryptMocks,
  };
  const jwtMock = {
    sign: () => 'signed-token',
    ...jwtMocks,
  };

  const { loadedModule, cleanup } = loadModuleWithMocks('../../src/routes/auth', {
    '../config/db': dbMock,
    'bcryptjs': bcryptMock,
    'jsonwebtoken': jwtMock,
  });

  return {
    router: loadedModule,
    dbMock,
    bcryptMock,
    jwtMock,
    cleanup,
  };
}

test('POST /register returns 400 when required fields are missing', async () => {
  const { router, cleanup } = loadAuthRoute();
  const handler = getRouteHandler(router, 'post', '/register');
  const res = createMockResponse();

  await handler({ body: { email: '', password: '', name: '' } }, res);

  assert.equal(res.statusCode, 400);
  assert.deepEqual(res.body, {
    success: false,
    message: 'Please provide name, email and password',
  });

  cleanup();
});

test('POST /register returns 400 when the user already exists', async () => {
  const { router, dbMock, cleanup } = loadAuthRoute({
    querySequence: [
      [[{ id: 1, email: 'taken@example.com' }]],
    ],
  });
  const handler = getRouteHandler(router, 'post', '/register');
  const res = createMockResponse();

  await handler({
    body: {
      name: 'Taken User',
      email: 'taken@example.com',
      password: 'secret123',
    },
  }, res);

  assert.equal(res.statusCode, 400);
  assert.equal(res.body.message, 'User already exists');
  assert.equal(dbMock.query.calls.length, 1);

  cleanup();
});

test('POST /register creates a new user and returns normalized data', async () => {
  const { router, dbMock, cleanup } = loadAuthRoute({
    querySequence: [
      [[]],
      [{ insertId: 42 }],
      [[{
        id: 42,
        name: 'New User',
        email: 'new@example.com',
        role: 'CUSTOMER',
        phone_number: '0812345678',
      }]],
    ],
  });
  const handler = getRouteHandler(router, 'post', '/register');
  const res = createMockResponse();

  await handler({
    body: {
      name: 'New User',
      email: 'new@example.com',
      password: 'secret123',
      phoneNumber: '0812345678',
    },
  }, res);

  assert.equal(res.statusCode, 201);
  assert.equal(res.body.success, true);
  assert.equal(res.body.data.user.id, 42);
  assert.equal(res.body.data.user.phoneNumber, '0812345678');
  assert.equal(dbMock.query.calls.length, 3);

  cleanup();
});

test('POST /login returns 400 when the user does not exist', async () => {
  const { router, cleanup } = loadAuthRoute({
    querySequence: [
      [[]],
    ],
  });
  const handler = getRouteHandler(router, 'post', '/login');
  const res = createMockResponse();

  await handler({ body: { email: 'missing@example.com', password: 'secret123' } }, res);

  assert.equal(res.statusCode, 400);
  assert.equal(res.body.message, 'Invalid credentials');

  cleanup();
});

test('POST /login accepts demo passwords even when bcrypt compare fails', async () => {
  const { router, cleanup } = loadAuthRoute({
    querySequence: [
      [[{
        id: 5,
        name: 'Demo User',
        email: 'demo@example.com',
        role: 'CUSTOMER',
        phone_number: '0899999999',
        password: 'stored-hash',
      }]],
    ],
    bcryptMocks: {
      compare: async () => false,
    },
  });
  const handler = getRouteHandler(router, 'post', '/login');
  const res = createMockResponse();

  await handler({ body: { email: 'demo@example.com', password: 'customer123' } }, res);

  assert.equal(res.statusCode, 200);
  assert.equal(res.body.success, true);
  assert.equal(res.body.data.user.email, 'demo@example.com');

  cleanup();
});

test('POST /login returns 400 when both bcrypt and demo password checks fail', async () => {
  const { router, cleanup } = loadAuthRoute({
    querySequence: [
      [[{
        id: 5,
        name: 'Real User',
        email: 'real@example.com',
        role: 'CUSTOMER',
        phone_number: '0899999999',
        password: 'stored-hash',
      }]],
    ],
    bcryptMocks: {
      compare: async () => false,
    },
  });
  const handler = getRouteHandler(router, 'post', '/login');
  const res = createMockResponse();

  await handler({ body: { email: 'real@example.com', password: 'wrong-password' } }, res);

  assert.equal(res.statusCode, 400);
  assert.equal(res.body.message, 'Invalid credentials');

  cleanup();
});

test('POST /otp returns 400 when the user cannot be found', async () => {
  const { router, cleanup } = loadAuthRoute({
    querySequence: [
      [[]],
    ],
  });
  const handler = getRouteHandler(router, 'post', '/otp');
  const res = createMockResponse();

  await handler({ body: { email: 'missing@example.com', otp: '123456' } }, res);

  assert.equal(res.statusCode, 400);
  assert.equal(res.body.message, 'User not found');

  cleanup();
});

test('POST /otp returns a signed token for an existing user', async () => {
  const { router, cleanup } = loadAuthRoute({
    querySequence: [
      [[{
        id: 7,
        name: 'OTP User',
        email: 'otp@example.com',
        role: 'CUSTOMER',
        phone_number: '0800000000',
      }]],
    ],
  });
  const handler = getRouteHandler(router, 'post', '/otp');
  const res = createMockResponse();

  await handler({ body: { email: 'otp@example.com', otp: '000000' } }, res);

  assert.equal(res.statusCode, 200);
  assert.equal(res.body.data.token, 'signed-token');
  assert.equal(res.body.data.refreshToken, 'signed-token');

  cleanup();
});

test('POST /otp returns 500 when the database query fails', async () => {
  const { router, cleanup } = loadAuthRoute({
    querySequence: [
      new Error('db down'),
    ],
  });
  const handler = getRouteHandler(router, 'post', '/otp');
  const res = createMockResponse();

  await handler({ body: { email: 'otp@example.com', otp: '000000' } }, res);

  assert.equal(res.statusCode, 500);
  assert.equal(res.body.message, 'Server error');

  cleanup();
});
