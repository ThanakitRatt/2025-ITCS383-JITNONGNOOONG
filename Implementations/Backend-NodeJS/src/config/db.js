require('dotenv').config();
const mysql = require('mysql2/promise');

const dbConfig = {
  host: process.env.DB_HOST || process.env.MYSQL_HOST || 'localhost',
  port: Number(process.env.DB_PORT || process.env.MYSQL_PORT || 3306),
  user: process.env.DB_USER || process.env.MYSQL_USER || 'mharruengsang',
  password: process.env.DB_PASSWORD || process.env.MYSQL_PASSWORD || 'mhar1234',
  database: process.env.DB_NAME || process.env.MYSQL_DATABASE || 'mharruengsang',
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