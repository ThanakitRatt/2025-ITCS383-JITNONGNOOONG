const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');

const { loadModuleWithMocks } = require('../helpers/router-test-utils');

const ORIGINAL_ENV = { ...process.env };

function loadDbConfig({ env = {} } = {}) {
  process.env = {
    ...ORIGINAL_ENV,
    ...env,
  };

  const createPoolCalls = [];
  const mysqlMock = {
    createPool(config) {
      createPoolCalls.push(config);
      return { config };
    },
  };
  const { loadedModule, cleanup } = loadModuleWithMocks('../../src/config/db', {
    'mysql2/promise': mysqlMock,
  });

  return {
    pool: loadedModule,
    config: createPoolCalls[0],
    cleanup: () => {
      cleanup();
      process.env = { ...ORIGINAL_ENV };
    },
  };
}

test('db config uses local defaults when SSL is disabled', () => {
  const { config, cleanup } = loadDbConfig({
    env: {
      DB_HOST: '',
      DB_PORT: '',
      DB_USER: '',
      DB_PASSWORD: '',
      DB_NAME: '',
      DB_SSL: 'false',
    },
  });

  assert.equal(config.host, 'localhost');
  assert.equal(config.port, 3306);
  assert.equal(config.user, 'mharruengsang');
  assert.equal(config.password, 'mhar1234');
  assert.equal(config.database, 'mharruengsang');
  assert.equal(config.ssl, undefined);

  cleanup();
});

test('db config builds SSL settings from env certificate text', () => {
  const { config, cleanup } = loadDbConfig({
    env: {
      DB_HOST: 'cloud-host',
      DB_PORT: '11879',
      DB_USER: 'avnadmin',
      DB_PASSWORD: 'secret',
      DB_NAME: 'defaultdb',
      DB_SSL: 'true',
      DB_SSL_REJECT_UNAUTHORIZED: 'false',
      DB_SSL_CA: 'LINE1\\nLINE2',
    },
  });

  assert.equal(config.host, 'cloud-host');
  assert.equal(config.port, 11879);
  assert.equal(config.user, 'avnadmin');
  assert.equal(config.password, 'secret');
  assert.equal(config.database, 'defaultdb');
  assert.deepEqual(config.ssl, {
    rejectUnauthorized: false,
    ca: 'LINE1\nLINE2',
  });

  cleanup();
});

test('db config reads SSL certificate from a CA path when provided', () => {
  const tempCaPath = '/tmp/mharruengsang-test-ca.pem';
  fs.writeFileSync(tempCaPath, 'CA FILE CONTENT', 'utf8');

  const { config, cleanup } = loadDbConfig({
    env: {
      MYSQL_SSL: 'true',
      MYSQL_SSL_REJECT_UNAUTHORIZED: 'true',
      MYSQL_SSL_CA_PATH: tempCaPath,
    },
  });

  assert.deepEqual(config.ssl, {
    rejectUnauthorized: true,
    ca: 'CA FILE CONTENT',
  });

  fs.unlinkSync(tempCaPath);
  cleanup();
});
