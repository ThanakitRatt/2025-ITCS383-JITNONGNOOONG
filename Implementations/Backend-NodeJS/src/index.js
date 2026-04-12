require('dotenv').config();
const express = require('express');
const cors = require('cors');

const app = express();

// Middleware
app.use(cors({
  origin: ['http://localhost:5173', 'http://127.0.0.1:5173'],
  credentials: true
}));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Global response interceptor to convert snake_case to camelCase
const isObject = (obj) => obj !== null && typeof obj === 'object' && !(obj instanceof Date);
const toCamelCaseStr = (str) => str.replace(/_([a-z])/g, (g) => g[1].toUpperCase());

const convertKeysToCamelCase = (obj) => {
  if (Array.isArray(obj)) {
    return obj.map(v => convertKeysToCamelCase(v));
  } else if (isObject(obj)) {
    return Object.keys(obj).reduce((result, key) => {
      const camelKey = toCamelCaseStr(key);
      result[camelKey] = convertKeysToCamelCase(obj[key]);
      return result;
    }, {});
  }
  return obj;
};

app.use((req, res, next) => {
  const originalJson = res.json;
  res.json = function (obj) {
    originalJson.call(this, convertKeysToCamelCase(obj));
  };
  next();
});

// Basic Route
app.get('/', (req, res) => {
  res.json({ message: 'Welcome to MharRuengSang Node.js API' });
});

// Import Routes
const authRoutes = require('./routes/auth');
const restaurantRoutes = require('./routes/restaurants');
const orderRoutes = require('./routes/orders');
const riderRoutes = require('./routes/riders');
const paymentRoutes = require('./routes/payments');
const customerRoutes = require('./routes/customers');

// Use Routes matching Frontend v1 expectations
app.use('/api/v1/auth', authRoutes);
app.use('/api/v1/restaurants', restaurantRoutes);
app.use('/api/v1/orders', orderRoutes);
app.use('/api/v1/riders', riderRoutes);
app.use('/api/v1/payments', paymentRoutes);
app.use('/api/v1/customers', customerRoutes);

const PORT = process.env.PORT || 8080;

app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}.`);
});