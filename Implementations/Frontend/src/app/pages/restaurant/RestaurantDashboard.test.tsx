import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import RestaurantDashboard from './RestaurantDashboard';

const mockNavigate = vi.fn();
const logoutMock = vi.fn();

vi.mock('react-router', () => ({
  useNavigate: () => mockNavigate,
}));

vi.mock('../../contexts/AppContext', () => ({
  useApp: () => ({
    user: { id: 'R1', role: 'restaurant', name: 'My Restaurant', email: 'r@test.com' },
    logout: logoutMock,
  }),
}));

vi.mock('../../services/restaurant.service', () => ({
  default: {
    getRestaurantMenu: vi.fn(),
    getOwnerRestaurants: vi.fn(),
  },
}));

vi.mock('../../services/order.service', () => ({
  default: {
    getRestaurantOrders: vi.fn(),
  },
}));

import restaurantService from '../../services/restaurant.service';
import orderService from '../../services/order.service';

const todayOrder = {
  id: 'ORD_TODAY',
  orderNumber: 'ORD-001',
  customerId: 'CUST1',
  restaurantId: 'R1',
  status: 'DELIVERED',
  totalAmount: 480,
  createdAt: new Date().toISOString(),
  deliveryAddress: '123 Road',
};

beforeEach(() => {
  vi.clearAllMocks();
  mockNavigate.mockReset();
  logoutMock.mockReset();
  vi.mocked(restaurantService.getOwnerRestaurants).mockResolvedValue([
    { id: 'R1', name: 'My Restaurant', averageRating: 4.7, totalReviews: 18 } as any,
  ]);
  vi.mocked(restaurantService.getRestaurantMenu).mockResolvedValue([]);
  vi.mocked(orderService.getRestaurantOrders).mockResolvedValue({ content: [todayOrder] } as any);
});

describe('RestaurantDashboard', () => {
  it('renders the restaurant portal header', () => {
    render(<RestaurantDashboard />);
    expect(screen.getByText(/restaurant portal/i)).toBeInTheDocument();
  });

  it('renders Quick Actions buttons', () => {
    render(<RestaurantDashboard />);
    expect(screen.getByText(/view orders/i)).toBeInTheDocument();
    expect(screen.getByText(/manage menu/i)).toBeInTheDocument();
  });

  it("shows today's revenue after loading", async () => {
    render(<RestaurantDashboard />);
    await waitFor(() => {
      // totalAmount of 480 surfaced as revenue — verifies the field is read
      const matches = screen.getAllByText(/฿480/);
      expect(matches.length).toBeGreaterThan(0);
    });
  });

  it('shows Recent Orders section after loading', async () => {
    render(<RestaurantDashboard />);
    await waitFor(() => {
      expect(screen.getByText(/recent orders/i)).toBeInTheDocument();
    });
  });

  it('shows the average rating card and summary', async () => {
    render(<RestaurantDashboard />);
    await waitFor(() => {
      expect(screen.getAllByText(/4\.7/).length).toBeGreaterThan(0);
      expect(screen.getByText(/18 customer reviews/i)).toBeInTheDocument();
    });
  });

  it('shows no orders yet when the restaurant has no recent orders', async () => {
    vi.mocked(orderService.getRestaurantOrders).mockResolvedValueOnce({ content: [] } as any);

    render(<RestaurantDashboard />);

    await waitFor(() => {
      expect(screen.getByText(/no orders yet/i)).toBeInTheDocument();
    });
  });

  it('uses paginated menu content length when menu data is not a plain array', async () => {
    vi.mocked(restaurantService.getRestaurantMenu).mockResolvedValueOnce({
      content: [{ id: 'M1' }, { id: 'M2' }],
      totalElements: 7,
    } as any);

    render(<RestaurantDashboard />);

    await waitFor(() => {
      expect(screen.getByText('2')).toBeInTheDocument();
    });
  });

  it('falls back gracefully when no restaurant exists for the owner', async () => {
    vi.mocked(restaurantService.getOwnerRestaurants).mockResolvedValueOnce([]);

    render(<RestaurantDashboard />);

    await waitFor(() => {
      expect(screen.getByText(/my restaurant/i)).toBeInTheDocument();
      expect(screen.getByText(/no orders yet/i)).toBeInTheDocument();
    });

    expect(screen.queryByText(/customer rating/i)).not.toBeInTheDocument();
  });

  it('navigates from stat cards and quick actions', async () => {
    render(<RestaurantDashboard />);

    await waitFor(() => {
      expect(screen.getByText(/pending orders/i)).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText(/pending orders/i));
    fireEvent.click(screen.getByText(/menu items/i));
    fireEvent.click(screen.getByRole('button', { name: /view orders/i }));
    fireEvent.click(screen.getByRole('button', { name: /manage menu/i }));
    fireEvent.click(screen.getByRole('button', { name: /create promotion/i }));

    expect(mockNavigate).toHaveBeenCalledWith('/restaurant/orders');
    expect(mockNavigate).toHaveBeenCalledWith('/restaurant/menu');
    expect(mockNavigate).toHaveBeenCalledWith('/restaurant/promotions');
  });

  it('logs out and exits to the login page', () => {
    render(<RestaurantDashboard />);

    fireEvent.click(screen.getByRole('button', { name: /exit/i }));

    expect(logoutMock).toHaveBeenCalledOnce();
    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });

  it('falls back to an empty recent-order list when order loading fails', async () => {
    vi.mocked(orderService.getRestaurantOrders).mockRejectedValueOnce(new Error('orders down'));

    render(<RestaurantDashboard />);

    await waitFor(() => {
      expect(screen.getByText(/no orders yet/i)).toBeInTheDocument();
    });
  });
});
