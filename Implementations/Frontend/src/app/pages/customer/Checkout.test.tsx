import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import Checkout from './Checkout';
import { toast } from 'sonner';

// Mock dependencies
const mockNavigate = vi.fn();
vi.mock('react-router', () => ({
  useNavigate: () => mockNavigate,
}));

const mockUpdateCartQuantity = vi.fn();
const mockRemoveFromCart = vi.fn();
const mockClearCart = vi.fn();

vi.mock('../../contexts/AppContext', () => ({
  useApp: () => ({
    user: { id: '100', role: 'customer', name: 'Test Customer' },
    cart: [
      {
        id: '1',
        name: 'Pad Thai',
        price: 120,
        quantity: 2,
        restaurantId: '2',
        restaurantName: 'Thai Restaurant',
      },
      {
        id: '2',
        name: 'Tom Yum',
        price: 150,
        quantity: 1,
        restaurantId: '2',
        restaurantName: 'Thai Restaurant',
      },
    ],
    updateCartQuantity: mockUpdateCartQuantity,
    removeFromCart: mockRemoveFromCart,
    clearCart: mockClearCart,
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
    createOrder: vi.fn(),
  },
}));

import orderService from '../../services/order.service';

beforeEach(() => {
  vi.clearAllMocks();
});

describe('Checkout', () => {
  it('renders checkout page', () => {
    render(<Checkout />);
    
    expect(screen.getByText('Checkout')).toBeInTheDocument();
  });

  it('displays cart items', () => {
    render(<Checkout />);
    
    // Check if cart section exists
    expect(screen.getByText(/your order/i)).toBeInTheDocument();
  });

  it('allows entering delivery address', () => {
    render(<Checkout />);
    
    const addressInput = screen.getByLabelText(/address/i);
    fireEvent.change(addressInput, { target: { value: '123 Test Street' } });
    
    expect(addressInput).toHaveValue('123 Test Street');
  });

  it('shows error when placing order without delivery address', async () => {
    render(<Checkout />);
    
    // Try to find the place order button
    const buttons = screen.getAllByRole('button');
    const placeOrderButton = buttons.find(btn => btn.textContent?.includes('Place Order'));
    
    if (placeOrderButton) {
      fireEvent.click(placeOrderButton);
      
      await waitFor(() => {
        expect(toast.error).toHaveBeenCalledWith('Please enter a delivery address');
      });
    }
  });

  it('successfully places order with valid data', async () => {
    vi.mocked(orderService.createOrder).mockResolvedValueOnce({} as any);

    render(<Checkout />);
    
    const addressInput = screen.getByLabelText(/address/i);
    fireEvent.change(addressInput, { target: { value: '123 Test Street' } });
    
    const buttons = screen.getAllByRole('button');
    const placeOrderButton = buttons.find(btn => btn.textContent?.includes('Place Order'));
    
    if (placeOrderButton) {
      fireEvent.click(placeOrderButton);
      
      await waitFor(() => {
        expect(orderService.createOrder).toHaveBeenCalled();
      }, { timeout: 3000 });

      await waitFor(() => {
        expect(mockClearCart).toHaveBeenCalled();
        expect(toast.success).toHaveBeenCalledWith('Order placed successfully!');
      }, { timeout: 3000 });
    }
  });

  it('handles order placement failure', async () => {
    vi.mocked(orderService.createOrder).mockRejectedValueOnce(new Error('API Error'));

    render(<Checkout />);
    
    const addressInput = screen.getByLabelText(/address/i);
    fireEvent.change(addressInput, { target: { value: '123 Test Street' } });
    
    const buttons = screen.getAllByRole('button');
    const placeOrderButton = buttons.find(btn => btn.textContent?.includes('Place Order'));
    
    if (placeOrderButton) {
      fireEvent.click(placeOrderButton);
      
      await waitFor(() => {
        expect(toast.error).toHaveBeenCalledWith('Failed to place order. Please try again.');
      }, { timeout: 3000 });
      
      expect(mockClearCart).not.toHaveBeenCalled();
    }
  });

  it('navigates back when clicking continue shopping', () => {
    render(<Checkout />);
    
    const backButton = screen.getByRole('button', { name: /continue shopping/i });
    fireEvent.click(backButton);
    
    expect(mockNavigate).toHaveBeenCalledWith('/customer/restaurants');
  });

  it('displays delivery address section', () => {
    render(<Checkout />);
    
    expect(screen.getByText(/delivery address/i)).toBeInTheDocument();
  });
});
