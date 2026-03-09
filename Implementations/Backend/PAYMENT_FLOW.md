# Payment Flow - Detailed Technical Documentation

## 1. PAYMENT INITIATION FLOW

```
┌─────────────────────────────────────────────────────────────────────┐
│                    CUSTOMER INITIATES CHECKOUT                       │
└────────────────────────────────────┬────────────────────────────────┘
                                     │
                                     ▼
                    Total: $100 | Items: [Pizza, Coke]
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────┐
│              FRONTEND: POST /api/payments/initiate                   │
│                                                                      │
│  REQUEST:                                                           │
│  {                                                                   │
│    "orderId": 123,                                                  │
│    "amount": 100.00,                                                │
│    "paymentMethod": "CREDIT_CARD",  /* or PROMPTPAY, BANK_TRANSFER */
│    "provider": "STRIPE",             /* or OMISE */             │
│    "cardNumber": "4242424242424242",                             │
│    "cardholderName": "John Doe",                                 │
│    "expiryMonth": "12",                                          │
│    "expiryYear": "25",                                           │
│    "cvc": "123"                                                  │
│  }                                                                   │
└────────────────────────────────────┬────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────┐
│              BACKEND: PaymentController.initiatePayment()            │
│                                                                      │
│  1. Validate order exists                                           │
│  2. Validate order.status == CONFIRMED                              │
│  3. Create Payment entity:                                          │
│     - status = PENDING                                              │
│     - amount = 100.00                                               │
│     - paymentMethod = CREDIT_CARD                                   │
│     - provider = STRIPE                                             │
│  4. Save to database                                                │
│  5. Call processPayment()                                           │
└────────────────────────────────────┬────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────┐
│          BACKEND: PaymentServiceImpl.processPayment()                │
│                                                                      │
│  Switch on paymentMethod:                                           │
│                                                                      │
│  CASE CREDIT_CARD:                                                  │
│    ├─ Payment.status = PROCESSING                                   │
│    ├─ Get PaymentGatewayProvider (Stripe)                           │
│    └─ Call: StripePaymentProvider.processCardPayment()             │
│                                                                      │
│  CASE PROMPTPAY:                                                    │
│    ├─ Payment.status = PROCESSING                                   │
│    └─ Call: generatePromptPayQR()                                   │
│                                                                      │
│  CASE BANK_TRANSFER:                                                │
│    ├─ Generate bank reference: BANK-1234567890                     │
│    └─ Payment.bankReferenceNumber = BANK-1234567890               │
└────────────────────────────────────┬────────────────────────────────┘
                                     │
                       ┌─────────────┼─────────────┐
                       │             │             │
                   CREDIT      PROMPTPAY      BANK
                   CARD          QR CODE      TRANSFER
                    │             │             │
                    ▼             ▼             ▼
              ┌──────────┐  ┌──────────┐  ┌──────────┐
              │ Stripe   │  │ PROMPTPAY│  │ Bank Ref │
              │ Process  │  │ QR Gen   │  │Generated │
              └────┬─────┘  └────┬─────┘  └────┬─────┘
                   │            │             │
                   ▼            ▼             ▼
              Transaction   QR Code      Reference
              ID Created    Displayed    Number
                   │            │             │
                   └─────────────┼─────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│              FRONTEND: Receive Response                              │
│                                                                      │
│  RESPONSE:                                                          │
│  {                                                                   │
│    "success": true,                                                 │
│    "data": {                                                        │
│      "id": 456,                                                     │
│      "orderId": 123,                                                │
│      "amount": 100.00,                                              │
│      "status": "PROCESSING" | "VERIFIED" | "PENDING",             │
│      "paymentMethod": "CREDIT_CARD",                                │
│      "transactionId": "ch_1234567890abc",  /* If card */           │
│      "qrCodeData": "<base64_png>",         /* If PromptPay */     │
│      "bankReferenceNumber": "BANK-1234567890"  /* If Bank */      │
│    }                                                                 │
│  }                                                                   │
└────────────────────────────────────┬────────────────────────────────┘
                                     │
                    ┌────────────────┼────────────────┐
                    │                │                │
              Card Already      QR Displayed      Bank Reference
              Processed         Awaiting Scan    Awaiting Transfer
                    │                │                │
                    ▼                ▼                ▼
```

---

## 2. PAYMENT VERIFICATION & CONFIRMATION

### A) Credit Card (Immediate)

```
┌────────────────────────────────┐
│ payment.status = VERIFIED      │  ← Created by StripePaymentProvider
│ transactionId = ch_xxxx        │
│ order.status = PAID            │
└────────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────────────────────────────────┐
│  ASYNC: publishOrderPaidEvent()                                    │
│  ├─ Publish to Kafka: ORDER_PAID event                            │
│  ├─ Find nearby riders (geolocationService)                       │
│  └─ Assign rider to order                                         │
└────────────────────────────────────────────────────────────────────┘
```

### B) PromptPay QR (Polling)

```
FRONTEND: Auto-poll every 3 seconds
│
├─> GET /api/payments/{paymentId}/verify
│
├─> Response: payment.status = PENDING (still)
│
├─> Repeat every 3 seconds...
│
└─> UNTIL: payment.status = COMPLETED
          └─> Order.status updated to PAID
          └─> Publish ORDER_PAID event
```

### C) Bank Transfer (Manual Verification)

```
BACKEND: Scheduled job (every 30 seconds)
│
├─> Query bank API or Omise API
│
├─> Check if payment received for reference: BANK-1234567890
│
└─> If received:
    ├─ Payment.status = COMPLETED
    ├─ Payment.verifiedAt = now()
    └─ Publish ORDER_PAID event
```

---

## 3. ORDER SERVICE COORDINATION

```
┌─────────────────────────────────────────────────────────────────┐
│  KAFKA EVENT: ORDER_PAID                                        │
│  Topic: order-paid-topic                                        │
│  Payload:                                                       │
│  {                                                               │
│    "eventType": "ORDER_PAID",                                   │
│    "orderId": 123,                                              │
│    "customerId": 456,                                           │
│    "restaurantId": 789,                                         │
│    "paymentId": 456,                                            │
│    "amount": 100.00,                                            │
│    "timestamp": 1704067200000                                   │
│  }                                                               │
└────────────────────────────────────┬──────────────────────────┘
                                     │
                   ┌─────────────────┼─────────────────┐
                   │                 │                 │
          ┌────────▼─────────┐  ┌────▼────────────┐  │
          │  ORDER SERVICE   │  │ RIDER SERVICE   │  │
          │ (Thanaporn)      │  │                 │  │
          ├──────────────────┤  ├─────────────────┤  │
          │ Listen event     │  │ Listen event    │  │
          │ Update status:   │  │ Find nearby:    │  │
          │ PAID → CONFIRMED │  │ SELECT * FROM   │  │
          │ Notify customer  │  │ rider_locations │  │
          └──────────────────┘  │ WHERE status=   │  │
                                │ AVAILABLE       │  │
                                │ AND distance < 5km
                                │ Assign rider    │  │
                                │ Update order    │  │
                                │ rider_id = xxx  │  │
                                └─────────────────┘  │
                                                    │
                                                    └─ Continue...
```

---

## 4. REAL-TIME RIDER TRACKING

```
┌──────────────────────────────────────────────────────────────────┐
│              RIDER: Updates Location Every 10 seconds             │
│              POST /api/riders/{riderId}/location                  │
│                                                                   │
│  {                                                                │
│    "latitude": 13.7563,                                          │
│    "longitude": 100.5018,                                        │
│    "status": "ON_DELIVERY"                                       │
│  }                                                                │
└────────────────────────────────────┬─────────────────────────────┘
                                     │
                                     ▼
                    Update RiderLocation table
              Updates: latitude, longitude, updatedAt
                                     │
                                     ▼
                    FRONTEND: Polling every 3 seconds
           GET /api/riders/{riderId}/location
                       │
                       ▼
              Show updated marker on map
              Calculate distance to delivery
```

---

## 5. DELIVERY COMPLETION & RATING

```
┌──────────────────────────────────────────────────────────────────┐
│              RIDER: Confirms Delivery                             │
│              POST /api/riders/{riderId}/confirm-delivery/{orderId}│
│                                                                   │
│  Backend:                                                        │
│  ├─ Order.status = DELIVERED                      │
│  ├─ Order.deliveredAt = now()                     │
│  ├─ Rider.status = AVAILABLE (back to work)       │
│  └─ Publish EVENT: ORDER_DELIVERED               │
└────────────────────────────────────┬──────────────────────────────┘
                                     │
                                     ▼
              ┌──────────────────────────────────────┐
              │  FRONTEND: Show Rating Page          │
              │  File: pages/customer/RateRider.jsx  │
              └──────────────────────────────────────┘
                                     │
                                     ▼
┌──────────────────────────────────────────────────────────────────┐
│              CUSTOMER: Rates Rider                                │
│              POST /api/ratings                                    │
│                                                                   │
│  {                                                                │
│    "orderId": 123,                                               │
│    "customerId": 456,                                            │
│    "riderId": 789,                                               │
│    "politenessScore": 5,      /* 1-5 scale */                    │
│    "speedScore": 4,           /* 1-5 scale */                    │
│    "review": "Great delivery, very professional!"                │
│  }                                                                │
└────────────────────────────────────┬──────────────────────────────┘
                                     │
                                     ▼
                  Backend: Calculate overall score
                  overallScore = (5 + 4) / 2 = 4.5
                                     │
                                     ▼
                  Save RiderRating entity
                  Rider average rating updates
                  
                  SELECT AVG(overallScore)
                  FROM rider_ratings
                  WHERE rider_id = 789
                  = 4.6 ⭐ (across all deliveries)
```

---

## 6. PROMOTION/DISCOUNT APPLICATION

```
┌──────────────────────────────────────────────────────────────────┐
│              FRONTEND: Checkout Page                              │
│              User enters promo code: "SUMMER20"                   │
└────────────────────────────────────┬─────────────────────────────┘
                                     │
                                     ▼
┌──────────────────────────────────────────────────────────────────┐
│              POST /api/promotions/apply                           │
│              Parameters:                                         │
│              - orderId: 123                                      │
│              - promotionCode: "SUMMER20"                         │
│              - customerId: 456                                   │
└────────────────────────────────────┬──────────────────────────────┘
                                     │
                                     ▼
┌──────────────────────────────────────────────────────────────────┐
│              Backend: PromotionServiceImpl.applyPromotion()       │
│                                                                   │
│  1. Find: SELECT * FROM promotions WHERE code = "SUMMER20"      │
│                                                                   │
│  2. Validate:                                                    │
│     ✓ Promotion is ACTIVE                                       │
│     ✓ Current time is within validFrom & validUntil             │
│     ✓ Usage count < usageLimit                                  │
│     ✓ Order.amount >= minimumOrderAmount                        │
│                                                                   │
│  3. Calculate discount:                                          │
│     if Type == PERCENTAGE:                                       │
│       discount = order.amount * 20% = 100 * 0.20 = $20          │
│     if Type == FIXED_AMOUNT:                                     │
│       discount = $50                                             │
│     if Type == FREE_DELIVERY:                                    │
│       discount = deliveryFee                                     │
│                                                                   │
│  4. Cap discount at maximum:                                     │
│     if discount > maxDiscountAmount:                            │
│       discount = maxDiscountAmount                               │
│                                                                   │
│  5. Update Order:                                                │
│     order.discountCode = "SUMMER20"                              │
│     order.discountAmount = $20                                   │
│                                                                   │
│  6. Update Promotion:                                            │
│     promotion.usageCount += 1                                    │
└────────────────────────────────────┬──────────────────────────────┘
                                     │
                                     ▼
┌──────────────────────────────────────────────────────────────────┐
│              RESPONSE:                                            │
│  {                                                                │
│    "success": true,                                              │
│    "data": {                                                      │
│      "discountAmount": 20.00,                                    │
│      "newTotal": 80.00,                                          │
│      "message": "Promotion applied successfully"                 │
│    }                                                              │
│  }                                                                │
│                                                                   │
│  FRONTEND: Update display                                        │
│  Original Total: $100.00                                         │
│  Discount (-20%): -$20.00                                        │
│  FINAL TOTAL: $80.00                                             │
└──────────────────────────────────────────────────────────────────┘
```

---

## 7. ERROR HANDLING & RETRY LOGIC

### Stripe Card Failure
```
POST /api/payments/initiate
├─ Stripe rejects card
├─ Return: { success: false, error: "Card declined" }
├─ Payment.status = FAILED
├─ Order.status stays CONFIRMED
└─ Frontend: User can retry
```

### PromptPay Timeout
```
Payment waiting > 24 hours?
├─ Automatic order cancellation
└─ Async task: cancellExpiredPayments()
```

### Bank Transfer Verification Failure
```
Backend couldn't verify payment reference
├─ Keep trying for 5 days
├─ After 5 days: Order.status = CANCELLED
└─ Refund available
```

---

## 8. ASYNCHRONOUS ARCHITECTURE

```
Payment Service (Synchronous)
│
└─> Publish EVENT to Kafka
    │
    ├─> Order Service subscribes
    │    └─ Updates order status ASYNCHRONOUSLY
    │
    ├─> Rider Service subscribes
    │    └─ Finds nearby riders ASYNCHRONOUSLY
    │    └─ Assigns rider ASYNCHRONOUSLY
    │
    └─> Notification Service subscribes
         └─ Sends SMS/Email ASYNCHRONOUSLY

Advantages:
✓ Payment endpoint responds immediately
✓ Other services process in background
✓ Fault tolerance (if Order Service down, event queued)
✓ Scalable (multiple workers consume events)
```

---

## 9. SUPPORT FOR 10 MILLION CONCURRENT USERS

### Database Optimization
```sql
-- Add indexes
CREATE INDEX idx_order_customer ON orders(customer_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_payment_order ON payments(order_id);
CREATE INDEX idx_payment_status ON payments(status);

-- Partition by date
ALTER TABLE orders PARTITION BY RANGE (YEAR(created_at));
```

### Caching Layer
```java
@Cacheable("payment-status")
public PaymentDTO getPaymentDetails(Long paymentId) { ... }

// Clear cache on update
@CacheEvict("payment-status", key="#paymentId")
public void updatePaymentStatus(Long paymentId, PaymentStatus status) { ... }
```

### Load Distribution
```
[10M Users]
     ↓↓↓
[Load Balancer - Nginx]
     ↓↓↓
  ┌──┴────┬────────┬──────┐
  ▼       ▼        ▼      ▼
[Backend Instance 1] [Backend Instance 2] ... [Backend Instance 10]
  │       │        │      │
  └───────┴────────┴──────┘
          ↓
      [MySQL Cluster]
          ↓
    [Read Replicas: 3]
```

---

**End of Payment Flow Documentation**
