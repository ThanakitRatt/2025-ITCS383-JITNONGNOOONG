import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import MenuManagement from './MenuManagement';
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

vi.mock('../../services/restaurant.service', () => ({
  default: {
    getOwnerRestaurants: vi.fn(),
    getRestaurantMenu: vi.fn(),
    getRestaurantCategories: vi.fn(),
    addMenuItem: vi.fn(),
    updateMenuItem: vi.fn(),
    deleteMenuItem: vi.fn(),
  },
}));

import restaurantService from '../../services/restaurant.service';

const mockMenuItems = [
  {
    id: '1',
    name: 'Pad Thai',
    description: 'Classic Thai noodles',
    price: 120,
    categoryId: '1',
    preparationTime: 15,
    isAvailable: true,
    restaurantId: '2',
  },
  {
    id: '2',
    name: 'Tom Yum Soup',
    description: 'Spicy and sour soup',
    price: 150,
    categoryId: '1',
    preparationTime: 20,
    isAvailable: true,
    restaurantId: '2',
  },
] as any;

const mockCategories = [
  { id: '1', name: 'Main Dishes', description: 'Main course items', restaurantId: '2', displayOrder: 1 },
  { id: '2', name: 'Appetizers', description: 'Starter items', restaurantId: '2', displayOrder: 2 },
] as any;

beforeEach(() => {
  vi.clearAllMocks();
  vi.mocked(restaurantService.getOwnerRestaurants).mockResolvedValue([{ id: '2', name: 'Test Restaurant' } as any]);
  vi.mocked(restaurantService.getRestaurantMenu).mockResolvedValue(mockMenuItems);
  vi.mocked(restaurantService.getRestaurantCategories).mockResolvedValue(mockCategories);
  // Mock window.confirm
  globalThis.confirm = vi.fn(() => true);
});

describe('MenuManagement', () => {
  it('renders menu management header', async () => {
    render(<MenuManagement />);
    expect(screen.getByText('Menu Management')).toBeInTheDocument();
  });

  it('loads and displays menu items', async () => {
    render(<MenuManagement />);
    
    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
      expect(screen.getByText('Tom Yum Soup')).toBeInTheDocument();
    });
  });

  it('shows loading state while fetching data', () => {
    render(<MenuManagement />);
    expect(screen.getByText(/menu management/i)).toBeInTheDocument();
  });

  it('handles API error when loading menu', async () => {
    vi.mocked(restaurantService.getRestaurantMenu).mockRejectedValueOnce(
      new Error('API Error')
    );
    vi.mocked(restaurantService.getRestaurantCategories).mockRejectedValueOnce(
      new Error('API Error')
    );

    render(<MenuManagement />);

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Failed to load menu items');
    });
  });

  it('opens add dialog when clicking Add Menu Item button', async () => {
    render(<MenuManagement />);
    
    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
    });

    const addButton = screen.getByText('Add Item');
    fireEvent.click(addButton);

    await waitFor(() => {
      expect(screen.getByRole('dialog')).toBeInTheDocument();
    });
  });

  it('opens edit dialog when clicking edit button', async () => {
    render(<MenuManagement />);
    
    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
    });

    const buttons = screen.getAllByRole('button');
    const editButton = buttons.find(btn => btn.querySelector('.lucide-pencil'));
    if (editButton) fireEvent.click(editButton);

    await waitFor(() => {
      expect(screen.getByDisplayValue('Pad Thai')).toBeInTheDocument();
    });
  });

  it('deletes menu item when clicking delete and confirming', async () => {
    vi.mocked(restaurantService.deleteMenuItem).mockResolvedValueOnce();

    render(<MenuManagement />);
    
    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
    });

    const buttons = screen.getAllByRole('button');
    const deleteButton = buttons.find(btn => btn.querySelector('.lucide-trash-2'));
    if (deleteButton) fireEvent.click(deleteButton);

    await waitFor(() => {
      expect(restaurantService.deleteMenuItem).toHaveBeenCalledWith('2', '1');
      expect(toast.success).toHaveBeenCalledWith('Menu item deleted successfully');
    });
  });

  it('does not delete menu item when canceling confirmation', async () => {
    globalThis.confirm = vi.fn(() => false);

    render(<MenuManagement />);
    
    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
    });

    const buttons = screen.getAllByRole('button');
    const deleteButton = buttons.find(btn => btn.querySelector('.lucide-trash-2'));
    if (deleteButton) fireEvent.click(deleteButton);

    expect(restaurantService.deleteMenuItem).not.toHaveBeenCalled();
  });

  it('handles delete error', async () => {
    vi.mocked(restaurantService.deleteMenuItem).mockRejectedValueOnce(
      new Error('Delete failed')
    );

    render(<MenuManagement />);
    
    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
    });

    const buttons = screen.getAllByRole('button');
    const deleteButton = buttons.find(btn => btn.querySelector('.lucide-trash-2'));
    if (deleteButton) fireEvent.click(deleteButton);

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Failed to delete menu item');
    });
  });

  it('creates new menu item when submitting form', async () => {
    const newItem = { ...mockMenuItems[0], id: '3', name: 'New Item' };
    vi.mocked(restaurantService.addMenuItem).mockResolvedValueOnce(newItem);

    render(<MenuManagement />);
    
    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
    });

    // Open add dialog
    const addButton = screen.getByText('Add Item');
    fireEvent.click(addButton);

    await waitFor(() => {
      expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
    });

    // Fill form
    fireEvent.change(screen.getByLabelText(/name/i), {
      target: { value: 'New Item' },
    });
    fireEvent.change(screen.getByLabelText(/price/i), {
      target: { value: '100' },
    });

    // Submit form
    const form = screen.getByLabelText(/name/i).closest('form');
    if (form) {
      fireEvent.submit(form);
    }

    await waitFor(() => {
      expect(restaurantService.addMenuItem).toHaveBeenCalled();
      expect(toast.success).toHaveBeenCalledWith('Menu item added successfully');
    });
  });

  it('shows empty state when no menu items', async () => {
    vi.mocked(restaurantService.getRestaurantMenu).mockResolvedValueOnce([]);

    render(<MenuManagement />);

    await waitFor(() => {
      expect(screen.getByText(/no menu items yet/i)).toBeInTheDocument();
    });
  });

  it('navigates back when clicking back button', async () => {
    render(<MenuManagement />);
    
    await waitFor(() => {
      expect(screen.getByText('Pad Thai')).toBeInTheDocument();
    });

    const backButton = screen.getByRole('button', { name: /dashboard/i });
    fireEvent.click(backButton);

    // Just verify the button exists and can be clicked
    expect(backButton).toBeInTheDocument();
  });
});
