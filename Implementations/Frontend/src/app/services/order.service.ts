/**
 * Order Service
 * 
 * Handles all order-related API calls:
 * - Creating orders
 * - Fetching order details
 * - Updating order status
 * - Cancelling orders
 */

import apiClient, { ApiResponse, PaginatedResponse, handleApiError } from './apiClient';
import { API_ENDPOINTS } from '../config/api.config';

/**
 * Order Type Definitions
 */
export interface Order {
  id: number;
  orderNumber: string;
  customerId: number;
  customerName?: string;
  customerPhoneNumber?: string;
  restaurantId: number;
  restaurantName?: string;
  riderId?: number;
  deliveryAddress: string;
  deliveryLatitude?: number;
  deliveryLongitude?: number;
  orderItems?: OrderItem[];
  deliveryFee: number;
  totalAmount: number;
  specialInstructions?: string;
  restaurantReviewId?: number;
  restaurantRating?: number;
  restaurantReviewText?: string;
  restaurantReviewedAt?: string;
  status: OrderStatus;
  estimatedDeliveryTime?: string;
  createdAt: string;
  updatedAt: string;
}

export interface OrderItem {
  id: string;
  orderId: string;
  menuItemId: string;
  menuItemName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  specialRequests?: string;
}

export enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  PREPARING = 'PREPARING',
  READY_FOR_PICKUP = 'READY_FOR_PICKUP',
  PICKED_UP = 'PICKED_UP',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED',
}

export enum PaymentMethod {
  CREDIT_CARD = 'CREDIT_CARD',
  BANK_TRANSFER_QR = 'BANK_TRANSFER_QR',
  PROMPTPAY = 'PROMPTPAY',
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED',
}

export interface CreateOrderRequest {
  customerId: string;
  restaurantId: string;
  deliveryAddress: string;
  deliveryLatitude?: number;
  deliveryLongitude?: number;
  orderItems: CreateOrderItem[];
  specialInstructions?: string;
}

export interface CreateOrderItem {
  menuItemId: string;
  menuItemName: string;
  quantity: number;
  unitPrice: number;
  specialRequests?: string;
}

export interface UpdateOrderStatusRequest {
  newStatus: OrderStatus;
  updatedBy: number;
  notes?: string;
}

interface CustomerContactInfo {
  id: number;
  name?: string;
  phoneNumber?: string;
}

/**
 * Order Service Class
 */
class OrderService {
  private normalizeOrderItem(item: any): OrderItem {
    return {
      ...item,
      id: String(item.id),
      orderId: String(item.orderId ?? item.order_id ?? ''),
      menuItemId: String(item.menuItemId ?? item.menu_item_id ?? ''),
      menuItemName: item.menuItemName ?? item.menu_item_name ?? '',
      quantity: Number(item.quantity ?? 0),
      unitPrice: Number(item.unitPrice ?? item.unit_price ?? 0),
      totalPrice: Number(item.totalPrice ?? item.total_price ?? 0),
      specialRequests: item.specialRequests ?? item.special_instructions,
    };
  }

  private normalizeOrder(order: any): Order {
    const orderItems = order.orderItems ?? order.order_items ?? order.items ?? [];

    return {
      ...order,
      id: Number(order.id),
      orderNumber: order.orderNumber ?? order.order_number ?? '',
      customerId: Number(order.customerId ?? order.customer_id ?? 0),
      customerName: order.customerName ?? order.customer_name,
      customerPhoneNumber: order.customerPhoneNumber ?? order.customer_phone_number,
      restaurantId: Number(order.restaurantId ?? order.restaurant_id ?? 0),
      restaurantName: order.restaurantName ?? order.restaurant_name,
      riderId: order.riderId ?? order.rider_id,
      deliveryAddress: order.deliveryAddress ?? order.delivery_address ?? '',
      deliveryLatitude: order.deliveryLatitude ?? order.delivery_latitude,
      deliveryLongitude: order.deliveryLongitude ?? order.delivery_longitude,
      orderItems: orderItems.map((item: any) => this.normalizeOrderItem(item)),
      deliveryFee: Number(order.deliveryFee ?? order.delivery_fee ?? 0),
      totalAmount: Number(order.totalAmount ?? order.total_amount ?? 0),
      specialInstructions: order.specialInstructions ?? order.special_instructions,
      restaurantReviewId: order.restaurantReviewId ?? order.restaurant_review_id ?? undefined,
      restaurantRating: order.restaurantRating ?? order.restaurant_rating ?? undefined,
      restaurantReviewText: order.restaurantReviewText ?? order.restaurant_review_text ?? undefined,
      restaurantReviewedAt: order.restaurantReviewedAt ?? order.restaurant_reviewed_at ?? undefined,
      createdAt: order.createdAt ?? order.created_at,
      updatedAt: order.updatedAt ?? order.updated_at,
    };
  }

  private async enrichOrderWithCustomerContact(order: Order): Promise<Order> {
    if (order.customerName && order.customerPhoneNumber) {
      return order;
    }

    try {
      const customerResponse = await apiClient.get<ApiResponse<CustomerContactInfo>>(
        API_ENDPOINTS.CUSTOMERS.BY_ID(String(order.customerId))
      );

      const customer = customerResponse.data.data;

      return {
        ...order,
        customerName: order.customerName || customer.name,
        customerPhoneNumber: order.customerPhoneNumber || customer.phoneNumber,
      };
    } catch (error) {
      console.error(`Failed to fetch customer ${order.customerId} contact:`, handleApiError(error));
      return order;
    }
  }

  /**
   * Create new order
   */
  async createOrder(orderData: CreateOrderRequest): Promise<Order> {
    try {
      const response = await apiClient.post<ApiResponse<Order>>(
        API_ENDPOINTS.ORDERS.BASE,
        orderData
      );
      
      return this.normalizeOrder(response.data.data);
    } catch (error) {
      console.error('Failed to create order:', handleApiError(error));
      throw error;
    }
  }

  /**
   * Get order by ID
   */
  async getOrderById(id: number | string): Promise<Order> {
    try {
      const response = await apiClient.get<ApiResponse<Order>>(
        API_ENDPOINTS.ORDERS.BY_ID(String(id))
      );
      
      return this.enrichOrderWithCustomerContact(this.normalizeOrder(response.data.data));
    } catch (error) {
      console.error(`Failed to fetch order ${id}:`, handleApiError(error));
      throw error;
    }
  }

  /**
   * Get order by order number
   */
  async getOrderByNumber(orderNumber: string): Promise<Order> {
    try {
      const response = await apiClient.get<ApiResponse<Order>>(
        API_ENDPOINTS.ORDERS.BY_NUMBER(orderNumber)
      );
      
      return response.data.data;
    } catch (error) {
      console.error(`Failed to fetch order ${orderNumber}:`, handleApiError(error));
      throw error;
    }
  }

  /**
   * Get customer orders
   */
  async getCustomerOrders(
    customerId: number | string,
    page: number = 0,
    size: number = 20,
    status?: OrderStatus
  ): Promise<PaginatedResponse<Order>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<Order>>>(
        API_ENDPOINTS.ORDERS.CUSTOMER(String(customerId)),
        {
          params: { page, size, status },
        }
      );
      
      return {
        ...response.data.data,
        content: (response.data.data.content ?? []).map((order) => this.normalizeOrder(order)),
      };
    } catch (error) {
      console.error(`Failed to fetch orders for customer ${customerId}:`, handleApiError(error));
      throw error;
    }
  }

  /**
   * Get restaurant orders
   */
  async getRestaurantOrders(
    restaurantId: number | string,
    page: number = 0,
    size: number = 20,
    status?: OrderStatus
  ): Promise<PaginatedResponse<Order>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<Order>>>(
        API_ENDPOINTS.ORDERS.RESTAURANT(String(restaurantId)),
        {
          params: { page, size, status },
        }
      );
      
      return {
        ...response.data.data,
        content: (response.data.data.content ?? []).map((order) => this.normalizeOrder(order)),
      };
    } catch (error) {
      console.error(`Failed to fetch orders for restaurant ${restaurantId}:`, handleApiError(error));
      throw error;
    }
  }

  /**
   * Update order status
   */
  async updateOrderStatus(
    orderId: number | string,
    statusData: UpdateOrderStatusRequest
  ): Promise<Order> {
    try {
      const response = await apiClient.put<ApiResponse<Order>>(
        API_ENDPOINTS.ORDERS.STATUS(String(orderId)),
        statusData
      );
      
      return this.normalizeOrder(response.data.data);
    } catch (error) {
      console.error(`Failed to update order ${orderId} status:`, handleApiError(error));
      throw error;
    }
  }

  /**
   * Cancel order
   */
  async cancelOrder(orderId: number | string, userId: number | string, reason?: string): Promise<void> {
    try {
      await apiClient.delete(API_ENDPOINTS.ORDERS.CANCEL(String(orderId)), {
        params: { userId, reason },
      });
    } catch (error) {
      console.error(`Failed to cancel order ${orderId}:`, handleApiError(error));
      throw error;
    }
  }

  /**
   * Get orders by status (for riders to see available orders)
   */
  async getOrdersByStatus(
    status: OrderStatus,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<Order>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<Order>>>(
        API_ENDPOINTS.ORDERS.BASE,
        {
          params: { page, size, status },
        }
      );
      
      return response.data.data;
    } catch (error) {
      console.error(`Failed to fetch orders with status ${status}:`, handleApiError(error));
      throw error;
    }
  }

  /**
   * Get available orders for riders
   * Returns orders with READY_FOR_PICKUP status that have no assigned rider
   */
  async getAvailableOrdersForRiders(
    page: number = 0,
    size: number = 50
  ): Promise<PaginatedResponse<Order>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<Order>>>(
        API_ENDPOINTS.ORDERS.RIDER_AVAILABLE,
        {
          params: { page, size },
        }
      );
      
      return response.data.data;
    } catch (error) {
      console.error('Failed to fetch available orders for riders:', handleApiError(error));
      throw error;
    }
  }

  /**
   * Get orders assigned to a specific rider
   */
  async getRiderOrders(
    riderId: number,
    page: number = 0,
    size: number = 50
  ): Promise<PaginatedResponse<Order>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<Order>>>(
        API_ENDPOINTS.ORDERS.RIDER(String(riderId)),
        { params: { page, size } }
      );
      return response.data.data;
    } catch (error) {
      console.error('Failed to fetch rider orders:', handleApiError(error));
      throw error;
    }
  }

  /**
   * Get readable status text
   */
  getStatusText(status: OrderStatus): string {
    const statusMap: Record<OrderStatus, string> = {
      [OrderStatus.PENDING]: 'Pending',
      [OrderStatus.CONFIRMED]: 'Confirmed',
      [OrderStatus.PREPARING]: 'Preparing',
      [OrderStatus.READY_FOR_PICKUP]: 'Ready for Pickup',
      [OrderStatus.PICKED_UP]: 'Picked Up',
      [OrderStatus.DELIVERED]: 'Delivered',
      [OrderStatus.CANCELLED]: 'Cancelled',
      [OrderStatus.REFUNDED]: 'Refunded',
    };
    
    return statusMap[status] || status;
  }

  /**
   * Get status color for UI
   */
  getStatusColor(status: OrderStatus): string {
    const colorMap: Record<OrderStatus, string> = {
      [OrderStatus.PENDING]: 'text-yellow-600',
      [OrderStatus.CONFIRMED]: 'text-blue-600',
      [OrderStatus.PREPARING]: 'text-orange-600',
      [OrderStatus.READY_FOR_PICKUP]: 'text-purple-600',
      [OrderStatus.PICKED_UP]: 'text-indigo-600',
      [OrderStatus.DELIVERED]: 'text-green-600',
      [OrderStatus.CANCELLED]: 'text-red-600',
      [OrderStatus.REFUNDED]: 'text-gray-600',
    };
    
    return colorMap[status] || 'text-gray-600';
  }
}

// Export singleton instance
export const orderService = new OrderService();
export default orderService;
