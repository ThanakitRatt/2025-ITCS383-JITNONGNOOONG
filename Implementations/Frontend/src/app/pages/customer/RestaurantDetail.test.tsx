import { render, screen, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import RestaurantDetail from './RestaurantDetail';

vi.mock('react-router', () => ({
  useParams: () => ({ id: '1' }),
  useNavigate: () => vi.fn(),
}));

vi.mock('../../contexts/AppContext', () => ({
  useApp: () => ({ addToCart: vi.fn(), cart: [] }),
}));

vi.mock('../../components/figma/ImageWithFallback', () => ({
  ImageWithFallback: ({ alt }: { alt: string }) => <img alt={alt} />,
}));

vi.mock('sonner', () => ({
  toast: { success: vi.fn() },
}));

vi.mock('../../services/restaurant.service', () => ({
  default: {
    getRestaurantById: vi.fn(),
    getRestaurantMenu: vi.fn(),
  },
}));

import restaurantService from '../../services/restaurant.service';

const mockRestaurant = {
  id: '1',
  name: 'Thai Palace',
  description: 'Authentic Thai food',
  cuisineType: 'THAI',
  address: '123 Sukhumvit Rd, Bangkok',
  coverImageUrl: '',
  logoUrl: '',
  openingTime: '09:00',
  closingTime: '22:00',
  deliveryFee: 25,
  minimumOrderAmount: 100,
  estimatedDeliveryTime: 30,
  averageRating: 4.5,
  totalReviews: 50,
  categoryId: 1,
  categoryName: 'Thai',
  acceptsOrders: true,
  status: 'APPROVED',
};

const mockMenu = [
  {
    id: 'M1',
    name: 'Pad Thai',
    price: 120,
    description: 'Stir-fried noodles',
    isAvailable: true,
    categoryName: 'Noodles',
  },
  {
    id: 'M2',
    name: 'Green Curry',
    price: 150,
    description: 'Spicy green curry',
    isAvailable: true,
    categoryName: 'Curry',
  },
];

beforeEach(() => {
  vi.mocked(restaurantService.getRestaurantById).mockResolvedValue(mockRestaurant as any);
  vi.mocked(restaurantService.getRestaurantMenu).mockResolvedValue(mockMenu as any);
});

describe('RestaurantDetail', () => {
  it('shows a loading spinner on initial render', () => {
    render(<RestaurantDetail />);
    expect(document.querySelector('.animate-spin')).toBeInTheDocument();
  });

  it('renders the restaurant name after data loads', async () => {
    render(<RestaurantDetail />);
    await waitFor(() => {
      expect(screen.getByText('Thai Palace')).toBeInTheDocument();
    });
  });

  it('renders menu items using categoryName', async () => {
    render(<RestaurantDetail />);
    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
      expect(screen.getByText('Green Curry')).toBeInTheDocument();
    });
  });

  it('renders category headings derived from categoryName', async () => {
    render(<RestaurantDetail />);
    await waitFor(() => {
      expect(screen.getByText('Noodles')).toBeInTheDocument();
      expect(screen.getByText('Curry')).toBeInTheDocument();
    });
  });

  it('renders minimumOrderAmount from restaurant data', async () => {
    render(<RestaurantDetail />);
    await waitFor(() => {
      // minimumOrderAmount: 100 should appear somewhere (e.g. "Min order ฿100")
      expect(screen.getByText(/100/)).toBeInTheDocument();
    });
  });

  it('shows not-found state when the service call fails', async () => {
    vi.mocked(restaurantService.getRestaurantById).mockRejectedValueOnce(new Error('Not found'));
    render(<RestaurantDetail />);
    await waitFor(() => {
      expect(screen.getByText(/restaurant not found/i)).toBeInTheDocument();
    });
  });
});
