# MharRuengSang Delivery Platform
## Maintenance by JITNONGNOOONG

## Project Overview: MharRuengSang Food Delivery System

The goal of this project is to build a food delivery platform that connects customers, restaurants, riders, and administrators in one system. The current implementation is designed as a web-based platform with a React frontend, a Node.js Express backend, and a shared cloud-hosted MySQL database.

## Food Delivery Mobile App
Link: https://github.com/minxc9/JITNONGNOOONG_Mobile-App.git

### 1. User Roles

**Customer**
- Browse restaurants and menus
- Place food orders
- Make payments
- Track order-related activity

**Restaurant**
- Manage restaurant information
- Manage menu categories and menu items
- Receive and handle customer orders
- Review customer rating

**Rider**
- View rider-related delivery tasks
- Access delivery information from the system

**Admin**
- Monitor platform statistics
- Access management-related system data

The current runnable implementation in this repository is:

- Frontend: React + Vite
- Backend: Node.js + Express
- Database: cloud-hosted MySQL

## Project Structure

```text
.
├── README.md
├── HANDOVER.md
├── D2_CODE_QUALITY.md
├── D3_CHANGE_REQUESTS.md
├── D4_IMPACT_ANALYSIS.md
├── D5_AI-USAGE.md
├── Designs/
└── Implementations/
    ├── Frontend/          # React + Vite application
    └── Backend-NodeJS/    # Active Express + MySQL backend
```

## Development URLs

| Service | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Deployed Node.js API | https://two025-itcs383-jitnongnooong-2.onrender.com |
| Deployed API root | https://two025-itcs383-jitnongnooong-2.onrender.com/api/v1 |

## Current Deployment

The current web frontend is already connected to the deployed backend on Render, and that backend already uses the shared cloud MySQL database.

```text
https://two025-itcs383-jitnongnooong-2.onrender.com
```

The frontend configuration in `Implementations/Frontend` currently defaults to that Render backend unless you override `VITE_API_BASE_URL`.

## Prerequisites

- Node.js 18+
- npm 9+

Check your environment:

```bash
node --version
npm --version
```

## Quick Start

### 1. Run the frontend only

```bash
cd Implementations/Frontend
npm install
npm run dev
```

The frontend will start at `http://localhost:5173`.

For the current version of this project, you do not need to run the backend or database locally.

Already deployed for you:

- Backend API on Render
- Shared cloud MySQL database

So for normal use, you do not need to:

- start `Implementations/Backend-NodeJS`
- install local MySQL
- create a local database
- create or edit a frontend `.env.local` file

### 2. Optional: run the backend locally

Only do this if you are specifically developing or debugging backend code.

```bash
cd Implementations/Backend-NodeJS
npm install
npm run dev
```

The local API will start at `http://localhost:8080`.

To confirm the local backend is running successfully, open:

```text
http://localhost:8080/
```

You should see:

```json
{"message":"Welcome to MharRuengSang Node.js API"}
```

## Backend Configuration

- The deployed backend already uses the project's shared cloud database.
- No local backend or local database setup is required for normal frontend development.
- The local backend in `Implementations/Backend-NodeJS` is optional and only needed for backend work.

## Frontend API Configuration

The frontend reads `VITE_API_BASE_URL`.

- Current default deployed backend: `https://two025-itcs383-jitnongnooong-2.onrender.com`
- Common local override: `http://localhost:8080`

If you want the frontend to talk to your local backend instead of Render, create `Implementations/Frontend/.env.local` with:

```bash
VITE_API_BASE_URL=http://localhost:8080
```

Then restart the Vite dev server.

## Authentication Flow

The current backend uses a mock OTP flow:

1. Log in with email and password.
2. Submit any 6-digit OTP code.
3. The backend returns a token and logs the user in.

## Demo Accounts

These demo accounts are intended for the shared backend data used by the project.

| Role | Email | Password |
|---|---|---|
| Customer | `customer@foodexpress.com` | `customer123` |
| Customer | `sarah@foodexpress.com` | `sarah123` |
| Restaurant | `restaurant@foodexpress.com` | `restaurant123` |
| Restaurant | `sushi@foodexpress.com` | `sushi123` |
| Rider | `rider@foodexpress.com` | `rider123` |
| Admin | `admin@foodexpress.com` | `admin123` |

Example OTP: `123456`

## Useful API Checks

```bash
curl https://two025-itcs383-jitnongnooong-2.onrender.com/
curl https://two025-itcs383-jitnongnooong-2.onrender.com/api/v1/restaurants/search
curl https://two025-itcs383-jitnongnooong-2.onrender.com/api/v1/orders/customer/100
```

Optional local backend checks:

```bash
curl http://localhost:8080/
curl http://localhost:8080/api/v1/restaurants
curl http://localhost:8080/api/v1/orders/admin/stats
```

## Running Tests

Backend:

```bash
cd Implementations/Backend-NodeJS
npm test
```

Backend coverage:

```bash
cd Implementations/Backend-NodeJS
npm run test:coverage
```

Frontend:

```bash
cd Implementations/Frontend
npm test
```

Frontend coverage:

```bash
cd Implementations/Frontend
npm run test:coverage
```

## Main Features

- User registration and login
- Mock OTP verification
- Restaurant browsing and menu management
- Order creation and order tracking
- Rider and customer endpoints
- Payment-related API routes
- Admin statistics endpoints

## Extra Documentation

- `HANDOVER.md`
- `Implementations/QUICK_START.md`
- `Implementations/INTEGRATION_GUIDE.md`
- `Implementations/AUTHENTICATION_README.md`
- `Implementations/ARCHITECTURE.md`
- `Implementations/backend-architecture.md`

## Troubleshooting

If the backend cannot connect to the database:

- Check that you are using the project’s default backend configuration.
- If you created a custom `.env`, verify it is not overriding the cloud database settings.
- Confirm your network can reach the hosted database service.
- Confirm the Aiven MySQL service is powered on.

If the frontend cannot reach the backend:

- For the default setup, make sure the frontend is using `https://two025-itcs383-jitnongnooong-2.onrender.com`.
- If you intentionally switched to local backend development, make sure the backend is running on port `8080`.
- For local backend development, make sure the frontend is using `http://localhost:8080` as `VITE_API_BASE_URL`.

If demo login fails:

- Confirm the shared database is reachable.
- Check whether the shared demo data has been changed.

If order history fails with a database error:

- Confirm the `restaurant_reviews` table exists in the cloud database.

If restaurant order status updates fail with `Data truncated for column 'status'`:

- Update the `orders.status` enum in the cloud database so it includes:
  `PENDING`, `CONFIRMED`, `PREPARING`, `READY_FOR_PICKUP`, `PICKED_UP`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`, and `REFUNDED`.


### 2. System Workflow

The system workflow in the current codebase is:

1. The frontend starts on `http://localhost:5173`.
2. The frontend sends API requests to the deployed backend using `/api/v1/...` endpoints.
3. The deployed backend processes authentication, restaurant, order, payment, rider, and customer requests.
4. The deployed backend reads from and writes to the shared cloud database.
5. The frontend displays returned data based on the logged-in user's role.

If you choose to run the backend locally for backend development, it follows the same flow through `http://localhost:8080`.

---

## Functional Requirements

### **A. Customer Journey**

- **Registration & Profile:** Must provide First Name, Last Name, Address, and Payment Info (Credit Card).
- **Security:** Multi-factor authentication (OTP via mobile) is required for login. Passwords must be updated every month.
- **Discovery:** View a list of restaurants with filters for:
    - Cuisine type (Thai, Japanese, Western, etc.)
    - Distance from the customer's location.
- **Ordering:** Select food items and checkout.
- **Payment:** Support for Credit Cards and Bank Transfer (QR Code/PromptPay).


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

## New Features Request

## 1. Restaurant Rating System

### **FR1.1: Submit Rating and Review**

- **Description:** The system shall allow logged-in Customers to submit a star rating (1 to 5 stars) and an optional text review for a restaurant.
- **Condition:** A customer can only rate a restaurant after an order from that restaurant has been marked as "Completed" or "Delivered".

### **FR1.2: Display Aggregate Ratings**

- **Description:** The system shall display the average star rating and the total number of reviews on the restaurant's card (in search/listing views) and on the restaurant's detailed profile page.

### **FR1.3: Real-time Rating Calculation**

- **Description:** The system shall automatically recalculate and update the restaurant's average rating whenever a new review is submitted.

### **FR1.4: Review Visibility & Sorting**

- **Description:** The system shall display a list of customer reviews on the restaurant's profile page, sortable by "Most Recent", "Highest Rated", and "Lowest Rated".

### **FR1.5: Restaurant Owner Dashboard View**

- **Description:** The system shall provide Restaurant Owners with a dedicated dashboard section to view their overall rating metrics and read individual customer reviews.

---

## 2. Driver Tracking System with Map - Mock Data

### **FR2.1: Live Map UI Initialization**

- **Description:** The system shall display an interactive map interface on the Customer's "Order Tracking" screen once an order status changes to "Picked Up" or "On the Way".

### **FR2.2: Mock GPS Coordinate Generation**

- **Description:** The system shall generate mock GPS coordinates (latitude and longitude) that represent the Rider's current location relative to the restaurant and the delivery address.

### **FR2.3: Rider Movement Simulation**

- **Description:** The system shall periodically (e.g., every 5 seconds) update the mock GPS coordinates to simulate the Rider moving along a path toward the delivery destination.

### **FR2.4: Display Markers & Route**

- **Description:** The map shall display distinct visual markers for the Restaurant location, the Customer's delivery location, and a moving icon (e.g., a motorcycle) representing the Rider's mock location.

### **FR2.5: Estimated Time of Arrival (ETA)**

- **Description:** The system shall calculate and display a simulated Estimated Time of Arrival (ETA) that dynamically updates based on the remaining distance of the mock coordinates to the destination.

### **FR2.6: Arrival and Status Transition**

- **Description:** When the mock coordinates reach a defined radius of the delivery location, the system shall prompt the mock UI to transition the order status to "Arrived" or "Delivered".

---

### Workflow Overview

**A. Customer Flow**
- Register and log in with email and password
- Complete login using the current mock OTP flow
- View restaurant listings and available menu items
- Place orders through the frontend
- Use payment-related features supported by the backend

**B. Restaurant Flow**
- Manage restaurant records
- Add and organize menu categories
- Add, update, and manage menu items
- View incoming order data from customers

**C. Rider Flow**
- Access rider endpoints from the backend
- View available delivery-related information
- Support delivery operations inside the platform workflow

**D. Admin Flow**
- View admin statistics endpoints
- Access order and restaurant summary data
- Use platform data for management and monitoring

### 4. Authentication and Data Flow

The current authentication flow is implemented as:

1. The user enters email and password.
2. The backend validates the login request.
3. The system asks for OTP verification.
4. Any 6-digit OTP is accepted in the current mock implementation.
5. The backend returns an authentication token.
6. The frontend uses that token for protected API requests.

All major data flows through the same pattern:

- Frontend sends request
- Backend validates and processes request
- Backend queries or updates the cloud database
- Backend returns JSON response
- Frontend renders the result to the user
