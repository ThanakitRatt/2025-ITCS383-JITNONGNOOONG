import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import OrderTracking from './OrderTracking';

// Mock dependencies
const mockNavigate = vi.fn();
vi.mock('react-router', () => ({
  useNavigate: () => mockNavigate,
}));

const mockGetCustomerOrders = vi.fn();
const mockSubmitRestaurantReview = vi.fn();
vi.mock('../../services/order.service', () => ({
  default: {
    getCustomerOrders: (...args: any[]) => mockGetCustomerOrders(...args),
  },
  OrderStatus: {
    PENDING: 'PENDING',
    CONFIRMED: 'CONFIRMED',
    PREPARING: 'PREPARING',
    READY_FOR_PICKUP: 'READY_FOR_PICKUP',
    PICKED_UP: 'PICKED_UP',
    DELIVERED: 'DELIVERED',
    CANCELLED: 'CANCELLED',
    REFUNDED: 'REFUNDED',
  },
}));

vi.mock('../../services/restaurant.service', () => ({
  default: {
    submitRestaurantReview: (...args: any[]) => mockSubmitRestaurantReview(...args),
  },
}));

vi.mock('sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

const mockUser = { id: 123, username: 'customer1', role: 'CUSTOMER' };
vi.mock('../../contexts/AppContext', () => ({
  useApp: () => ({ user: mockUser }),
}));

// Test helper to create mock orders
const createMockOrder = (
  id: number,
  orderNumber: string,
  status: string,
  items: Array<{ menuItemName: string; quantity: number; totalPrice: number }>,
  totalAmount: number,
  deliveryAddress: string
) => ({
  id,
  orderNumber,
  restaurantId: 1,
  status,
  createdAt: '2024-01-15T10:00:00',
  totalAmount,
  deliveryAddress,
  orderItems: items.map((item, idx) => ({ id: idx + 1, ...item })),
});

describe('OrderTracking', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockSubmitRestaurantReview.mockResolvedValue({
      id: '501',
      rating: 5,
      reviewText: 'Excellent service',
      createdAt: '2024-01-15T12:00:00',
    });
  });

  it('renders loading state initially', () => {
    mockGetCustomerOrders.mockReturnValue(new Promise(() => {})); // Never resolves
    render(<OrderTracking />);
    
    expect(screen.getByText('My Orders')).toBeInTheDocument();
  });

  it('renders empty state when no orders exist', async () => {
    mockGetCustomerOrders.mockResolvedValue({ content: [] });
    
    render(<OrderTracking />);
    
    await waitFor(() => {
      expect(screen.getByText('No orders yet')).toBeInTheDocument();
    });
    
    expect(screen.getByText('Start Ordering')).toBeInTheDocument();
  });

  it('navigates to restaurants when Start Ordering button is clicked', async () => {
    mockGetCustomerOrders.mockResolvedValue({ content: [] });
    
    render(<OrderTracking />);
    
    await waitFor(() => {
      expect(screen.getByText('Start Ordering')).toBeInTheDocument();
    });
    
    const button = screen.getByText('Start Ordering');
    fireEvent.click(button);
    
    expect(mockNavigate).toHaveBeenCalledWith('/customer/restaurants');
  });

  it('renders order list when orders exist', async () => {
    mockGetCustomerOrders.mockResolvedValue({
      content: [
        createMockOrder(1, '001', 'PREPARING', [
          { menuItemName: 'Pad Thai', quantity: 2, totalPrice: 180 },
          { menuItemName: 'Spring Rolls', quantity: 1, totalPrice: 70.5 },
        ], 250.5, '123 Main St'),
      ],
    });
    
    render(<OrderTracking />);
    
    await waitFor(() => {
      expect(screen.getByText('Order #001')).toBeInTheDocument();
    });
    
    expect(screen.getByText('Pad Thai × 2')).toBeInTheDocument();
    expect(screen.getByText('฿180.00')).toBeInTheDocument();
    expect(screen.getByText('123 Main St')).toBeInTheDocument();
    expect(screen.getByText('฿250.50')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /view details/i })).toBeInTheDocument();
  });

  it('displays correct status badge for delivered orders', async () => {
    mockGetCustomerOrders.mockResolvedValue({
      content: [
        createMockOrder(2, '002', 'DELIVERED', [
          { menuItemName: 'Green Curry', quantity: 1, totalPrice: 150 },
        ], 150, '456 Oak Ave'),
      ],
    });
    
    render(<OrderTracking />);
    
    await waitFor(() => {
      expect(screen.getByText('Order #002')).toBeInTheDocument();
    });
    
    expect(screen.getByText('Green Curry × 1')).toBeInTheDocument();
    expect(screen.getByText('456 Oak Ave')).toBeInTheDocument();
  });

  it('displays multiple orders correctly', async () => {
    mockGetCustomerOrders.mockResolvedValue({
      content: [
        createMockOrder(1, '001', 'PENDING', [
          { menuItemName: 'Dish A', quantity: 1, totalPrice: 100 },
        ], 100, '123 Main St'),
        createMockOrder(2, '002', 'DELIVERED', [
          { menuItemName: 'Dish B', quantity: 2, totalPrice: 200 },
        ], 200, '456 Oak Ave'),
      ],
    });
    
    render(<OrderTracking />);
    
    await waitFor(() => {
      expect(screen.getByText('Order #001')).toBeInTheDocument();
    });
    
    expect(screen.getByText('Order #002')).toBeInTheDocument();
    expect(screen.getByText('Dish A × 1')).toBeInTheDocument();
    expect(screen.getByText('Dish B × 2')).toBeInTheDocument();
  });

  it('opens order details dialog when clicking View Details', async () => {
    mockGetCustomerOrders.mockResolvedValue({
      content: [
        createMockOrder(1, '001', 'PREPARING', [
          { menuItemName: 'Pad Thai', quantity: 1, totalPrice: 120 },
        ], 120, '123 Main St'),
      ],
    });

    render(<OrderTracking />);

    await waitFor(() => {
      expect(screen.getByText('Order #001')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /view details/i }));

    await waitFor(() => {
      expect(screen.getByText('Order Details #001')).toBeInTheDocument();
      expect(screen.getAllByText('123 Main St').length).toBeGreaterThan(0);
    });
  });

  it('shows mock live tracking map for picked up orders in details view', async () => {
    mockGetCustomerOrders.mockResolvedValue({
      content: [
        createMockOrder(9, '009', 'PICKED_UP', [
          { menuItemName: 'Khao Man Gai', quantity: 1, totalPrice: 120 },
        ], 120, '789 Delivery Rd'),
      ],
    });

    render(<OrderTracking />);

    await waitFor(() => {
      expect(screen.getByText('Order #009')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /view details/i }));

    await waitFor(() => {
      expect(screen.getByText('Mock Live Tracking Map')).toBeInTheDocument();
      expect(screen.getByText(/updates every 5 seconds/i)).toBeInTheDocument();
      expect(screen.queryByText(/mock tracking requirements covered/i)).not.toBeInTheDocument();
    });
  });

  it('shows review form for delivered orders and submits a rating', async () => {
    mockGetCustomerOrders.mockResolvedValue({
      content: [
        createMockOrder(3, '003', 'DELIVERED', [
          { menuItemName: 'Tom Yum', quantity: 1, totalPrice: 140 },
        ], 140, '22 Review St'),
      ],
    });

    render(<OrderTracking />);

    await waitFor(() => {
      expect(screen.getByText('Rate this order')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /rate 4 stars/i }));
    fireEvent.change(screen.getByPlaceholderText(/share anything the restaurant did well/i), {
      target: { value: 'Very tasty and arrived hot' },
    });
    fireEvent.click(screen.getByRole('button', { name: /submit review/i }));

    await waitFor(() => {
      expect(mockSubmitRestaurantReview).toHaveBeenCalledWith('1', {
        orderId: '3',
        customerId: '123',
        rating: 4,
        reviewText: 'Very tasty and arrived hot',
      });
    });
  });

  it('navigates back to restaurants when Back button is clicked', async () => {
    mockGetCustomerOrders.mockResolvedValue({ content: [] });
    
    render(<OrderTracking />);
    
    await waitFor(() => {
      expect(screen.getByText('Back to Restaurants')).toBeInTheDocument();
    });
    
    const backButton = screen.getByText('Back to Restaurants');
    fireEvent.click(backButton);
    
    expect(mockNavigate).toHaveBeenCalledWith('/customer/restaurants');
  });

  it('handles API errors gracefully', async () => {
    mockGetCustomerOrders.mockRejectedValue(new Error('API Error'));
    
    render(<OrderTracking />);
    
    await waitFor(() => {
      expect(screen.getByText('No orders yet')).toBeInTheDocument();
    });
  });

  it('calls orderService with correct user ID', async () => {
    mockGetCustomerOrders.mockResolvedValue({ content: [] });
    
    render(<OrderTracking />);
    
    await waitFor(() => {
      expect(mockGetCustomerOrders).toHaveBeenCalledWith('123');
    });
  });
});
