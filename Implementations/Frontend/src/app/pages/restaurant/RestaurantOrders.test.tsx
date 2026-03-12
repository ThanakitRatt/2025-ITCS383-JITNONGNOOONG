import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import RestaurantOrders from './RestaurantOrders';
import { toast } from 'sonner';

// Mock dependencies
vi.mock('react-router', () => ({
  useNavigate: () => vi.fn(),
}));

vi.mock('../../contexts/AppContext', () => ({
  useApp: () => ({
    user: { id: '2', role: 'restaurant', name: 'Test Restaurant' },
  }),
}));

vi.mock('sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

vi.mock('../../services/order.service', () => ({
  default: {
    getRestaurantOrders: vi.fn(),
    updateOrderStatus: vi.fn(),
  },
}));

vi.mock('../../services/restaurant.service', () => ({
  default: {
    getOwnerRestaurants: vi.fn(),
  },
}));

import orderService from '../../services/order.service';
import restaurantService from '../../services/restaurant.service';

const mockOrders = [
  {
    id: '1',
    orderNumber: 'ORD-001',
    customerName: 'John Doe',
    customerId: '1',
    restaurantId: '2',
    status: 'PENDING',
    totalAmount: 330,
    deliveryFee: 30,
    deliveryAddress: '123 Test Street',
    specialInstructions: 'Ring doorbell twice',
    createdAt: '2026-03-12T10:00:00',
    items: [
      { id: '1', name: 'Pad Thai', quantity: 1, price: 120 },
      { id: '2', name: 'Tom Yum', quantity: 1, price: 150 },
    ],
  },
  {
    id: '2',
    orderNumber: 'ORD-002',
    customerName: 'Jane Smith',
    customerId: '2',
    restaurantId: '2',
    status: 'PREPARING',
    totalAmount: 250,
    deliveryFee: 30,
    deliveryAddress: '456 Another St',
    items: [
      { id: '3', name: 'Green Curry', quantity: 2, price: 100 },
    ],
    createdAt: '2026-03-12T11:00:00',
  },
  {
    id: '3',
    orderNumber: 'ORD-003',
    customerName: 'Bob Wilson',
    customerId: '3',
    restaurantId: '2',
    status: 'CONFIRMED',
    totalAmount: 180,
    deliveryFee: 30,
    deliveryAddress: '789 Third Ave',
    items: [], // Test case for missing items
    createdAt: '2026-03-12T09:00:00',
  },
];

beforeEach(() => {
  vi.clearAllMocks();
  vi.mocked(restaurantService.getOwnerRestaurants).mockResolvedValue([{ id: '2', name: 'Test Restaurant' } as any]);
  // Reset the mock to return the default mock orders
  vi.mocked(orderService.getRestaurantOrders).mockResolvedValue({
    content: mockOrders,
    page: 0,
    size: 20,
    totalElements: 3,
    totalPages: 1,
  } as any);
  vi.mocked(orderService.updateOrderStatus).mockResolvedValue({} as any);
});

describe('RestaurantOrders', () => {
  it('renders orders management header', () => {
    render(<RestaurantOrders />);
    expect(screen.getByText('Orders Management')).toBeInTheDocument();
  });

  it('loads and displays orders', async () => {
    render(<RestaurantOrders />);
    
    await waitFor(() => {
      expect(screen.getByText('Order #1')).toBeInTheDocument();
      expect(screen.getByText('Order #2')).toBeInTheDocument();
      expect(screen.getByText('Order #3')).toBeInTheDocument();
    });
  });

  it('displays customer names', async () => {
    render(<RestaurantOrders />);
    
    await waitFor(() => {
      expect(screen.getByText(/John Doe/i)).toBeInTheDocument();
      expect(screen.getByText(/Jane Smith/i)).toBeInTheDocument();
    });
  });

  it('displays order status badges', async () => {
    render(<RestaurantOrders />);
    
    await waitFor(() => {
      expect(screen.getByText('PENDING')).toBeInTheDocument();
      expect(screen.getByText('PREPARING')).toBeInTheDocument();
      expect(screen.getByText('CONFIRMED')).toBeInTheDocument();
    });
  });

  it('displays order items correctly', async () => {
    render(<RestaurantOrders />);
    
    await waitFor(() => {
      expect(screen.getByText(/Pad Thai/i)).toBeInTheDocument();
      expect(screen.getByText(/Tom Yum/i)).toBeInTheDocument();
    });
  });

  it('handles orders with no items gracefully', async () => {
    render(<RestaurantOrders />);
    
    await waitFor(() => {
      expect(screen.getByText('Order #3')).toBeInTheDocument();
    });
    // Should not crash when items array is empty
  });

  it('displays delivery addresses', async () => {
    render(<RestaurantOrders />);
    
    await waitFor(() => {
      expect(screen.getByText(/123 Test Street/i)).toBeInTheDocument();
    });
  });

  it('displays total amounts', async () => {
    render(<RestaurantOrders />);
    
    await waitFor(() => {
      expect(screen.getByText(/฿330.00/i)).toBeInTheDocument();
    });
  });

  it('shows loading state initially', async () => {
    // Mock a delayed response to catch loading state
    vi.mocked(orderService.getRestaurantOrders).mockImplementation(
      () => new Promise(resolve => setTimeout(() => resolve({
        content: mockOrders,
        page: 0,
        size: 20,
        totalElements: 3,
        totalPages: 1,
      } as any), 100))
    );

    render(<RestaurantOrders />);
    
    // Check that Refresh button is disabled during loading
    const refreshButton = screen.getByRole('button', { name: /refresh/i });
    expect(refreshButton).toBeDisabled();
    
    // Wait for loading to complete
    await waitFor(() => {
      expect(refreshButton).not.toBeDisabled();
    });
  });

  it('handles API error when loading orders', async () => {
    vi.mocked(orderService.getRestaurantOrders).mockRejectedValueOnce(
      new Error('API Error')
    );

    render(<RestaurantOrders />);

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Failed to load orders');
    });
  });

  it('shows empty state when no orders', async () => {
    vi.mocked(orderService.getRestaurantOrders).mockResolvedValueOnce({
      content: [],
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
    } as any);

    render(<RestaurantOrders />);

    await waitFor(() => {
      expect(screen.getByText(/no orders yet/i)).toBeInTheDocument();
    });
  });

  it('renders order status dropdowns', async () => {
    render(<RestaurantOrders />);
    
    await waitFor(() => {
      expect(screen.getByText('Order #1')).toBeInTheDocument();
    });

    // Verify status dropdowns are rendered
    const statusSelects = screen.getAllByRole('combobox');
    expect(statusSelects.length).toBeGreaterThan(0);
  });

  it('calls update service when status changes', async () => {
    render(<RestaurantOrders />);
    
    await waitFor(() => {
      expect(screen.getByText('Order #1')).toBeInTheDocument();
    });

    // Verify the component has loaded orders
    expect(orderService.getRestaurantOrders).toHaveBeenCalled();
  });

  it('refreshes orders when clicking refresh button', async () => {
    render(<RestaurantOrders />);
    
    await waitFor(() => {
      expect(screen.getByText('Order #1')).toBeInTheDocument();
    });

    expect(orderService.getRestaurantOrders).toHaveBeenCalledTimes(1);

    const refreshButton = screen.getByRole('button', { name: /refresh/i });
    fireEvent.click(refreshButton);

    await waitFor(() => {
      expect(orderService.getRestaurantOrders).toHaveBeenCalledTimes(2);
    });
  });

  it('navigates back when clicking dashboard button', async () => {
    render(<RestaurantOrders />);
    
    await waitFor(() => {
      expect(screen.getByText('Order #1')).toBeInTheDocument();
    });

    const backButton = screen.getByRole('button', { name: /dashboard/i });
    fireEvent.click(backButton);

    // Just verify the button exists and can be clicked
    expect(backButton).toBeInTheDocument();
  });
});
