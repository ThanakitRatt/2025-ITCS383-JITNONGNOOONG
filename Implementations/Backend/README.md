# MharRuengSang Food Delivery Platform - Backend Setup Guide

## Project Structure
```
Backend/
├── pom.xml                          # Maven dependencies
├── src/
│   ├── main/
│   │   ├── java/com/mharruengsang/
│   │   │   ├── FoodDeliveryApplication.java  # Entry point
│   │   │   ├── controller/                   # REST API endpoints
│   │   │   │   ├── PaymentController.java
│   │   │   │   ├── RiderController.java
│   │   │   │   ├── PromotionController.java
│   │   │   │   └── RatingController.java
│   │   │   ├── service/                      # Business logic
│   │   │   │   ├── PaymentService.java
│   │   │   │   ├── RiderService.java
│   │   │   │   ├── PromotionService.java
│   │   │   │   ├── RatingService.java
│   │   │   │   ├── GeolocationService.java
│   │   │   │   ├── QRCodeService.java
│   │   │   │   └── impl/                     # Service implementations
│   │   │   ├── entity/                       # JPA Entity models
│   │   │   ├── dto/                          # Data Transfer Objects
│   │   │   ├── repository/                   # Database access
│   │   │   └── config/                       # Configuration
│   │   └── resources/
│   │       └── application.yml
│   └── test/
└── README.md
```

## Installation & Setup

### 1. Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+ (or PostgreSQL 14+)
- Kafka (optional, for async processing)

### 2. Database Setup
Create a MySQL database:
```sql
CREATE DATABASE mharruengsang;
USE mharruengsang;
```

### 3. Configure Properties
Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mharruengsang?useSSL=false&serverTimezone=UTC
    username: root
    password: your_password

payment:
  stripe:
    api-key: sk_test_your_key_here    # Get from https://stripe.com
  omise:
    api-key: skey_test_your_key_here  # Get from https://omise.co
```

### 4. Build & Run Backend
```bash
cd Backend
mvn clean install
mvn spring-boot:run
```
Backend runs on: `http://localhost:8080/api`

## Payment Flow Design

### Customer Payment Journey
```
1. Customer adds items to cart
   └─> Cart value: $100

2. Customer clicks "Checkout"
   └─> Frontend calls: POST /api/payments/initiate
   └─> Payload: { orderId: 123, amount: 100, paymentMethod: "CREDIT_CARD", ... }

3. Backend: CREATE Payment record (status=PENDING)
   └─> Order status: CONFIRMED → PAYMENT_PROCESSING

4. Backend: PROCESS Payment with Stripe/Omise
   ├─ If method=CREDIT_CARD: Process card directly
   ├─ If method=PROMPTPAY: Generate QR code
   └─ If method=BANK_TRANSFER: Generate reference number

5. Frontend: Receive response with transaction details
   ├─ For QR: Display QR code to customer
   ├─ For Card: Already processed
   └─ For Bank: Show reference number & deadline

6. ASYNC: Verify payment status
   └─> Backend polls: GET /api/payments/{paymentId}/verify
   └─> Update if status changes

7. Payment Confirmed (Webhook OR Polling)
   ├─ Payment status: COMPLETED
   ├─ Order status: PAID ✓
   └─ Publish EVENT: ORDER_PAID → Kafka

8. ASYNC: Order & Rider Services consume ORDER_PAID event
   ├─ Order Service: Update order status
   ├─ Rider Service: Find nearby available riders
   └─ Assign rider to delivery

9. Customer can now track delivery
   └─> Rider updates location in real-time
```

### Payment Method Flows

#### A) Credit Card (Stripe/Omise)
```
POST /api/payments/initiate
├─ Input: Card number, expiry, CVC
├─ Tokenize card
├─ Create charge
└─ Return: transactionId (if success)
  
GET /api/payments/{paymentId}/verify
└─ Query Stripe: stripePaymentProvider.verifyPayment(txnId)
└─ Update Payment.status = COMPLETED
```

#### B) PromptPay QR Code
```
POST /api/payments/{orderId}/promptpay
├─ Input: restaurantPhoneNumber, orderAmount
├─ Generate QR using PromptPayQRCodeService
│  └─ Uses EMV QRCPS standard (Thai standard)
│  └─ QR data includes: phone, amount, reference
├─ Return: QR code image (base64 PNG)
└─ Store: Payment.qrCodeData = base64

User scans QR in banking app, transfers money

Periodic polling: GET /api/payments/{paymentId}/verify
├─ Check if payment received
├─ Backend queries bank/Omise for payment status
└─ Once verified: Update ALL downstream services
```

#### C) Bank Transfer
```
POST /api/payments/initiate (bankTransfer)
├─ Generate reference number: BANK-1234567890
├─ Return: Bank account, reference, deadline (24hrs)
└─ Store: Payment.bankReferenceNumber

User manually transfers to bank account with reference

Backend: Polling or Webhook
├─ Check bank API if payment received
├─ Matchreferencenumberfororderidentification
└─ Update payment status
```

## API Endpoints Summary

### Payment APIs
```
POST   /api/payments/initiate              - Start payment
POST   /api/payments/process               - Process with gateway
GET    /api/payments/{paymentId}/verify    - Check status
POST   /api/payments/{orderId}/promptpay   - Generate PromptPay QR
GET    /api/payments/{paymentId}           - Get details
POST   /api/payments/{paymentId}/refund    - Refund payment
POST   /api/payments/callback              - Webhook from provider
```

### Rider APIs
```
POST   /api/riders/{riderId}/location              - GPS update
GET    /api/riders/{riderId}/location              - Get location
POST   /api/riders/{riderId}/accept-order/{orderId} - Accept delivery
POST   /api/riders/{riderId}/confirm-delivery/{orderId} - Complete delivery
```

### Promotion APIs
```
POST   /api/promotions                     - Create promo
PUT    /api/promotions/{id}                - Update promo
DELETE /api/promotions/{id}                - Delete promo
GET    /api/promotions/code/{code}         - Get by code (checkout)
GET    /api/promotions/restaurant/{id}     - List restaurant promos
POST   /api/promotions/apply               - Apply code to order
```

### Rating APIs
```
POST   /api/ratings                        - Submit rider rating
GET    /api/ratings/rider/{riderId}        - Get rider ratings
GET    /api/ratings/rider/{riderId}/average - Get average rating
GET    /api/ratings/order/{orderId}        - Get order rating
```

## Frontend Integration

### 1. Update Frontend API Base URL
In `Implementation/src/api.js`:
```javascript
import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',  // Changed from localhost:4000
  timeout: 5000,
})

export default api
```

### 2. Update Checkout Component
In `Implementation/src/pages/customer/Checkout.jsx`:

```javascript
import api from '../../api'
import { useState } from 'react'

export default function Checkout() {
  const { items, total, clearCart } = useCart()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [processing, setProcessing] = useState(false)
  const [paymentMethod, setPaymentMethod] = useState('CREDIT_CARD')

  async function initiatePayment(e) {
    e.preventDefault()
    if (!user) return alert('Please login')
    
    setProcessing(true)
    try {
      // Step 1: Create order
      const orderRes = await api.post('/orders', {
        customerId: user.id,
        items: items.map(it => ({ name: it.name, price: it.price })),
        totalAmount: total,
        deliveryAddress: user.address,
        status: 'CONFIRMED'
      })
      
      const orderId = orderRes.data.data.id
      
      // Step 2: Initiate payment
      const paymentRes = await api.post('/payments/initiate', {
        orderId: orderId,
        amount: total,
        paymentMethod: paymentMethod,
        provider: 'STRIPE',
        // Card details (for CREDIT_CARD method)
        cardNumber: document.getElementById('cardNumber').value,
        cardholderName: document.getElementById('cardholderName').value,
        expiryMonth: document.getElementById('expiryMonth').value,
        expiryYear: document.getElementById('expiryYear').value,
        cvc: document.getElementById('cvc').value
      })
      
      const payment = paymentRes.data.data
      
      if (paymentMethod === 'PROMPTPAY') {
        // Display QR code
        const qrRes = await api.post(`/payments/${orderId}/promptpay`, {
          restaurantPhone: '0812345678'
        })
        displayQRCode(qrRes.data.data.qrCodeData)
        
        // Start polling to verify payment
        pollPaymentStatus(payment.id)
      } else {
        // Payment processed
        if (payment.status === 'COMPLETED') {
          clearCart()
          alert('Payment successful!')
          navigate('/customer')
        } else {
          alert('Payment verification pending...')
        }
      }
      
    } catch (err) {
      alert('Payment failed: ' + err.message)
    }
    setProcessing(false)
  }
  
  function pollPaymentStatus(paymentId) {
    const interval = setInterval(async () => {
      try {
        const res = await api.get(`/payments/${paymentId}/verify`)
        const payment = res.data.data
        
        if (payment.status === 'COMPLETED') {
          clearInterval(interval)
          clearCart()
          alert('Payment confirmed!')
          navigate('/customer')
        }
      } catch (err) {
        console.error('Poll error:', err)
      }
    }, 3000) // Check every 3 seconds
  }
  
  function displayQRCode(qrBase64) {
    // Display QR code image
    const img = document.getElementById('qrCodeImage')
    img.src = `data:image/png;base64,${qrBase64}`
    img.style.display = 'block'
  }

  return (
    <div>
      <h2>Checkout</h2>
      
      <div className="card">
        <div><strong>Total:</strong> ${total.toFixed(2)}</div>
      </div>
      
      <form onSubmit={initiatePayment} className="card">
        <h3>Payment Method</h3>
        
        <label>
          <input type="radio" value="CREDIT_CARD" checked={paymentMethod === 'CREDIT_CARD'}
                 onChange={(e) => setPaymentMethod(e.target.value)} />
          Credit Card
        </label>
        
        <label>
          <input type="radio" value="PROMPTPAY" checked={paymentMethod === 'PROMPTPAY'}
                 onChange={(e) => setPaymentMethod(e.target.value)} />
          PromptPay QR Code
        </label>
        
        <label>
          <input type="radio" value="BANK_TRANSFER" checked={paymentMethod === 'BANK_TRANSFER'}
                 onChange={(e) => setPaymentMethod(e.target.value)} />
          Bank Transfer
        </label>
        
        {paymentMethod === 'CREDIT_CARD' && (
          <>
            <input id="cardholderName" placeholder="Cardholder name" required />
            <input id="cardNumber" placeholder="Card number" required />
            <input id="expiryMonth" placeholder="MM" maxLength="2" required />
            <input id="expiryYear" placeholder="YY" maxLength="2" required />
            <input id="cvc" placeholder="CVC" maxLength="3" required />
          </>
        )}
        
        {paymentMethod === 'PROMPTPAY' && (
          <div>
            <p>Scan this QR code with your banking app:</p>
            <img id="qrCodeImage" style={{maxWidth: '300px', display: 'none'}} />
          </div>
        )}
        
        <button type="submit" disabled={processing}>
          {processing ? 'Processing...' : `Pay ${total.toFixed(2)}`}
        </button>
      </form>
    </div>
  )
}
```

### 3. Rating Component (After Delivery)
Create `Implementation/src/pages/customer/RateRider.jsx`:

```javascript
import { useState } from 'react'
import api from '../../api'

export default function RateRider({ orderId, riderId, onComplete }) {
  const [politeness, setPoliteness] = useState(5)
  const [speed, setSpeed] = useState(5)
  const [review, setReview] = useState('')
  const [submitted, setSubmitted] = useState(false)

  async function submitRating(e) {
    e.preventDefault()
    try {
      await api.post('/ratings', {
        orderId: orderId,
        customerId: userId,
        riderId: riderId,
        politenessScore: politeness,
        speedScore: speed,
        review: review
      })
      setSubmitted(true)
      onComplete()
    } catch (err) {
      alert('Rating failed: ' + err.message)
    }
  }

  if (submitted) return <div>Thank you for rating!</div>

  return (
    <form onSubmit={submitRating} className="card">
      <h3>Rate Your Rider</h3>
      
      <div>
        <label>Politeness: {politeness}/5</label>
        <input type="range" min="1" max="5" value={politeness}
               onChange={(e) => setPoliteness(e.target.value)} />
      </div>
      
      <div>
        <label>Speed: {speed}/5</label>
        <input type="range" min="1" max="5" value={speed}
               onChange={(e) => setSpeed(e.target.value)} />
      </div>
      
      <textarea placeholder="Additional feedback" value={review}
                 onChange={(e) => setReview(e.target.value)} />
      
      <button type="submit">Submit Rating</button>
    </form>
  )
}
```

## Database Schema (Auto-Generated by JPA)

### Tables Created:
- `users` - Customers, riders, restaurants
- `orders` - Order records
- `payments` - Payment transactions
- `promotions` - Discount codes
- `rider_locations` - Real-time GPS tracking
- `rider_ratings` - Delivery performance ratings

## Async Processing with Kafka

Events published:
1. `ORDER_PAID` → Order Service confirms order
2. `DELIVERY_ASSIGNED` → Notify customer & rider
3. `ORDER_DELIVERED` → Complete order, enable rating

To enable Kafka:
```bash
# Start Kafka locally
docker-compose up -d kafka zookeeper

# Or use Kafka topics manually
kafka-topics.sh --create --topic order-paid-topic --bootstrap-servers localhost:9092
```

## Performance Considerations

### Supporting 10 Million Concurrent Users:

1. **Async Payment Processing**
   - Use `@Async` for long-running payment verifications
   - Kafka queues for order events
   - Non-blocking HTTP calls

2. **Database Optimization**
   - Index on: orderId, customerId, rider Id, payment status
   - Partition tables by time (orders by date)
   - Connection pooling (HikariCP - default in Spring Boot)

3. **Caching**
   ```java
   @Cacheable("promotions")
   public PromotionDTO getPromotionByCode(String code)
   ```

4. **Load Balancing**
   - Run multiple backend instances
   - Use Nginx/HAProxy
   - Sticky sessions for real-time tracking

## Security Checklist

- [ ] ✓ JWT authentication (add SecurityConfig)
- [ ] ✓ Password hashing (BCrypt)
- [ ] ✓ HTTPS only in production
- [ ] ✓ API key rotation for payment gateways
- [ ] ✓ Rate limiting on payment endpoints
- [ ] ✓ Input validation & sanitization
- [ ] ✓ PCI DSS compliance (never log card details)

## Troubleshooting

**Port 8080 already in use:**
```bash
lsof -i :8080
kill -9 <PID>
```

**Database connection refused:**
```bash
mysql -u root -p
# Verify database exists: SHOW DATABASES;
```

**Stripe/Omise Payment Failing:**
- Check API keys in application.yml
- Verify keys are for TEST environment
- Check card number: 4242 4242 4242 4242 (Stripe test)

## Next Steps

1. ✓ Implement JWT Security Config
2. ✓ Add unit tests for payment service
3. ✓ Setup Kafka for event streaming
4. ✓ Deploy with Docker
5. ✓ Setup monitoring (ELK stack, Prometheus)
6. ✓ Database replication for HA
7. ✓ Coordinate with Order Service team
8. ✓ Coordinate with Promotion Service (Natkrittar)

---
For questions, contact: Backend Development Team
