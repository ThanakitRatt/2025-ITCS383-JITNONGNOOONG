# API Reference - MharRuengSang Backend

**Base URL:** `http://localhost:8080/api`

---

## 1. PAYMENT ENDPOINTS

### 1.1 Initiate Payment
**POST** `/payments/initiate`

**Description:** Initiate payment for an order. Creates payment record and processes based on method.

**Request Body:**
```json
{
  "orderId": 123,
  "amount": 500.50,
  "paymentMethod": "CREDIT_CARD|PROMPTPAY|BANK_TRANSFER",
  "provider": "STRIPE|OMISE",
  "cardNumber": "4242424242424242",
  "cardholderName": "John Doe",
  "expiryMonth": "12",
  "expiryYear": "25",
  "cvc": "123"
}
```

**Response (Success - Credit Card):**
```json
{
  "success": true,
  "message": "Payment initiated successfully",
  "data": {
    "id": 456,
    "orderId": 123,
    "customerId": 789,
    "amount": 500.50,
    "paymentMethod": "CREDIT_CARD",
    "status": "VERIFIED",
    "provider": "STRIPE",
    "transactionId": "ch_1234567890abc",
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

**Response (Success - PromptPay):**
```json
{
  "success": true,
  "message": "Payment initiated successfully",
  "data": {
    "id": 456,
    "status": "PROCESSING",
    "qrCodeData": "<base64_png_image_string>",
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

**HTTP Status:** 200 OK / 400 Bad Request

---

### 1.2 Verify Payment Status
**GET** `/payments/{paymentId}/verify`

**Description:** Check payment status with payment gateway.

**Path Parameters:**
- `paymentId` (Long): Payment ID

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 456,
    "orderId": 123,
    "status": "COMPLETED|PENDING|FAILED",
    "verifiedAt": "2024-01-15T10:35:00"
  }
}
```

**HTTP Status:** 200 OK

---

### 1.3 Generate PromptPay QR
**POST** `/payments/{orderId}/promptpay`

**Description:** Generate PromptPay QR code for payment.

**Path Parameters:**
- `orderId` (Long): Order ID

**Query Parameters:**
- `restaurantPhone` (String): Restaurant's phone number

**Response:**
```json
{
  "success": true,
  "data": {
    "qrCodeData": "data:image/png;base64,iVBORw0KGgoAAAANS...",
    "status": "PROCESSING"
  }
}
```

**HTTP Status:** 200 OK

---

### 1.4 Get Payment Details
**GET** `/payments/{paymentId}`

**Description:** Retrieve payment record details.

**Path Parameters:**
- `paymentId` (Long): Payment ID

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 456,
    "orderId": 123,
    "customerId": 789,
    "amount": 500.50,
    "paymentMethod": "CREDIT_CARD",
    "status": "COMPLETED",
    "provider": "STRIPE",
    "transactionId": "ch_1234567890abc",
    "createdAt": "2024-01-15T10:30:00",
    "verifiedAt": "2024-01-15T10:35:00"
  }
}
```

**HTTP Status:** 200 OK / 404 Not Found

---

### 1.5 Refund Payment
**POST** `/payments/{paymentId}/refund`

**Description:** Refund a completed payment.

**Path Parameters:**
- `paymentId` (Long): Payment ID

**Response:**
```json
{
  "success": true,
  "message": "Refund processed",
  "data": {
    "id": 456,
    "status": "REFUNDED"
  }
}
```

**HTTP Status:** 200 OK / 400 Bad Request

---

### 1.6 Payment Webhook Callback
**POST** `/payments/callback`

**Description:** Webhook endpoint for payment gateway callbacks (Stripe, Omise).

**Query Parameters:**
- `transactionId` (String): Transaction ID from gateway
- `status` (String): 'success', 'failed', 'pending'

**Response:**
```json
{
  "success": true,
  "message": "Callback processed"
}
```

**HTTP Status:** 200 OK

---

## 2. RIDER ENDPOINTS

### 2.1 Update Rider Location
**POST** `/riders/{riderId}/location`

**Description:** Update rider's GPS location (real-time tracking).

**Path Parameters:**
- `riderId` (Long): Rider ID

**Query Parameters:**
- `latitude` (Double): GPS latitude
- `longitude` (Double): GPS longitude
- `status` (String): 'AVAILABLE|ON_DELIVERY|OFFLINE|ON_BREAK'

**Response:**
```json
{
  "success": true,
  "data": {
    "riderId": 789,
    "latitude": 13.7563,
    "longitude": 100.5018,
    "status": "ON_DELIVERY"
  }
}
```

**HTTP Status:** 200 OK

---

### 2.2 Get Rider Location
**GET** `/riders/{riderId}/location`

**Description:** Get rider's current GPS location.

**Path Parameters:**
- `riderId` (Long): Rider ID

**Response:**
```json
{
  "success": true,
  "data": {
    "riderId": 789,
    "latitude": 13.7563,
    "longitude": 100.5018,
    "status": "ON_DELIVERY"
  }
}
```

**HTTP Status:** 200 OK

---

### 2.3 Accept Order
**POST** `/riders/{riderId}/accept-order/{orderId}`

**Description:** Rider accepts an available order for delivery.

**Path Parameters:**
- `riderId` (Long): Rider ID
- `orderId` (Long): Order ID

**Response:**
```json
{
  "success": true,
  "message": "Order accepted successfully"
}
```

**HTTP Status:** 200 OK

---

### 2.4 Confirm Delivery
**POST** `/riders/{riderId}/confirm-delivery/{orderId}`

**Description:** Rider confirms delivery completion. Order becomes available for customer rating.

**Path Parameters:**
- `riderId` (Long): Rider ID
- `orderId` (Long): Order ID

**Response:**
```json
{
  "success": true,
  "message": "Delivery confirmed"
}
```

**HTTP Status:** 200 OK

---

## 3. PROMOTION ENDPOINTS

### 3.1 Create Promotion
**POST** `/promotions`

**Description:** Create a new promotional discount (for restaurants).

**Request Body:**
```json
{
  "restaurantId": 789,
  "code": "SUMMER20",
  "promotionName": "Summer Discount",
  "description": "20% off all items",
  "type": "PERCENTAGE|FIXED_AMOUNT|FREE_DELIVERY",
  "discountValue": 20,
  "minimumOrderAmount": 100,
  "maximumDiscountAmount": 50,
  "usageLimit": 1000,
  "userUsageLimit": 3,
  "validFrom": "2024-01-01T00:00:00",
  "validUntil": "2024-12-31T23:59:59"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Promotion created",
  "data": {
    "id": 111,
    "code": "SUMMER20",
    "type": "PERCENTAGE",
    "discountValue": 20,
    "active": true
  }
}
```

**HTTP Status:** 200 OK

---

### 3.2 Get Promotion by Code
**GET** `/promotions/code/{code}`

**Description:** Get promotion details by code (used during checkout).

**Path Parameters:**
- `code` (String): Promotion code

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 111,
    "code": "SUMMER20",
    "promotionName": "Summer Discount",
    "type": "PERCENTAGE",
    "discountValue": 20,
    "minimumOrderAmount": 100,
    "active": true
  }
}
```

**HTTP Status:** 200 OK / 404 Not Found

---

### 3.3 Apply Promotion to Order
**POST** `/promotions/apply`

**Description:** Apply promotion code to order during checkout.

**Query Parameters:**
- `orderId` (Long): Order ID
- `promotionCode` (String): Promotion code (e.g., "SUMMER20")
- `customerId` (Long): Customer ID

**Response (Success):**
```json
{
  "success": true,
  "message": "Promotion applied",
  "data": {
    "success": true,
    "discountAmount": 20.00,
    "newTotal": 80.00,
    "message": "Promotion applied successfully"
  }
}
```

**Response (Failed):**
```json
{
  "success": false,
  "error": "Promotion expired or not yet valid"
}
```

**HTTP Status:** 200 OK / 400 Bad Request

---

### 3.4 Get Restaurant Promotions
**GET** `/promotions/restaurant/{restaurantId}`

**Description:** Get all active promotions for a restaurant.

**Path Parameters:**
- `restaurantId` (Long): Restaurant ID

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 111,
      "code": "SUMMER20",
      "type": "PERCENTAGE",
      "discountValue": 20,
      "active": true
    },
    {
      "id": 112,
      "code": "DINNER15",
      "type": "FIXED_AMOUNT",
      "discountValue": 50,
      "active": true
    }
  ]
}
```

**HTTP Status:** 200 OK

---

### 3.5 Update Promotion
**PUT** `/promotions/{id}`

**Description:** Update an existing promotion.

**Path Parameters:**
- `id` (Long): Promotion ID

**Request Body:** Same as Create Promotion

**Response:**
```json
{
  "success": true,
  "message": "Promotion updated",
  "data": { ... }
}
```

**HTTP Status:** 200 OK

---

### 3.6 Delete Promotion
**DELETE** `/promotions/{id}`

**Description:** Delete a promotion.

**Path Parameters:**
- `id` (Long): Promotion ID

**Response:**
```json
{
  "success": true,
  "message": "Promotion deleted"
}
```

**HTTP Status:** 200 OK

---

## 4. RATING ENDPOINTS

### 4.1 Submit Rider Rating
**POST** `/ratings`

**Description:** Customer rates rider after delivery (politeness & speed).

**Query Parameters:**
- `orderId` (Long): Order ID
- `customerId` (Long): Customer ID

**Request Body:**
```json
{
  "riderId": 789,
  "politenessScore": 5,
  "speedScore": 4,
  "review": "Great delivery, very professional and polite!"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Rating submitted",
  "data": {
    "id": 222,
    "riderId": 789,
    "orderId": 123,
    "politenessScore": 5,
    "speedScore": 4,
    "overallScore": 4.5
  }
}
```

**HTTP Status:** 200 OK / 400 Bad Request

---

### 4.2 Get Rider All Ratings
**GET** `/ratings/rider/{riderId}`

**Description:** Get all ratings for a specific rider.

**Path Parameters:**
- `riderId` (Long): Rider ID

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 222,
      "riderId": 789,
      "customerId": 456,
      "politenessScore": 5,
      "speedScore": 4,
      "overallScore": 4.5,
      "review": "Great delivery!"
    },
    {
      "id": 223,
      "riderId": 789,
      "customerId": 457,
      "politenessScore": 4,
      "speedScore": 5,
      "overallScore": 4.5,
      "review": "Very fast delivery!"
    }
  ]
}
```

**HTTP Status:** 200 OK

---

### 4.3 Get Rider Average Rating
**GET** `/ratings/rider/{riderId}/average`

**Description:** Get average rating for a rider.

**Path Parameters:**
- `riderId` (Long): Rider ID

**Response:**
```json
{
  "success": true,
  "data": 4.6
}
```

**HTTP Status:** 200 OK

---

### 4.4 Get Order Rating
**GET** `/ratings/order/{orderId}`

**Description:** Get rating for a specific order.

**Path Parameters:**
- `orderId` (Long): Order ID

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 222,
    "riderId": 789,
    "orderId": 123,
    "politenessScore": 5,
    "speedScore": 4,
    "overallScore": 4.5,
    "review": "Great delivery!"
  }
}
```

**HTTP Status:** 200 OK / 404 Not Found

---

## Error Response Format

All error responses follow this format:

```json
{
  "success": false,
  "error": "Descriptive error message"
}
```

### Common HTTP Status Codes
- `200` - Success
- `400` - Bad Request (validation error)
- `401` - Unauthorized (authentication required)
- `403` - Forbidden (insufficient permission)
- `404` - Not Found (resource doesn't exist)
- `500` - Server Error

---

## Rate Limiting

Payment endpoints are rate-limited to prevent abuse:
- `/payments/initiate` - 10 requests per minute per user
- `/ratings` - 5 requests per hour per user

---

## Authentication (Future Enhancement)

Add Bearer token to all requests:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

**Last Updated:** 2024-01-15
