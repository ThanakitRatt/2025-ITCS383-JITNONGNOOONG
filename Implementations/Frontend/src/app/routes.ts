import { createHashRouter } from "react-router";
import Root from "./components/Root";
import SplashScreen from "./pages/SplashScreen";
import LoginPage from "./pages/LoginPage";
import CustomerRegistration from "./pages/customer/CustomerRegistration";
import RestaurantList from "./pages/customer/RestaurantList";
import RestaurantDetail from "./pages/customer/RestaurantDetail";
import Checkout from "./pages/customer/Checkout";
import OrderTracking from "./pages/customer/OrderTracking";
import RateRider from "./pages/customer/RateRider";
import RestaurantDashboard from "./pages/restaurant/RestaurantDashboard";
import RestaurantOrders from "./pages/restaurant/RestaurantOrders";
import MenuManagement from "./pages/restaurant/MenuManagement";
import RestaurantPromotions from "./pages/restaurant/RestaurantPromotions";
import RestaurantCategories from "./pages/restaurant/RestaurantCategories";
import RiderDashboard from "./pages/rider/RiderDashboard";
import RiderDelivery from "./pages/rider/RiderDelivery";
import AdminDashboard from "./pages/admin/AdminDashboard";
import AdminAccounts from "./pages/admin/AdminAccounts";
import AdminPromotions from "./pages/admin/AdminPromotions";

export const router = createHashRouter([
  {
    path: "/",
    Component: Root,
    children: [
      // Splash & Auth
      { index: true, Component: SplashScreen },
      { path: "login", Component: LoginPage },

      // Customer Routes
      { path: "customer/register", Component: CustomerRegistration },
      { path: "customer/restaurants", Component: RestaurantList },
      { path: "customer/restaurant/:id", Component: RestaurantDetail },
      { path: "customer/checkout", Component: Checkout },
      { path: "customer/orders", Component: OrderTracking },
      { path: "customer/rate/:orderId", Component: RateRider },

      // Restaurant Routes
      { path: "restaurant/dashboard", Component: RestaurantDashboard },
      { path: "restaurant/orders", Component: RestaurantOrders },
      { path: "restaurant/menu", Component: MenuManagement },
      { path: "restaurant/promotions", Component: RestaurantPromotions },
      { path: "restaurant/categories", Component: RestaurantCategories },

      // Rider Routes
      { path: "rider/dashboard", Component: RiderDashboard },
      { path: "rider/delivery/:orderId", Component: RiderDelivery },

      // Admin Routes
      { path: "admin/dashboard", Component: AdminDashboard },
      { path: "admin/accounts", Component: AdminAccounts },
      { path: "admin/promotions", Component: AdminPromotions },
    ],
  },
]);