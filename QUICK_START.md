# Quick Start Guide - Get Running in 5 Minutes

## Prerequisites
- Java 17+ installed: `java -version`
- MySQL running: `mysql -u root -p`
- Node.js running frontend: check if `npm` works

## Step 1: Database Setup (1 min)
```bash
mysql -u root -p
CREATE DATABASE mharruengsang;
USE mharruengsang;
EXIT;
```

## Step 2: Configure Backend (1 min)
Edit `Backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mharruengsang?useSSL=false&serverTimezone=UTC
    username: root
    password: YOUR_MYSQL_ROOT_PASSWORD  # ← Replace with your MySQL root password
```

**Where to find your MySQL password:**
- If you installed MySQL recently, check your installation notes
- Default MySQL root password (if you didn't set one): leave blank or use `root`
- If using XAMPP: password is usually blank (leave empty)
- If using WAMP/MAMP: check your setup documentation

## Step 3: Run Backend (2 min)
```bash
cd Backend
mvn clean install
mvn spring-boot:run
```

**Expected output:**
```
Tomcat started on port(s): 8080
Started FoodDeliveryApplication in 3.456 seconds
```

✅ Backend running at: `http://localhost:8080/api`

## Step 4: Update Frontend API (30 sec)
Edit `Implementation/src/api.js`:

```javascript
const api = axios.create({
  baseURL: 'http://localhost:8080/api',  // ← Change this line
  timeout: 5000,
})
```

## Step 5: Run Frontend (30 sec)
```bash
cd Implementation
npm install
npm run dev
```

✅ Frontend running at: `http://localhost:5173`

---

## Test It Out!

### 1. Go to Checkout
- http://localhost:5173/customer/checkout
- Click "Checkout"

### 2. Test Credit Card Payment
```
Card: 4242 4242 4242 4242
Expiry: 12/25
CVC: 123
```

### 3. See Payment Processed
- Response shows payment confirmed
- Order status updated to PAID
- ✅ Success!

---

## Test Other Features

### Test Rider Location Update
```bash
curl -X POST "http://localhost:8080/api/riders/1/location?latitude=13.7563&longitude=100.5018&status=AVAILABLE"
```

### Test Promotion
```bash
curl -X GET "http://localhost:8080/api/promotions/code/SUMMER20"
```

### Test Rating
```bash
curl -X POST "http://localhost:8080/api/ratings?orderId=1&customerId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "riderId": 1,
    "politenessScore": 5,
    "speedScore": 4,
    "review": "Great service!"
  }'
```

---

## Common Issues

### "Port 8080 already in use"
```bash
# Stop other Java process
lsof -i :8080
kill -9 <PID>
```

### "Connection refused: localhost:3306"
```bash
# Start MySQL
mysql.server start  # macOS
# OR
mysqld  # Windows
```

### CORS error in browser
- Ensure backend is running on port 8080
- Check `CorsConfig.java` includes your frontend URL

---

## Project Files Overview

```
2025-ITCS383-MharRuengSang/
├── Backend/                    # ← Java Spring Boot backend (NEW!)
│   ├── pom.xml                 # Maven dependencies
│   ├── src/main/java/          # All backend code
│   ├── src/main/resources/
│   │   └── application.yml     # 🔧 Configure here
│   ├── README.md               # Full documentation
│   ├── PAYMENT_FLOW.md         # Payment architecture
│   └── API_REFERENCE.md        # All API endpoints
│
├── Implementation/             # Your React frontend
│   ├── src/
│   │   ├── api.js              # 🔧 Update baseURL here
│   │   └── pages/
│   │       └── customer/
│   │           └── Checkout.jsx # 🔧 Payment implementation here
│   └── package.json
│
├── INTEGRATION_GUIDE.md        # Frontend-backend integration
└── BACKEND_SUMMARY.md          # What's been built
```

---

## API Quick Reference

### Payment
```
POST /api/payments/initiate        - Start payment
GET  /api/payments/{id}/verify     - Check status
POST /api/payments/{orderId}/promptpay - Show QR code
```

### Riders
```
POST /api/riders/{id}/location           - Update GPS
POST /api/riders/{id}/accept-order/{oid} - Accept delivery
POST /api/riders/{id}/confirm-delivery/{oid} - Complete
```

### Promotions
```
GET  /api/promotions/{id}             - Get promotion
POST /api/promotions/apply            - Apply discount code
```

### Ratings
```
POST /api/ratings       - Submit rating
GET  /api/ratings/rider/{id}/average - Get avg rating
```

See **API_REFERENCE.md** for full documentation.

---

## Next Steps

1. ✅ Read `PAYMENT_FLOW.md` to understand architecture
2. ✅ Check `INTEGRATION_GUIDE.md` for detailed integration
3. ✅ Review `API_REFERENCE.md` for all endpoints
4. ✅ Run sample tests above
5. ✅ Connect with Thanaporn (Order Service) for event coordination
6. ✅ Connect with Natkrittar (Promotion Service) for discount validation

---

## Architecture Diagram

```
FRONTEND (React/Vite)
    ↓ HTTP
┌────────────────────────────────┐
│  Backend (Spring Boot)         │
│  ├─ Payment Service            │
│  ├─ Rider Service              │
│  ├─ Promotion Service          │
│  ├─ Rating Service             │
│  └─ Geolocation Service        │
└────────────────┬───────────────┘
                 ├─ Stripe API (Payment)
                 ├─ MySQL Database
                 ├─ Kafka Events (Order, Rider)
                 └─ QR Code Generator
```

---

## Database Schema (Auto-created)

Tables automatically created:
- `users` - Customers, riders, restaurants
- `orders` - Order records
- `payments` - Payment transactions
- `promotions` - Discount codes
- `rider_locations` - GPS tracking
- `rider_ratings` - Delivery ratings

---

## 10-Minute Checklist

- [ ] Database created: `mharruengsang`
- [ ] Backend configured: `application.yml`
- [ ] Backend running: `http://localhost:8080/api`
- [ ] Frontend API updated: `http://localhost:8080/api`
- [ ] Frontend running: `http://localhost:5173`
- [ ] Test payment endpoint works
- [ ] See payment processed successfully
- [ ]✅ Complete!

---

## Still Have Questions?

1. **How does payment work?** → `PAYMENT_FLOW.md`
2. **What APIs exist?** → `API_REFERENCE.md`
3. **How do I integrate frontend?** → `INTEGRATION_GUIDE.md`
4. **Full documentation?** → `Backend/README.md`

---

## Support Contacts

- **Backend Issues:** Check logs in Spring Boot terminal
- **Database Issues:** `mysql -u root -p` and verify tables created
- **Frontend Issues:** Check browser console (F12)
- **Payment provider:** Check API keys in `application.yml`

---

**You're all set! Happy coding! 🚀**

Next: Read INTEGRATION_GUIDE.md for detailed integration steps.
