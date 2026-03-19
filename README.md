# MharRuengSang — Food Delivery Platform

A scalable food delivery platform (similar to Grab or Line Man) that connects customers, restaurants, and riders, managed by a central administration team.

Built with **Java Spring Boot** microservices (backend) and **React + Vite** (frontend).

---

## Service URLs

| Service | Port | Local URL |
|---|---|---|
| API Gateway | 8080 | http://localhost:8080 |
| Order Service | 8081 | http://localhost:8081 |
| Restaurant Service | 8082 | http://localhost:8082 |
| Frontend (React) | 5173 | http://localhost:5173 |
| PostgreSQL | 5432 | localhost:5432 |

---

## Requirements

| Tool | Version | Install |
|---|---|---|
| Java JDK | 21+ | https://adoptium.net |
| Maven | 3.9+ | `brew install maven` |
| Node.js | 18+ | https://nodejs.org |
| npm | 9+ | bundled with Node.js |
| PostgreSQL | 14+ | `brew install postgresql@14` |

Verify your versions:
```bash
java -version
mvn -version
node --version
psql --version
```

---

## Setup

### 1. Database

Start PostgreSQL and create the two databases:

```bash
# Start PostgreSQL (macOS)
brew services start postgresql@14

# Open psql and run these commands:
psql -U postgres
```

```sql
CREATE DATABASE mhar_restaurants;
CREATE DATABASE mhar_orders;
CREATE USER mhar_user WITH PASSWORD 'mhar_password';
GRANT ALL PRIVILEGES ON DATABASE mhar_restaurants TO mhar_user;
GRANT ALL PRIVILEGES ON DATABASE mhar_orders TO mhar_user;
\q
```

Default connection settings (used by `application.yml` in each service):
- Host: `localhost:5432`
- Databases: `mhar_restaurants`, `mhar_orders`
- Username: `mhar_user` / Password: `mhar_password`

To override, set environment variables before starting the services:
```bash
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
```

### 2. Build Backend

```bash
cd Implementations/Backend
mvn clean install -DskipTests
```

This compiles all three modules: `api-gateway`, `restaurant-service`, and `order-service`.

### 3. Run Backend

**Option A — All services in one command (recommended):**
```bash
cd Implementations/Backend
./start-all.sh
```

**Option B — Each service in a separate terminal:**
```bash
# Terminal 1 – Restaurant Service → http://localhost:8082
cd Implementations/Backend/restaurant-service
mvn spring-boot:run

# Terminal 2 – Order Service → http://localhost:8081
cd Implementations/Backend/order-service
mvn spring-boot:run

# Terminal 3 – API Gateway → http://localhost:8080
cd Implementations/Backend/api-gateway
mvn spring-boot:run
```

To stop all services started with `start-all.sh`:
```bash
./stop-all.sh
```

### 4. Run Frontend

```bash
cd Implementations/Frontend
npm install
npm run dev
```

Frontend is available at **http://localhost:5173**

---

## Run Tests

**Backend:**
```bash
cd Implementations/Backend
mvn test
```

**Frontend:**
```bash
cd Implementations/Frontend
npx vitest run
```

---

## Example Usage

### Demo Accounts

Log in with any of the following demo accounts, then enter any 6-digit OTP (e.g. `123456`):

| Name | Email | Password | Role | Dashboard |
|---|---|---|---|---|
| John Doe | `customer@foodexpress.com` | `Customer123!` | Customer | Customer Dashboard |
| Sarah Wilson | `sarah@foodexpress.com` | `Sarah123!` | Customer | Customer Dashboard |
| Bangkok Street Food | `restaurant@foodexpress.com` | `Restaurant123!` | Restaurant | Restaurant Dashboard |
| Sushi Master | `sushi@foodexpress.com` | `Sushi123!` | Restaurant | Restaurant Dashboard |
| Mike Chen | `rider@foodexpress.com` | `Rider123!` | Rider | Rider Dashboard |
| Admin User | `admin@foodexpress.com` | `Admin123!` | Admin | Admin Dashboard |

Open **http://localhost:5173**, click **Login**, enter a demo email and password, then enter `123456` as the OTP.

### API Examples

```bash
# Health checks
curl http://localhost:8082/actuator/health   # Restaurant Service
curl http://localhost:8081/actuator/health   # Order Service
curl http://localhost:8080/actuator/health   # API Gateway

# List all restaurants (through API Gateway)
curl http://localhost:8080/api/restaurants

# List all restaurants (direct)
curl http://localhost:8082/api/restaurants

# Get a restaurant by ID
curl http://localhost:8082/api/restaurants/1

# Create a restaurant
curl -X POST http://localhost:8082/api/restaurants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Restaurant",
    "cuisineType": "THAI",
    "phoneNumber": "+66-2-123-4567",
    "email": "test@example.com",
    "address": "123 Test St",
    "latitude": 13.7563,
    "longitude": 100.5018,
    "deliveryFee": 25.00,
    "minimumOrderAmount": 100.00,
    "openingTime": "09:00",
    "closingTime": "22:00",
    "estimatedDeliveryTime": 30,
    "ownerId": 1
  }'

# Create an order
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "restaurantId": 1,
    "deliveryAddress": "456 Customer St",
    "deliveryLatitude": 13.7400,
    "deliveryLongitude": 100.5100,
    "orderItems": [
      {
        "menuItemId": 1,
        "menuItemName": "Pad Thai",
        "quantity": 2,
        "unitPrice": 120.00
      }
    ]
  }'

# Get admin statistics
curl http://localhost:8081/orders/admin/stats
curl http://localhost:8082/api/restaurants/stats
```

---

## Architecture

```
Browser / React App (http://localhost:5173)
        │
        ▼ HTTP
  API Gateway (http://localhost:8080)
        │
   ┌────┴────┐
   ▼         ▼
Restaurant  Order
Service     Service
(:8082)     (:8081)
   │         │
   ▼         ▼
PostgreSQL  PostgreSQL
mhar_       mhar_
restaurants orders
```

---

## Troubleshooting

**Port already in use:**
```bash
lsof -i :8080   # find the process
kill -9 <PID>
```

**PostgreSQL not running:**
```bash
brew services start postgresql@14
psql -U postgres -c "SELECT version();"
```

**Build fails:**
```bash
# Ensure JAVA_HOME points to JDK 21+
export JAVA_HOME=$(/usr/libexec/java_home)
java -version
```

**View service logs:**
```bash
tail -f Implementations/Backend/logs/restaurant-service.log
tail -f Implementations/Backend/logs/order-service.log
tail -f Implementations/Backend/logs/api-gateway.log
```

---

## Project Overview: Food Delivery System

The goal is to build a scalable food delivery platform (similar to Grab or Line Man) that connects customers, restaurants, and riders, managed by a central administration team.

---

## 1. User Roles (Actors)

- **Customer:** Browses, orders food, pays, and rates the service.
- **Restaurant:** Manages menus, prices, and prepares orders.
- **Rider (Delivery Man):** Accepts deliveries and transports food to customers.
- **System Administrator:** Monitors revenue, manages user accounts, and creates system-wide promotions.

---

## 2. Functional Requirements

### **A. Customer Journey**

- **Registration & Profile:** Must provide First Name, Last Name, Address, and Payment Info (Credit Card).
- **Security:** Multi-factor authentication (OTP via mobile) is required for login. Passwords must be updated every month.
- **Discovery:** View a list of restaurants with filters for:
    - Cuisine type (Thai, Japanese, Western, etc.)
    - Distance from the customer's location.
- **Ordering:** Select food items and checkout.
- **Payment:** Support for Credit Cards and Bank Transfer (QR Code/PromptPay).
- **Rating:** Ability to rate riders based on politeness and speed after delivery.

### **B. Restaurant Management**

- **Order Handling:** Receive and view incoming orders to begin preparation.
- **Menu Management:** Add, delete, or update menu items and adjust prices.
- **Marketing:** Create restaurant-specific promotions or special packages to boost sales.

### **C. Rider Operations**

- **Order Picking:** View and accept orders near their current location.
- **Navigation:** Access customer address for delivery.
- **Completion:** Confirm delivery to the customer.

### **D. Administration & Business Logic**

- **Revenue Tracking:** Monitor daily/monthly revenue (Company takes a 10% commission on top of food prices).
- **Account Governance:** Ability to enable/disable/delete restaurant or customer accounts (e.g., to prevent scams).
- **System Promotions:** Generate discount codes for specific members or timeframes to increase app usage.

---

## 3. Technical & Non-Functional Requirements

- **Platform Availability:**
    - **Web:** Accessible via all computers and operating systems.
    - **Mobile:** Native or cross-platform apps for both **iOS and Android**.
- **Tech Stack:** Must be developed using **Java technology** only (due to legacy system compatibility).
- **Scalability:** The system must support **10 million concurrent users**.
- **Integrations:**
    - Third-party OTP service for login.
    - Payment Gateways (Credit Card processing and Mobile Banking).

## 4. Quick Start Guide - Get Running in 5 Minutes

### Prerequisites
- Java 17+ installed: `java -version`
- PostgreSQL running: `psql --version`
- Node.js installed: `node --version`

### Step 1: Database Setup (1 min)
```bash
# Create databases for microservices
psql -U postgres

CREATE DATABASE mhar_restaurants;
CREATE DATABASE mhar_orders;
CREATE USER mhar_user WITH PASSWORD 'mhar_password';
GRANT ALL PRIVILEGES ON DATABAOptional

Default configuration works out of the box with:
- PostgreSQL: `localhost:5432`
- Databases: `mhar_restaurants`, `mhar_orders`
- User: `mhar_user` / Password: `mhar_password`

**To customize**, edit `Backend/*/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mhar_restaurants
    username: ${DB_USERNAME:mhar_user}
    password: ${DB_PASSWORD:mhar_password}
```

**Set environment variables:**
```bash
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
```
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

### Option A: Quick Start (One Command) ⭐ Recommended
```bash
cd Implementations/Backend
./start-all.sh
```

This starts all microservices in one terminal:
- 🌐 API Gateway: `http://localhost:8080`
- 🍽️ Restaurant Service: `http://localhost:8082`
- 📦 Order Service: `http://localhost:8081`

**To stop all services:**
```bash
./stop-all.sh
```

### Option B: Manual Start (Separate Terminals)
```bash
# Terminal 1 - Restaurant Service
cd Implementations/Backend/restaurant-service
mvn spring-boot:run

# Terminal 2 - Order Service  
cd Implementations/Backend/order-service
mvn spring-boot:run

# Terminal 3 - API Gateway
cd Implementations/Backend/api-gateway
mvn spFrontend/src/app/services/api.config.ts` or similar:

```javascript
const api = axios.create({
  baseURL: 'http://localhost:8080/api',  // ← API Gateway URL
## Step 4: Update Frontend API (30 sec)
Edit `Implementation/src/api.js`:

```javascript
const api = axios.create({
  baseURL: 'http://localhost:8080/api',  // ← Change this line
  timeout: 5000,s/Frontend
npm install
npm run dev
```

✅ Frontend running at: `http://localhost:5173`

---

### Using the Application

### Mock Authentication (Email OTP)

**Currently uses email-based OTP** (free method) instead of SMS. When you log in:

1. **Login Step**: Enter credentials → OTP sent to email message appears
2. **OTP Step**: Enter any 6-digit code (e.g., `123456`)
3. **Success**: Redirected to your dashboard

**Demo Accounts** (use any password):
- `customer@foodexpress.com` → Customer Dashboard
- `restaurant@foodexpress.com` → Restaurant Dashboard  
- `rider@foodexpress.com` → Rider Dashboard
- `admin@foodexpress.com` → Admin Dashboard

**Note**: Auth service (port 8083) is not yet implemented, so frontend uses mock authentication fallback.

---

### Test It Out!

### 1. Test Restaurant Service
```bash
curl http://localhost:8082/api/restaurants
```

### 2. Test Through API Gateway
```bash
curl http://localhost:8080/api/restaurants
```

### 3. Check Service Health
```bash
# Restaurant Service
curl http://localhost:8082/actuator/health

# Order Service
curl http://localhost:8081/actuator/health

# AAPI Endpoints

### Restaurant Service (Port 8082)
```bash
# Get all restaurants
curl http://localhost:8082/api/restaurants

# Get restaurant by ID
curl http://localhost:8082/api/restaurants/1

# Create restaurant
curl -X POST http://localhost:8082/api/restaurants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Restaurant",
    "cuisineType": "THAI",
    "phoneNumber": "+66-2-123-4567",
    "email": "test@example.com",
    "address": "123 Test St",
    "latitude": 13.7563,
    "longitude": 100.5018,
    "deliveryFee": 25.00,
    "minimumOrderAmount": 100.00,
    "openingTime": "09:00",
    "closingTime": "22:00",
    "estimatedDeliveryTime": 30,
    "ownerId": 1
  }'
```/8081/8082 already in use"
```bash
# Check what's using the port
lsof -i :8080

# Kill and restart
./stop-all.sh
./start-all.sh
```

### "Connection refused: PostgreSQL"
```bash
# Start PostgreSQL
brew services start postgresql  # macOS
sudo service postgresql start   # Linux

# Check if running
psql -U postgres -c "SELECT version();"
```

### "Unable to find a suitable main class"
```bash
# Don't run from parent directory
# Always use ./start-all.sh or go to individual service directory
cd restaurant-service
mvn spring-boot:run
```

### Services fail to start
```bash
# Check logs
tail -f logs/restaurant-service.log
tail -f logs/order-service.log
tail -f logs/api-gateway.log

# Rebuild if needed
mvn clean install
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

### Common Issues

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

--- :5173)
         ↓ HTTP
    API GATEWAY (:8080)
    ┌─────┴─────┐
    ↓           ↓
RESTAURANT   ORDER
SERVICE      SERVICE
(:8082)      (:8081)
    ↓           ↓
PostgreSQL  PostgreSQL
(mhar_restaurants) (mhar_orders)
```

**Microservices Architecture:**
- Each service has its own database
- API Gateway routes requests to services
- Services communicate via REST APIs
- Redis caching (optional, disabled in tests)T /api/riders/{id}/accept-order/{oid} - Accept delivery
POST /api/riders/{id}/confirm-delivery/{oid} - Complete
```
**Restaurant Service Database** (`mhar_restaurants`):
- `restaurants` - Restaurant information
- `menu_categories` - Menu category organization
- `menu_items` - Restaurant menu items

**Order Service Database** (`mhar_orders`):
- `orders` - Order records
- `order_items` - Individual items in orders
- `order_status_history` - Order status tracking
### Ratings
```
POST /api/ratings       - Submit rating
GET  /PostgreSQL installed and running
- [ ] Databases created: `mhar_restaurants`, `mhar_orders`
- [ ] Backend built: `mvn clean install` (from Backend directory)
- [ ] All services started: `./start-all.sh`
- [ ] Restaurant Service running: `http://localhost:8082`
- [ ] Order Service running: `http://localhost:8081`
- [ ] API Gateway running: `http://localhost:8080`
- [ ] Frontend configured to use API Gateway
- [ ] Frontend running: `http://localhost:5173`
- [ ] Test endpoints work: `curl http://localhost:8080/api/restaurants`
- [ ] ✅ Complete!

---

## Viewing Logs

All service logs are saved to `Backend/logs/`:

```bash
# View all logs simultaneously
tail -f logs/*.log

# View specific service
tail -f logs/restaurant-service.log
tail -f logs/order-service.log
tail -f logs/api-gateway.log

# Search for errors
grep -i error logs/*.log
```

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


# Team Member
1. 6688005 - Thanaporn Aritidtayamontol
2. 6688064 - Natkrittar Maswongwiwat
3. 6688091 - Pundharee Puckdinukul
4. 6688110 - Kornkanok Soongswang
5. 6688175 - Panatthaphong Yoodee
6. 6688249 - Sahatsawat Nitjaphant
