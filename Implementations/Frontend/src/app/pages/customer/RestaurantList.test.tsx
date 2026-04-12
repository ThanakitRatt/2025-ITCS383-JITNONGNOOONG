import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import RestaurantList from './RestaurantList';

const mockNavigate = vi.fn();
const mockLogout = vi.fn();

vi.mock('react-router', () => ({
  useNavigate: () => mockNavigate,
}));

vi.mock('../../contexts/AppContext', () => ({
  useApp: () => ({
    user: { id: '1', name: 'Test Customer' },
    cart: [{ id: '1', quantity: 2 }],
    logout: mockLogout,
  }),
}));

vi.mock('../../services/restaurant.service', () => ({
  default: {
    searchRestaurants: vi.fn(),
    getCuisineTypes: vi.fn(),
  },
}));

vi.mock('../../components/figma/ImageWithFallback', () => ({
  ImageWithFallback: ({ alt }: { alt: string }) => <img alt={alt} />,
}));

import restaurantService from '../../services/restaurant.service';

describe('RestaurantList', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers();
    vi.mocked(restaurantService.getCuisineTypes).mockResolvedValue(['THAI', 'ITALIAN']);
    vi.mocked(restaurantService.searchRestaurants).mockResolvedValue({
      content: [
        {
          id: '1',
          name: 'Thai Place',
          cuisineType: 'THAI',
          averageRating: 4.5,
          totalReviews: 10,
          estimatedDeliveryTime: 25,
          minimumOrderAmount: 100,
        },
      ],
      totalElements: 1,
    } as any);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('loads restaurants and shows the search result count', async () => {
    render(<RestaurantList />);

    await act(async () => {
      vi.advanceTimersByTime(300);
    });

    await waitFor(() => {
      expect(screen.getByText('Thai Place')).toBeInTheDocument();
      expect(screen.getByText(/1 restaurants found/i)).toBeInTheDocument();
    });
  });

  it('falls back to a generic restaurant name when missing', async () => {
    vi.mocked(restaurantService.searchRestaurants).mockResolvedValueOnce({
      content: [
        {
          id: '2',
          name: '   ',
          cuisineType: 'JAPANESE',
          averageRating: 0,
          totalReviews: 0,
        },
      ],
      totalElements: 1,
    } as any);

    render(<RestaurantList />);

    await act(async () => {
      vi.advanceTimersByTime(300);
    });

    await waitFor(() => {
      expect(screen.getByText('Restaurant')).toBeInTheDocument();
    });
  });

  it('shows an empty state when the search returns no restaurants', async () => {
    vi.mocked(restaurantService.searchRestaurants).mockResolvedValueOnce({
      content: [],
      totalElements: 0,
    } as any);

    render(<RestaurantList />);

    await act(async () => {
      vi.advanceTimersByTime(300);
    });

    await waitFor(() => {
      expect(screen.getByText(/no restaurants found matching your criteria/i)).toBeInTheDocument();
    });
  });

  it('logs out and navigates back to login', async () => {
    render(<RestaurantList />);

    await act(async () => {
      vi.advanceTimersByTime(300);
    });

    fireEvent.click(screen.getAllByRole('button')[2]);

    expect(mockLogout).toHaveBeenCalled();
    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });
});
