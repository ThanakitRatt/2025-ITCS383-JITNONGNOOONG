require('dotenv').config();
const mysql = require('mysql2/promise');
const fs = require('node:fs');

const parseBoolean = (value, defaultValue = false) => {
  if (value === undefined) {
    return defaultValue;
  }

  return ['true', '1', 'yes', 'on'].includes(String(value).toLowerCase());
};

const buildSslConfig = () => {
  const sslEnabled = parseBoolean(process.env.DB_SSL || process.env.MYSQL_SSL, false);

  if (!sslEnabled) {
    return undefined;
  }

  const caFromEnv = process.env.DB_SSL_CA || process.env.MYSQL_SSL_CA;
  const caPath = process.env.DB_SSL_CA_PATH || process.env.MYSQL_SSL_CA_PATH;

  return {
    rejectUnauthorized: parseBoolean(
      process.env.DB_SSL_REJECT_UNAUTHORIZED || process.env.MYSQL_SSL_REJECT_UNAUTHORIZED,
      true
    ),
    ...(caFromEnv ? { ca: caFromEnv.replaceAll(String.raw`\n`, '\n') } : {}),
    ...(caPath ? { ca: fs.readFileSync(caPath, 'utf8') } : {}),
  };
};

const dbConfig = {
  host: process.env.DB_HOST || process.env.MYSQL_HOST || 'localhost',
  port: Number(process.env.DB_PORT || process.env.MYSQL_PORT || 3306),
  user: process.env.DB_USER || process.env.MYSQL_USER || 'mharruengsang',
  password: process.env.DB_PASSWORD || process.env.MYSQL_PASSWORD || 'mhar1234',
  database: process.env.DB_NAME || process.env.MYSQL_DATABASE || 'mharruengsang',
  ssl: buildSslConfig(),
};

// Create the connection pool
const pool = mysql.createPool({
  ...dbConfig,
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0,
  typeCast: function (field, next) {
    if (field.type === 'TINY' && field.length === 1) {
      return (field.string() === '1'); // 1 = true, 0 = false
    }
    if (field.type === 'DECIMAL' || field.type === 'NEWDECIMAL') {
      return parseFloat(field.string()); // convert decimal to Number instead of String
    }
    return next();
  }
});

module.exports = pool;
