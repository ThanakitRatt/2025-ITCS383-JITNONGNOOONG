import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import RestaurantDetail from './RestaurantDetail';

const addToCartMock = vi.fn();
const mockNavigate = vi.fn();

vi.mock('react-router', () => ({
  useParams: () => ({ id: '1' }),
  useNavigate: () => mockNavigate,
}));

vi.mock('../../contexts/AppContext', () => ({
  useApp: () => ({ addToCart: addToCartMock, cart: [] }),
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
    getRestaurantReviews: vi.fn(),
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
  addToCartMock.mockReset();
  mockNavigate.mockReset();
  Object.defineProperty(HTMLElement.prototype, 'scrollIntoView', {
    configurable: true,
    value: vi.fn(),
  });
  vi.mocked(restaurantService.getRestaurantById).mockResolvedValue(mockRestaurant as any);
  vi.mocked(restaurantService.getRestaurantMenu).mockResolvedValue(mockMenu as any);
  vi.mocked(restaurantService.getRestaurantReviews).mockResolvedValue([
    {
      id: 'RV1',
      restaurantId: '1',
      orderId: 'O1',
      customerId: 'C1',
      customerName: 'Jane',
      rating: 5,
      reviewText: 'Loved it',
      createdAt: '2024-01-16T10:00:00',
    },
  ] as any);
});

describe('RestaurantDetail', () => {
  it('shows a loading spinner on initial render', () => {
    render(<RestaurantDetail />);
    expect(document.querySelector('.animate-spin')).toBeInTheDocument();
  });

  describe('after data loads', () => {
    beforeEach(async () => {
      render(<RestaurantDetail />);
      await waitFor(() => expect(screen.getByText('Thai Palace')).toBeInTheDocument());
    });

    it('renders the restaurant name', () => {
      expect(screen.getByText('Thai Palace')).toBeInTheDocument();
    });

    it('renders menu items using categoryName', () => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
      expect(screen.getByText('Green Curry')).toBeInTheDocument();
    });

    it('renders category headings derived from categoryName', () => {
      expect(screen.getByText('Noodles')).toBeInTheDocument();
      expect(screen.getByText('Curry')).toBeInTheDocument();
    });

    it('renders minimumOrderAmount from restaurant data', () => {
      // minimumOrderAmount: 100 should appear somewhere (e.g. "Min order ฿100")
      expect(screen.getByText(/100/)).toBeInTheDocument();
    });

    it('renders reviews and rating summary', () => {
      expect(screen.getByText(/50 total reviews/i)).toBeInTheDocument();
      expect(screen.getByText('Jane')).toBeInTheDocument();
      expect(screen.getByText('Loved it')).toBeInTheDocument();
    });
  });

  it('shows not-found state when the service call fails', async () => {
    vi.mocked(restaurantService.getRestaurantById).mockRejectedValueOnce(new Error('Not found'));
    render(<RestaurantDetail />);
    await waitFor(() => {
      expect(screen.getByText(/restaurant not found/i)).toBeInTheDocument();
    });
  });

  it('shows the empty menu and review states when no available items or reviews exist', async () => {
    vi.mocked(restaurantService.getRestaurantMenu).mockResolvedValueOnce([
      {
        id: 'M3',
        name: 'Hidden Item',
        price: 99,
        description: 'Unavailable item',
        isAvailable: false,
        categoryName: 'Secret',
      },
    ] as any);
    vi.mocked(restaurantService.getRestaurantReviews).mockResolvedValueOnce([]);

    render(<RestaurantDetail />);

    await waitFor(() => {
      expect(screen.getByText(/no menu items available/i)).toBeInTheDocument();
      expect(screen.getByText(/no reviews yet for this restaurant/i)).toBeInTheDocument();
    });
  });

  it('falls back to N/A when rating and delivery time are missing', async () => {
    vi.mocked(restaurantService.getRestaurantById).mockResolvedValueOnce({
      ...mockRestaurant,
      averageRating: undefined,
      estimatedDeliveryTime: undefined,
      minimumOrderAmount: 0,
    } as any);

    render(<RestaurantDetail />);

    await waitFor(() => {
      expect(screen.getAllByText('N/A').length).toBeGreaterThan(1);
    });

    expect(screen.queryByText(/minimum order:/i)).not.toBeInTheDocument();
  });

  it('opens the item dialog and adds a menu item to the cart', async () => {
    render(<RestaurantDetail />);

    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Pad Thai'));

    await waitFor(() => {
      expect(screen.getByText(/price: ฿120/i)).toBeInTheDocument();
    });

    const quantityButtons = screen.getAllByRole('button', { name: '' });
    fireEvent.click(quantityButtons[1]);
    expect(screen.getByText('2')).toBeInTheDocument();
    expect(screen.getByText(/total: ฿240/i)).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /add to cart/i }));

    expect(addToCartMock).toHaveBeenCalledWith({
      id: 'M1',
      name: 'Pad Thai',
      price: 120,
      quantity: 2,
      restaurantId: '1',
      restaurantName: 'Thai Palace',
    });
  });

  it('navigates using the header buttons', async () => {
    render(<RestaurantDetail />);

    await waitFor(() => {
      expect(screen.getByText('Thai Palace')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /back/i }));
    expect(mockNavigate).toHaveBeenCalledWith('/customer/restaurants');

    fireEvent.click(screen.getByRole('button', { name: /cart \(0\)/i }));
    expect(mockNavigate).toHaveBeenCalledWith('/customer/checkout');
  });

  it('re-fetches reviews when the sort order changes', async () => {
    render(<RestaurantDetail />);

    await waitFor(() => {
      expect(screen.getByText('Thai Palace')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('combobox'));
    fireEvent.click(await screen.findByText(/highest rated/i));

    await waitFor(() => {
      expect(vi.mocked(restaurantService.getRestaurantReviews)).toHaveBeenLastCalledWith('1', 'highest');
    });
  });
});
