/**
 * Services Index
 * 
 * Central export point for all API services
 */

export { default as apiClient, handleApiError } from './apiClient';
export type { ApiResponse, PaginatedResponse } from './apiClient';

export { default as authService } from './auth.service';
export type {
  LoginRequest,
  RegisterRequest,
  OtpRequest,
  UserDTO,
  LoginResponse,
} from './auth.service';

export { default as restaurantService } from './restaurant.service';
export type {
  Restaurant,
  MenuItem,
  Address,
  SearchFilters,
} from './restaurant.service';

export { default as orderService } from './order.service';
export {
  OrderStatus,
  PaymentMethod,
  PaymentStatus,
} from './order.service';
export type {
  Order,
  OrderItem,
  CreateOrderRequest,
  CreateOrderItem,
  UpdateOrderStatusRequest,
} from './order.service';
