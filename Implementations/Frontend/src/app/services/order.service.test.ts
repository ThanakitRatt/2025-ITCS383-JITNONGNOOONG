import { describe, it, expect, vi, beforeEach } from 'vitest';
import type { AxiosResponse } from 'axios';

vi.mock('./apiClient', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
  handleApiError: vi.fn((e: unknown) => (e instanceof Error ? e.message : 'error')),
}));

import apiClient from './apiClient';
import orderService, { OrderStatus } from './order.service';

function fakeResponse<T>(data: T): AxiosResponse<{ success: boolean; data: T }> {
  return { data: { success: true, data } } as AxiosResponse<{ success: boolean; data: T }>;
}

const mockOrder = {
  id: 1,
  orderNumber: 'MR-0001',
  customerId: 100,
  restaurantId: 10,
  status: 'PENDING',
  totalAmount: 350,
  createdAt: '2025-01-01T10:00:00Z',
  updatedAt: undefined,
  deliveryAddress: '123 Test St',
  deliveryFee: 0,
  deliveryLatitude: undefined,
  deliveryLongitude: undefined,
  customerName: undefined,
  customerPhoneNumber: undefined,
  restaurantName: undefined,
  riderId: undefined,
  orderItems: [],
  specialInstructions: undefined,
  restaurantReviewId: undefined,
  restaurantRating: undefined,
  restaurantReviewText: undefined,
  restaurantReviewedAt: undefined,
};

const mockPaginated = {
  content: [mockOrder],
  page: 0,
  size: 20,
  totalElements: 1,
  totalPages: 1,
};

beforeEach(() => {
  vi.resetAllMocks();
});

// ========== createOrder ==========
describe('createOrder', () => {
  it('returns the created order', async () => {
    vi.mocked(apiClient.post).mockResolvedValueOnce(
      fakeResponse({
        id: '1',
        order_number: 'MR-0001',
        customer_id: '100',
        restaurant_id: '10',
        status: 'PENDING',
        total_amount: '350',
        delivery_fee: '0',
        delivery_address: '123 Test St',
        created_at: '2025-01-01T10:00:00Z',
        order_items: [
          {
            id: 1,
            order_id: 1,
            menu_item_id: 7,
            menu_item_name: 'Pad Thai',
            quantity: 2,
            unit_price: 120,
            total_price: 240,
            special_instructions: 'No peanuts',
          },
        ],
      } as any),
    );

    const result = await orderService.createOrder({
      customerId: '100',
      restaurantId: '10',
      deliveryAddress: '123 Test St',
      orderItems: [],
    });

    expect(result).toEqual(
      expect.objectContaining({
        ...mockOrder,
        orderItems: [
          expect.objectContaining({
            id: '1',
            orderId: '1',
            menuItemId: '7',
            menuItemName: 'Pad Thai',
            quantity: 2,
            unitPrice: 120,
            totalPrice: 240,
            specialRequests: 'No peanuts',
          }),
        ],
      }),
    );
    expect(apiClient.post).toHaveBeenCalledOnce();
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.post).mockRejectedValueOnce(new Error('server error'));

    await expect(
      orderService.createOrder({ customerId: '1', restaurantId: '1', deliveryAddress: '', orderItems: [] }),
    ).rejects.toThrow();
  });
});

// ========== getOrderById ==========
describe('getOrderById', () => {
  it('returns order by id', async () => {
    vi.mocked(apiClient.get)
      .mockResolvedValueOnce(fakeResponse(mockOrder))
      .mockResolvedValueOnce(
        fakeResponse({ id: 100, name: 'John Doe', phoneNumber: '+66812345678' }),
      );

    const result = await orderService.getOrderById(1);

    expect(result).toEqual(
      expect.objectContaining({
        ...mockOrder,
        customerName: 'John Doe',
        customerPhoneNumber: '+66812345678',
      }),
    );
    expect(apiClient.get).toHaveBeenCalledTimes(2);
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('not found'));

    await expect(orderService.getOrderById(999)).rejects.toThrow();
  });

  it('returns the order unchanged when customer enrichment fails', async () => {
    vi.mocked(apiClient.get)
      .mockResolvedValueOnce(fakeResponse(mockOrder))
      .mockRejectedValueOnce(new Error('customer lookup failed'));

    const result = await orderService.getOrderById(1);

    expect(result).toEqual(mockOrder);
  });

  it('skips customer enrichment when the order already includes contact details', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce(
      fakeResponse({
        ...mockOrder,
        customerName: 'Ready Customer',
        customerPhoneNumber: '+66810000000',
      } as any),
    );

    const result = await orderService.getOrderById(1);

    expect(result).toEqual(
      expect.objectContaining({
        customerName: 'Ready Customer',
        customerPhoneNumber: '+66810000000',
      }),
    );
    expect(apiClient.get).toHaveBeenCalledTimes(1);
  });
});

// ========== getOrderByNumber ==========
describe('getOrderByNumber', () => {
  it('returns order by order number', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce(fakeResponse(mockOrder));

    const result = await orderService.getOrderByNumber('MR-0001');

    expect(result).toEqual(expect.objectContaining(mockOrder));
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('error'));

    await expect(orderService.getOrderByNumber('INVALID')).rejects.toThrow();
  });
});

// ========== getCustomerOrders ==========
describe('getCustomerOrders', () => {
  it('returns paginated orders for customer', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce(fakeResponse(mockPaginated));

    const result = await orderService.getCustomerOrders(100);

    expect(result).toEqual({
      ...mockPaginated,
      content: [mockOrder],
    });
    expect(apiClient.get).toHaveBeenCalledOnce();
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('error'));

    await expect(orderService.getCustomerOrders(999)).rejects.toThrow();
  });
});

// ========== getRestaurantOrders ==========
describe('getRestaurantOrders', () => {
  it('returns paginated orders for restaurant', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce(fakeResponse(mockPaginated));

    const result = await orderService.getRestaurantOrders(10);

    expect(result).toEqual({
      ...mockPaginated,
      content: [mockOrder],
    });
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('error'));

    await expect(orderService.getRestaurantOrders(10)).rejects.toThrow();
  });
});

// ========== updateOrderStatus ==========
describe('updateOrderStatus', () => {
  it('returns updated order', async () => {
    const updated = { ...mockOrder, status: 'CONFIRMED' };
    vi.mocked(apiClient.put).mockResolvedValueOnce(fakeResponse(updated));

    const result = await orderService.updateOrderStatus(1, {
      newStatus: OrderStatus.CONFIRMED,
      updatedBy: 10,
    });

    expect(result.status).toBe('CONFIRMED');
    expect(apiClient.put).toHaveBeenCalledOnce();
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.put).mockRejectedValueOnce(new Error('error'));

    await expect(
      orderService.updateOrderStatus(1, { newStatus: OrderStatus.CANCELLED, updatedBy: 1 }),
    ).rejects.toThrow();
  });
});

// ========== cancelOrder ==========
describe('cancelOrder', () => {
  it('calls delete endpoint', async () => {
    vi.mocked(apiClient.delete).mockResolvedValueOnce({});

    await orderService.cancelOrder(1, 100, 'Changed mind');

    expect(apiClient.delete).toHaveBeenCalledOnce();
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.delete).mockRejectedValueOnce(new Error('error'));

    await expect(orderService.cancelOrder(1, 100)).rejects.toThrow();
  });
});

// ========== getOrdersByStatus ==========
describe('getOrdersByStatus', () => {
  it('returns paginated orders by status', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce(fakeResponse(mockPaginated));

    const result = await orderService.getOrdersByStatus(OrderStatus.PENDING);

    expect(result).toEqual({
      ...mockPaginated,
      content: [mockOrder],
    });
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('error'));

    await expect(orderService.getOrdersByStatus(OrderStatus.PENDING)).rejects.toThrow();
  });
});

// ========== getAvailableOrdersForRiders ==========
describe('getAvailableOrdersForRiders', () => {
  it('returns available orders', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce(fakeResponse(mockPaginated));

    const result = await orderService.getAvailableOrdersForRiders();

    expect(result).toEqual({
      ...mockPaginated,
      content: [mockOrder],
    });
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('error'));

    await expect(orderService.getAvailableOrdersForRiders()).rejects.toThrow();
  });
});

// ========== getRiderOrders ==========
describe('getRiderOrders', () => {
  it('returns orders for a rider', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce(fakeResponse(mockPaginated));

    const result = await orderService.getRiderOrders(200);

    expect(result).toEqual(mockPaginated);
    expect(apiClient.get).toHaveBeenCalledOnce();
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('error'));

    await expect(orderService.getRiderOrders(200)).rejects.toThrow();
  });
});

describe('fallback mappings', () => {
  it('returns fallback text and color for unknown statuses', () => {
    expect(orderService.getStatusText('UNKNOWN' as OrderStatus)).toBe('UNKNOWN');
    expect(orderService.getStatusColor('UNKNOWN' as OrderStatus)).toBe('text-gray-600');
  });
});

// ========== getStatusText ==========
describe('getStatusText', () => {
  it('returns human-readable text for PENDING', () => {
    expect(orderService.getStatusText(OrderStatus.PENDING)).toBe('Pending');
  });

  it('returns human-readable text for DELIVERED', () => {
    expect(orderService.getStatusText(OrderStatus.DELIVERED)).toBe('Delivered');
  });

  it('returns human-readable text for CANCELLED', () => {
    expect(orderService.getStatusText(OrderStatus.CANCELLED)).toBe('Cancelled');
  });

  it('returns human-readable text for READY_FOR_PICKUP', () => {
    expect(orderService.getStatusText(OrderStatus.READY_FOR_PICKUP)).toBe('Ready for Pickup');
  });

  it('returns human-readable text for PICKED_UP and REFUNDED', () => {
    expect(orderService.getStatusText(OrderStatus.PICKED_UP)).toBe('Picked Up');
    expect(orderService.getStatusText(OrderStatus.REFUNDED)).toBe('Refunded');
  });
});

// ========== getStatusColor ==========
describe('getStatusColor', () => {
  it('returns green for DELIVERED', () => {
    expect(orderService.getStatusColor(OrderStatus.DELIVERED)).toBe('text-green-600');
  });

  it('returns yellow for PENDING', () => {
    expect(orderService.getStatusColor(OrderStatus.PENDING)).toBe('text-yellow-600');
  });

  it('returns red for CANCELLED', () => {
    expect(orderService.getStatusColor(OrderStatus.CANCELLED)).toBe('text-red-600');
  });

  it('returns blue for CONFIRMED', () => {
    expect(orderService.getStatusColor(OrderStatus.CONFIRMED)).toBe('text-blue-600');
  });

  it('returns mapped colors for PREPARING, READY_FOR_PICKUP, PICKED_UP, and REFUNDED', () => {
    expect(orderService.getStatusColor(OrderStatus.PREPARING)).toBe('text-orange-600');
    expect(orderService.getStatusColor(OrderStatus.READY_FOR_PICKUP)).toBe('text-purple-600');
    expect(orderService.getStatusColor(OrderStatus.PICKED_UP)).toBe('text-indigo-600');
    expect(orderService.getStatusColor(OrderStatus.REFUNDED)).toBe('text-gray-600');
  });
});
