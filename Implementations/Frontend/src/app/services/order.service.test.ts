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
  deliveryAddress: '123 Test St',
  orderItems: [],
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
    vi.mocked(apiClient.post).mockResolvedValueOnce(fakeResponse(mockOrder));

    const result = await orderService.createOrder({
      customerId: '100',
      restaurantId: '10',
      deliveryAddress: '123 Test St',
      orderItems: [],
    });

    expect(result).toEqual(mockOrder);
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
    vi.mocked(apiClient.get).mockResolvedValueOnce(fakeResponse(mockOrder));

    const result = await orderService.getOrderById(1);

    expect(result).toEqual(mockOrder);
    expect(apiClient.get).toHaveBeenCalledOnce();
  });

  it('rethrows on API error', async () => {
    vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('not found'));

    await expect(orderService.getOrderById(999)).rejects.toThrow();
  });
});

// ========== getOrderByNumber ==========
describe('getOrderByNumber', () => {
  it('returns order by order number', async () => {
    vi.mocked(apiClient.get).mockResolvedValueOnce(fakeResponse(mockOrder));

    const result = await orderService.getOrderByNumber('MR-0001');

    expect(result).toEqual(mockOrder);
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

    expect(result).toEqual(mockPaginated);
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

    expect(result).toEqual(mockPaginated);
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

    expect(result).toEqual(mockPaginated);
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

    expect(result).toEqual(mockPaginated);
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
});
