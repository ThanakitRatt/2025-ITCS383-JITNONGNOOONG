# 🎯 MharRuengSang Backend - Team Lead Action Plan

## **IMMEDIATE TASKS (Next 7 Days)**

### **📋 Phase 1: Foundation Setup (Days 1-2)**
- ✅ **DONE**: Created Maven multi-module project structure
- ✅ **DONE**: Set up API Gateway with Spring Cloud Gateway
- ✅ **DONE**: Created common module with shared DTOs
- ✅ **DONE**: Set up PostgreSQL databases for each service
- ✅ **DONE**: Set up Redis for caching and rate limiting
- ✅ **DONE**: Configure Docker Compose for local development

### **📋 Phase 2: Core Services (Days 3-5)**
- ✅ **COMPLETED**: Order Service implementation
  - ✅ Order entity with business logic
  - ✅ OrderItem entity with validation  
  - ✅ OrderStatusHistory for audit trail
  - ✅ OrderRepository with optimized queries
  - ✅ Request/Response DTOs
  - ✅ OrderService business layer (Full CRUD + status management)
  - ✅ OrderController REST endpoints (8 endpoints)
  - ✅ Redis caching configuration
  - ✅ Application configuration with profiles
- ⏳ **TODO**: Complete Restaurant Service implementation  
- ⏳ **TODO**: Set up Service Discovery (Eureka Server)
- ⏳ **TODO**: Implement inter-service communication with OpenFeign

### **📋 Phase 3: Team Coordination (Days 6-7)**
- ⏳ **TODO**: Define API contracts for team members
- ⏳ **TODO**: Set up CI/CD pipeline structure
- ⏳ **TODO**: Create development documentation
- ⏳ **TODO**: Conduct team code review sessions

---

## **🚀 NEXT IMMEDIATE STEPS**

### **1. Set Up Local Development Environment**
```bash
# Navigate to backend directory
cd /Users/thanapornartidtayamontol/Mhar/2025-ITCS383-MharRuengSang/backend

# Install PostgreSQL (if not installed)
brew install postgresql
brew services start postgresql

# Install Redis
brew install redis  
brew services start redis

# Create databases
psql -U postgres -c "CREATE DATABASE mhar_orders;"
psql -U postgres -c "CREATE DATABASE mhar_restaurants;"
```

### **2. Complete Order Service Entity Implementation**
**Priority**: HIGH - This is your core responsibility

**Files to create:**
- `Order.java` - Main order entity
- `OrderItem.java` - Order items entity
- `OrderStatusHistory.java` - Status tracking entity
- `OrderRepository.java` - Data access layer
- `OrderService.java` - Business logic
- `OrderController.java` - REST API endpoints

### **3. Complete Restaurant Service Implementation**
**Priority**: HIGH - Second core responsibility

**Files to create:**
- `Restaurant.java` - Restaurant entity
- `MenuItem.java` - Menu items entity
- `MenuCategory.java` - Menu categories entity
- `RestaurantRepository.java` - Data access layer
- `RestaurantService.java` - Business logic
- `RestaurantController.java` - REST API endpoints

---

## **📊 API CONTRACTS FOR TEAM COORDINATION**

### **Order Service APIs** (Your responsibility)
```
POST /api/v1/orders                    # Create new order
GET /api/v1/orders/{id}               # Get order details
PUT /api/v1/orders/{id}/status        # Update order status
GET /api/v1/orders/customer/{id}      # Get customer orders
GET /api/v1/orders/restaurant/{id}    # Get restaurant orders
```

### **Restaurant Service APIs** (Your responsibility)
```
GET /api/v1/restaurants               # List restaurants
GET /api/v1/restaurants/{id}          # Get restaurant details
GET /api/v1/restaurants/{id}/menu     # Get restaurant menu
POST /api/v1/restaurants/{id}/menu    # Add menu item
PUT /api/v1/restaurants/{id}/menu/{itemId}  # Update menu item
```

### **Auth Service APIs** (Natkrittar's responsibility)
```
POST /api/v1/auth/register           # User registration
POST /api/v1/auth/login              # User login
POST /api/v1/auth/validate           # Token validation
POST /api/v1/auth/refresh            # Refresh token
```

### **Payment Service APIs** (Pundharee's responsibility)
```
POST /api/v1/payments                # Process payment
GET /api/v1/payments/{id}            # Get payment status
POST /api/v1/payments/{id}/refund    # Refund payment
```

---

## **🏗️ TEAM COORDINATION STRATEGY**

### **Daily Standups** (Recommended)
- **Time**: 9:00 AM daily
- **Duration**: 15 minutes
- **Format**: What I did yesterday, what I'm doing today, any blockers

### **Code Review Process**
1. All code must go through pull requests
2. Minimum 1 reviewer (you as team lead)
3. Run tests before merging
4. Follow Java coding standards

### **Branch Strategy**
```
main                    # Production ready code
develop                 # Integration branch
feature/order-service   # Your order service work
feature/restaurant-service # Your restaurant service work
feature/auth-service    # Natkrittar's work
feature/payment-service # Pundharee's work
```

---

## **⚡ SCALABILITY CHECKPOINTS**

### **Database Performance**
- [ ] Connection pooling configured (HikariCP)
- [ ] Database indexes on frequently queried fields
- [ ] Read replicas for heavy read operations
- [ ] Database partitioning strategy for orders table

### **Caching Strategy**
- [ ] Redis caching for restaurant data
- [ ] Application-level caching for menu items
- [ ] Cache invalidation strategy
- [ ] Cache monitoring and metrics

### **Service Communication**
- [ ] Circuit breaker pattern implemented
- [ ] Retry logic with exponential backoff
- [ ] Async processing for non-critical operations
- [ ] Event-driven architecture with Kafka (future)

---

## **📞 SUPPORT CONTACTS**

**Team Lead (You)**: Order Service + Restaurant Service + Coordination
**Natkrittar**: Auth Service + Admin Panel
**Pundharee**: Payment Service + Rider Management  
**Kornkanok**: Web Frontend (React)
**Panatthaphong**: Mobile App
**Sahatsawat**: DevOps + QA + Testing

---

Would you like me to help you with:
1. **Setting up the local environment** (PostgreSQL + Redis)
2. **Implementing Order Service entities and controllers**
3. **Creating the Restaurant Service**
4. **Setting up Docker Compose for the team**
