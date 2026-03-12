import { render, screen, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import RestaurantDashboard from './RestaurantDashboard';

vi.mock('react-router', () => ({
  useNavigate: () => vi.fn(),
}));

vi.mock('../../contexts/AppContext', () => ({
  useApp: () => ({
    user: { id: 'R1', role: 'restaurant', name: 'My Restaurant', email: 'r@test.com' },
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
  vi.mocked(restaurantService.getOwnerRestaurants).mockResolvedValue([{ id: 'R1', name: 'My Restaurant' } as any]);
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
});
