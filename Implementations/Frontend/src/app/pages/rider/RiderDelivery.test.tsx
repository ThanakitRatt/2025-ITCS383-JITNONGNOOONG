import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import RiderDelivery from './RiderDelivery';
import { OrderStatus } from '../../services/order.service';

const mockNavigate = vi.fn();
const mockUseParams = vi.fn();

vi.mock('react-router', () => ({
  useNavigate: () => mockNavigate,
  useParams: () => mockUseParams(),
}));

vi.mock('../../contexts/AppContext', () => ({
  useApp: () => ({
    user: { id: '9', role: 'rider', name: 'Rider' },
  }),
}));

vi.mock('sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

vi.mock('../../services/order.service', () => ({
  orderService: {
    getOrderById: vi.fn(),
    updateOrderStatus: vi.fn(),
  },
  default: {
    getOrderById: vi.fn(),
    updateOrderStatus: vi.fn(),
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

import { toast } from 'sonner';
import { orderService } from '../../services/order.service';

const mockOrder = {
  id: 12,
  orderNumber: 'MR-0012',
  customerId: 3,
  customerName: '',
  customerPhoneNumber: '',
  restaurantId: 2,
  deliveryAddress: '123 Delivery Street',
  deliveryFee: 50,
  totalAmount: 350,
  status: OrderStatus.PICKED_UP,
  orderItems: [{ menuItemId: '1', menuItemName: 'Pad Thai', quantity: 1, totalPrice: 120 }],
  createdAt: '2025-01-01T12:00:00Z',
};

describe('RiderDelivery', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseParams.mockReturnValue({ orderId: '12' });
    vi.mocked(orderService.getOrderById).mockResolvedValue(mockOrder as any);
    vi.mocked(orderService.updateOrderStatus).mockResolvedValue({ ...mockOrder, status: OrderStatus.DELIVERED } as any);
    vi.stubGlobal('open', vi.fn());
  });

  it('redirects to the dashboard when no order id is present', async () => {
    mockUseParams.mockReturnValue({});

    render(<RiderDelivery />);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/rider/dashboard');
    });
  });

  it('shows order details and customer fallback values', async () => {
    render(<RiderDelivery />);

    await waitFor(() => {
      expect(screen.getByText(/order #mr-0012/i)).toBeInTheDocument();
      expect(screen.getByText(/^3$/)).toBeInTheDocument();
      expect(screen.getByText(/pad thai/i)).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /contact customer/i }));

    await waitFor(() => {
      expect(screen.getByText(/phone number not available/i)).toBeInTheDocument();
    });
  });

  it('opens google maps for the delivery address', async () => {
    render(<RiderDelivery />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /open delivery location in google maps/i })).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /open delivery location in google maps/i }));

    expect(window.open).toHaveBeenCalledWith(
      expect.stringContaining(encodeURIComponent('123 Delivery Street')),
      '_blank',
    );
  });

  it('completes the delivery for picked up orders', async () => {
    render(<RiderDelivery />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /complete delivery/i })).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /complete delivery/i }));

    await waitFor(() => {
      expect(orderService.updateOrderStatus).toHaveBeenCalledWith(12, {
        newStatus: OrderStatus.DELIVERED,
        updatedBy: 9,
        notes: 'Delivery completed by rider',
      });
      expect(toast.success).toHaveBeenCalledWith('Delivery completed! ฿50 earned');
      expect(mockNavigate).toHaveBeenCalledWith('/rider/dashboard');
    });
  });

  it('shows an error toast and redirects when loading fails', async () => {
    vi.mocked(orderService.getOrderById).mockRejectedValueOnce(new Error('not found'));

    render(<RiderDelivery />);

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Failed to load order details');
      expect(mockNavigate).toHaveBeenCalledWith('/rider/dashboard');
    });
  });
});
