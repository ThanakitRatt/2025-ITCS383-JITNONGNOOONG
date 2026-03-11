/**
 * Admin Service
 *
 * Fetches platform-wide statistics for the admin dashboard.
 */

import apiClient from './apiClient';
import { API_ENDPOINTS } from '../config/api.config';

export interface OrderStats {
  todayOrders: number;
  monthOrders: number;
  totalOrders: number;
  todayRevenue: number;
  monthRevenue: number;
}

export interface RestaurantStats {
  totalRestaurants: number;
  activeRestaurants: number;
}

export interface AdminStats {
  orders: OrderStats;
  restaurants: RestaurantStats;
}

export const getAdminStats = async (): Promise<AdminStats> => {
  const [ordersRes, restaurantsRes] = await Promise.all([
    apiClient.get<{ success: boolean; data: OrderStats }>(API_ENDPOINTS.ADMIN.ORDER_STATS),
    apiClient.get<{ success: boolean; data: RestaurantStats }>(API_ENDPOINTS.ADMIN.RESTAURANT_STATS),
  ]);
  return {
    orders: ordersRes.data.data,
    restaurants: restaurantsRes.data.data,
  };
};
