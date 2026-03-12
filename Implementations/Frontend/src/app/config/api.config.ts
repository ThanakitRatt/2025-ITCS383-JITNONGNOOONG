/**
 * API Configuration
 * 
 * Central configuration for all API calls to the backend.
 * Uses environment variables for flexibility across environments.
 */

// API Base URL - defaults to localhost for development
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

// API Version
export const API_VERSION = 'v1';

// API Endpoints
export const API_ENDPOINTS = {
  // Authentication
  AUTH: {
    LOGIN: `/api/${API_VERSION}/auth/login`,
    REGISTER: `/api/${API_VERSION}/auth/register`,
    LOGOUT: `/api/${API_VERSION}/auth/logout`,
    REFRESH: `/api/${API_VERSION}/auth/refresh`,
    OTP: `/api/${API_VERSION}/auth/otp`,
  },
  
  // Restaurants
  RESTAURANTS: {
    BASE: `/api/${API_VERSION}/restaurants`,
    BY_ID: (id: string) => `/api/${API_VERSION}/restaurants/${id}`,
    SEARCH: `/api/${API_VERSION}/restaurants/search`,
    BY_CUISINE: (cuisine: string) => `/api/${API_VERSION}/restaurants/cuisine/${cuisine}`,
    TOP_RATED: `/api/${API_VERSION}/restaurants/top-rated`,
    BY_OWNER: (ownerId: string) => `/api/${API_VERSION}/restaurants/owner/${ownerId}`,
    STATUS: (id: string) => `/api/${API_VERSION}/restaurants/${id}/status`,
    AVAILABILITY: (id: string) => `/api/${API_VERSION}/restaurants/${id}/availability`,
    MENU: (restaurantId: string) => `/api/${API_VERSION}/restaurants/${restaurantId}/menu`,
    MENU_ITEM: (restaurantId: string, itemId: string) => 
      `/api/${API_VERSION}/restaurants/${restaurantId}/menu/${itemId}`,
    MENU_SEARCH: (restaurantId: string) => `/api/${API_VERSION}/restaurants/${restaurantId}/menu/search`,
    CATEGORIES: (restaurantId: string) => `/api/${API_VERSION}/restaurants/${restaurantId}/categories`,
    CATEGORY: (restaurantId: string, categoryId: string) => 
      `/api/${API_VERSION}/restaurants/${restaurantId}/categories/${categoryId}`,
  },
  
  // Orders
  ORDERS: {
    BASE: `/api/${API_VERSION}/orders`,
    BY_ID: (id: string) => `/api/${API_VERSION}/orders/${id}`,
    BY_NUMBER: (orderNumber: string) => `/api/${API_VERSION}/orders/number/${orderNumber}`,
    CUSTOMER: (customerId: string) => `/api/${API_VERSION}/orders/customer/${customerId}`,
    RESTAURANT: (restaurantId: string) => `/api/${API_VERSION}/orders/restaurant/${restaurantId}`,
    STATUS: (id: string) => `/api/${API_VERSION}/orders/${id}/status`,
    CANCEL: (id: string) => `/api/${API_VERSION}/orders/${id}`,
    ADMIN_STATS: `/api/${API_VERSION}/orders/admin/stats`,
    RIDER_AVAILABLE: `/api/${API_VERSION}/orders/rider/available`,
    RIDER: (riderId: string) => `/api/${API_VERSION}/orders/rider/${riderId}`,
  },
  
  // Payments
  PAYMENTS: {
    BASE: `/api/${API_VERSION}/payments`,
    PROCESS: `/api/${API_VERSION}/payments/process`,
    VERIFY: `/api/${API_VERSION}/payments/verify`,
    HISTORY: (customerId: string) => `/api/${API_VERSION}/payments/customer/${customerId}`,
  },
  
  // Riders
  RIDERS: {
    BASE: `/api/${API_VERSION}/riders`,
    AVAILABLE: `/api/${API_VERSION}/riders/available`,
    BY_ID: (id: string) => `/api/${API_VERSION}/riders/${id}`,
    LOCATION: (id: string) => `/api/${API_VERSION}/riders/${id}/location`,
  },
  
  // Customers
  CUSTOMERS: {
    BASE: `/api/${API_VERSION}/customers`,
    BY_ID: (id: string) => `/api/${API_VERSION}/customers/${id}`,
    PROFILE: `/api/${API_VERSION}/customers/profile`,
    ADDRESSES: `/api/${API_VERSION}/customers/addresses`,
    PAYMENT_METHODS: `/api/${API_VERSION}/customers/payment-methods`,
  },

  // Admin
  ADMIN: {
    ORDER_STATS: `/api/${API_VERSION}/orders/admin/stats`,
    RESTAURANT_STATS: `/api/${API_VERSION}/restaurants/stats`,
  },
};

// Request timeout (30 seconds)
export const REQUEST_TIMEOUT = 30000;

// Request retry configuration
export const RETRY_CONFIG = {
  retries: 3,
  retryDelay: 1000,
  retryOn: [408, 429, 500, 502, 503, 504],
};
