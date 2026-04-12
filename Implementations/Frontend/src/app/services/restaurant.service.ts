/**
 * Restaurant Service
 * 
 * Handles all restaurant-related API calls:
 * - Fetching restaurant lists
 * - Searching restaurants
 * - Getting restaurant details
 * - Managing menus
 */

import apiClient, { ApiResponse, PaginatedResponse, handleApiError } from './apiClient';
import { API_ENDPOINTS } from '../config/api.config';

/**
 * Restaurant Type Definitions
 */
export interface Restaurant {
  id: string;
  name: string;
  description?: string;
  cuisineType: string;
  address: string;
  latitude?: number;
  longitude?: number;
  phoneNumber: string;
  email?: string;
  openingTime?: string;
  closingTime?: string;
  ownerId?: number;
  status?: string;
  isActive: boolean;
  acceptsOrders?: boolean;
  averageRating: number;
  totalReviews?: number;
  logoUrl?: string;
  coverImageUrl?: string;
  minimumOrderAmount?: number;
  deliveryFee?: number;
  estimatedDeliveryTime?: number;
  isCurrentlyOpen?: boolean;
}

export interface Address {
  street: string;
  city: string;
  postalCode: string;
  latitude: number;
  longitude: number;
}

export interface MenuItem {
  id: string;
  restaurantId: string;
  name: string;
  description: string;
  price: number;
  categoryId?: number;
  categoryName?: string;
  imageUrl?: string;
  isAvailable: boolean;
  preparationTime?: number;
}

export interface MenuCategory {
  id: string;
  restaurantId: string;
  name: string;
  description?: string;
  displayOrder: number;
}

export interface RestaurantReview {
  id: string;
  restaurantId: string;
  orderId: string;
  customerId: string;
  customerName: string;
  rating: number;
  reviewText?: string;
  createdAt: string;
}

export type ReviewSort = 'recent' | 'highest' | 'lowest';

export interface CreateRestaurantReviewRequest {
  orderId: string;
  customerId: string;
  rating: number;
  reviewText?: string;
}

export interface CreateRestaurantRequest {
  name: string;
  description?: string;
  cuisineType: string;
  phoneNumber: string;
  email: string;
  address: string;
  latitude: number;
  longitude: number;
  deliveryFee: number;
  minimumOrderAmount: number;
  openingTime: string;
  closingTime: string;
  estimatedDeliveryTime: number;
  ownerId: number;
}

export interface UpdateRestaurantRequest {
  name?: string;
  description?: string;
  cuisineType?: string;
  phoneNumber?: string;
  email?: string;
  address?: string;
  latitude?: number;
  longitude?: number;
  deliveryFee?: number;
  minimumOrderAmount?: number;
  openingTime?: string;
  closingTime?: string;
  estimatedDeliveryTime?: number;
}

export interface CreateMenuItemRequest {
  name: string;
  description: string;
  price: number;
  categoryId: number;
  imageUrl?: string;
  preparationTime?: number;
  isAvailable?: boolean;
  displayOrder?: number;
}

export interface UpdateMenuItemRequest {
  name?: string;
  description?: string;
  price?: number;
  categoryId?: number;
  imageUrl?: string;
  preparationTime?: number;
  isAvailable?: boolean;
  displayOrder?: number;
}

export interface CreateCategoryRequest {
  name: string;
  description?: string;
  displayOrder?: number;
}

export interface SearchFilters {
  searchTerm?: string;
  cuisineType?: string;
  minRating?: number;
  maxDeliveryFee?: number;
  latitude?: number;
  longitude?: number;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}

/**
 * Restaurant Service Class
 */
class RestaurantService {
  private normalizeRestaurant(restaurant: any): Restaurant {
    const source = Array.isArray(restaurant) ? (restaurant[0] ?? {}) : (restaurant ?? {});
    let safeName = 'Restaurant';

    if (typeof source.name === 'string') {
      safeName = source.name;
    } else if (typeof source.restaurantName === 'string') {
      safeName = source.restaurantName;
    }

    return {
      ...source,
      id: String(source.id ?? ''),
      name: safeName,
      description: source.description,
      address: source.address,
      cuisineType: typeof (source.cuisineType ?? source.cuisine_type) === 'string' ? (source.cuisineType ?? source.cuisine_type) : source.cuisineType,
      phoneNumber: typeof (source.phoneNumber ?? source.phone_number) === 'string' ? (source.phoneNumber ?? source.phone_number) : source.phoneNumber,
      ownerId: source.ownerId ?? source.owner_id ?? source.ownerId,
      isActive: source.isActive ?? source.is_active ?? source.isActive,
      acceptsOrders: source.acceptsOrders ?? source.accepts_orders,
      averageRating: Number(source.averageRating ?? source.average_rating ?? 0),
      totalReviews: source.totalReviews ?? source.total_reviews,
      logoUrl: source.logoUrl ?? source.logo_url,
      coverImageUrl: source.coverImageUrl ?? source.cover_image_url,
      minimumOrderAmount: source.minimumOrderAmount ?? source.minimum_order_amount,
      deliveryFee: source.deliveryFee ?? source.delivery_fee,
      estimatedDeliveryTime: source.estimatedDeliveryTime ?? source.estimated_delivery_time,
      openingTime: source.openingTime ?? source.opening_time,
      closingTime: source.closingTime ?? source.closing_time,
    };
  }

  private normalizeReview(review: any): RestaurantReview {
    return {
      ...review,
      id: String(review.id),
      restaurantId: String(review.restaurantId ?? review.restaurant_id),
      orderId: String(review.orderId ?? review.order_id),
      customerId: String(review.customerId ?? review.customer_id),
      customerName: review.customerName ?? review.customer_name ?? 'Customer',
      rating: Number(review.rating ?? 0),
      reviewText: review.reviewText ?? review.review_text,
      createdAt: review.createdAt ?? review.created_at,
    };
  }

  /**
   * Get all restaurants with pagination
   */
  async getAllRestaurants(
    page: number = 0,
    size: number = 20,
    sortBy: string = 'name',
    sortDir: 'asc' | 'desc' = 'asc'
  ): Promise<PaginatedResponse<Restaurant>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<Restaurant>>>(
        API_ENDPOINTS.RESTAURANTS.BASE,
        {
          params: { page, size, sortBy, sortDir },
        }
      );
      
      return {
        ...response.data.data,
        content: (response.data.data.content ?? []).map((restaurant) => this.normalizeRestaurant(restaurant)),
      };
    } catch (error) {
      console.error('Failed to fetch restaurants:', handleApiError(error));
      throw error;
    }
  }

  /**
   * Search restaurants with filters
   */
  async searchRestaurants(filters: SearchFilters): Promise<PaginatedResponse<Restaurant>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<Restaurant>>>(
        API_ENDPOINTS.RESTAURANTS.SEARCH,
        {
          params: {
            ...filters,
            query: filters.searchTerm,
            cuisine: filters.cuisineType,
          },
        }
      );
      
      const payload = response.data.data;
      const content = Array.isArray(payload) ? payload : payload.content ?? [];

      return {
        ...(Array.isArray(payload) ? { content, totalElements: content.length, totalPages: 1, size: content.length, number: 0, first: true, last: true } : payload),
        content: content.map((restaurant: any) => this.normalizeRestaurant(restaurant)),
      };
    } catch (error) {
      console.error('Failed to search restaurants:', handleApiError(error));
      throw error;
    }
  }

  /**
   * Get restaurant by ID
   */
  async getRestaurantById(id: string): Promise<Restaurant> {
    try {
      const response = await apiClient.get<ApiResponse<Restaurant>>(
        API_ENDPOINTS.RESTAURANTS.BY_ID(id)
      );
      
      return this.normalizeRestaurant(response.data.data);
    } catch (error) {
      console.error(`Failed to fetch restaurant ${id}:`, handleApiError(error));
      throw error;
    }
  }

  async getRestaurantReviews(
    restaurantId: string,
    sort: ReviewSort = 'recent'
  ): Promise<RestaurantReview[]> {
    try {
      const response = await apiClient.get<ApiResponse<RestaurantReview[]>>(
        API_ENDPOINTS.RESTAURANTS.REVIEWS(restaurantId),
        {
          params: { sort },
        }
      );

      return response.data.data.map((review) => this.normalizeReview(review));
    } catch (error) {
      console.error(`Failed to fetch reviews for restaurant ${restaurantId}:`, handleApiError(error));
      throw error;
    }
  }

  async submitRestaurantReview(
    restaurantId: string,
    reviewData: CreateRestaurantReviewRequest
  ): Promise<RestaurantReview> {
    try {
      const response = await apiClient.post<ApiResponse<RestaurantReview>>(
        API_ENDPOINTS.RESTAURANTS.REVIEWS(restaurantId),
        reviewData
      );

      return this.normalizeReview(response.data.data);
    } catch (error) {
      console.error(`Failed to submit review for restaurant ${restaurantId}:`, handleApiError(error));
      throw error;
    }
  }

  /**
   * Get restaurants by cuisine type
   */
  async getRestaurantsByCuisine(
    cuisineType: string,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<Restaurant>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<Restaurant>>>(
        API_ENDPOINTS.RESTAURANTS.BY_CUISINE(cuisineType),
        {
          params: { page, size },
        }
      );
      
      return {
        ...response.data.data,
        content: (response.data.data.content ?? []).map((restaurant) => this.normalizeRestaurant(restaurant)),
      };
    } catch (error) {
      console.error(`Failed to fetch ${cuisineType} restaurants:`, handleApiError(error));
      throw error;
    }
  }

  /**
   * Get available cuisine types
   */
  async getCuisineTypes(): Promise<string[]> {
    try {
      const response = await apiClient.get<ApiResponse<string[]>>(
        `${API_ENDPOINTS.RESTAURANTS.BASE}/cuisine-types`
      );
      return response.data.data;
    } catch (error) {
      console.error('Failed to fetch cuisine types:', handleApiError(error));
      throw error;
    }
  }

  /**
   * Get top rated restaurants
   */
  async getTopRatedRestaurants(limit: number = 10): Promise<Restaurant[]> {
    try {
      const response = await apiClient.get<ApiResponse<Restaurant[]>>(
        API_ENDPOINTS.RESTAURANTS.TOP_RATED,
        {
          params: { limit },
        }
      );
      
      return response.data.data;
    } catch (error) {
      console.error('Failed to fetch top rated restaurants:', handleApiError(error));
      throw error;
    }
  }

  /**
   * Get restaurant menu
   */
  async getRestaurantMenu(restaurantId: string): Promise<MenuItem[]> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<MenuItem>>>(
        API_ENDPOINTS.RESTAURANTS.MENU(restaurantId)
      );
      
      return response.data.data.content ?? [];
    } catch (error) {
      console.error(`Failed to fetch menu for restaurant ${restaurantId}:`, handleApiError(error));
      throw error;
    }
  }

  /**
   * Get menu item details
   */
  async getMenuItem(restaurantId: string, itemId: string): Promise<MenuItem> {
    try {
      const response = await apiClient.get<ApiResponse<MenuItem>>(
        API_ENDPOINTS.RESTAURANTS.MENU_ITEM(restaurantId, itemId)
      );
      
      return response.data.data;
    } catch (error) {
      console.error(`Failed to fetch menu item ${itemId}:`, handleApiError(error));
      throw error;
    }
  }

  // ==================== CREATE OPERATIONS ====================

  /**
   * Create new restaurant
   */
  async createRestaurant(data: CreateRestaurantRequest): Promise<Restaurant> {
    try {
      const response = await apiClient.post<ApiResponse<Restaurant>>(
        API_ENDPOINTS.RESTAURANTS.BASE,
        data
      );
      
      return response.data.data;
    } catch (error) {
      console.error('Failed to create restaurant:', handleApiError(error));
      throw error;
    }
  }

  /**
   * Add menu item to restaurant
   */
  async addMenuItem(restaurantId: string, data: CreateMenuItemRequest): Promise<MenuItem> {
    try {
      const response = await apiClient.post<ApiResponse<MenuItem>>(
        API_ENDPOINTS.RESTAURANTS.MENU(restaurantId),
        data
      );
      
      return response.data.data;
    } catch (error) {
      console.error('Failed to add menu item:', handleApiError(error));
      throw error;
    }
  }

  /**
   * Add menu category
   */
  async addMenuCategory(restaurantId: string, data: CreateCategoryRequest): Promise<MenuCategory> {
    try {
      const response = await apiClient.post<ApiResponse<MenuCategory>>(
        API_ENDPOINTS.RESTAURANTS.CATEGORIES(restaurantId),
        data
      );
      
      return response.data.data;
    } catch (error) {
      console.error('Failed to add category:', handleApiError(error));
      throw error;
    }
  }

  // ==================== UPDATE OPERATIONS ====================

  /**
   * Update restaurant information
   */
  async updateRestaurant(id: string, data: UpdateRestaurantRequest): Promise<Restaurant> {
    try {
      const response = await apiClient.put<ApiResponse<Restaurant>>(
        API_ENDPOINTS.RESTAURANTS.BY_ID(id),
        data
      );
      
      return response.data.data;
    } catch (error) {
      console.error('Failed to update restaurant:', handleApiError(error));
      throw error;
    }
  }

  /**
   * Update menu item
   */
  async updateMenuItem(
    restaurantId: string,
    itemId: string,
    data: UpdateMenuItemRequest
  ): Promise<MenuItem> {
    try {
      const response = await apiClient.put<ApiResponse<MenuItem>>(
        API_ENDPOINTS.RESTAURANTS.MENU_ITEM(restaurantId, itemId),
        data
      );
      
      return response.data.data;
    } catch (error) {
      console.error('Failed to update menu item:', handleApiError(error));
      throw error;
    }
  }

  /**
   * Update restaurant status (Admin)
   */
  async updateRestaurantStatus(
    id: string,
    status: string,
    reason?: string
  ): Promise<Restaurant> {
    try {
      const response = await apiClient.put<ApiResponse<Restaurant>>(
        API_ENDPOINTS.RESTAURANTS.STATUS(id),
        null,
        { params: { status, reason } }
      );
      
      return response.data.data;
    } catch (error) {
      console.error('Failed to update restaurant status:', handleApiError(error));
      throw error;
    }
  }

  /**
   * Toggle restaurant availability
   */
  async toggleRestaurantAvailability(
    id: string,
    acceptsOrders: boolean
  ): Promise<Restaurant> {
    try {
      const response = await apiClient.put<ApiResponse<Restaurant>>(
        API_ENDPOINTS.RESTAURANTS.AVAILABILITY(id),
        null,
        { params: { acceptsOrders } }
      );
      
      return response.data.data;
    } catch (error) {
      console.error('Failed to toggle availability:', handleApiError(error));
      throw error;
    }
  }

  // ==================== DELETE OPERATIONS ====================

  /**
   * Delete restaurant
   */
  async deleteRestaurant(id: string): Promise<void> {
    try {
      await apiClient.delete(API_ENDPOINTS.RESTAURANTS.BY_ID(id));
    } catch (error) {
      console.error('Failed to delete restaurant:', handleApiError(error));
      throw error;
    }
  }

  /**
   * Delete menu item
   */
  async deleteMenuItem(restaurantId: string, itemId: string): Promise<void> {
    try {
      await apiClient.delete(
        API_ENDPOINTS.RESTAURANTS.MENU_ITEM(restaurantId, itemId)
      );
    } catch (error) {
      console.error('Failed to delete menu item:', handleApiError(error));
      throw error;
    }
  }

  /**
   * Delete menu category
   */
  async deleteMenuCategory(restaurantId: string, categoryId: string): Promise<void> {
    try {
      await apiClient.delete(
        API_ENDPOINTS.RESTAURANTS.CATEGORY(restaurantId, categoryId)
      );
    } catch (error) {
      console.error('Failed to delete category:', handleApiError(error));
      throw error;
    }
  }

  // ==================== ADDITIONAL OPERATIONS ====================

  /**
   * Get restaurants by owner
   */
  async getOwnerRestaurants(ownerId: string): Promise<Restaurant[]> {
    try {
      const response = await apiClient.get<ApiResponse<Restaurant[]>>(
        API_ENDPOINTS.RESTAURANTS.BY_OWNER(ownerId)
      );
      
      return response.data.data;
    } catch (error) {
      console.error('Failed to fetch owner restaurants:', handleApiError(error));
      throw error;
    }
  }

  /**
   * Get restaurant categories
   */
  async getRestaurantCategories(restaurantId: string): Promise<MenuCategory[]> {
    try {
      const response = await apiClient.get<ApiResponse<MenuCategory[]>>(
        API_ENDPOINTS.RESTAURANTS.CATEGORIES(restaurantId)
      );
      
      return response.data.data;
    } catch (error) {
      console.error('Failed to fetch categories:', handleApiError(error));
      throw error;
    }
  }

  /**
   * Search menu items within restaurant
   */
  async searchMenuItems(
    restaurantId: string,
    searchTerm: string,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<MenuItem>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<MenuItem>>>(
        API_ENDPOINTS.RESTAURANTS.MENU_SEARCH(restaurantId),
        { params: { searchTerm, page, size } }
      );
      
      return response.data.data;
    } catch (error) {
      console.error('Failed to search menu items:', handleApiError(error));
      throw error;
    }
  }

  /**
   * Calculate distance between two coordinates (Haversine formula)
   */
  calculateDistance(
    lat1: number,
    lon1: number,
    lat2: number,
    lon2: number
  ): number {
    const R = 6371; // Earth's radius in km
    const dLat = this.deg2rad(lat2 - lat1);
    const dLon = this.deg2rad(lon2 - lon1);
    
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(this.deg2rad(lat1)) *
        Math.cos(this.deg2rad(lat2)) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);
    
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distance = R * c;
    
    return Math.round(distance * 10) / 10; // Round to 1 decimal place
  }

  private deg2rad(deg: number): number {
    return deg * (Math.PI / 180);
  }
}

// Export singleton instance
export const restaurantService = new RestaurantService();
export default restaurantService;
